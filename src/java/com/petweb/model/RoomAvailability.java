package com.petweb.model;

import java.math.BigDecimal;

/**
 * Tình trạng còn trống của một hạng phòng trong một khoảng thời gian cụ thể.
 *
 * Khác với {@link RoomType} chỉ mô tả hạng phòng, lớp này gắn thêm số phòng
 * đang bị chiếm trong đúng khoảng ngày khách hỏi, nên chỉ có ý nghĩa kèm theo
 * khoảng thời gian đã dùng để tính ra nó.
 */
public class RoomAvailability {

    /** Dưới ngưỡng này thì hiển thị lời nhắc "sắp hết" để khách quyết định nhanh. */
    private static final int LOW_STOCK = 2;

    private String roomCode;
    private String roomName;
    private String description;
    private BigDecimal pricePerDay;
    private int totalRooms;
    private int busyRooms;

    /** Khoảng thời gian chưa xác định: chỉ biết tổng số phòng, chưa biết còn mấy phòng. */
    private boolean windowKnown;

    public RoomAvailability() {
    }

    public static RoomAvailability of(RoomType type, int busyRooms, boolean windowKnown) {
        RoomAvailability a = new RoomAvailability();
        a.roomCode = type.getRoomCode();
        a.roomName = type.getRoomName();
        a.description = type.getDescription();
        a.pricePerDay = type.getPricePerDay();
        a.totalRooms = type.getTotalRooms();
        a.busyRooms = busyRooms;
        a.windowKnown = windowKnown;
        return a;
    }

    /** Số phòng còn trống, không bao giờ âm dù dữ liệu có bất thường. */
    public int getFreeRooms() {
        return Math.max(0, totalRooms - busyRooms);
    }

    public boolean isSoldOut() {
        return windowKnown && getFreeRooms() <= 0;
    }

    public boolean isLowStock() {
        return windowKnown && getFreeRooms() > 0 && getFreeRooms() <= LOW_STOCK;
    }

    /** Đã biết khoảng ngày nên con số phòng trống là thật, không phải phỏng đoán. */
    public boolean isWindowKnown() {
        return windowKnown;
    }

    /** Chữ hiển thị trên thẻ chọn phòng. */
    public String getStatusLabel() {
        if (!windowKnown) {
            return "Tổng " + totalRooms + " phòng";
        }
        if (isSoldOut()) {
            return "Hết phòng";
        }
        return "Còn " + getFreeRooms() + "/" + totalRooms + " phòng";
    }

    /** Tên màu dùng cho nhãn tình trạng, khớp bảng màu chung của giao diện. */
    public String getStatusColor() {
        if (!windowKnown) return "blue";
        if (isSoldOut()) return "pink";
        if (isLowStock()) return "amber";
        return "teal";
    }

    public String getRoomCode() { return roomCode; }
    public void setRoomCode(String roomCode) { this.roomCode = roomCode; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPricePerDay() { return pricePerDay; }
    public void setPricePerDay(BigDecimal pricePerDay) { this.pricePerDay = pricePerDay; }

    public int getTotalRooms() { return totalRooms; }
    public void setTotalRooms(int totalRooms) { this.totalRooms = totalRooms; }

    public int getBusyRooms() { return busyRooms; }
    public void setBusyRooms(int busyRooms) { this.busyRooms = busyRooms; }
}
