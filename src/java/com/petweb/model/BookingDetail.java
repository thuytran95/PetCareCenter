/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.petweb.model;

import java.math.BigDecimal;

public class BookingDetail {
    private int bookingDetailId;
    private int bookingId;
    private int petId;
    private int serviceId;
    private BigDecimal totalPrice;

    public BookingDetail() {}

    public BookingDetail(int bookingDetailId, int bookingId, int petId, int serviceId, BigDecimal totalPrice) {
        this.bookingDetailId = bookingDetailId;
        this.bookingId = bookingId;
        this.petId = petId;
        this.serviceId = serviceId;
        this.totalPrice = totalPrice;
    }

    public int getBookingDetailId() { return bookingDetailId; }
    public void setBookingDetailId(int bookingDetailId) { this.bookingDetailId = bookingDetailId; }

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public int getPetId() { return petId; }
    public void setPetId(int petId) { this.petId = petId; }

    public int getServiceId() { return serviceId; }
    public void setServiceId(int serviceId) { this.serviceId = serviceId; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    /** Nhận tổng từ các service con (Spa, Medical, Hotel) */
    public void calculateTotal(ServiceInfo service) {
        if (service != null) {
            this.totalPrice = service.getTotalPrice();
        }
    }
}
