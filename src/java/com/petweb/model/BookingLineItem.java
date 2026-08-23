package com.petweb.model;

import java.math.BigDecimal;

/**
 * Một hạng mục con của dòng SPA hoặc MEDICAL (ví dụ "Cắt tỉa lông", "Tiêm phòng dại").
 *
 * itemName và itemPrice là bản chụp tại thời điểm đặt lịch: khi trung tâm đổi bảng giá
 * về sau, hóa đơn cũ vẫn giữ nguyên số tiền khách đã chốt. Bảng spa_detail_item cũ
 * không lưu giá nên hóa đơn spa cũ bị đổi theo mỗi lần sửa bảng giá.
 *
 * itemId trỏ tới spa_service_item hoặc medical_service_item tùy serviceType của dòng cha,
 * nên không đặt được khóa ngoại; hai cột snapshot ở trên đã đủ để dòng này tự diễn giải.
 */
public class BookingLineItem {

    private int lineItemId;
    private int lineId;
    private int itemId;
    private String itemName;
    private BigDecimal itemPrice = BigDecimal.ZERO;

    public BookingLineItem() {
    }

    public BookingLineItem(int itemId, String itemName, BigDecimal itemPrice) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.itemPrice = itemPrice;
    }

    public int getLineItemId() { return lineItemId; }
    public void setLineItemId(int lineItemId) { this.lineItemId = lineItemId; }

    public int getLineId() { return lineId; }
    public void setLineId(int lineId) { this.lineId = lineId; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public BigDecimal getItemPrice() { return itemPrice; }
    public void setItemPrice(BigDecimal itemPrice) { this.itemPrice = itemPrice; }
}
