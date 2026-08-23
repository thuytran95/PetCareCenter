package com.petweb.model;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Một mục trong sổ sức khỏe của thú cưng: một lần tiêm, tẩy giun hoặc khám.
 *
 * Các mục này KHÔNG nhập tay mà được sinh tự động từ đơn y tế đã thanh toán
 * (xem HealthRecordService), nên sổ sức khỏe luôn khớp với lịch sử đặt lịch.
 */
public class HealthRecord {

    public static final String TYPE_VACCINE = "VACCINE";
    public static final String TYPE_CHECKUP = "CHECKUP";
    public static final String TYPE_DEWORM = "DEWORM";
    public static final String TYPE_OTHER = "OTHER";

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private int recordId;
    private int petId;
    private Integer bookingId;
    private Integer lineId;
    private String recordType;
    private Integer itemId;
    private String itemName;
    private Timestamp performedAt;
    private Timestamp nextDueAt;
    private String note;

    /** Tên thú cưng — chỉ dùng khi truy vấn gộp nhiều bé (bảng nhắc lịch ở trang chủ). */
    private String petName;

    public String getTypeLabel() {
        if (recordType == null) return "";
        switch (recordType) {
            case TYPE_VACCINE: return "Tiêm phòng";
            case TYPE_CHECKUP: return "Khám sức khỏe";
            case TYPE_DEWORM: return "Tẩy giun";
            default: return "Dịch vụ khác";
        }
    }

    public String getIconClass() {
        if (recordType == null) return "fa-notes-medical";
        switch (recordType) {
            case TYPE_VACCINE: return "fa-syringe";
            case TYPE_CHECKUP: return "fa-stethoscope";
            case TYPE_DEWORM: return "fa-shield-virus";
            default: return "fa-notes-medical";
        }
    }

    public String getColorName() {
        if (recordType == null) return "blue";
        switch (recordType) {
            case TYPE_VACCINE: return "amber";
            case TYPE_CHECKUP: return "teal";
            case TYPE_DEWORM: return "pink";
            default: return "blue";
        }
    }

    public String getFormattedPerformedAt() {
        return performedAt == null ? "" : performedAt.toLocalDateTime().format(DATE);
    }

    public String getFormattedNextDueAt() {
        return nextDueAt == null ? "" : nextDueAt.toLocalDateTime().format(DATE);
    }

    /** Mục này có cần làm lại định kỳ không. */
    public boolean hasNextDue() {
        return nextDueAt != null;
    }

    /** Số ngày còn lại tới hạn; âm nghĩa là đã quá hạn. */
    public long daysUntilDue() {
        if (nextDueAt == null) return Long.MAX_VALUE;
        return ChronoUnit.DAYS.between(LocalDate.now(), nextDueAt.toLocalDateTime().toLocalDate());
    }

    /** Đã quá hạn làm lại. */
    public boolean isOverdue() {
        return hasNextDue() && daysUntilDue() < 0;
    }

    /** Sắp tới hạn trong vòng 30 ngày. */
    public boolean isDueSoon() {
        if (!hasNextDue()) return false;
        long d = daysUntilDue();
        return d >= 0 && d <= 30;
    }

    /** Câu nhắc thân thiện: "Quá hạn 5 ngày" / "Còn 12 ngày" / "Hôm nay". */
    public String getDueText() {
        if (!hasNextDue()) return "Không cần làm lại";
        long d = daysUntilDue();
        if (d < 0) return "Quá hạn " + (-d) + " ngày";
        if (d == 0) return "Đến hạn hôm nay";
        if (d == 1) return "Còn 1 ngày";
        return "Còn " + d + " ngày";
    }

    public int getRecordId() { return recordId; }
    public void setRecordId(int recordId) { this.recordId = recordId; }

    public int getPetId() { return petId; }
    public void setPetId(int petId) { this.petId = petId; }

    public Integer getBookingId() { return bookingId; }
    public void setBookingId(Integer bookingId) { this.bookingId = bookingId; }

    public Integer getLineId() { return lineId; }
    public void setLineId(Integer lineId) { this.lineId = lineId; }

    public String getRecordType() { return recordType; }
    public void setRecordType(String recordType) { this.recordType = recordType; }

    public Integer getItemId() { return itemId; }
    public void setItemId(Integer itemId) { this.itemId = itemId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public Timestamp getPerformedAt() { return performedAt; }
    public void setPerformedAt(Timestamp performedAt) { this.performedAt = performedAt; }

    public Timestamp getNextDueAt() { return nextDueAt; }
    public void setNextDueAt(Timestamp nextDueAt) { this.nextDueAt = nextDueAt; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getPetName() { return petName; }
    public void setPetName(String petName) { this.petName = petName; }
}
