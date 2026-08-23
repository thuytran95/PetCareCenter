package com.petweb.dao;

import com.petweb.model.Notification;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Truy cập bảng notification — lịch sử thông báo đã gửi tới khách. */
public class NotificationDAO {

    private static final String COLUMNS =
            "notification_id, booking_id, user_id, recipient, channel, event_type, content, created_at";

    public static int insert(Connection conn, Notification n) throws SQLException {
        String sql = """
            INSERT INTO notification (booking_id, user_id, recipient, channel, event_type, content)
            VALUES (?,?,?,?,?,?)
            RETURNING notification_id
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (n.getBookingId() != null) ps.setInt(1, n.getBookingId()); else ps.setNull(1, Types.INTEGER);
            if (n.getUserId() != null) ps.setInt(2, n.getUserId()); else ps.setNull(2, Types.INTEGER);
            ps.setString(3, n.getRecipient());
            ps.setString(4, n.getChannel() == null ? "SMS" : n.getChannel());
            ps.setString(5, n.getEventType());
            ps.setString(6, n.getContent());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    n.setNotificationId(rs.getInt(1));
                    return n.getNotificationId();
                }
            }
        }
        return 0;
    }

    /** Lịch sử thông báo của một khách hàng đã đăng nhập. */
    public static List<Notification> findByUser(Connection conn, int userId, int limit) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM notification WHERE user_id = ?"
                + " ORDER BY created_at DESC, notification_id DESC LIMIT ?";
        List<Notification> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    /** Thông báo gắn với một đơn cụ thể (dùng cả cho khách vãng lai). */
    public static List<Notification> findByBooking(Connection conn, int bookingId) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM notification WHERE booking_id = ?"
                + " ORDER BY created_at DESC, notification_id DESC";
        List<Notification> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    /** Đếm thông báo của khách, dùng cho chấm đỏ trên chuông. */
    public static int countByUser(Connection conn, int userId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM notification WHERE user_id = ?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /** Đã gửi loại thông báo này cho đơn chưa — tránh nhắc lịch trùng nhiều lần. */
    public static boolean existsForBooking(Connection conn, int bookingId, String eventType)
            throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT 1 FROM notification WHERE booking_id = ? AND event_type = ? LIMIT 1")) {
            ps.setInt(1, bookingId);
            ps.setString(2, eventType);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static Notification map(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setNotificationId(rs.getInt("notification_id"));
        int bid = rs.getInt("booking_id");
        n.setBookingId(rs.wasNull() ? null : bid);
        int uid = rs.getInt("user_id");
        n.setUserId(rs.wasNull() ? null : uid);
        n.setRecipient(rs.getString("recipient"));
        n.setChannel(rs.getString("channel"));
        n.setEventType(rs.getString("event_type"));
        n.setContent(rs.getString("content"));
        n.setCreatedAt(rs.getTimestamp("created_at"));
        return n;
    }
}
