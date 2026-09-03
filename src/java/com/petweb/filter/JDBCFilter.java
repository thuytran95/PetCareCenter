package com.petweb.filter;

import com.petweb.utils.ConnectionUtils;
import com.petweb.utils.MyUtils;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.sql.Connection;
import java.util.Collection;
import java.util.Map;

@WebFilter(filterName = "jdbcFilter", urlPatterns = { "/*" })
public class JDBCFilter implements Filter {

    // Xác định request có trỏ vào 1 servlet hay không
    private boolean needJDBC(HttpServletRequest request) {
        Map<String, ? extends ServletRegistration> servletRegistrations =
                request.getServletContext().getServletRegistrations();

        for (ServletRegistration sr : servletRegistrations.values()) {
            if (matchesMapping(request.getServletPath(), request.getPathInfo(), sr.getMappings())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Request này có khớp với một trong các ánh xạ của servlet không.
     *
     * Tách riêng và để công khai để kiểm thử được mà không cần dựng cả Tomcat.
     *
     * Điểm dễ sai: yêu cầu vào GỐC ứng dụng (http://host/petcare/) được đặc tả
     * Servlet quy định là servletPath rỗng và pathInfo bằng "/". Nếu chỉ ghép
     * servletPath + "/*" như cách cũ thì ra "/*", không khớp ánh xạ chuỗi rỗng
     * của trang chủ, và cả request đó chạy mà KHÔNG có kết nối CSDL. Servlet nào
     * ánh xạ vào gốc mà cần đọc dữ liệu sẽ âm thầm trả về trang trống — không
     * lỗi, không ngoại lệ, nên rất khó lần ra.
     */
    public static boolean matchesMapping(String servletPath, String pathInfo,
                                         Collection<String> mappings) {
        if (mappings == null || mappings.isEmpty()) return false;

        String path = (servletPath == null) ? "" : servletPath;

        if (mappings.contains(path)) return true;
        if (pathInfo != null && mappings.contains(path + "/*")) return true;

        // Gốc ứng dụng: khai báo bằng chuỗi rỗng, một số máy chủ báo cáo là "/"
        if (path.isEmpty()) {
            return mappings.contains("") || mappings.contains("/");
        }
        return false;
    }

    @Override public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;

        if (needJDBC(req)) {
            Connection conn = null;
            try {
                conn = ConnectionUtils.getConnection();
                conn.setAutoCommit(false);
                MyUtils.storeConnection(request, conn);

                chain.doFilter(request, response);
                conn.commit();
            } catch (Exception e) {
                ConnectionUtils.rollbackQuietly(conn);
                throw new ServletException(e);
            } finally {
                ConnectionUtils.closeQuietly(conn);
            }
        } else {
            chain.doFilter(request, response);
        }
    }
}
