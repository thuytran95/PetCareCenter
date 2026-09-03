package com.petweb.controller;

import com.petweb.model.Booking;
import com.petweb.model.UserAccount;
import com.petweb.service.BookingException;
import com.petweb.service.BookingService;
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
 * Thanh toán (MÔ PHỎNG) và hủy đơn đặt lịch.
 *
 * Không có cổng thanh toán thật: bấm "Thanh toán" chỉ chuyển trạng thái đơn
 * sang PAID và gửi một thông báo mô phỏng. Xem NotificationService để rõ hơn.
 *
 * Dùng POST cho cả hai hành động vì đây là thao tác thay đổi dữ liệu — tránh
 * việc tải lại trang hay trình duyệt prefetch làm thanh toán/hủy ngoài ý muốn.
 */
@WebServlet("/bookingAction")
public class BookingActionServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(BookingActionServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int bookingId;
        try {
            bookingId = Integer.parseInt(request.getParameter("bookingId"));
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu hoặc sai bookingId");
            return;
        }

        String action = request.getParameter("action");
        Connection conn = BookingServlet.requireConnection(request);

        try {
            // Chỉ chủ đơn mới được thao tác. Đơn của khách vãng lai không gắn tài
            // khoản nào, nên ai có mã tra cứu (đã qua bước tra cứu) thì được phép.
            Booking booking = BookingService.loadInvoice(conn, bookingId);
            if (!canModify(request, booking)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Bạn không có quyền thao tác trên đơn này");
                return;
            }

            if ("pay".equals(action)) {
                BookingService.pay(conn, bookingId);
                request.getSession().setAttribute("message",
                        "Đã thanh toán đơn #" + bookingId + ". Thông báo đã được gửi tới số điện thoại của bạn.");
            } else if ("cancel".equals(action)) {
                BookingService.cancel(conn, bookingId);
                request.getSession().setAttribute("message",
                        "Đã hủy đơn #" + bookingId + ".");
            } else if ("delete".equals(action)) {
                UserAccount owner = MyUtils.getLoginedUser(request.getSession());
                if (owner == null) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN,
                            "Cần đăng nhập để xóa đơn");
                    return;
                }
                BookingService.deleteFinished(conn, bookingId, owner.getId());
                request.getSession().setAttribute("message",
                        "Đã xóa đơn #" + bookingId + " khỏi lịch sử.");
            } else if ("checkout".equals(action)) {
                BookingService.checkOut(conn, bookingId);
                request.getSession().setAttribute("message",
                        "Đã trả phòng cho đơn #" + bookingId + ". Phòng đã được nhả ra cho khách khác.");
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Hành động không hợp lệ");
                return;
            }

        } catch (BookingException e) {
            request.getSession().setAttribute("error", e.getMessage());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi xử lý đơn #" + bookingId + ", action=" + action, e);
            throw new ServletException(e);
        }

        // Bấm từ hồ sơ thú cưng hay trang chủ thì quay lại đúng chỗ đó,
        // không quăng khách sang trang hóa đơn.
        String back = request.getParameter("back");
        // Đơn vừa bị xóa thì không còn hóa đơn nào để quay về
        if ("delete".equals(action) && back == null) {
            back = "myBookings";
        }
        if ("petProfile".equals(back)) {
            response.sendRedirect(request.getContextPath() + "/petProfile");
        } else if ("home".equals(back) || "index".equals(back)) {
            response.sendRedirect(request.getContextPath() + "/");
        } else if ("myBookings".equals(back)) {
            // Giữ lại bộ lọc theo bé: thao tác xong mà nhảy về danh sách đầy đủ
            // thì người dùng phải lọc lại từ đầu.
            String backPet = request.getParameter("backPetId");
            response.sendRedirect(request.getContextPath() + "/myBookings"
                    + (isPositiveInt(backPet) ? "?petId=" + backPet : ""));
        } else {
            response.sendRedirect(request.getContextPath() + "/invoice?bookingId=" + bookingId);
        }
    }

    /** Chỉ ghép petId vào URL khi nó thực sự là một số dương. */
    private static boolean isPositiveInt(String raw) {
        if (raw == null || raw.isBlank()) return false;
        try {
            return Integer.parseInt(raw.trim()) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Đơn của người dùng đã đăng nhập: chỉ chính chủ.
     * Đơn khách vãng lai: cho phép nếu phiên này vừa tự đặt đơn đó, hoặc đã tra
     * cứu thành công bằng mã + số điện thoại (xem GuestBookingAccess).
     */
    private boolean canModify(HttpServletRequest request, Booking booking) {
        if (booking.getUserId() != null) {
            UserAccount user = MyUtils.getLoginedUser(request.getSession());
            return user != null && user.getId() == booking.getUserId();
        }
        return GuestBookingAccess.has(request.getSession(), booking.getBookingId());
    }
}
