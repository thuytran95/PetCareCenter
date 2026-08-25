package com.petweb.model;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

/**
 * Một đợt lưu trú khách sạn của thú cưng, gắn với đơn đã đặt.
 *
 * Dùng để trả lời hai câu hỏi trên hồ sơ của bé: bé có đang ở khách sạn không,
 * và bé đã được đặt phòng cho lần tới chưa. Một bé không bao giờ có hai đợt lưu
 * trú chồng lấn nhau, nên mỗi thời điểm chỉ có tối đa một đợt "đang ở".
 */
public class PetStay {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
    private static final DateTimeFormatter D = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private int bookingId;
    private int lineId;
    private int petId;
    private String roomCode;
    private String roomName;
    private Timestamp startAt;
    private Timestamp endAt;
    private String bookingStatus;

    /** Bé đang ở khách sạn ngay lúc này. */
    public boolean isOngoing() {
        long now = System.currentTimeMillis();
        return startAt != null && endAt != null
                && startAt.getTime() <= now && endAt.getTime() > now;
    }

    /** Đợt lưu trú đã đặt nhưng chưa tới ngày nhận phòng. */
    public boolean isUpcoming() {
        return startAt != null && startAt.getTime() > System.currentTimeMillis();
    }

    /** Quá giờ trả phòng mà đơn vẫn chưa được đóng — cần nhắc chủ nuôi trả phòng. */
    public boolean isOverdue() {
        return endAt != null && endAt.getTime() <= System.currentTimeMillis();
    }

    /**
     * Đơn giữ phòng này còn hủy được không.
     *
     * Bé mới đặt phòng mà chưa tới ngày nhận thì chưa "trả phòng" được — cách
     * để nhả phòng ra và đặt lại khoảng khác chính là hủy đơn.
     */
    public boolean isCancellable() {
        return Booking.STATUS_CONFIRMED.equals(bookingStatus)
                || Booking.STATUS_PAID.equals(bookingStatus);
    }

    /** Đơn còn là bản nháp — khách đang đặt dở, chưa chốt. */
    public boolean isDraft() {
        return Booking.STATUS_DRAFT.equals(bookingStatus);
    }

    /** Số ngày còn lại của đợt lưu trú, làm tròn lên; đã quá hạn thì trả về 0. */
    public long getDaysLeft() {
        if (endAt == null) return 0;
        long ms = endAt.getTime() - System.currentTimeMillis();
        if (ms <= 0) return 0;
        return (ms + 86_400_000L - 1) / 86_400_000L;
    }

    /** Chữ mô tả ngắn để hiện trên thẻ thú cưng. */
    public String getStateText() {
        // Đơn nháp chưa chốt thì chưa phải một chỗ ở chắc chắn, phải nói rõ
        // để chủ nuôi biết mình còn phải hoàn tất đơn.
        if (isDraft()) return "Đơn đang đặt dở, chưa chốt";
        if (isOverdue()) return "Quá giờ trả phòng";
        if (isOngoing()) {
            long d = getDaysLeft();
            return d <= 1 ? "Đang ở, trả phòng hôm nay" : "Đang ở, còn " + d + " ngày";
        }
        if (isUpcoming()) return "Đã đặt phòng, chờ nhận phòng";
        return "Đã trả phòng";
    }

    public String getStateColor() {
        if (isDraft()) return "amber";
        if (isOverdue()) return "pink";
        if (isOngoing()) return "teal";
        if (isUpcoming()) return "blue";
        return "amber";
    }

    /** Đợt lưu trú đang diễn ra và đơn đã chốt — lúc này mới trả phòng được. */
    public boolean isCheckOutable() {
        return isCancellable() && !isUpcoming();
    }

    public String getFormattedStartAt() {
        return startAt == null ? "" : startAt.toLocalDateTime().format(DT);
    }

    public String getFormattedEndAt() {
        return endAt == null ? "" : endAt.toLocalDateTime().format(DT);
    }

    /** Khoảng ngày gọn gàng để nhét vừa thẻ thú cưng. */
    public String getFormattedRange() {
        if (startAt == null || endAt == null) return "";
        return startAt.toLocalDateTime().format(D) + " → " + endAt.toLocalDateTime().format(D);
    }

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public int getLineId() { return lineId; }
    public void setLineId(int lineId) { this.lineId = lineId; }

    public int getPetId() { return petId; }
    public void setPetId(int petId) { this.petId = petId; }

    public String getRoomCode() { return roomCode; }
    public void setRoomCode(String roomCode) { this.roomCode = roomCode; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public Timestamp getStartAt() { return startAt; }
    public void setStartAt(Timestamp startAt) { this.startAt = startAt; }

    public Timestamp getEndAt() { return endAt; }
    public void setEndAt(Timestamp endAt) { this.endAt = endAt; }

    public String getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(String bookingStatus) { this.bookingStatus = bookingStatus; }
}
