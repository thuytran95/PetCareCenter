package com.petweb.dao;

import com.petweb.model.MedicalServiceItem;
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
     * Đếm số phòng cùng loại đang bị chiếm trong khoảng thời gian yêu cầu.
     * Hai khoảng [a,b) và [c,d) giao nhau khi a < d và c < b.
     * Chỉ tính các đơn còn hiệu lực (DRAFT đang đặt dở hoặc đã CONFIRMED),
     * bỏ qua đơn đã hủy/đã hoàn tất.
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
              AND b.status IN ('DRAFT','CONFIRMED')
              AND bl.start_at < ?
              AND bl.end_at   > ?
        """);
        if (excludeBookingId != null) sql.append(" AND b.booking_id <> ? ");

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setString(1, roomCode);
            ps.setTimestamp(2, end);
            ps.setTimestamp(3, start);
            if (excludeBookingId != null) ps.setInt(4, excludeBookingId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
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
