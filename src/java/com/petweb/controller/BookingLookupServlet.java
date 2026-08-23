package com.petweb.controller;

import com.petweb.model.Booking;
import com.petweb.service.BookingException;
import com.petweb.service.BookingService;

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
 * Tra cứu đơn cho KHÁCH VÃNG LAI bằng mã tra cứu + số điện thoại.
 *
 * Vì sao cần: khách vãng lai không có tài khoản, nên sau khi đóng trình duyệt
 * thì không còn đường nào quay lại hóa đơn của mình. Mã tra cứu được sinh khi
 * chốt đơn và gửi kèm trong tin nhắn mô phỏng.
 *
 * Bắt buộc có CẢ mã VÀ số điện thoại: chỉ biết mã thì chưa xem được, tránh
 * trường hợp dò mã ngẫu nhiên để xem đơn người khác.
 */
@WebServlet("/lookup")
public class BookingLookupServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(BookingLookupServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/lookup.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String code = request.getParameter("lookupCode");
        String phone = request.getParameter("phone");
        Connection conn = BookingServlet.requireConnection(request);

        try {
            Booking booking = BookingService.lookupGuestBooking(conn, code, phone);

            // Ghi nhận phiên này đã xác thực được đơn, để cho phép thanh toán/hủy
            GuestBookingAccess.grant(request.getSession(), booking.getBookingId());
            response.sendRedirect(request.getContextPath()
                    + "/invoice?bookingId=" + booking.getBookingId());

        } catch (BookingException e) {
            request.setAttribute("error", e.getMessage());
            request.setAttribute("lookupCode", code);
            request.getRequestDispatcher("/lookup.jsp").forward(request, response);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi tra cứu đơn", e);
            throw new ServletException(e);
        }
    }
}
