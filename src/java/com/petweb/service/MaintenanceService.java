package com.petweb.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Các quy tắc tự dọn dẹp và tự chuyển trạng thái của dữ liệu đặt lịch.
 *
 * Tách khỏi AutoStatusListener để listener chỉ còn lo việc hẹn giờ, còn quy tắc
 * nghiệp vụ nằm đúng tầng của nó và kiểm thử được bằng một Connection bất kỳ.
 *
 * RANH GIỚI QUAN TRỌNG: lớp này chỉ dọn thứ KHÔNG phải chứng từ.
 * Hóa đơn — kể cả đơn đã hủy hay đã hoàn tất — không bao giờ bị xóa; chúng là
 * lịch sử giao dịch, không phải rác. Thứ bị xóa chỉ gồm đơn nháp khách bỏ dở
 * (chưa từng chốt, chưa từng là hóa đơn) và nhật ký thông báo quá cũ.
 */
public class MaintenanceService {

    /**
     * Khách đến muộn bao lâu thì vẫn còn giữ phòng. Quá mốc này mà đơn vẫn chưa
     * thanh toán thì coi là không đến và tự hủy để nhả phòng cho người khác.
     */
    public static final int NO_SHOW_GRACE_HOURS = 6;

    /**
     * Đơn nháp RỖNG (chưa chọn dịch vụ nào) được giữ bao lâu.
     * Ngắn hơn nhiều so với đơn đã chọn dịch vụ, vì nó không giữ chỗ của ai
     * và cũng chẳng có gì để khách quay lại hoàn tất.
     */
    public static final int EMPTY_DRAFT_HOURS = 3;

    /** Nhật ký thông báo giữ bao nhiêu ngày rồi dọn. */
    public static final int NOTIFICATION_KEEP_DAYS = 90;

    private MaintenanceService() {
    }

    /**
     * Đơn đã xác nhận nhưng quá giờ nhận phòng vẫn chưa thanh toán thì coi là
     * khách không đến, tự hủy để nhả phòng.
     *
     * Có khoảng ân hạn để khách đến muộn hoặc trả tiền tại quầy vẫn giữ được
     * phòng; đơn ĐÃ thanh toán thì không bao giờ bị hủy kiểu này.
     */
    public static int cancelNoShows(Connection conn) throws SQLException {
        String sql = """
            UPDATE booking b
            SET status = 'CANCELLED', cancelled_at = now()
            WHERE b.status = 'CONFIRMED'
              AND EXISTS (
                    SELECT 1 FROM booking_line l
                    WHERE l.booking_id = b.booking_id
                      AND l.start_at < now() - (? || ' hours')::interval
              )
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, String.valueOf(NO_SHOW_GRACE_HOURS));
            return ps.executeUpdate();
        }
    }

    /**
     * Đơn đã có hiệu lực mà mọi dòng dịch vụ đều đã qua thời gian thì tự đóng lại.
     *
     * Tính cả đơn CONFIRMED chứ không riêng PAID: khách đã ở xong rồi thì đợt lưu
     * trú kết thúc, chuyện còn nợ tiền là việc thu ngân, không phải lý do để giữ
     * phòng mãi trong hệ thống.
     */
    public static int markCompleted(Connection conn) throws SQLException {
        String sql = """
            UPDATE booking b
            SET status = 'COMPLETED'
            WHERE b.status IN ('CONFIRMED','PAID')
              AND EXISTS (SELECT 1 FROM booking_line l WHERE l.booking_id = b.booking_id)
              AND NOT EXISTS (
                    SELECT 1 FROM booking_line l
                    WHERE l.booking_id = b.booking_id
                      AND COALESCE(l.end_at, l.start_at) > now()
              )
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            return ps.executeUpdate();
        }
    }

    /**
     * Xóa đơn nháp bỏ dở; các dòng con tự xóa theo nhờ ON DELETE CASCADE.
     *
     * Hai mốc khác nhau vì đây là hai loại rác khác nhau:
     *  - Đơn nháp RỖNG: khách bấm "Đặt lịch" rồi thoát, chưa chọn dịch vụ nào.
     *    Nó không giữ chỗ và không có gì để mất, nên dọn sớm sau vài giờ.
     *  - Đơn nháp ĐÃ CÓ dịch vụ: khách chọn rồi nhưng chưa chốt. Nó đang giữ
     *    phòng và khách có thể quay lại hoàn tất, nên chờ đủ 24 giờ mới xóa.
     */
    public static int cleanExpiredDrafts(Connection conn) throws SQLException {
        int removed = 0;

        String empty = """
            DELETE FROM booking b
            WHERE b.status = 'DRAFT'
              AND b.created_at < now() - (? || ' hours')::interval
              AND NOT EXISTS (SELECT 1 FROM booking_line l WHERE l.booking_id = b.booking_id)
        """;
        try (PreparedStatement ps = conn.prepareStatement(empty)) {
            ps.setString(1, String.valueOf(EMPTY_DRAFT_HOURS));
            removed += ps.executeUpdate();
        }

        String started = "DELETE FROM booking WHERE status = 'DRAFT'"
                + " AND created_at < now() - (? || ' hours')::interval";
        try (PreparedStatement ps = conn.prepareStatement(started)) {
            ps.setString(1, String.valueOf(BookingService.DRAFT_EXPIRE_HOURS));
            removed += ps.executeUpdate();
        }
        return removed;
    }

    /**
     * Xóa nhật ký thông báo quá cũ.
     *
     * Bảng này chỉ ghi lại các tin đã gửi (mô phỏng) để khách tra lại khi thắc
     * mắc; giữ vài tháng là quá đủ, không dọn thì nó lớn mãi mà chẳng ai đọc.
     */
    public static int cleanOldNotifications(Connection conn) throws SQLException {
        String sql = "DELETE FROM notification"
                + " WHERE created_at < now() - (? || ' days')::interval";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, String.valueOf(NOTIFICATION_KEEP_DAYS));
            return ps.executeUpdate();
        }
    }
}
