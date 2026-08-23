package com.petweb.service;

import com.petweb.dao.HealthRecordDAO;
import com.petweb.model.Booking;
import com.petweb.model.BookingLine;
import com.petweb.model.BookingLineItem;
import com.petweb.model.HealthRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sinh sổ sức khỏe (sổ tiêm / khám định kỳ) cho thú cưng.
 *
 * Nguyên tắc: KHÔNG bắt người dùng nhập lại. Mỗi khi một đơn y tế được thanh toán,
 * hệ thống tự ghi các hạng mục trong đơn vào sổ của bé, kèm ngày cần làm lại tính
 * từ chu kỳ khai báo ở bảng giá (medical_service_item.repeat_months).
 *
 * Nhờ vậy sổ sức khỏe luôn khớp với lịch sử đặt lịch, không có chuyện lệch nhau.
 *
 * Chỉ áp dụng cho thú cưng ĐÃ LƯU trong hệ thống (booking.pet_id khác null).
 * Đơn của khách vãng lai không có hồ sơ thú cưng nên không ghi sổ được.
 */
public class HealthRecordService {

    private static final Logger LOGGER = Logger.getLogger(HealthRecordService.class.getName());

    private HealthRecordService() {
    }

    /**
     * Ghi sổ cho toàn bộ hạng mục y tế của một đơn.
     * Gọi sau khi đơn được thanh toán. An toàn khi gọi lại nhiều lần: DAO dùng
     * ON CONFLICT DO NOTHING nên không sinh bản ghi trùng.
     *
     * Ghi sổ là việc phụ — hỏng thì chỉ ghi log, không được làm hỏng thanh toán.
     */
    public static int recordFromBooking(Connection conn, Booking booking) {
        if (booking == null || booking.getPetId() == null) {
            return 0; // khách vãng lai: không có hồ sơ thú cưng để ghi sổ
        }

        try {
            Map<Integer, ItemInfo> catalog = loadMedicalCatalog(conn);
            int written = 0;

            for (BookingLine line : booking.getLines()) {
                if (!line.isMedical()) continue;

                for (BookingLineItem item : line.getItems()) {
                    ItemInfo info = catalog.get(item.getItemId());
                    String type = (info == null) ? HealthRecord.TYPE_OTHER : info.category;
                    Integer repeat = (info == null) ? null : info.repeatMonths;

                    HealthRecord r = new HealthRecord();
                    r.setPetId(booking.getPetId());
                    r.setBookingId(booking.getBookingId());
                    r.setLineId(line.getLineId());
                    r.setRecordType(type);
                    r.setItemId(item.getItemId());
                    r.setItemName(item.getItemName());
                    r.setPerformedAt(line.getStartAt());
                    r.setNextDueAt(computeNextDue(line.getStartAt(), repeat));

                    written += HealthRecordDAO.insertIfAbsent(conn, r);
                }
            }
            return written;

        } catch (SQLException e) {
            LOGGER.log(Level.WARNING,
                    "Không ghi được sổ sức khỏe cho đơn #" + booking.getBookingId(), e);
            return 0;
        }
    }

    /**
     * Ngày cần làm lại = ngày thực hiện + số tháng của chu kỳ.
     * Trả về null khi hạng mục không cần lặp lại (ví dụ triệt sản).
     */
    public static Timestamp computeNextDue(Timestamp performedAt, Integer repeatMonths) {
        if (performedAt == null || repeatMonths == null || repeatMonths <= 0) {
            return null;
        }
        return Timestamp.valueOf(performedAt.toLocalDateTime().plusMonths(repeatMonths));
    }

    /** Đọc phân loại + chu kỳ của toàn bộ hạng mục y tế trong một lần truy vấn. */
    private static Map<Integer, ItemInfo> loadMedicalCatalog(Connection conn) throws SQLException {
        Map<Integer, ItemInfo> map = new HashMap<>();
        String sql = "SELECT item_id, category, repeat_months FROM medical_service_item";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ItemInfo info = new ItemInfo();
                info.category = rs.getString("category");
                int rm = rs.getInt("repeat_months");
                info.repeatMonths = rs.wasNull() ? null : rm;
                map.put(rs.getInt("item_id"), info);
            }
        }
        return map;
    }

    /** Phân loại và chu kỳ nhắc lại của một hạng mục y tế. */
    private static class ItemInfo {
        String category;
        Integer repeatMonths;
    }
}
