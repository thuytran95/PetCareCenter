package com.petweb.service;

import com.petweb.dao.NotificationDAO;
import com.petweb.model.Booking;
import com.petweb.model.BookingLine;
import com.petweb.model.Notification;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gửi thông báo cho khách khi có thay đổi về đơn đặt lịch.
 *
 * ===================== MÔ PHỎNG, KHÔNG GỬI SMS THẬT =====================
 * Lớp này KHÔNG kết nối tới nhà cung cấp SMS nào. Mỗi lần "gửi", nó chỉ:
 *   1. Soạn nội dung tin nhắn,
 *   2. Ghi một dòng vào bảng notification,
 *   3. Ghi log ở mức INFO để xem được trong console Tomcat.
 *
 * Nhờ vậy toàn bộ luồng nghiệp vụ (đặt lịch → nhận tin → thanh toán → nhận tin)
 * chạy đầy đủ và có dữ liệu thật để trình bày, mà không tốn chi phí SMS.
 * Muốn gửi thật về sau chỉ cần thay phần thân của phương thức send().
 * ========================================================================
 *
 * Thông báo là việc phụ: nếu ghi thất bại thì CHỈ ghi log chứ không làm hỏng
 * giao dịch đặt lịch của khách.
 */
public class NotificationService {

    /** Tên hiển thị của trung tâm ở đầu mỗi tin nhắn. */
    private static final String BRAND = "PetCare";

    private static final Logger LOGGER = Logger.getLogger(NotificationService.class.getName());

    private NotificationService() {
    }

    // =====================================================================
    // Các sự kiện gửi tin
    // =====================================================================

    /** Sau khi khách chốt đơn. */
    public static void sendBookingConfirmed(Connection conn, Booking booking) {
        String services = summarizeServices(booking);
        String content = BRAND + ": Ban da dat lich thanh cong cho "
                + booking.getPetName() + "."
                + (services.isEmpty() ? "" : " Dich vu: " + services + ".")
                + " Tong tien: " + formatMoney(booking.getTotalPrice()) + " d."
                + " Ma don: #" + booking.getBookingId() + "."
                + (booking.getLookupCode() == null ? ""
                        : " Ma tra cuu: " + booking.getLookupCode() + ".");
        send(conn, booking, Notification.EVENT_BOOKING_CONFIRMED, content);
    }

    /** Sau khi khách bấm thanh toán (mô phỏng). */
    public static void sendPaymentReceived(Connection conn, Booking booking) {
        String content = BRAND + ": Da nhan thanh toan "
                + formatMoney(booking.getTotalPrice()) + " d cho don #"
                + booking.getBookingId() + ". Cam on ban da tin tuong PetCare!";
        send(conn, booking, Notification.EVENT_PAYMENT, content);
    }

    /** Sau khi khách hủy đơn. */
    public static void sendBookingCancelled(Connection conn, Booking booking) {
        String content = BRAND + ": Don #" + booking.getBookingId() + " cho "
                + booking.getPetName() + " da duoc huy."
                + " Neu can dat lai, vui long truy cap website.";
        send(conn, booking, Notification.EVENT_CANCELLED, content);
    }

    /**
     * Nhắc lịch hẹn sắp tới. Chỉ gửi MỘT lần cho mỗi đơn — trước khi gửi
     * kiểm tra trong bảng xem đã có tin nhắc nào cho đơn đó chưa.
     */
    public static void sendReminderOnce(Connection conn, Booking booking, BookingLine line) {
        try {
            if (NotificationDAO.existsForBooking(conn, booking.getBookingId(),
                    Notification.EVENT_REMINDER)) {
                return;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Không kiểm tra được lịch sử nhắc hẹn", e);
            return;
        }

        String content = BRAND + ": Nhac lich " + line.getServiceLabel()
                + " cho " + booking.getPetName()
                + " vao " + line.getFormattedStartAt() + "."
                + " Vui long den dung gio. Don #" + booking.getBookingId() + ".";
        send(conn, booking, Notification.EVENT_REMINDER, content);
    }

    // =====================================================================
    // Phần lõi
    // =====================================================================

    /**
     * "Gửi" tin: soạn xong thì ghi lại vào CSDL và log ra console.
     * Đây là chỗ duy nhất cần thay nếu sau này muốn gửi SMS thật.
     */
    private static void send(Connection conn, Booking booking, String eventType, String content) {
        String phone = resolvePhone(booking);
        if (!isSendablePhone(phone)) {
            LOGGER.log(Level.WARNING,
                    "Đơn #{0} không có số điện thoại hợp lệ ({1}), bỏ qua gửi thông báo.",
                    new Object[]{booking.getBookingId(), phone});
            return;
        }
        phone = phone.trim();

        Notification n = new Notification();
        n.setBookingId(booking.getBookingId());
        n.setUserId(booking.getUserId());
        n.setRecipient(phone);
        n.setChannel("SMS");
        n.setEventType(eventType);
        n.setContent(content);

        try {
            NotificationDAO.insert(conn, n);
            // Log để thấy được luồng gửi tin khi chạy thử
            LOGGER.log(Level.INFO, "[SMS MÔ PHỎNG] -> {0}: {1}", new Object[]{phone, content});
        } catch (SQLException e) {
            // Thông báo hỏng thì đơn hàng vẫn phải thành công
            LOGGER.log(Level.WARNING,
                    "Không ghi được thông báo cho đơn #" + booking.getBookingId(), e);
        }
    }

    /**
     * Số điện thoại có gửi được không.
     *
     * Ngoài null/rỗng còn phải loại các giá trị rác kiểu chuỗi "null"/"undefined"
     * (dữ liệu cũ trong CSDL có trường hợp này do form trước đây lưu thẳng chuỗi
     * "null" vào cột phone) và các số quá ngắn để là số điện thoại thật.
     */
    public static boolean isSendablePhone(String phone) {
        if (phone == null) return false;
        String p = phone.trim();
        if (p.isEmpty()) return false;
        if (p.equalsIgnoreCase("null") || p.equalsIgnoreCase("undefined")) return false;

        int digits = 0;
        for (char ch : p.toCharArray()) {
            if (Character.isDigit(ch)) digits++;
        }
        return digits >= 8; // số điện thoại Việt Nam ngắn nhất cũng 9-10 chữ số
    }

    /**
     * Số điện thoại nhận tin: khách vãng lai lấy từ đơn, khách đã đăng nhập
     * lấy từ hồ sơ tài khoản (do BookingService gán sẵn vào đơn trước khi gọi).
     */
    private static String resolvePhone(Booking booking) {
        return booking.getContactPhone();
    }

    /** Liệt kê gọn các dịch vụ trong đơn: "Spa, Khách sạn". */
    private static String summarizeServices(Booking booking) {
        List<BookingLine> lines = booking.getLines();
        if (lines == null || lines.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (BookingLine l : lines) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(l.getServiceLabel());
        }
        return sb.toString();
    }

    private static String formatMoney(java.math.BigDecimal amount) {
        return amount == null ? "0" : String.format("%,.0f", amount);
    }
}
