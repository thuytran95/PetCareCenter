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

@WebServlet(urlPatterns = {"/register"})
@MultipartConfig(maxFileSize = 16177215)
public class RegisterServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        RequestDispatcher dispatcher
                = this.getServletContext().getRequestDispatcher("/register.jsp");
        dispatcher.forward(request, response);
    }

    @Override  protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Connection conn = MyUtils.getStoredConnection(request);

        String userName = request.getParameter("userName");
        String password = request.getParameter("password");
        String gender = request.getParameter("gender");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String address = request.getParameter("address");
        String fullName = request.getParameter("fullName");

        Part filePart = request.getPart("avatar");
        byte[] avatar = null;
        if (filePart != null && filePart.getSize() > 0) {
            try (InputStream is = filePart.getInputStream()) {
                avatar = is.readAllBytes();
            }
        }

        UserAccount newUser = new UserAccount();
        newUser.setUserName(userName);
        newUser.setPassword(password);
        newUser.setGender(gender);
        newUser.setEmail(email);
        newUser.setPhone(phone);
        newUser.setAddress(address);
        newUser.setFullName(fullName);
        newUser.setAvatar(avatar);

        try {
            DBUtils.register(conn, newUser);
            request.setAttribute("message", "Đăng ký thành công! Vui lòng đăng nhập.");
            response.sendRedirect("login.jsp");
        } catch (SQLException e) {
            System.out.println("error");
            request.setAttribute("error", e.getMessage());
            request.setAttribute("user", newUser);
            request.getRequestDispatcher("register.jsp").forward(request, response);
        }
    }
}
