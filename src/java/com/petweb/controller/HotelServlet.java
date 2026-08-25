package com.petweb.controller;

import com.petweb.dao.ServiceCatalogDAO;
import com.petweb.model.BookingLine;
import com.petweb.service.BookingException;
import com.petweb.service.BookingService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Đặt phòng khách sạn. Dùng chung cho khách đã đăng nhập và khách vãng lai
 * (trước đây có thêm bản HotelServletGuest trùng lặp).
 *
 * Servlet chỉ đọc tham số và hiển thị; giá phòng, số ngày và việc kiểm tra
 * còn phòng trống đều do BookingService quyết định.
 */
@WebServlet("/HotelServlet")
public class HotelServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(HotelServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Connection conn = BookingServlet.requireConnection(request);
        try {
            exposeRooms(request, conn);
            BookingServlet.exposeDraft(request, conn);
            request.getRequestDispatcher("/HotelDetail.jsp").forward(request, response);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi tải bảng giá phòng", e);
            throw new ServletException(e);
        }
    }

    /**
     * Đổ bảng giá kèm tình trạng còn trống. Ngày lấy từ chính tham số khách vừa
     * gửi, nên sau khi bị báo hết phòng, trang hiện lại vẫn cho thấy đúng số
     * phòng trống của khoảng ngày đó thay vì con số chung chung.
     */
    private static void exposeRooms(HttpServletRequest request, Connection conn)
            throws SQLException {

        Timestamp checkIn = parseOrNull(request.getParameter("checkIn"));
        Timestamp checkOut = parseOrNull(request.getParameter("checkOut"));
        Integer currentBooking = (Integer) request.getSession()
                .getAttribute(BookingServlet.SESSION_BOOKING_ID);

        request.setAttribute("rooms",
                ServiceCatalogDAO.findAvailability(conn, checkIn, checkOut, currentBooking));
        request.setAttribute("checkIn", request.getParameter("checkIn"));
        request.setAttribute("checkOut", request.getParameter("checkOut"));
    }

    /** Ngày giờ không hợp lệ thì coi như khách chưa chọn, không phải lỗi. */
    private static Timestamp parseOrNull(String raw) {
        try {
            return parseDateTime(raw, "");
        } catch (BookingException e) {
            return null;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        Connection conn = BookingServlet.requireConnection(request);

        try {
            int bookingId = BookingServlet.requireDraftId(session);

            Timestamp checkIn = parseDateTime(request.getParameter("checkIn"),
                    "Ngày nhận phòng không hợp lệ.");
            Timestamp checkOut = parseDateTime(request.getParameter("checkOut"),
                    "Ngày trả phòng không hợp lệ.");

            BookingLine line = BookingService.addHotelLine(conn, bookingId,
                    request.getParameter("roomType"), checkIn, checkOut);

            request.setAttribute("line", line);
            request.getRequestDispatcher("/bookingResult.jsp").forward(request, response);

        } catch (BookingException e) {
            // Trả lại form kèm thông báo và bảng giá để khách chọn lại
            request.setAttribute("error", e.getMessage());
            try {
                exposeRooms(request, conn);
            } catch (SQLException ignored) {
                // không tải được bảng giá thì JSP tự hiển thị danh sách rỗng
            }
            BookingServlet.exposeDraft(request, conn);
            request.getRequestDispatcher("/HotelDetail.jsp").forward(request, response);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi đặt phòng khách sạn", e);
            throw new ServletException(e);
        }
    }

    /** Chuyển giá trị từ input datetime-local (yyyy-MM-ddTHH:mm) sang Timestamp. */
    static Timestamp parseDateTime(String raw, String message) throws BookingException {
        if (raw == null || raw.isBlank()) throw new BookingException(message);
        try {
            return Timestamp.valueOf(LocalDateTime.parse(raw));
        } catch (DateTimeParseException e) {
            throw new BookingException(message);
        }
    }
}
