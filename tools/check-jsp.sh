#!/usr/bin/env bash
#
# Dịch thử TOÀN BỘ file JSP bằng chính trình biên dịch Jasper của Tomcat,
# để bắt lỗi trong JSP TRƯỚC khi deploy.
#
# Lý do cần: `javac` chỉ kiểm tra thư mục src/java. JSP được Tomcat dịch lúc
# chạy, nên những lỗi như trùng tên biến ngầm định (page, request, session...)
# hoặc gọi sai phương thức chỉ lộ ra khi người dùng bấm vào trang.
#
# Cách dùng:  bash tools/check-jsp.sh
# Trả về 0 nếu tất cả JSP dịch được, khác 0 nếu có lỗi.

set -uo pipefail

PROJ_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

JDK_HOME="${JDK_HOME:-/c/Users/Duyet/Desktop/jdk-17.0.20+8}"
TOMCAT_HOME="${TOMCAT_HOME:-/c/Users/Duyet/Desktop/apache-tomcat-11.0.25}"
ANT_JAR="${ANT_JAR:-/c/Program Files/Apache NetBeans/extide/ant/lib/ant.jar}"

JAVAC="$JDK_HOME/bin/javac.exe"
JAVA="$JDK_HOME/bin/java.exe"

[ -x "$JAVAC" ] || { echo "Không tìm thấy javac tại $JAVAC — đặt biến JDK_HOME cho đúng."; exit 2; }
[ -x "$JAVA" ]  || { echo "Không tìm thấy java tại $JAVA — đặt biến JDK_HOME cho đúng."; exit 2; }
[ -d "$TOMCAT_HOME/lib" ] || { echo "Không tìm thấy Tomcat tại $TOMCAT_HOME"; exit 2; }
[ -f "$ANT_JAR" ] || { echo "Không tìm thấy ant.jar tại $ANT_JAR"; exit 2; }

# javac và Jasper không đọc được đường dẫn có dấu tiếng Việt, nên toàn bộ
# mã nguồn và thư viện được chép sang thư mục tạm chỉ gồm ký tự ASCII.
WORK="${TMPDIR:-/tmp}/pcc-jspcheck"
APP="$WORK/webapp"
CLASSES="$APP/WEB-INF/classes"
GEN="$WORK/generated"
SRC="$WORK/src"
LIBS="$WORK/libs"

rm -rf "$WORK"
mkdir -p "$CLASSES" "$GEN" "$SRC" "$LIBS"

cp "$PROJ_DIR/lib-provided/jakarta.servlet-api-6.0.0.jar" "$LIBS/"
cp "$PROJ_DIR"/web/WEB-INF/lib/*.jar "$LIBS/"

JAVA_CP=""
for j in "$LIBS"/*.jar; do
    JAVA_CP="$JAVA_CP$(cygpath -w "$j");"
done

echo "== 1/2: Biên dịch mã Java =="
cp -r "$PROJ_DIR/src/java/com" "$SRC/"
find "$SRC" -name '*.java' | while read -r f; do cygpath -w "$f"; done > "$WORK/sources.txt"

if ! "$JAVAC" -encoding UTF-8 -nowarn \
        -cp "$JAVA_CP" -d "$(cygpath -w "$CLASSES")" \
        @"$(cygpath -w "$WORK/sources.txt")"; then
    echo "!! Mã Java lỗi biên dịch — sửa xong hãy chạy lại."
    exit 1
fi
echo "   OK: $(find "$CLASSES" -name '*.class' | wc -l) class"

echo "== 2/2: Dịch thử toàn bộ JSP bằng Jasper =="
# Chép nội dung web/ nhưng GIỮ LẠI thư mục classes vừa biên dịch ở trên
cp -r "$PROJ_DIR"/web/*.jsp "$APP/" 2>/dev/null || true
for d in css js image WEB-INF META-INF; do
    [ -d "$PROJ_DIR/web/$d" ] && cp -r "$PROJ_DIR/web/$d" "$APP/" 2>/dev/null
done
mkdir -p "$CLASSES"
"$JAVAC" -encoding UTF-8 -nowarn -cp "$JAVA_CP" -d "$(cygpath -w "$CLASSES")" \
    @"$(cygpath -w "$WORK/sources.txt")" >/dev/null 2>&1

JSP_CP=""
for j in "$TOMCAT_HOME"/lib/*.jar; do
    JSP_CP="$JSP_CP$(cygpath -w "$j");"
done
JSP_CP="$JSP_CP$(cygpath -w "$TOMCAT_HOME/bin/tomcat-juli.jar");"
JSP_CP="$JSP_CP$(cygpath -w "$ANT_JAR");"
JSP_CP="$JSP_CP$(cygpath -w "$CLASSES");"
for j in "$LIBS"/*.jar; do
    JSP_CP="$JSP_CP$(cygpath -w "$j");"
done

LOG="$WORK/jspc.log"
"$JAVA" -cp "$JSP_CP" org.apache.jasper.JspC \
    -webapp "$(cygpath -w "$APP")" \
    -d "$(cygpath -w "$GEN")" \
    -compile > "$LOG" 2>&1

if grep -qiE "SEVERE|JasperException|An error occurred at line" "$LOG"; then
    echo "!! JSP có lỗi:"
    echo
    grep -iE -A4 "An error occurred at line|SEVERE" "$LOG" | head -40
    exit 1
fi

COUNT=$(find "$GEN" -name '*.class' | wc -l)
echo "   OK: $COUNT trang JSP dịch sạch, không lỗi."
echo
echo "Tất cả đều ổn."
