package com.petweb.utils;

import com.petweb.dao.BookingDAO;
import com.petweb.model.Appointment;
import com.petweb.model.Booking;
import com.petweb.model.BookingLine;
import com.petweb.service.BookingService;
import com.petweb.service.NotificationService;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tác vụ nền chạy định kỳ khi server hoạt động.
 *
 * Trước đây listener này reset cột status của hotel_detail/spa_detail/medical_detail —
 * các bảng đó đã được thay bằng booking/booking_line nên nhiệm vụ giờ là:
 *
 *  1. Đánh dấu COMPLETED cho những đơn đã xác nhận mà toàn bộ dịch vụ đã qua thời gian.
 *  2. Dọn các đơn nháp (DRAFT) khách bỏ dở quá lâu — trước đây những dòng PENDING
 *     kiểu này nằm lại trong DB vĩnh viễn.
 */
public class AutoStatusListener implements ServletContextListener {

    private static final Logger LOGGER = Logger.getLogger(AutoStatusListener.class.getName());

    private ScheduledExecutorService scheduler;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "petcare-auto-status");
            t.setDaemon(true); // không giữ JVM sống khi Tomcat dừng
            return t;
        });
        scheduler.scheduleAtFixedRate(this::sweep, 1, 60, TimeUnit.MINUTES);
    }

    private void sweep() {
        try (Connection conn = ConnectionUtils.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int completed = markCompleted(conn);
                int cleaned = cleanExpiredDrafts(conn);
                int reminded = sendUpcomingReminders(conn);
                conn.commit();
                if (completed > 0 || cleaned > 0 || reminded > 0) {
                    LOGGER.log(Level.INFO,
                            "Tác vụ nền: {0} đơn hoàn tất, {1} đơn nháp quá hạn được dọn, {2} tin nhắc hẹn.",
                            new Object[]{completed, cleaned, reminded});
                }
            } catch (Exception ex) {
                ConnectionUtils.rollbackQuietly(conn);
                throw ex;
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Tác vụ nền cập nhật trạng thái đơn thất bại", e);
        }
    }

    /** Đơn CONFIRMED mà mọi dòng dịch vụ đều đã kết thúc thì chuyển sang COMPLETED. */
    private int markCompleted(Connection conn) throws Exception {
        String sql = """
            UPDATE booking b
            SET status = 'COMPLETED'
            WHERE b.status = 'PAID'
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
     * Gửi tin nhắc hẹn (mô phỏng) cho các lịch diễn ra trong 24 giờ tới.
     * NotificationService tự kiểm tra để mỗi đơn chỉ nhắc một lần.
     */
    private int sendUpcomingReminders(Connection conn) throws Exception {
        String sql = """
            SELECT DISTINCT b.booking_id
            FROM booking_line l
            JOIN booking b ON b.booking_id = l.booking_id
            WHERE b.status IN ('CONFIRMED', 'PAID')
              AND l.start_at BETWEEN now() AND now() + interval '24 hours'
        """;
        List<Integer> ids = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) ids.add(rs.getInt(1));
        }

        int sent = 0;
        for (Integer id : ids) {
            Booking booking = BookingDAO.findByIdWithLines(conn, id);
            if (booking == null || booking.getLines().isEmpty()) continue;
            BookingService.attachContactPhone(conn, booking);
            BookingLine first = booking.getLines().get(0);
            NotificationService.sendReminderOnce(conn, booking, first);
            sent++;
        }
        return sent;
    }

    /** Xóa đơn nháp bỏ dở quá hạn; các dòng con tự xóa theo nhờ ON DELETE CASCADE. */
    private int cleanExpiredDrafts(Connection conn) throws Exception {
        String sql = "DELETE FROM booking WHERE status = 'DRAFT' AND created_at < now() - (? || ' hours')::interval";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, String.valueOf(BookingService.DRAFT_EXPIRE_HOURS));
            return ps.executeUpdate();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }
}
