package com.petweb.controller;

import com.petweb.model.Pet;
import com.petweb.model.UserAccount;
import com.petweb.utils.ConnectionUtils;
import com.petweb.utils.DBUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.Connection;

@WebServlet("/editUser")
@MultipartConfig
public class EditUserServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try (Connection conn = ConnectionUtils.getConnection()) {
            int id = Integer.parseInt(request.getParameter("id"));
            String userName = request.getParameter("userName");
            String fullName = request.getParameter("fullName");
            String email = request.getParameter("email");
            String phone = request.getParameter("phone");
            String address = request.getParameter("address");
            String role = request.getParameter("role");
            String password = request.getParameter("password");

            // lấy user cũ theo id
            UserAccount oldUser = DBUtils.findUser(conn,  request.getParameter("userName"));

            UserAccount u = new UserAccount();
            u.setId(id);
            u.setUserName(userName);
            u.setFullName(fullName);
            u.setEmail(email);
            u.setPhone(phone);
            u.setAddress(address);
            u.setRole(role);
            u.setPassword(password);

            Part avatarPart = request.getPart("avatar");
            if (avatarPart != null && avatarPart.getSize() > 0) {
                u.setAvatar(avatarPart.getInputStream().readAllBytes());
            } else {
                u.setAvatar(oldUser.getAvatar());
            }

            DBUtils.updateUserById(conn, u);
            response.sendRedirect(request.getContextPath() + "/index.jsp");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}