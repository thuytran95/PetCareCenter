package com.petweb.dao;

import com.petweb.model.Booking;
import com.petweb.model.BookingLine;
import com.petweb.model.Appointment;
import com.petweb.model.BookingLineItem;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Truy cập các bảng booking / booking_line / booking_line_item.
 *
 * DAO chỉ chịu trách nhiệm đọc-ghi; mọi quy tắc nghiệp vụ (tính tiền, kiểm tra
 * phòng trống, chuyển trạng thái) nằm ở com.petweb.service.BookingService.
 */
public class BookingDAO {

    private static final String BOOKING_COLUMNS = """
        booking_id, user_id, guest_name, guest_phone, guest_email,
        pet_id, pet_name, pet_species, status, total_price,
        created_at, confirmed_at, paid_at, cancelled_at, lookup_code
    """;

    // ------------------------------------------------------------------ đơn

    /** Tạo đơn nháp cho khách đã đăng nhập. */
    public static int insertDraftForUser(Connection conn, int userId, Integer petId,
                                         String petName, String petSpecies) throws SQLException {
        String sql = """
            INSERT INTO booking (user_id, pet_id, pet_name, pet_species, status)
            VALUES (?, ?, ?, ?, 'DRAFT')
            RETURNING booking_id
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            if (petId != null) ps.setInt(2, petId); else ps.setNull(2, Types.INTEGER);
            ps.setString(3, petName);
            ps.setString(4, petSpecies);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Tạo đơn nháp cho khách vãng lai.
     * Không tạo tài khoản giả trong user_account như luồng cũ — thông tin liên hệ
     * và thú cưng được lưu thẳng trên đơn.
     */
    public static int insertDraftForGuest(Connection conn, String guestName, String guestPhone,
                                          String guestEmail, String petName, String petSpecies)
            throws SQLException {
        String sql = """
            INSERT INTO booking (guest_name, guest_phone, guest_email, pet_name, pet_species, status)
            VALUES (?, ?, ?, ?, ?, 'DRAFT')
            RETURNING booking_id
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, guestName);
            ps.setString(2, guestPhone);
            ps.setString(3, guestEmail);
            ps.setString(4, petName);
            ps.setString(5, petSpecies);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public static Booking findById(Connection conn, int bookingId) throws SQLException {
        String sql = "SELECT " + BOOKING_COLUMNS + " FROM booking WHERE booking_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapBooking(rs) : null;
            }
        }
    }

    /** Đơn kèm toàn bộ dòng dịch vụ và hạng mục con — dùng cho trang hóa đơn. */
    public static Booking findByIdWithLines(Connection conn, int bookingId) throws SQLException {
        Booking b = findById(conn, bookingId);
        if (b == null) return null;
        b.setLines(findLines(conn, bookingId));
        return b;
    }

    /** Lịch sử đặt lịch của một khách hàng. */
    public static List<Booking> findByUser(Connection conn, int userId) throws SQLException {
        String sql = "SELECT " + BOOKING_COLUMNS
                + " FROM booking WHERE user_id = ? ORDER BY created_at DESC";
        List<Booking> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapBooking(rs));
            }
        }
        return list;
    }

    public static void updateStatus(Connection conn, int bookingId, String status) throws SQLException {
        String sql = "UPDATE booking SET status = ?, confirmed_at = CASE WHEN ? = 'CONFIRMED' THEN now() ELSE confirmed_at END WHERE booking_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, status);
            ps.setInt(3, bookingId);
            ps.executeUpdate();
        }
    }

    public static void updateTotal(Connection conn, int bookingId, BigDecimal total) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE booking SET total_price = ? WHERE booking_id = ?")) {
            ps.setBigDecimal(1, total);
            ps.setInt(2, bookingId);
            ps.executeUpdate();
        }
    }

    /** Tổng tiền tính từ các dòng, đọc thẳng từ DB (không phụ thuộc đối tượng trong bộ nhớ). */
    public static BigDecimal sumLineTotals(Connection conn, int bookingId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COALESCE(SUM(line_total), 0) FROM booking_line WHERE booking_id = ?")) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        }
    }

    // ------------------------------------------------------------ dòng dịch vụ

    public static int insertLine(Connection conn, BookingLine line) throws SQLException {
        String sql = """
            INSERT INTO booking_line
                (booking_id, service_type, start_at, end_at, room_code, quantity, line_total, note)
            VALUES (?,?,?,?,?,?,?,?)
            RETURNING line_id
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, line.getBookingId());
            ps.setString(2, line.getServiceType());
            ps.setTimestamp(3, line.getStartAt());
            ps.setTimestamp(4, line.getEndAt());
            ps.setString(5, line.getRoomCode());
            ps.setInt(6, line.getQuantity());
            ps.setBigDecimal(7, line.getLineTotal());
            ps.setString(8, line.getNote());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    line.setLineId(rs.getInt(1));
                    return line.getLineId();
                }
            }
        }
        return 0;
    }

    public static List<BookingLine> findLines(Connection conn, int bookingId) throws SQLException {
        String sql = """
            SELECT line_id, booking_id, service_type, start_at, end_at,
                   room_code, quantity, line_total, note, created_at
            FROM booking_line WHERE booking_id = ? ORDER BY line_id
        """;
        List<BookingLine> lines = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lines.add(mapLine(rs));
            }
        }
        for (BookingLine line : lines) {
            line.setItems(findLineItems(conn, line.getLineId()));
        }
        return lines;
    }

    public static int deleteLine(Connection conn, int lineId, int bookingId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM booking_line WHERE line_id = ? AND booking_id = ?")) {
            ps.setInt(1, lineId);
            ps.setInt(2, bookingId);
            return ps.executeUpdate();
        }
    }

    // ------------------------------------------------------- hạng mục của dòng

    /** Ghi các hạng mục con kèm bản chụp tên + giá tại thời điểm đặt. */
    public static void insertLineItems(Connection conn, int lineId, List<BookingLineItem> items)
            throws SQLException {
        if (items == null || items.isEmpty()) return;
        String sql = """
            INSERT INTO booking_line_item (line_id, item_id, item_name, item_price)
            VALUES (?,?,?,?)
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (BookingLineItem it : items) {
                ps.setInt(1, lineId);
                ps.setInt(2, it.getItemId());
                ps.setString(3, it.getItemName());
                ps.setBigDecimal(4, it.getItemPrice());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public static List<BookingLineItem> findLineItems(Connection conn, int lineId) throws SQLException {
        String sql = """
            SELECT line_item_id, line_id, item_id, item_name, item_price
            FROM booking_line_item WHERE line_id = ? ORDER BY line_item_id
        """;
        List<BookingLineItem> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, lineId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BookingLineItem it = new BookingLineItem();
                    it.setLineItemId(rs.getInt("line_item_id"));
                    it.setLineId(rs.getInt("line_id"));
                    it.setItemId(rs.getInt("item_id"));
                    it.setItemName(rs.getString("item_name"));
                    it.setItemPrice(rs.getBigDecimal("item_price"));
                    list.add(it);
                }
            }
        }
        return list;
    }

    /** Gán mã tra cứu cho đơn (dùng cho khách vãng lai). */
    public static void updateLookupCode(Connection conn, int bookingId, String code)
            throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE booking SET lookup_code = ? WHERE booking_id = ?")) {
            ps.setString(1, code);
            ps.setInt(2, bookingId);
            ps.executeUpdate();
        }
    }

    /**
     * Đánh dấu đã thanh toán. Điều kiện trạng thái nằm ngay trong câu UPDATE nên
     * bấm hai lần liên tiếp chỉ ăn một lần (lần sau trả về 0 dòng).
     */
    public static int markPaid(Connection conn, int bookingId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE booking SET status = 'PAID', paid_at = now()"
              + " WHERE booking_id = ? AND status = 'CONFIRMED'")) {
            ps.setInt(1, bookingId);
            return ps.executeUpdate();
        }
    }

    /** Hủy đơn; chỉ tác dụng khi đơn đang ở trạng thái còn hủy được. */
    public static int markCancelled(Connection conn, int bookingId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE booking SET status = 'CANCELLED', cancelled_at = now()"
              + " WHERE booking_id = ? AND status IN ('CONFIRMED','PAID')")) {
            ps.setInt(1, bookingId);
            return ps.executeUpdate();
        }
    }

    /**
     * Tra cứu đơn bằng mã + số điện thoại — lối vào cho khách vãng lai.
     *
     * So khớp số điện thoại theo CHỮ SỐ, bỏ qua khoảng trắng, dấu gạch, dấu ngoặc:
     * khách hay gõ lại số theo định dạng khác lúc đặt (0912 345 678 vs 0912345678)
     * mà vẫn phải tra ra đúng đơn của mình.
     */
    public static Booking findByLookupCode(Connection conn, String code, String phone)
            throws SQLException {
        String sql = "SELECT " + BOOKING_COLUMNS
                + " FROM booking WHERE lookup_code = ?"
                + " AND regexp_replace(coalesce(guest_phone, ''), '[^0-9]', '', 'g') = ?";
        String digitsOnly = (phone == null) ? "" : phone.replaceAll("[^0-9]", "");
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setString(2, digitsOnly);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapBooking(rs) : null;
            }
        }
    }

    /** Kiểm tra mã tra cứu đã tồn tại chưa (để sinh mã không trùng). */
    public static boolean lookupCodeExists(Connection conn, String code) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT 1 FROM booking WHERE lookup_code = ? LIMIT 1")) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // -------------------------------------------------- bảng tin trang chủ

    /**
     * Các lịch hẹn SẮP TỚI của một khách hàng, dùng cho khối nhắc hẹn ở trang chủ.
     * Chỉ lấy đơn đã xác nhận và mốc thời gian còn ở tương lai, gần nhất lên đầu.
     */
    public static List<Appointment> findUpcomingAppointments(Connection conn, int userId, int limit)
            throws SQLException {
        String sql = """
            SELECT b.booking_id, b.pet_name, l.line_id, l.service_type,
                   l.room_code, l.note, l.start_at, l.end_at, l.line_total
            FROM booking_line l
            JOIN booking b ON b.booking_id = l.booking_id
            WHERE b.user_id = ?
              AND b.status IN ('CONFIRMED','PAID')
              AND COALESCE(l.end_at, l.start_at) >= now()
            ORDER BY l.start_at
            LIMIT ?
        """;
        List<Appointment> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Appointment a = new Appointment();
                    a.setBookingId(rs.getInt("booking_id"));
                    a.setPetName(rs.getString("pet_name"));
                    a.setLineId(rs.getInt("line_id"));
                    a.setServiceType(rs.getString("service_type"));
                    String note = rs.getString("note");
                    a.setRoomLabel(note != null ? note : rs.getString("room_code"));
                    a.setStartAt(rs.getTimestamp("start_at"));
                    a.setEndAt(rs.getTimestamp("end_at"));
                    a.setLineTotal(rs.getBigDecimal("line_total"));
                    list.add(a);
                }
            }
        }
        return list;
    }

    /** Các hóa đơn gần đây của khách hàng (bỏ qua đơn nháp chưa chốt). */
    public static List<Booking> findRecentByUser(Connection conn, int userId, int limit)
            throws SQLException {
        String sql = "SELECT " + BOOKING_COLUMNS
                + " FROM booking WHERE user_id = ? AND status <> 'DRAFT'"
                + " ORDER BY created_at DESC LIMIT ?";
        List<Booking> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapBooking(rs));
            }
        }
        return list;
    }

    // ----------------------------------------------------------------- ánh xạ

    private static Booking mapBooking(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setBookingId(rs.getInt("booking_id"));
        int uid = rs.getInt("user_id");
        b.setUserId(rs.wasNull() ? null : uid);
        b.setGuestName(rs.getString("guest_name"));
        b.setGuestPhone(rs.getString("guest_phone"));
        b.setGuestEmail(rs.getString("guest_email"));
        int pid = rs.getInt("pet_id");
        b.setPetId(rs.wasNull() ? null : pid);
        b.setPetName(rs.getString("pet_name"));
        b.setPetSpecies(rs.getString("pet_species"));
        b.setStatus(rs.getString("status"));
        b.setTotalPrice(rs.getBigDecimal("total_price"));
        b.setCreatedAt(rs.getTimestamp("created_at"));
        b.setConfirmedAt(rs.getTimestamp("confirmed_at"));
        b.setPaidAt(rs.getTimestamp("paid_at"));
        b.setCancelledAt(rs.getTimestamp("cancelled_at"));
        b.setLookupCode(rs.getString("lookup_code"));
        return b;
    }

    private static BookingLine mapLine(ResultSet rs) throws SQLException {
        BookingLine l = new BookingLine();
        l.setLineId(rs.getInt("line_id"));
        l.setBookingId(rs.getInt("booking_id"));
        l.setServiceType(rs.getString("service_type"));
        l.setStartAt(rs.getTimestamp("start_at"));
        l.setEndAt(rs.getTimestamp("end_at"));
        l.setRoomCode(rs.getString("room_code"));
        l.setQuantity(rs.getInt("quantity"));
        l.setLineTotal(rs.getBigDecimal("line_total"));
        l.setNote(rs.getString("note"));
        l.setCreatedAt(rs.getTimestamp("created_at"));
        return l;
    }
}
