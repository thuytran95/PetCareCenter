// MedicalDetail.java
package com.petweb.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MedicalDetail {
    private int medicalId;
    private int serviceId;
    private BigDecimal medicalPrice;
    private Timestamp admissionDate;
    private String status;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public MedicalDetail() {}

    public MedicalDetail(int medicalId, int serviceId, BigDecimal medicalPrice, Timestamp admissionDate, String status) {
        this.medicalId = medicalId;
        this.serviceId = serviceId;
        this.medicalPrice = medicalPrice;
        this.admissionDate = admissionDate;
        this.status = status;
    }

    public int getMedicalId() { return medicalId; }
    public void setMedicalId(int medicalId) { this.medicalId = medicalId; }

    public int getServiceId() { return serviceId; }
    public void setServiceId(int serviceId) { this.serviceId = serviceId; }

    public BigDecimal getMedicalPrice() { return medicalPrice; }
    public void setMedicalPrice(BigDecimal medicalPrice) { this.medicalPrice = medicalPrice; }

    public Timestamp getAdmissionDate() { return admissionDate; }
    public void setAdmissionDate(Timestamp admissionDate) { this.admissionDate = admissionDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getFormattedAdmissionDate() {
        if (admissionDate == null) return "";
        return admissionDate.toLocalDateTime().format(FORMATTER);
    }

    public void calculatePrice(List<MedicalServiceItem> items) {
        BigDecimal total = BigDecimal.ZERO;
        if(items != null) {
            for(MedicalServiceItem item : items) {
                total = total.add(item.getItemPrice());
            }
        }
        this.medicalPrice = total;
    }
}
