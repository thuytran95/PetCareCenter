package com.petweb.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Một lịch hẹn sắp tới, ghép từ booking + booking_line để hiển thị trên trang chủ.
 *
 * Đây là đối tượng chỉ để đọc/hiển thị (không ánh xạ 1-1 với bảng nào), gom sẵn
 * tên thú cưng và loại dịch vụ để JSP không phải truy vấn thêm.
 */
public class Appointment {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("HH:mm 'ngày' dd/MM/yyyy");

    private int bookingId;
    private int lineId;
    private String petName;
    private String serviceType;
    private String roomLabel;
    private Timestamp startAt;
    private Timestamp endAt;
    private BigDecimal lineTotal = BigDecimal.ZERO;

    public String getServiceLabel() {
        if (serviceType == null) return "";
        switch (serviceType) {
            case BookingLine.TYPE_HOTEL: return "Khách sạn";
            case BookingLine.TYPE_SPA: return "Spa";
            case BookingLine.TYPE_MEDICAL: return "Y tế";
            default: return serviceType;
        }
    }

    /** Tên icon FontAwesome tương ứng, để JSP không phải tự if/else. */
    public String getIconClass() {
        if (serviceType == null) return "fa-paw";
        switch (serviceType) {
            case BookingLine.TYPE_HOTEL: return "fa-house";
            case BookingLine.TYPE_SPA: return "fa-spa";
            case BookingLine.TYPE_MEDICAL: return "fa-briefcase-medical";
            default: return "fa-paw";
        }
    }

    /** Màu chủ đề tương ứng loại dịch vụ (khớp bộ màu trong common.css). */
    public String getColorName() {
        if (serviceType == null) return "blue";
        switch (serviceType) {
            case BookingLine.TYPE_HOTEL: return "blue";
            case BookingLine.TYPE_SPA: return "pink";
            case BookingLine.TYPE_MEDICAL: return "teal";
            default: return "blue";
        }
    }

    public String getFormattedStartAt() {
        return startAt == null ? "" : startAt.toLocalDateTime().format(DATE_TIME);
    }

    /**
     * Lời nhắc dạng thân thiện: "Hôm nay", "Ngày mai", "Còn 5 ngày".
     * Tính theo NGÀY LỊCH nên "ngày mai" luôn đúng nghĩa dù bây giờ là mấy giờ.
     */
    public String getReminderText() {
        if (startAt == null) return "";
        LocalDate today = LocalDate.now();
        LocalDate day = startAt.toLocalDateTime().toLocalDate();
        long days = ChronoUnit.DAYS.between(today, day);

        if (days < 0) return "Đã qua";
        if (days == 0) return "Hôm nay";
        if (days == 1) return "Ngày mai";
        return "Còn " + days + " ngày";
    }

    /** Lịch hẹn trong vòng 24 giờ tới — dùng để làm nổi bật thẻ nhắc. */
    public boolean isUrgent() {
        if (startAt == null) return false;
        LocalDateTime start = startAt.toLocalDateTime();
        return !start.isBefore(LocalDateTime.now())
                && start.isBefore(LocalDateTime.now().plusDays(1));
    }

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public int getLineId() { return lineId; }
    public void setLineId(int lineId) { this.lineId = lineId; }

    public String getPetName() { return petName; }
    public void setPetName(String petName) { this.petName = petName; }

    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    public String getRoomLabel() { return roomLabel; }
    public void setRoomLabel(String roomLabel) { this.roomLabel = roomLabel; }

    public Timestamp getStartAt() { return startAt; }
    public void setStartAt(Timestamp startAt) { this.startAt = startAt; }

    public Timestamp getEndAt() { return endAt; }
    public void setEndAt(Timestamp endAt) { this.endAt = endAt; }

    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
}
