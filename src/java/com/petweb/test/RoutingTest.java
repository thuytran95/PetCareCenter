package com.petweb.test;

import com.petweb.filter.JDBCFilter;

import jakarta.servlet.annotation.WebServlet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Kiểm thử luật nhận diện "request này có trỏ vào servlet không" của JDBCFilter.
 *
 * Luật này quyết định request có được cấp kết nối CSDL hay không. Sai ở đây thì
 * trang vẫn mở được nhưng MẤT SẠCH dữ liệu — không có lỗi, không có ngoại lệ,
 * chỉ là khoảng trắng. Đó là kiểu lỗi khó tìm nhất, nên khóa chặt bằng kiểm thử.
 *
 * Không cần CSDL và không cần Tomcat.
 */
public class RoutingTest {

    static int pass = 0, fail = 0;

    static void check(String name, boolean ok, String detail) {
        if (ok) { pass++; System.out.println("  [OK]   " + name); }
        else    { fail++; System.out.println("  [FAIL] " + name + " -> " + detail); }
    }

    public static void main(String[] args) {
        // Đúng bộ ánh xạ của trang chủ hiện tại
        Set<String> home = Set.of("", "/index", "/home");

        System.out.println("\n-- Nhom 1: vao goc ung dung --");

        // Đặc tả Servlet: yêu cầu vào gốc có servletPath rỗng và pathInfo là "/"
        check("vao goc ung dung van duoc cap ket noi",
                JDBCFilter.matchesMapping("", "/", home),
                "day chinh la loi lam mat bang tin sau khi dang nhap");
        check("mot so may chu bao cao anh xa goc la dau gach cheo",
                JDBCFilter.matchesMapping("", "/", Set.of("/", "/home")), "khong khop");
        check("goc ung dung khong khop servlet khong lien quan",
                !JDBCFilter.matchesMapping("", "/", Set.of("/petProfile")), "khop nham");

        System.out.println("\n-- Nhom 2: duong dan thong thuong --");

        check("/home khop", JDBCFilter.matchesMapping("/home", null, home), "khong khop");
        check("/index khop", JDBCFilter.matchesMapping("/index", null, home), "khong khop");
        check("/petProfile khop dung servlet cua no",
                JDBCFilter.matchesMapping("/petProfile", null, Set.of("/petProfile")), "khong khop");
        check("duong dan la thi khong khop",
                !JDBCFilter.matchesMapping("/khong-co-that", null, home), "khop nham");

        System.out.println("\n-- Nhom 3: anh xa co duoi /* --");

        check("servlet dang /api/* khop khi co pathInfo",
                JDBCFilter.matchesMapping("/api", "/thing", Set.of("/api/*")), "khong khop");
        check("khong co pathInfo thi khong doi sang dang /*",
                !JDBCFilter.matchesMapping("/api", null, Set.of("/api/*")), "khop nham");

        System.out.println("\n-- Nhom 4: tep tinh va JSP tho --");

        check("tep css khong can ket noi",
                !JDBCFilter.matchesMapping("/css/home.css", null, home), "lai cap ket noi");
        check("goi thang file jsp khong can ket noi",
                !JDBCFilter.matchesMapping("/index.jsp", null, home), "lai cap ket noi");

        System.out.println("\n-- Nhom 5: dau vao bat thuong --");

        check("danh sach anh xa rong", !JDBCFilter.matchesMapping("/home", null, Set.of()),
                "khop nham");
        check("danh sach anh xa null", !JDBCFilter.matchesMapping("/home", null, null),
                "khop nham");
        check("servletPath null duoc coi nhu goc ung dung",
                JDBCFilter.matchesMapping(null, "/", home), "khong khop");

        System.out.println("\n-- Nhom 6: cac trang thuc su can CSDL deu co anh xa --");

        // Trang chủ phải nhận cả ba lối vào, vì LoginServlet chuyển hướng về gốc
        checkServletMaps("com.petweb.controller.HomeController", "", "/index", "/home");
        // Bảng tin trang chủ nay nạp qua API, nên chính API này mới là chỗ
        // bắt buộc phải có kết nối CSDL.
        checkServletMaps("com.petweb.controller.HomeDashboardServlet", "/api/homeDashboard");
        check("duong dan API bang tin duoc cap ket noi",
                JDBCFilter.matchesMapping("/api/homeDashboard", null,
                        Set.of("/api/homeDashboard")), "khong khop");

        checkServletMaps("com.petweb.controller.PetProfileServlet", "/petProfile");
        checkServletMaps("com.petweb.controller.MyBookingsServlet", "/myBookings");
        checkServletMaps("com.petweb.controller.PetHealthServlet", "/petHealth");
        checkServletMaps("com.petweb.controller.ChooseServiceServlet", "/chooseService");
        checkServletMaps("com.petweb.controller.RoomAvailabilityServlet", "/roomAvailability");
        checkServletMaps("com.petweb.controller.BookingLookupServlet", "/lookup");

        System.out.println("\n===== " + pass + " dat / " + fail + " hong =====");
        if (fail > 0) System.exit(1);
    }

    /** Lớp servlet phải khai báo đủ các ánh xạ mong đợi. */
    static void checkServletMaps(String className, String... expected) {
        String shortName = className.substring(className.lastIndexOf('.') + 1);
        try {
            Class<?> c = Class.forName(className);
            WebServlet ann = c.getAnnotation(WebServlet.class);
            if (ann == null) {
                check(shortName + " co @WebServlet", false, "thieu chu thich");
                return;
            }
            List<String> urls = new ArrayList<>();
            urls.addAll(Arrays.asList(ann.value()));
            urls.addAll(Arrays.asList(ann.urlPatterns()));

            for (String e : expected) {
                String label = e.isEmpty() ? "(goc ung dung)" : e;
                check(shortName + " anh xa " + label, urls.contains(e), "cac anh xa: " + urls);
            }
        } catch (ClassNotFoundException e) {
            check(shortName + " ton tai", false, "khong tim thay lop");
        }
    }
}
