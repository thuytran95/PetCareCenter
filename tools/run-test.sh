#!/usr/bin/env bash
#
# Biên dịch toàn bộ mã dự án rồi chạy một lớp kiểm thử trên CSDL thật.
# Lớp kiểm thử tự mở giao dịch và rollback ở cuối nên không đổi dữ liệu.
#
# Cách dùng:  bash tools/run-test.sh com.petweb.test.HealthRecordTest
#
# Cần đường dẫn ASCII vì javac không đọc được thư mục có dấu tiếng Việt,
# nên mã nguồn được chép sang thư mục tạm trước khi biên dịch.
set -uo pipefail

CLS="${1:-com.petweb.test.HealthRecordTest}"
PROJ_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
JDK_HOME="${JDK_HOME:-/c/Users/Duyet/Desktop/jdk-17.0.20+8}"

JAVAC="$JDK_HOME/bin/javac.exe"
JAVA="$JDK_HOME/bin/java.exe"
[ -x "$JAVAC" ] || { echo "Không tìm thấy javac tại $JAVAC"; exit 2; }

WORK="${TMPDIR:-/tmp}/pcc-test"
OUT="$WORK/classes"; SRC="$WORK/src"; LIBS="$WORK/libs"
rm -rf "$WORK"; mkdir -p "$OUT" "$SRC" "$LIBS"

cp "$PROJ_DIR/lib-provided/jakarta.servlet-api-6.0.0.jar" "$LIBS/"
cp "$PROJ_DIR"/web/WEB-INF/lib/*.jar "$LIBS/"
cp -r "$PROJ_DIR/src/java/com" "$SRC/"

CP=""
for j in "$LIBS"/*.jar; do CP="$CP$(cygpath -w "$j");"; done

find "$SRC" -name '*.java' | while read -r f; do cygpath -w "$f"; done > "$WORK/src.txt"
"$JAVAC" -encoding UTF-8 -nowarn -cp "$CP" \
    -d "$(cygpath -w "$OUT")" @"$(cygpath -w "$WORK/src.txt")" || exit 1

"$JAVA" -Dfile.encoding=UTF-8 -cp "$CP$(cygpath -w "$OUT")" "$CLS"
