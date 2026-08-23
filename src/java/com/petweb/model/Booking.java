package com.petweb.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Một đơn đặt lịch. Phục vụ cả khách đã đăng nhập lẫn khách vãng lai:
 * - userId != null  → khách đã đăng nhập, petId trỏ tới thú cưng đã lưu.
 * - userId == null  → khách vãng lai, thông tin liên hệ nằm ở guestName/guestPhone/guestEmail
 *                     và thú cưng chỉ tồn tại dưới dạng snapshot petName/petSpecies.
 *
 * petName/petSpecies luôn là bản chụp tại thời điểm đặt, nên sửa hoặc xóa thú cưng
 * về sau không làm thay đổi các đơn cũ.
 */
public class Booking {

    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_CONFIRMED = "CONFIRMED";
    public static final String STATUS_PAID = "PAID";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String STATUS_COMPLETED = "COMPLETED";

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private int bookingId;

    private Integer userId;
    private String guestName;
    private String guestPhone;
    private String guestEmail;

    private Integer petId;
    private String petName;
    private String petSpecies;

    private String status;
    private BigDecimal totalPrice = BigDecimal.ZERO;
    private Timestamp createdAt;
    private Timestamp confirmedAt;
    private Timestamp paidAt;
    private Timestamp cancelledAt;

    /** Mã tra cứu cho khách vãng lai (khách đã đăng nhập tra qua tài khoản). */
    private String lookupCode;

    /**
     * Số điện thoại nhận thông báo. Không lưu trong CSDL: khách vãng lai lấy từ
     * guestPhone của đơn, khách đã đăng nhập được gán từ hồ sơ tài khoản lúc chạy.
     */
    private String contactPhone;

    /** Các dòng dịch vụ của đơn — chỉ được nạp khi cần hiển thị chi tiết/hóa đơn. */
    private List<BookingLine> lines = new ArrayList<>();

    public Booking() {
    }

    public boolean isGuestBooking() {
        return userId == null;
    }

    public boolean isDraft()     { return STATUS_DRAFT.equals(status); }
    public boolean isConfirmed() { return STATUS_CONFIRMED.equals(status); }
    public boolean isPaid()      { return STATUS_PAID.equals(status); }
    public boolean isCompleted() { return STATUS_COMPLETED.equals(status); }
    public boolean isCancelled() { return STATUS_CANCELLED.equals(status); }

    /** Đơn còn chờ khách thanh toán. */
    public boolean isAwaitingPayment() {
        return STATUS_CONFIRMED.equals(status);
    }

    /** Còn hủy được không: chỉ khi chưa thanh toán và chưa hoàn tất. */
    public boolean isCancellable() {
        return STATUS_CONFIRMED.equals(status) || STATUS_PAID.equals(status);
    }

    /**
     * Đơn QUÁ HẠN THANH TOÁN: đã chốt, chưa trả tiền, mà mọi mốc dịch vụ đã trôi qua.
     * Đây là trạng thái TÍNH RA lúc hiển thị chứ không lưu trong CSDL, nên luôn
     * đúng theo thời điểm xem, không phụ thuộc tác vụ nền có chạy hay chưa.
     */
    public boolean isOverdue() {
        if (!STATUS_CONFIRMED.equals(status)) return false;
        if (lines == null || lines.isEmpty()) return false;
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        for (BookingLine line : lines) {
            Timestamp mark = (line.getEndAt() != null) ? line.getEndAt() : line.getStartAt();
            if (mark != null && mark.toLocalDateTime().isAfter(now)) {
                return false; // còn ít nhất một mốc ở tương lai
            }
        }
        return true;
    }

    /** Nhãn trạng thái hiển thị cho người dùng. */
    public String getStatusLabel() {
        if (isOverdue()) return "Quá hạn thanh toán";
        if (status == null) return "";
        switch (status) {
            case STATUS_DRAFT: return "Đang đặt";
            case STATUS_CONFIRMED: return "Chờ thanh toán";
            case STATUS_PAID: return "Đã thanh toán";
            case STATUS_COMPLETED: return "Đã hoàn tất";
            case STATUS_CANCELLED: return "Đã hủy";
            default: return status;
        }
    }

    /** Màu hiển thị tương ứng trạng thái (khớp bộ màu trong common.css). */
    public String getStatusColor() {
        if (isOverdue()) return "pink";
        if (status == null) return "blue";
        switch (status) {
            case STATUS_DRAFT: return "blue";
            case STATUS_CONFIRMED: return "amber";
            case STATUS_PAID: return "teal";
            case STATUS_COMPLETED: return "blue";
            case STATUS_CANCELLED: return "pink";
            default: return "blue";
        }
    }

    /** Tên người đặt để hiển thị, không phụ thuộc khách đăng nhập hay vãng lai. */
    public String getCustomerName() {
        return (guestName != null && !guestName.isBlank()) ? guestName : "";
    }

    public String getFormattedCreatedAt() {
        return createdAt == null ? "" : createdAt.toLocalDateTime().format(FORMATTER);
    }

    public String getFormattedConfirmedAt() {
        return confirmedAt == null ? "" : confirmedAt.toLocalDateTime().format(FORMATTER);
    }

    /** Tổng tiền tính lại từ các dòng đang giữ trong bộ nhớ (dùng để đối chiếu với totalPrice trong DB). */
    public BigDecimal sumLineTotals() {
        BigDecimal sum = BigDecimal.ZERO;
        for (BookingLine line : lines) {
            if (line.getLineTotal() != null) {
                sum = sum.add(line.getLineTotal());
            }
        }
        return sum;
    }

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }

    public String getGuestPhone() { return guestPhone; }
    public void setGuestPhone(String guestPhone) { this.guestPhone = guestPhone; }

    public String getGuestEmail() { return guestEmail; }
    public void setGuestEmail(String guestEmail) { this.guestEmail = guestEmail; }

    public Integer getPetId() { return petId; }
    public void setPetId(Integer petId) { this.petId = petId; }

    public String getPetName() { return petName; }
    public void setPetName(String petName) { this.petName = petName; }

    public String getPetSpecies() { return petSpecies; }
    public void setPetSpecies(String petSpecies) { this.petSpecies = petSpecies; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(Timestamp confirmedAt) { this.confirmedAt = confirmedAt; }

    public Timestamp getPaidAt() { return paidAt; }
    public void setPaidAt(Timestamp paidAt) { this.paidAt = paidAt; }

    public Timestamp getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(Timestamp cancelledAt) { this.cancelledAt = cancelledAt; }

    /** Số nhận thông báo: ưu tiên số gán lúc chạy, không có thì dùng số trên đơn. */
    public String getContactPhone() {
        return (contactPhone != null && !contactPhone.isBlank()) ? contactPhone : guestPhone;
    }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getLookupCode() { return lookupCode; }
    public void setLookupCode(String lookupCode) { this.lookupCode = lookupCode; }

    public String getFormattedPaidAt() {
        return paidAt == null ? "" : paidAt.toLocalDateTime().format(FORMATTER);
    }

    public List<BookingLine> getLines() { return lines; }
    public void setLines(List<BookingLine> lines) {
        this.lines = (lines == null) ? new ArrayList<>() : lines;
    }
}
