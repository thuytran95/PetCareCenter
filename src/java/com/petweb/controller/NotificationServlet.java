package com.petweb.controller;

import com.petweb.dao.NotificationDAO;
import com.petweb.model.UserAccount;
import com.petweb.utils.MyUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Lịch sử thông báo của khách đã đăng nhập.
 *
 * Thay cho notificationSetting.jsp cũ — trang đó chỉ có hai ô checkbox không
 * có thuộc tính name, không gửi đi đâu và không lưu gì, tức là hoàn toàn trang trí.
 * Giờ nó hiển thị dữ liệu thật từ bảng notification.
 */
@WebServlet("/notifications")
public class NotificationServlet extends HttpServlet {

    private static final int MAX_ITEMS = 50;

    private static final Logger LOGGER = Logger.getLogger(NotificationServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        UserAccount user = MyUtils.getLoginedUser(request.getSession());
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Connection conn = BookingServlet.requireConnection(request);
        try {
            request.setAttribute("notifications",
                    NotificationDAO.findByUser(conn, user.getId(), MAX_ITEMS));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi tải thông báo của userId=" + user.getId(), e);
            request.setAttribute("loadError", "Không tải được danh sách thông báo.");
        }
        request.getRequestDispatcher("/notificationSetting.jsp").forward(request, response);
    }
}
