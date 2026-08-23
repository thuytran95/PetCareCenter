package com.petweb.controller;
import com.petweb.utils.MyUtils;
import com.petweb.utils.DBUtils;
import com.petweb.model.UserAccount;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/changePassword")
public class ChangePasswordServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ChangePasswordServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/changePassword.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String oldPass = request.getParameter("oldPassword");
        String newPass = request.getParameter("newPassword");
        String confirmPass = request.getParameter("confirmPassword");

        // Lấy user hiện tại từ session
        HttpSession session = request.getSession();
        UserAccount user = (UserAccount) session.getAttribute("loginedUser");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        if (oldPass == null || newPass == null || oldPass.isBlank() || newPass.isBlank()) {
            request.setAttribute("error", "Vui lòng nhập đầy đủ thông tin!");
            request.getRequestDispatcher("/changePassword.jsp").forward(request, response);
            return;
        }

        if (!newPass.equals(confirmPass)) {
            request.setAttribute("error", "Mật khẩu mới và xác nhận mật khẩu không khớp!");
            request.getRequestDispatcher("/changePassword.jsp").forward(request, response);
            return;
        }

        if (!user.getPassword().equals(oldPass)) {
            request.setAttribute("error", "Mật khẩu cũ không đúng!");
            request.getRequestDispatcher("/changePassword.jsp").forward(request, response);
            return;
        }

        Connection conn = MyUtils.getStoredConnection(request);
        if (conn == null) {
            throw new ServletException("Không có kết nối CSDL cho request này");
        }

        try {
            DBUtils.updatePassword(conn, user.getId(), newPass);

            // Cập nhật lại session
            user.setPassword(newPass);
            session.setAttribute("loginedUser", user);

            request.setAttribute("success", "Đổi mật khẩu thành công!");
            request.getRequestDispatcher("/changePassword.jsp").forward(request, response);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi đổi mật khẩu cho user id=" + user.getId(), e);
            request.setAttribute("error", "Có lỗi xảy ra, vui lòng thử lại sau!");
            request.getRequestDispatcher("/changePassword.jsp").forward(request, response);
        }
    }
}
