package com.petweb.model;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

/**
 * Một thông báo đã "gửi" tới khách.
 *
 * LƯU Ý: hệ thống KHÔNG gửi SMS thật. Mỗi lần gửi chỉ ghi lại một dòng ở đây
 * để khách xem được lịch sử và để mô phỏng đúng luồng nghiệp vụ. Muốn gửi thật
 * thì chỉ cần thay phần ghi bảng bằng lệnh gọi tới nhà cung cấp SMS.
 */
public class Notification {

    public static final String EVENT_BOOKING_CONFIRMED = "BOOKING_CONFIRMED";
    public static final String EVENT_PAYMENT = "PAYMENT";
    public static final String EVENT_CANCELLED = "CANCELLED";
    public static final String EVENT_REMINDER = "REMINDER";

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    private int notificationId;
    private Integer bookingId;
    private Integer userId;
    private String recipient;
    private String channel;
    private String eventType;
    private String content;
    private Timestamp createdAt;

    public String getFormattedCreatedAt() {
        return createdAt == null ? "" : createdAt.toLocalDateTime().format(FORMATTER);
    }

    /** Nhãn tiếng Việt của loại thông báo, để JSP không phải tự if/else. */
    public String getEventLabel() {
        if (eventType == null) return "";
        switch (eventType) {
            case EVENT_BOOKING_CONFIRMED: return "Xác nhận đặt lịch";
            case EVENT_PAYMENT: return "Thanh toán";
            case EVENT_CANCELLED: return "Hủy lịch";
            case EVENT_REMINDER: return "Nhắc lịch hẹn";
            default: return eventType;
        }
    }

    public String getIconClass() {
        if (eventType == null) return "fa-bell";
        switch (eventType) {
            case EVENT_BOOKING_CONFIRMED: return "fa-circle-check";
            case EVENT_PAYMENT: return "fa-credit-card";
            case EVENT_CANCELLED: return "fa-circle-xmark";
            case EVENT_REMINDER: return "fa-bell";
            default: return "fa-bell";
        }
    }

    public String getColorName() {
        if (eventType == null) return "blue";
        switch (eventType) {
            case EVENT_BOOKING_CONFIRMED: return "teal";
            case EVENT_PAYMENT: return "amber";
            case EVENT_CANCELLED: return "pink";
            case EVENT_REMINDER: return "blue";
            default: return "blue";
        }
    }

    /** Che bớt số điện thoại khi hiển thị: 0912345678 -> 0912***678 */
    public String getMaskedRecipient() {
        if (recipient == null || recipient.length() < 7) return recipient == null ? "" : recipient;
        int keepTail = 3;
        int keepHead = 4;
        return recipient.substring(0, keepHead) + "***"
                + recipient.substring(recipient.length() - keepTail);
    }

    public int getNotificationId() { return notificationId; }
    public void setNotificationId(int notificationId) { this.notificationId = notificationId; }

    public Integer getBookingId() { return bookingId; }
    public void setBookingId(Integer bookingId) { this.bookingId = bookingId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
