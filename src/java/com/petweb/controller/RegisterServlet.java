package com.petweb.controller;

import com.petweb.model.UserAccount;
import com.petweb.utils.DBUtils;
import com.petweb.utils.MyUtils;

import jakarta.servlet.*;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(urlPatterns = {"/register"})
@MultipartConfig(maxFileSize = 16177215)
public class RegisterServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(RegisterServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        RequestDispatcher dispatcher
                = this.getServletContext().getRequestDispatcher("/register.jsp");
        dispatcher.forward(request, response);
    }

    @Override  protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String userName = request.getParameter("userName");
        String password = request.getParameter("password");
        String gender = request.getParameter("gender");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String address = request.getParameter("address");
        String fullName = request.getParameter("fullName");

        UserAccount newUser = new UserAccount();
        newUser.setUserName(userName);
        newUser.setPassword(password);
        newUser.setGender(gender);
        newUser.setEmail(email);
        newUser.setPhone(phone);
        newUser.setAddress(address);
        newUser.setFullName(fullName);

        if (userName == null || password == null || userName.isBlank() || password.isBlank()) {
            request.setAttribute("error", "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!");
            request.setAttribute("user", newUser);
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        Connection conn = MyUtils.getStoredConnection(request);
        if (conn == null) {
            throw new ServletException("Không có kết nối CSDL cho request này");
        }

        try {
            Part filePart = request.getPart("avatar");
            byte[] avatar = null;
            if (filePart != null && filePart.getSize() > 0) {
                try (InputStream is = filePart.getInputStream()) {
                    avatar = is.readAllBytes();
                }
            }
            newUser.setAvatar(avatar);

            DBUtils.register(conn, newUser);
            response.sendRedirect(request.getContextPath() + "/login");
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Đăng ký thất bại cho userName=" + userName, e);
            request.setAttribute("error", e.getMessage());
            request.setAttribute("user", newUser);
            request.getRequestDispatcher("/register.jsp").forward(request, response);
        }
    }
}
