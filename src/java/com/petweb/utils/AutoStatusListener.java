package com.petweb.utils;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import java.sql.Connection;
import java.sql.Statement;
import java.util.concurrent.*;

/**
 * AutoStatusListener chạy ngầm khi server start
 * Định kỳ kiểm tra hotel_detail để reset trạng thái phòng đã hết hạn
 */
public class AutoStatusListener implements ServletContextListener {
    private ScheduledExecutorService scheduler;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        scheduler = Executors.newSingleThreadScheduledExecutor();

        // Chạy ngay khi start server, sau đó 1 tiếng lặp lại 1 lần
        scheduler.scheduleAtFixedRate(() -> {
            try (Connection conn = ConnectionUtils.getConnection();
                 Statement stmt = conn.createStatement()) {

                // Cập nhật các phòng đã hết hạn -> status = 'available'
                String sql = """
                    UPDATE hotel_detail
                    SET status = 'available'
                    WHERE check_out < NOW()
                      AND status = 'busy'
                    """;

                int rows = stmt.executeUpdate(sql);
                System.out.println("Auto reset expired rooms! Rows affected = " + rows);

                String sqlSpa = """
                    UPDATE spa_detail
                    SET status = 'available'
                    WHERE booking_date + INTERVAL '1 day' < NOW()
                    AND status = 'busy'
                    """;
                
                int rowsSpa = stmt.executeUpdate(sqlSpa);
                System.out.println("Auto reset expired spa! Rows affected = " + rowsSpa);
                
                    String sqlMedical = """
                        UPDATE medical_detail
                        SET status = 'available'
                        WHERE admission_date + INTERVAL '1 day' < NOW()
                        AND status = 'busy'
                    """;
                int rowsMedical = stmt.executeUpdate(sqlMedical);
                System.out.println("Auto reset expired medical! Rows affected = " + rowsMedical);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 1, TimeUnit.HOURS); // chạy mỗi 1 tiếng
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }
}
