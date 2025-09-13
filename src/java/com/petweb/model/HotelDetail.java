package com.petweb.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HotelDetail {
    private int hotelId;
    private int serviceId;
    private Timestamp checkIn;
    private Timestamp checkOut;
    private String roomType;    // Loại phòng (vip1, vip2, vip3, ...)
    private String status;      // available / busy
    private int roomQuantity;   // số lượng phòng

    // Formatter để hiển thị ngày giờ
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public HotelDetail() {}

    public HotelDetail(int hotelId, int serviceId, Timestamp checkIn, Timestamp checkOut,
                       String roomType, String status, int roomQuantity) {
        this.hotelId = hotelId;
        this.serviceId = serviceId;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.roomType = roomType;
        this.status = status;
   
    }

    // ====== LẤY GIÁ THEO LOẠI PHÒNG ======
    private BigDecimal getGiaTheoLoaiPhong(String roomType) {
        if (roomType == null) return BigDecimal.ZERO;

        switch (roomType.toLowerCase()) {
            case "vip1": return BigDecimal.valueOf(100_000);
            case "vip2": return BigDecimal.valueOf(200_000);
            case "vip3": return BigDecimal.valueOf(500_000);
            default: return BigDecimal.ZERO;
        }
    }

    // ====== HÀM TÍNH SỐ NGÀY Ở ======
public long getSoNgayO() {
    if (checkIn == null || checkOut == null) return 0;

    LocalDateTime in = checkIn.toLocalDateTime();
    LocalDateTime out = checkOut.toLocalDateTime();

    
    if (out.isBefore(in)) {
        throw new IllegalArgumentException("Ngày check-out không được trước ngày check-in!");
    }

    long days = Duration.between(in, out).toDays();

    // Nếu cùng ngày (days = 0) thì tính là 1 ngày
    return days <= 0 ? 1 : days;
}

    // ====== HÀM TÍNH TỔNG TIỀN ======
    public BigDecimal tinhTongTien() {
        long soNgay = getSoNgayO();
        if (roomType == null) return BigDecimal.ZERO;

        BigDecimal giaPhong = getGiaTheoLoaiPhong(roomType);
        return giaPhong.multiply(BigDecimal.valueOf(soNgay));
    }

    // ====== HÀM ĐỊNH DẠNG NGÀY GIỜ ======
    public String getFormattedCheckIn() {
        if (checkIn == null) return "";
        return checkIn.toLocalDateTime().format(FORMATTER);
    }

    public String getFormattedCheckOut() {
        if (checkOut == null) return "";
        return checkOut.toLocalDateTime().format(FORMATTER);
    }

    // ====== Getter & Setter ======
    public int getHotelId() { return hotelId; }
    public void setHotelId(int hotelId) { this.hotelId = hotelId; }

    public int getServiceId() { return serviceId; }
    public void setServiceId(int serviceId) { this.serviceId = serviceId; }

    public Timestamp getCheckIn() { return checkIn; }
    public void setCheckIn(Timestamp checkIn) { this.checkIn = checkIn; }

    public Timestamp getCheckOut() { return checkOut; }
    public void setCheckOut(Timestamp checkOut) { this.checkOut = checkOut; }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }


}