package com.petweb.model;

import java.math.BigDecimal;

/**
 * Loại phòng khách sạn thú cưng, đọc từ bảng room_type.
 *
 * Trước đây giá phòng nằm cứng trong HotelDetail.getGiaTheoLoaiPhong() và lặp lại
 * lần nữa trong HotelDetail.jsp; giờ chỉ còn một nguồn duy nhất là DB nên đổi giá
 * không phải sửa code.
 */
public class RoomType {

    private String roomCode;
    private String roomName;
    private BigDecimal pricePerDay = BigDecimal.ZERO;
    private String description;
    private int totalRooms;
    private boolean active = true;

    public RoomType() {
    }

    public String getRoomCode() { return roomCode; }
    public void setRoomCode(String roomCode) { this.roomCode = roomCode; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public BigDecimal getPricePerDay() { return pricePerDay; }
    public void setPricePerDay(BigDecimal pricePerDay) { this.pricePerDay = pricePerDay; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getTotalRooms() { return totalRooms; }
    public void setTotalRooms(int totalRooms) { this.totalRooms = totalRooms; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
