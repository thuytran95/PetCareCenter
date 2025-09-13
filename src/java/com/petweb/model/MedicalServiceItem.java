
package com.petweb.model;

import java.math.BigDecimal;

public class MedicalServiceItem {
    private int itemId;
    private String itemName;
    private BigDecimal itemPrice;

    public MedicalServiceItem() {}

    public MedicalServiceItem(int itemId, String itemName, BigDecimal itemPrice) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.itemPrice = itemPrice;
    }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public BigDecimal getItemPrice() { return itemPrice; }
    public void setItemPrice(BigDecimal itemPrice) { this.itemPrice = itemPrice; }
}
