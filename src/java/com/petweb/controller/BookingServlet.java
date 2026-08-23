package com.petweb.controller;

import com.petweb.model.Booking;
import com.petweb.model.UserAccount;
import com.petweb.service.BookingException;
import com.petweb.service.BookingService;
import com.petweb.utils.MyUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Điều phối luồng đặt lịch cho CẢ khách đã đăng nhập lẫn khách vãng lai
 * (trước đây tách thành BookingServlet + GuestBookingServlet trùng nhau ~95%).
 *
 * Trạng thái duy nhất giữ trong session là SESSION_BOOKING_ID; mọi thông tin khác
 * đọc lại từ DB theo id đó, nên mở nhiều tab hay bấm Back không làm hỏng dữ liệu.
 *
 * Các bước:
 *   GET  ?petId=..        → mở đơn nháp cho thú cưng của chính mình
 *   POST action=startGuest→ mở đơn nháp cho khách vãng lai
 *   POST action=add       → chọn loại dịch vụ, chuyển sang trang nhập chi tiết
 *   POST action=finish    → chốt đơn, sang hóa đơn
 */
@WebServlet("/BookingServlet")
public class BookingServlet extends HttpServlet {

    /** Khóa session duy nhất của luồng đặt lịch. */
    public static final String SESSION_BOOKING_ID = "currentBookingId";

    private static final Logger LOGGER = Logger.getLogger(BookingServlet.class.getName());

    /**
     * Điểm vào của luồng đặt lịch.
     *
     *   ?petId=..                 → mở đơn nháp cho thú cưng đó (khách đã đăng nhập)
     *   ?serviceType=spa (không petId) → chưa chọn thú cưng: đưa về đúng nơi cần đi
     *                                    (chọn thú cưng nếu đã đăng nhập, form khách vãng lai nếu chưa)
     *
     * serviceType được mang theo suốt để sau khi có đơn nháp là nhảy thẳng vào
     * đúng dịch vụ khách bấm từ trang chủ, không bắt chọn lại.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        UserAccount user = MyUtils.getLoginedUser(session);
        String serviceType = normalizeServiceType(request.getParameter("serviceType"));
        String petIdStr = request.getParameter("petId");

        // Chưa chọn thú cưng: điều hướng tới bước phù hợp thay vì để rơi vào ngõ cụt
        if (petIdStr == null || petIdStr.isBlank()) {
            String suffix = (serviceType == null) ? "" : "?serviceType=" + serviceType;
            if (user == null) {
                // Khách vãng lai khai thông tin trước
                response.sendRedirect(request.getContextPath() + "/booking.jsp" + suffix);
            } else {
                // Đã đăng nhập thì chọn xem đặt cho bé nào
                response.sendRedirect(request.getContextPath() + "/petProfile" + suffix);
            }
            return;
        }

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int petId;
        try {
            petId = Integer.parseInt(petIdStr);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/petProfile");
            return;
        }

        Connection conn = requireConnection(request);
        try {
            int bookingId = BookingService.startDraftForUser(conn, user.getId(), petId);
            session.setAttribute(SESSION_BOOKING_ID, bookingId);
            response.sendRedirect(nextStepUrl(request, serviceType));
        } catch (BookingException e) {
            session.setAttribute("error", e.getMessage());
            response.sendRedirect(request.getContextPath() + "/petProfile");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi mở đơn đặt lịch cho petId=" + petId, e);
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        HttpSession session = request.getSession();
        Connection conn = requireConnection(request);

        try {
            if ("startGuest".equals(action)) {
                handleStartGuest(request, response, conn, session);
            } else if ("add".equals(action)) {
                handleAdd(request, response, session);
            } else if ("finish".equals(action)) {
                handleFinish(request, response, conn, session);
            } else {
                request.setAttribute("error", "Hành động không hợp lệ.");
                exposeDraft(request, conn);
                request.getRequestDispatcher("/chooseService.jsp").forward(request, response);
            }
        } catch (BookingException e) {
            // Lỗi nghiệp vụ: hiện thông báo thân thiện, không phải sự cố hệ thống
            request.setAttribute("error", e.getMessage());
            if (!"startGuest".equals(action)) {
                exposeDraft(request, conn);
            }
            request.getRequestDispatcher(
                    "startGuest".equals(action) ? "/booking.jsp" : "/chooseService.jsp")
                    .forward(request, response);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi hệ thống khi xử lý đặt lịch, action=" + action, e);
            throw new ServletException(e);
        }
    }

    /** Khách vãng lai: nhận thông tin liên hệ + thú cưng, mở đơn nháp, không tạo tài khoản. */
    private void handleStartGuest(HttpServletRequest request, HttpServletResponse response,
                                  Connection conn, HttpSession session)
            throws SQLException, BookingException, IOException {

        int bookingId = BookingService.startDraftForGuest(conn,
                request.getParameter("guestName"),
                request.getParameter("guestPhone"),
                request.getParameter("guestEmail"),
                request.getParameter("petName"),
                request.getParameter("petSpecies"));

        session.setAttribute(SESSION_BOOKING_ID, bookingId);
        response.sendRedirect(nextStepUrl(request,
                normalizeServiceType(request.getParameter("serviceType"))));
    }

    /**
     * Chọn loại dịch vụ rồi chuyển sang trang nhập chi tiết tương ứng.
     * Chưa ghi gì xuống DB ở bước này — dòng dịch vụ chỉ được tạo khi khách
     * nhập xong ngày giờ và bấm xác nhận ở trang chi tiết.
     */
    private void handleAdd(HttpServletRequest request, HttpServletResponse response,
                           HttpSession session)
            throws BookingException, IOException {

        requireDraftId(session);
        String serviceType = request.getParameter("serviceType");

        if ("hotel".equalsIgnoreCase(serviceType)) {
            response.sendRedirect(request.getContextPath() + "/HotelServlet");
        } else if ("spa".equalsIgnoreCase(serviceType)) {
            response.sendRedirect(request.getContextPath() + "/SpaBookingServlet");
        } else if ("medical".equalsIgnoreCase(serviceType)) {
            response.sendRedirect(request.getContextPath() + "/MedicalBookingServlet");
        } else {
            throw new BookingException("Vui lòng chọn một dịch vụ hợp lệ.");
        }
    }

    /** Chốt đơn và chuyển sang hóa đơn. */
    private void handleFinish(HttpServletRequest request, HttpServletResponse response,
                              Connection conn, HttpSession session)
            throws SQLException, BookingException, IOException {

        int bookingId = requireDraftId(session);
        Booking booking = BookingService.confirm(conn, bookingId);

        // Đơn của khách vãng lai không gắn tài khoản nào. Cấp quyền cho chính phiên
        // vừa đặt, để khách thanh toán/hủy được ngay mà không phải đi tra cứu lại.
        if (booking.isGuestBooking()) {
            GuestBookingAccess.grant(session, bookingId);
        }

        // Đơn đã chốt: xóa khỏi session để lần đặt sau bắt đầu đơn mới
        session.removeAttribute(SESSION_BOOKING_ID);
        response.sendRedirect(request.getContextPath() + "/invoice?bookingId=" + booking.getBookingId());
    }

    /** Lấy id đơn nháp trong session, báo lỗi nghiệp vụ nếu phiên đã mất. */
    static int requireDraftId(HttpSession session) throws BookingException {
        Integer bookingId = (Integer) session.getAttribute(SESSION_BOOKING_ID);
        if (bookingId == null) {
            throw new BookingException("Phiên đặt lịch đã hết hạn, vui lòng bắt đầu lại.");
        }
        return bookingId;
    }

    /** Chỉ chấp nhận 3 loại dịch vụ hợp lệ; giá trị lạ coi như không có. */
    private static String normalizeServiceType(String raw) {
        if (raw == null) return null;
        String s = raw.trim().toLowerCase();
        return (s.equals("hotel") || s.equals("spa") || s.equals("medical")) ? s : null;
    }

    /** Sau khi có đơn nháp: vào thẳng dịch vụ đã chọn, hoặc màn hình chọn dịch vụ. */
    private static String nextStepUrl(HttpServletRequest request, String serviceType) {
        String ctx = request.getContextPath();
        if (serviceType == null) return ctx + "/chooseService";
        switch (serviceType) {
            case "hotel":   return ctx + "/HotelServlet";
            case "spa":     return ctx + "/SpaBookingServlet";
            case "medical": return ctx + "/MedicalBookingServlet";
            default:        return ctx + "/chooseService";
        }
    }

    /**
     * Nạp đơn nháp hiện tại (kèm các dòng dịch vụ đã thêm) vào request để các trang
     * dịch vụ hiển thị bảng tạm tính bên cạnh form, theo kiểu giỏ hàng.
     * Không có đơn nháp thì đơn giản là không đặt thuộc tính nào.
     */
    static void exposeDraft(HttpServletRequest request, Connection conn) {
        Integer bookingId = (Integer) request.getSession().getAttribute(SESSION_BOOKING_ID);
        if (bookingId == null || conn == null) return;
        try {
            Booking draft = com.petweb.dao.BookingDAO.findByIdWithLines(conn, bookingId);
            if (draft != null) request.setAttribute("draft", draft);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Không nạp được đơn nháp #" + bookingId, e);
        }
    }

    /** Kết nối do JDBCFilter mở cho cả request. */
    static Connection requireConnection(HttpServletRequest request) throws ServletException {
        Connection conn = MyUtils.getStoredConnection(request);
        if (conn == null) {
            throw new ServletException("Không có kết nối CSDL cho request này");
        }
        return conn;
    }
}
