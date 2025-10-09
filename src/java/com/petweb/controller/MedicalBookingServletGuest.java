/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.petweb.controller;

import com.petweb.model.MedicalDetail;
import com.petweb.model.MedicalServiceItem;
import com.petweb.utils.DBUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Duyet
 */
@WebServlet("/MedicalBookingServletGuest")
public class MedicalBookingServletGuest extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        try {
            // Lấy tất cả dịch vụ y tế để hiển thị cho user chọn
            List<MedicalServiceItem> allItems = DBUtils.getAllMedicalItems();
            request.setAttribute("allItems", allItems);
            request.setAttribute("medicalId", 1);

            // Forward sang JSP form booking
            request.getRequestDispatcher("bookMedicalGuest.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("<h3>Lỗi: " + e.getMessage() + "</h3>");
        }
    }

    @Override
protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
    try {
        // Lấy ngày nhập viện
        String admissionDateStr = request.getParameter("admissionDate");
        Timestamp admissionDate = null;
        if (admissionDateStr != null && !admissionDateStr.isEmpty()) {
            admissionDate = Timestamp.valueOf(admissionDateStr.replace("T", " ") + ":00");
        } else {
            response.getWriter().println("<h3>Lỗi: admissionDate không hợp lệ!</h3>");
            return;
        }
        Integer serviceId = (Integer) request.getSession().getAttribute("guestServiceId");
            if (serviceId == null) {
                response.getWriter().println("<h3>Lỗi: Chưa có serviceId trong session!</h3>");
                return;
            }
            request.setAttribute("serviceId", serviceId);
        // Lấy danh sách item user chọn
        String[] itemIds = request.getParameterValues("itemIds");
        if (itemIds == null || itemIds.length == 0) {
            response.getWriter().println("<h3>Lỗi: Chưa chọn dịch vụ nào!</h3>");
            return;
        }
        
        // Gọi hàm DBUtils để insert xuống DB
        int medicalId = 0; // hoặc lấy từ request nếu update
        BigDecimal totalPrice = DBUtils.addMedicalItemsAndCalculatePrice(medicalId,serviceId, itemIds, admissionDate);

        // Lấy lại MedicalDetail để hiển thị
        MedicalDetail medical = new MedicalDetail();
        medical.setAdmissionDate(admissionDate);
        medical.setMedicalPrice(totalPrice);

        // Lấy lại danh sách item đã chọn
        List<MedicalServiceItem> selectedItems = new ArrayList<>();
        for (String idStr : itemIds) {
            int id = Integer.parseInt(idStr);
            MedicalServiceItem item = DBUtils.getMedicalItemById(id);
            if (item != null) {
                selectedItems.add(item);
            }
        }

        request.setAttribute("medical", medical);
        request.setAttribute("selectedItems", selectedItems);
        request.setAttribute("totalPrice", totalPrice);

        request.getRequestDispatcher("bookMedicalResultGuest.jsp").forward(request, response);

    } catch (Exception e) {
        e.printStackTrace();
        response.getWriter().println("<h3>Lỗi: " + e.getMessage() + "</h3>");
    }
    }
}
