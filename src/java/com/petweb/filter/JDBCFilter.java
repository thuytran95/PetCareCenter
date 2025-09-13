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
        String servletPath = request.getServletPath();
        String pathInfo    = request.getPathInfo();
        String urlPattern  = (pathInfo == null) ? servletPath : (servletPath + "/*");

        Map<String, ? extends ServletRegistration> servletRegistrations =
                request.getServletContext().getServletRegistrations();

        Collection<? extends ServletRegistration> values = servletRegistrations.values();
        for (ServletRegistration sr : values) {
            if (sr.getMappings().contains(urlPattern)) return true;
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
