package com.petweb.controller;

import com.petweb.model.UserAccount;
import com.petweb.utils.DBUtils;
import com.petweb.utils.MyUtils;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet(urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String userName = request.getParameter("userName");
        String password = request.getParameter("password");
        String rememberMeStr = request.getParameter("rememberMe");
        boolean remember = "Y".equals(rememberMeStr);

        Connection conn = MyUtils.getStoredConnection(request);

        String error = null;
        UserAccount user = null;

        if (userName == null || password == null || userName.isBlank() || password.isBlank()) {
            error = "Vui lòng nhập tài khoản và mật khẩu!";
        } else {
            try {
                // Kiểm tra username có tồn tại không
                user = DBUtils.findUser(conn, userName);
                if (user == null) {
                    error = "Chưa có tài khoản, vui lòng đăng ký!";
                } else if (!user.getPassword().equals(password)) {
                    error = "Sai tên đăng nhập hoặc mật khẩu!";
                    user = null; // không lưu session
                }
            } catch (SQLException e) {
                throw new ServletException(e);
            }
        }

        if (error != null) {
            request.setAttribute("error", error);
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            

            return;
        }
        // login ok
        HttpSession session = request.getSession();
        MyUtils.storeLoginedUser(session, user);
        if (user != null) {
            session.setAttribute("userId", user.getId());
            session.setAttribute("userName", user.getUserName());
        }
        if (remember) {
            MyUtils.storeUserCookie(response, user);
        } else {
            MyUtils.deleteUserCookie(response);
        }

        // Nếu là admin → chuyển tới trang danh sách user, còn user bình thường → index.jsp
        response.sendRedirect(request.getContextPath() + "/index.jsp");

    }
}
