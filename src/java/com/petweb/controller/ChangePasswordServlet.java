package com.petweb.controller;
import com.petweb.utils.MyUtils;
import com.petweb.utils.DBUtils;
import com.petweb.model.UserAccount;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.Connection;

@WebServlet("/changePassword")
public class ChangePasswordServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/changePassword.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String oldPass = request.getParameter("oldPassword");
        String newPass = request.getParameter("newPassword");

        // Lấy user hiện tại từ session
        HttpSession session = request.getSession();
        UserAccount user = (UserAccount) session.getAttribute("loginedUser");

        if (user == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        try {
            Connection conn = MyUtils.getStoredConnection(request);

            if (!user.getPassword().equals(oldPass)) {
                request.setAttribute("error", "Mật khẩu cũ không đúng!");
                request.getRequestDispatcher("/changePassword.jsp").forward(request, response);
                return;
            }

            DBUtils.updatePassword(conn, user.getId(), newPass);

            // Cập nhật lại session
            user.setPassword(newPass);
            session.setAttribute("loginedUser", user);

            request.setAttribute("success", "Đổi mật khẩu thành công!");
            request.getRequestDispatcher("/changePassword.jsp").forward(request, response);

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
