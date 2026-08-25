package com.petweb.utils;

import com.petweb.dao.BookingDAO;
import com.petweb.model.Booking;
import com.petweb.model.BookingLine;
import com.petweb.service.BookingService;
import com.petweb.service.MaintenanceService;
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
 * Hẹn giờ chạy các tác vụ nền khi server hoạt động.
 *
 * Cứ 15 phút một lượt, hệ thống tự giữ cho dữ liệu đúng và không phình ra:
 *
 *  1. Hủy đơn khách không đến — đã xác nhận nhưng quá giờ nhận phòng vẫn chưa
 *     thanh toán, để nhả phòng cho người khác.
 *  2. Đóng đơn đã phục vụ xong thành COMPLETED.
 *  3. Dọn đơn nháp bỏ dở: đơn rỗng sau 3 giờ, đơn đã chọn dịch vụ sau 24 giờ.
 *  4. Xóa nhật ký thông báo cũ hơn 90 ngày.
 *  5. Gửi tin nhắc hẹn (mô phỏng) cho các lịch trong 24 giờ tới.
 *
 * Quy tắc của bốn việc đầu nằm ở {@link MaintenanceService}; lớp này chỉ lo
 * việc hẹn giờ và ghi log, nhờ vậy các quy tắc đó kiểm thử được độc lập.
 *
 * Ranh giới quan trọng: chỉ dọn thứ KHÔNG phải chứng từ. Hóa đơn — kể cả đơn đã
 * hủy hay đã hoàn tất — không bao giờ bị xóa.
 */
public class AutoStatusListener implements ServletContextListener {

    private static final Logger LOGGER = Logger.getLogger(AutoStatusListener.class.getName());

    /** Khoảng cách giữa hai lần dọn, tính bằng phút. */
    private static final int SWEEP_MINUTES = 15;

    private ScheduledExecutorService scheduler;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "petcare-auto-status");
            t.setDaemon(true); // không giữ JVM sống khi Tomcat dừng
            return t;
        });
        // Chạy một lượt ngay sau khi khởi động để dọn những gì đọng lại từ lần trước
        scheduler.scheduleAtFixedRate(this::sweep, 1, SWEEP_MINUTES, TimeUnit.MINUTES);
    }

    private void sweep() {
        try (Connection conn = ConnectionUtils.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Hủy khách không đến TRƯỚC khi đóng đơn hoàn tất, để một đơn quá
                // hạn chưa trả tiền không bị ghi nhầm thành đã phục vụ xong.
                int noShow = MaintenanceService.cancelNoShows(conn);
                int completed = MaintenanceService.markCompleted(conn);
                int cleaned = MaintenanceService.cleanExpiredDrafts(conn);
                int purged = MaintenanceService.cleanOldNotifications(conn);
                int reminded = sendUpcomingReminders(conn);
                conn.commit();
                if (noShow > 0 || completed > 0 || cleaned > 0 || purged > 0 || reminded > 0) {
                    LOGGER.log(Level.INFO,
                            "Tác vụ nền: {0} đơn khách không đến, {1} đơn hoàn tất, "
                            + "{2} đơn nháp quá hạn được dọn, {3} thông báo cũ được xóa, "
                            + "{4} tin nhắc hẹn.",
                            new Object[]{noShow, completed, cleaned, purged, reminded});
                }
            } catch (Exception ex) {
                ConnectionUtils.rollbackQuietly(conn);
                throw ex;
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Tác vụ nền cập nhật trạng thái đơn thất bại", e);
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

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }
}
