package com.petweb.controller;

import com.petweb.dao.BookingDAO;
import com.petweb.dao.HealthRecordDAO;
import com.petweb.model.PetStay;
import com.petweb.dao.PetDAO;
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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Trang chủ.
 *
 * Với khách đã đăng nhập, trang chủ đóng vai trò bảng tin cá nhân: danh sách
 * thú cưng, các lịch hẹn sắp tới và hóa đơn gần đây. Khách chưa đăng nhập chỉ
 * thấy phần giới thiệu dịch vụ — các thuộc tính bên dưới không được đặt nên
 * những khối đó không hiển thị.
 */
@WebServlet(urlPatterns = {"", "/index", "/home"})
public class HomeController extends HttpServlet {

    private static final int MAX_UPCOMING = 4;
    private static final int MAX_RECENT_INVOICES = 3;
    /** Nhắc trước bao nhiêu ngày với các mũi tiêm / lần khám tới hạn. */
    private static final int HEALTH_DUE_WITHIN_DAYS = 30;
    private static final int MAX_HEALTH_DUE = 4;

    private static final Logger LOGGER = Logger.getLogger(HomeController.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        UserAccount user = MyUtils.getLoginedUser(request.getSession());
        Connection conn = MyUtils.getStoredConnection(request);

        if (user != null && conn != null) {
            try {
                request.setAttribute("myPets", PetDAO.findByOwner(conn, user.getId()));

                // Toàn bộ đợt lưu trú còn hiệu lực, gom theo bé — cùng dữ liệu
                // với trang hồ sơ để hai nơi không nói khác nhau.
                request.setAttribute("stays", BookingDAO.groupStaysByPet(
                        BookingDAO.findCurrentStaysByOwner(conn, user.getId())));
                request.setAttribute("upcoming",
                        BookingDAO.findUpcomingAppointments(conn, user.getId(), MAX_UPCOMING));
                request.setAttribute("recentBookings",
                        BookingDAO.findRecentByUser(conn, user.getId(), MAX_RECENT_INVOICES));
                request.setAttribute("healthDue",
                        HealthRecordDAO.findUpcomingDueByOwner(
                                conn, user.getId(), HEALTH_DUE_WITHIN_DAYS, MAX_HEALTH_DUE));
            } catch (SQLException e) {
                // Trang chủ vẫn phải xem được dù phần bảng tin lỗi
                LOGGER.log(Level.WARNING,
                        "Không tải được bảng tin trang chủ cho userId=" + user.getId(), e);
            }
        }
        request.setAttribute("fromHomeController", Boolean.TRUE);
        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
