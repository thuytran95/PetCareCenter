package com.petweb.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Một dòng dịch vụ trong đơn. Thay cho service_info + hotel_detail + spa_detail + medical_detail cũ.
 *
 * Ý nghĩa các trường theo từng loại dịch vụ:
 *   HOTEL   : startAt = ngày nhận phòng, endAt = ngày trả phòng, roomCode, quantity = số ngày.
 *   SPA     : startAt = ngày hẹn, endAt = null, các dịch vụ con nằm ở items.
 *   MEDICAL : startAt = ngày nhập viện, endAt = null, các dịch vụ con nằm ở items.
 */
public class BookingLine {

    public static final String TYPE_HOTEL = "HOTEL";
    public static final String TYPE_SPA = "SPA";
    public static final String TYPE_MEDICAL = "MEDICAL";

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private int lineId;
    private int bookingId;
    private String serviceType;
    private Timestamp startAt;
    private Timestamp endAt;
    private String roomCode;
    private int quantity = 1;
    private BigDecimal lineTotal = BigDecimal.ZERO;
    private String note;
    private Timestamp createdAt;

    /** Chỉ dùng cho dòng SPA / MEDICAL. */
    private List<BookingLineItem> items = new ArrayList<>();

    public BookingLine() {
    }

    public boolean isHotel() { return TYPE_HOTEL.equals(serviceType); }
    public boolean isSpa() { return TYPE_SPA.equals(serviceType); }
    public boolean isMedical() { return TYPE_MEDICAL.equals(serviceType); }

    /** Tên dịch vụ hiển thị cho người dùng. */
    public String getServiceLabel() {
        if (serviceType == null) return "";
        switch (serviceType) {
            case TYPE_HOTEL: return "Khách sạn";
            case TYPE_SPA: return "Spa";
            case TYPE_MEDICAL: return "Y tế";
            default: return serviceType;
        }
    }

    public String getFormattedStartAt() {
        return startAt == null ? "" : startAt.toLocalDateTime().format(FORMATTER);
    }

    public String getFormattedEndAt() {
        return endAt == null ? "" : endAt.toLocalDateTime().format(FORMATTER);
    }

    public int getLineId() { return lineId; }
    public void setLineId(int lineId) { this.lineId = lineId; }

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    public Timestamp getStartAt() { return startAt; }
    public void setStartAt(Timestamp startAt) { this.startAt = startAt; }

    public Timestamp getEndAt() { return endAt; }
    public void setEndAt(Timestamp endAt) { this.endAt = endAt; }

    public String getRoomCode() { return roomCode; }
    public void setRoomCode(String roomCode) { this.roomCode = roomCode; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public List<BookingLineItem> getItems() { return items; }
    public void setItems(List<BookingLineItem> items) {
        this.items = (items == null) ? new ArrayList<>() : items;
    }
}
