package com.petweb.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SpaDetail {
    private int spaId;
    private int serviceId;
    private BigDecimal spaPrice;
    private Timestamp bookingDate;
    private String status;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public SpaDetail() {}
    
    public SpaDetail(int spaId, int serviceId, BigDecimal spaPrice, Timestamp bookingDate, String status) {
        this.spaId = spaId;
        this.serviceId = serviceId;
        this.spaPrice = spaPrice;
        this.bookingDate = bookingDate;
        this.status = status;
    }
    
    public int getSpaId() { return spaId; }
    public void setSpaId(int spaId) { this.spaId = spaId; }

    public int getServiceId() { return serviceId; }
    public void setServiceId(int serviceId) { this.serviceId = serviceId; }

    public BigDecimal getSpaPrice() { return spaPrice; }
    public void setSpaPrice(BigDecimal spaPrice) { this.spaPrice = spaPrice; }

    public Timestamp getBookingDate() { return bookingDate; }
    public void setBookingDate(Timestamp bookingDate) { this.bookingDate = bookingDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getFormattedbookingDate() {
        if (bookingDate == null) return "";
        return bookingDate.toLocalDateTime().format(FORMATTER);
    }

    // Tính tổng giá từ các item
    public void calculatePrice(List<SpaServiceItem> items) {
        BigDecimal total = BigDecimal.ZERO;
        if(items != null) {
            for(SpaServiceItem item : items) {
                total = total.add(item.getItemPrice());
            }
        }
        this.spaPrice = total;
    }
}
