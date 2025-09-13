/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.petweb.model;

import java.math.BigDecimal;
import java.util.List;

public class ServiceInfo {
    private int serviceId;
    private String serviceName;
    private String description;
    private BigDecimal totalPrice;

    public ServiceInfo() {}

    public ServiceInfo(int serviceId, String serviceName, String description, BigDecimal totalPrice) {
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.description = description;
        this.totalPrice = totalPrice;
    }

    public int getServiceId() { return serviceId; }
    public void setServiceId(int serviceId) { this.serviceId = serviceId; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    /** Tính tổng tiền từ Spa + Medical + Hotel */
    public void calculateTotal(SpaDetail spa, MedicalDetail medical, HotelDetail hotel) {
        BigDecimal total = BigDecimal.ZERO;

        if (spa != null && spa.getSpaPrice() != null) total = total.add(spa.getSpaPrice());
        if (medical != null && medical.getMedicalPrice() != null) total = total.add(medical.getMedicalPrice());
        if (hotel != null && hotel.tinhTongTien() != null) total = total.add(hotel.tinhTongTien());

        this.totalPrice = total;
    }

    /** Hoặc tính từ danh sách nhiều service nhỏ */
    public void calculateFromList(List<BigDecimal> prices) {
        BigDecimal total = BigDecimal.ZERO;
        if (prices != null) {
            for (BigDecimal p : prices) {
                if (p != null) total = total.add(p);
            }
        }
        this.totalPrice = total;
    }
}

