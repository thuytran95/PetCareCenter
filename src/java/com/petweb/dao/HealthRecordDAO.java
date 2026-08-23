package com.petweb.dao;

import com.petweb.model.HealthRecord;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Truy cập bảng pet_health_record — sổ sức khỏe của thú cưng. */
public class HealthRecordDAO {

    private static final String COLUMNS =
            "record_id, pet_id, booking_id, line_id, record_type, item_id, item_name, "
          + "performed_at, next_due_at, note";

    /**
     * Ghi một mục vào sổ. Dùng ON CONFLICT DO NOTHING dựa trên chỉ mục duy nhất
     * (booking_id, line_id, item_id) nên gọi lại nhiều lần cũng không tạo bản ghi trùng —
     * quan trọng vì thanh toán có thể bị bấm lại hoặc tác vụ nền chạy lại.
     */
    public static int insertIfAbsent(Connection conn, HealthRecord r) throws SQLException {
        String sql = """
            INSERT INTO pet_health_record
                (pet_id, booking_id, line_id, record_type, item_id, item_name,
                 performed_at, next_due_at, note)
            VALUES (?,?,?,?,?,?,?,?,?)
            ON CONFLICT DO NOTHING
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, r.getPetId());
            if (r.getBookingId() != null) ps.setInt(2, r.getBookingId()); else ps.setNull(2, Types.INTEGER);
            if (r.getLineId() != null) ps.setInt(3, r.getLineId()); else ps.setNull(3, Types.INTEGER);
            ps.setString(4, r.getRecordType());
            if (r.getItemId() != null) ps.setInt(5, r.getItemId()); else ps.setNull(5, Types.INTEGER);
            ps.setString(6, r.getItemName());
            ps.setTimestamp(7, r.getPerformedAt());
            ps.setTimestamp(8, r.getNextDueAt());
            ps.setString(9, r.getNote());
            return ps.executeUpdate();
        }
    }

    /** Toàn bộ sổ sức khỏe của một bé, mới nhất trước. */
    public static List<HealthRecord> findByPet(Connection conn, int petId) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM pet_health_record WHERE pet_id = ?"
                   + " ORDER BY performed_at DESC, record_id DESC";
        List<HealthRecord> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, petId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    /** Sổ sức khỏe của một bé, lọc theo loại (VACCINE / CHECKUP / DEWORM). */
    public static List<HealthRecord> findByPetAndType(Connection conn, int petId, String type)
            throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM pet_health_record"
                   + " WHERE pet_id = ? AND record_type = ?"
                   + " ORDER BY performed_at DESC, record_id DESC";
        List<HealthRecord> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, petId);
            ps.setString(2, type);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    /**
     * Các mũi/lần cần làm lại của TẤT CẢ thú cưng thuộc một chủ, dùng cho khối
     * nhắc lịch ở trang chủ.
     *
     * Chỉ lấy lần GẦN NHẤT của mỗi (thú cưng + hạng mục): nếu bé đã tiêm nhắc lại
     * rồi thì mũi cũ không được hiện nữa. DISTINCT ON là cú pháp riêng của PostgreSQL
     * cho đúng việc này.
     */
    public static List<HealthRecord> findUpcomingDueByOwner(Connection conn, int userId,
                                                            int withinDays, int limit)
            throws SQLException {
        String sql = """
            SELECT r.record_id, r.pet_id, r.booking_id, r.line_id, r.record_type,
                   r.item_id, r.item_name, r.performed_at, r.next_due_at, r.note, p.name AS pet_name
            FROM (
                SELECT DISTINCT ON (h.pet_id, h.item_name) h.*
                FROM pet_health_record h
                JOIN pet pt ON pt.pet_id = h.pet_id
                WHERE pt.user_id = ? AND h.next_due_at IS NOT NULL
                ORDER BY h.pet_id, h.item_name, h.performed_at DESC
            ) r
            JOIN pet p ON p.pet_id = r.pet_id
            WHERE r.next_due_at <= now() + (? || ' days')::interval
            ORDER BY r.next_due_at
            LIMIT ?
        """;
        List<HealthRecord> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, String.valueOf(withinDays));
            ps.setInt(3, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HealthRecord r = map(rs);
                    r.setPetName(rs.getString("pet_name"));
                    list.add(r);
                }
            }
        }
        return list;
    }

    /** Đếm số mục trong sổ của một bé — dùng để hiện huy hiệu ở thẻ thú cưng. */
    public static int countByPet(Connection conn, int petId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM pet_health_record WHERE pet_id = ?")) {
            ps.setInt(1, petId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private static HealthRecord map(ResultSet rs) throws SQLException {
        HealthRecord r = new HealthRecord();
        r.setRecordId(rs.getInt("record_id"));
        r.setPetId(rs.getInt("pet_id"));
        int bid = rs.getInt("booking_id");
        r.setBookingId(rs.wasNull() ? null : bid);
        int lid = rs.getInt("line_id");
        r.setLineId(rs.wasNull() ? null : lid);
        r.setRecordType(rs.getString("record_type"));
        int iid = rs.getInt("item_id");
        r.setItemId(rs.wasNull() ? null : iid);
        r.setItemName(rs.getString("item_name"));
        r.setPerformedAt(rs.getTimestamp("performed_at"));
        r.setNextDueAt(rs.getTimestamp("next_due_at"));
        r.setNote(rs.getString("note"));
        return r;
    }
}
