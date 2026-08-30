package com.petweb.dao;

import com.petweb.model.MedicalServiceItem;
import com.petweb.model.RoomAvailability;
import com.petweb.model.RoomType;
import com.petweb.model.SpaServiceItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Bảng giá dịch vụ: spa_service_item, medical_service_item và room_type.
 *
 * Đây là nguồn giá DUY NHẤT. Tầng nghiệp vụ luôn lấy giá từ đây rồi mới ghi
 * xuống đơn, không bao giờ tin số tiền do trình duyệt gửi lên.
 */
public class ServiceCatalogDAO {

    // ----- Spa -----

    public static List<SpaServiceItem> findAllSpaItems(Connection conn) throws SQLException {
        String sql = "SELECT item_id, item_name, item_price FROM spa_service_item ORDER BY item_id";
        List<SpaServiceItem> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new SpaServiceItem(
                        rs.getInt("item_id"),
                        rs.getString("item_name"),
                        rs.getBigDecimal("item_price")));
            }
        }
        return list;
    }

    /**
     * Lấy các dịch vụ spa theo danh sách id, trả về Map theo đúng thứ tự tìm thấy.
     * Id nào không tồn tại thì đơn giản là không có trong Map — tầng nghiệp vụ dựa
     * vào đó để báo lỗi thay vì âm thầm tính thiếu tiền.
     */
    public static Map<Integer, SpaServiceItem> findSpaItemsByIds(Connection conn, List<Integer> ids)
            throws SQLException {
        Map<Integer, SpaServiceItem> map = new LinkedHashMap<>();
        if (ids == null || ids.isEmpty()) return map;

        String sql = "SELECT item_id, item_name, item_price FROM spa_service_item WHERE item_id = ANY (?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setArray(1, conn.createArrayOf("integer", ids.toArray(new Integer[0])));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getInt("item_id"), new SpaServiceItem(
                            rs.getInt("item_id"),
                            rs.getString("item_name"),
                            rs.getBigDecimal("item_price")));
                }
            }
        }
        return map;
    }

    // ----- Y tế -----

    public static List<MedicalServiceItem> findAllMedicalItems(Connection conn) throws SQLException {
        String sql = "SELECT item_id, item_name, item_price FROM medical_service_item ORDER BY item_id";
        List<MedicalServiceItem> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new MedicalServiceItem(
                        rs.getInt("item_id"),
                        rs.getString("item_name"),
                        rs.getBigDecimal("item_price")));
            }
        }
        return list;
    }

    public static Map<Integer, MedicalServiceItem> findMedicalItemsByIds(Connection conn, List<Integer> ids)
            throws SQLException {
        Map<Integer, MedicalServiceItem> map = new LinkedHashMap<>();
        if (ids == null || ids.isEmpty()) return map;

        String sql = "SELECT item_id, item_name, item_price FROM medical_service_item WHERE item_id = ANY (?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setArray(1, conn.createArrayOf("integer", ids.toArray(new Integer[0])));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getInt("item_id"), new MedicalServiceItem(
                            rs.getInt("item_id"),
                            rs.getString("item_name"),
                            rs.getBigDecimal("item_price")));
                }
            }
        }
        return map;
    }

    // ----- Phòng khách sạn -----

    /**
     * Đơn nháp giữ chỗ được bao lâu trước khi coi như khách đã bỏ.
     * Đặt ở tầng DAO vì chính truy vấn đếm phòng cần tới nó; tầng nghiệp vụ
     * dùng lại hằng số này để hai nơi không bao giờ lệch nhau.
     */
    public static final int DRAFT_HOLD_HOURS = 24;

    public static List<RoomType> findActiveRoomTypes(Connection conn) throws SQLException {
        String sql = """
            SELECT room_code, room_name, price_per_day, description, total_rooms, active
            FROM room_type WHERE active = true ORDER BY price_per_day
        """;
        List<RoomType> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRoom(rs));
        }
        return list;
    }

    public static RoomType findRoomType(Connection conn, String roomCode) throws SQLException {
        if (roomCode == null || roomCode.isBlank()) return null;
        String sql = """
            SELECT room_code, room_name, price_per_day, description, total_rooms, active
            FROM room_type WHERE room_code = ?
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roomCode);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRoom(rs) : null;
            }
        }
    }

    /**
     * Điều kiện MỘT dòng đặt phòng đang thực sự giữ chỗ. Đây là quy tắc gốc,
     * mọi nơi đếm phòng đều dùng lại để không bao giờ lệch nhau:
     *
     *  - CONFIRMED và PAID: đơn có hiệu lực, chắc chắn giữ phòng.
     *  - DRAFT: chỉ giữ chỗ tạm trong lúc khách đang đặt dở. Quá hạn thì tự hết
     *    hiệu lực ngay tại đây, không phải chờ tác vụ nền dọn — nhờ vậy một đơn
     *    khách bỏ giữa chừng không khóa phòng của người khác thêm phút nào.
     *  - CANCELLED và COMPLETED: đã nhả phòng.
     */
    private static final String ROOM_HELD_CONDITION = """
              AND (
                    b.status IN ('CONFIRMED','PAID')
                 OR (b.status = 'DRAFT'
                     AND b.created_at > now() - (? || ' hours')::interval)
              )
        """;

    /**
     * Đếm số phòng cùng loại đang bị chiếm trong khoảng thời gian yêu cầu.
     * Hai khoảng [a,b) và [c,d) giao nhau khi a &lt; d và c &lt; b — nên khách
     * nhận phòng đúng lúc người trước trả phòng không bị tính là trùng.
     */
    public static int countOverlappingRooms(Connection conn, String roomCode,
                                            Timestamp start, Timestamp end,
                                            Integer excludeBookingId) throws SQLException {
        StringBuilder sql = new StringBuilder("""
            SELECT COUNT(*)
            FROM booking_line bl
            JOIN booking b ON b.booking_id = bl.booking_id
            WHERE bl.service_type = 'HOTEL'
              AND bl.room_code = ?
              AND bl.start_at < ?
              AND bl.end_at   > ?
        """);
        sql.append(ROOM_HELD_CONDITION);
        if (excludeBookingId != null) sql.append(" AND b.booking_id <> ? ");

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int i = 1;
            ps.setString(i++, roomCode);
            ps.setTimestamp(i++, end);
            ps.setTimestamp(i++, start);
            ps.setString(i++, String.valueOf(draftHoldHours()));
            if (excludeBookingId != null) ps.setInt(i, excludeBookingId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Tình trạng còn trống của TẤT CẢ hạng phòng trong một khoảng thời gian,
     * lấy bằng một truy vấn duy nhất thay vì hỏi từng hạng một.
     *
     * Truyền start/end là null khi khách chưa chọn ngày: khi đó chỉ trả về tổng
     * số phòng của mỗi hạng, và {@code isWindowKnown()} bằng false để giao diện
     * biết mà nhắc khách chọn ngày trước.
     */
    public static List<RoomAvailability> findAvailability(Connection conn,
                                                          Timestamp start, Timestamp end,
                                                          Integer excludeBookingId)
            throws SQLException {

        List<RoomType> types = findActiveRoomTypes(conn);
        boolean windowKnown = start != null && end != null && end.after(start);

        Map<String, Integer> busy = windowKnown
                ? countBusyByRoomType(conn, start, end, excludeBookingId)
                : Map.of();

        List<RoomAvailability> list = new ArrayList<>();
        for (RoomType t : types) {
            list.add(RoomAvailability.of(t, busy.getOrDefault(t.getRoomCode(), 0), windowKnown));
        }
        return list;
    }

    /** Số phòng đang bị chiếm của từng hạng, gộp trong một lần truy vấn. */
    private static Map<String, Integer> countBusyByRoomType(Connection conn,
                                                            Timestamp start, Timestamp end,
                                                            Integer excludeBookingId)
            throws SQLException {
        StringBuilder sql = new StringBuilder("""
            SELECT bl.room_code, COUNT(*) AS busy
            FROM booking_line bl
            JOIN booking b ON b.booking_id = bl.booking_id
            WHERE bl.service_type = 'HOTEL'
              AND bl.room_code IS NOT NULL
              AND bl.start_at < ?
              AND bl.end_at   > ?
        """);
        sql.append(ROOM_HELD_CONDITION);
        if (excludeBookingId != null) sql.append(" AND b.booking_id <> ? ");
        sql.append(" GROUP BY bl.room_code");

        Map<String, Integer> map = new LinkedHashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int i = 1;
            ps.setTimestamp(i++, end);
            ps.setTimestamp(i++, start);
            ps.setString(i++, String.valueOf(draftHoldHours()));
            if (excludeBookingId != null) ps.setInt(i, excludeBookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) map.put(rs.getString("room_code"), rs.getInt("busy"));
            }
        }
        return map;
    }

    /** Số giờ một đơn nháp còn được giữ chỗ trước khi tự hết hiệu lực. */
    private static int draftHoldHours() {
        return DRAFT_HOLD_HOURS;
    }

    private static RoomType mapRoom(ResultSet rs) throws SQLException {
        RoomType r = new RoomType();
        r.setRoomCode(rs.getString("room_code"));
        r.setRoomName(rs.getString("room_name"));
        r.setPricePerDay(rs.getBigDecimal("price_per_day"));
        r.setDescription(rs.getString("description"));
        r.setTotalRooms(rs.getInt("total_rooms"));
        r.setActive(rs.getBoolean("active"));
        return r;
    }
}
