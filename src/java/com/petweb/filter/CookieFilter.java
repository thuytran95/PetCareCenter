package com.petweb.filter;

import com.petweb.model.UserAccount;
import com.petweb.utils.DBUtils;
import com.petweb.utils.MyUtils;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebFilter(filterName = "cookieFilter", urlPatterns = { "/*" })
public class CookieFilter implements Filter {

    @Override public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpSession session = req.getSession();

        UserAccount userInSession = MyUtils.getLoginedUser(session);
        if (userInSession != null) {
            session.setAttribute("COOKIE_CHECKED", "CHECKED");
            chain.doFilter(request, response);
            return;
        }

        Connection conn = MyUtils.getStoredConnection(request);
        String checked = (String) session.getAttribute("COOKIE_CHECKED");

        if (checked == null && conn != null) {
            String userName = MyUtils.getUserNameInCookie(req);
            if (userName != null) {
                try {
                    UserAccount user = DBUtils.findUser(conn, userName);
                    if (user != null) MyUtils.storeLoginedUser(session, user);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            session.setAttribute("COOKIE_CHECKED", "CHECKED");
        }

        chain.doFilter(request, response);
    }
}
