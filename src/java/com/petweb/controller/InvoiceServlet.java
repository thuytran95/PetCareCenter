package com.petweb.controller;

import com.petweb.dao.NotificationDAO;
import com.petweb.model.Booking;
import com.petweb.model.UserAccount;
import com.petweb.service.BookingException;
import com.petweb.service.BookingService;
import com.petweb.service.NotificationService;
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
 * Hiển thị hóa đơn của một đơn đặt lịch. Dùng chung cho khách đã đăng nhập
 * và khách vãng lai (trước đây có thêm InvoiceServletGuest trùng lặp).
 *
 * Đây là màn hình CHỈ ĐỌC: khác bản cũ vốn gọi calculateTotalFromServices()
 * và ghi lại total_price ngay trong một request GET.
 */
@WebServlet("/invoice")
public class InvoiceServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(InvoiceServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idStr = request.getParameter("bookingId");
        if (idStr == null || idStr.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu bookingId");
            return;
        }

        int bookingId;
        try {
            bookingId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "bookingId không hợp lệ");
            return;
        }

        Connection conn = BookingServlet.requireConnection(request);
        try {
            Booking booking = BookingService.loadInvoice(conn, bookingId);

            // Đơn của người dùng đã đăng nhập chỉ được xem bởi chính chủ.
            // Đơn khách vãng lai (userId == null) không gắn tài khoản nào nên cho xem
            // bằng đường dẫn có bookingId — vừa đặt xong là được chuyển thẳng tới đây.
            if (booking.getUserId() != null) {
                UserAccount user = MyUtils.getLoginedUser(request.getSession());
                if (user == null || user.getId() != booking.getUserId()) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN,
                            "Bạn không có quyền xem hóa đơn này");
                    return;
                }
            }

            // Lịch sử thông báo đã gửi cho đơn này (mô phỏng SMS)
            request.setAttribute("notifications",
                    NotificationDAO.findByBooking(conn, bookingId));

            // Nếu chưa gửi được tin nào vì hồ sơ thiếu số điện thoại hợp lệ thì
            // nói rõ lý do, thay vì để người dùng tưởng chức năng bị hỏng.
            BookingService.attachContactPhone(conn, booking);
            if (!NotificationService.isSendablePhone(booking.getContactPhone())) {
                request.setAttribute("noPhoneWarning", Boolean.TRUE);
            }

            // Thông báo tạm sau khi thanh toán/hủy
            Object flashMsg = request.getSession().getAttribute("message");
            Object flashErr = request.getSession().getAttribute("error");
            if (flashMsg != null) {
                request.setAttribute("flashMessage", flashMsg);
                request.getSession().removeAttribute("message");
            }
            if (flashErr != null) {
                request.setAttribute("flashError", flashErr);
                request.getSession().removeAttribute("error");
            }

            request.setAttribute("booking", booking);
            request.getRequestDispatcher("/invoice.jsp").forward(request, response);

        } catch (BookingException e) {
            request.setAttribute("errorMessage", e.getMessage());
            request.getRequestDispatcher("/invoice.jsp").forward(request, response);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi tải hóa đơn bookingId=" + bookingId, e);
            throw new ServletException(e);
        }
    }
}
