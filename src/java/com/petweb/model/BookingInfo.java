/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.petweb.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

public class BookingInfo {
    private int bookingId;
    private int userId;
    private Timestamp bookingDate;
    private String status;
    private BigDecimal totalPrice;

    public BookingInfo() {}

    public BookingInfo(int bookingId, int userId, Timestamp bookingDate, String status, BigDecimal totalPrice) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.bookingDate = bookingDate;
        this.status = status;
        this.totalPrice = totalPrice;
    }

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public Timestamp getBookingDate() { return bookingDate; }
    public void setBookingDate(Timestamp bookingDate) { this.bookingDate = bookingDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    /** Tính tổng tiền từ nhiều BookingDetail */
    public void calculateTotal(List<BookingDetail> details) {
        BigDecimal total = BigDecimal.ZERO;
        if (details != null) {
            for (BookingDetail d : details) {
                if (d.getTotalPrice() != null) {
                    total = total.add(d.getTotalPrice());
                }
            }
        }
        this.totalPrice = total;
    }
}

