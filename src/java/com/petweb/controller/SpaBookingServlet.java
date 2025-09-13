package com.petweb.controller;

import com.petweb.model.SpaDetail;
import com.petweb.model.SpaServiceItem;
import com.petweb.utils.DBUtils;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@WebServlet("/SpaBookingServlet")
public class SpaBookingServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        try {
            // Lấy tất cả dịch vụ Spa từ DB
            List<SpaServiceItem> allItems = DBUtils.getSpaAllItems();
            request.setAttribute("allItems", allItems);

            // Forward sang trang đặt dịch vụ spa
            request.getRequestDispatcher("bookSpa.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("<h3>Lỗi: " + e.getMessage() + "</h3>");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        try {
            // Lấy ngày đặt lịch
            String bookingDateStr = request.getParameter("bookingDate");
            Timestamp bookingDate = null;
            if (bookingDateStr != null && !bookingDateStr.isEmpty()) {
                bookingDate = Timestamp.valueOf(bookingDateStr.replace("T", " ") + ":00");
            } else {
                response.getWriter().println("<h3>Lỗi: bookingDate không hợp lệ!</h3>");
                return;
            }

            // Lấy serviceId từ session
            Integer serviceId = (Integer) request.getSession().getAttribute("serviceId");
            if (serviceId == null) {
                response.getWriter().println("<h3>Lỗi: Chưa có serviceId trong session!</h3>");
                return;
            }

            // Lấy các item mà user chọn
            String[] itemIds = request.getParameterValues("itemIds");
            if (itemIds == null || itemIds.length == 0) {
                response.getWriter().println("<h3>Lỗi: Chưa chọn dịch vụ nào!</h3>");
                return;
            }

            // Tạo đối tượng SpaDetail
            SpaDetail spa = new SpaDetail();
            spa.setBookingDate(bookingDate);

            // Lưu item đã chọn vào DB + tính tổng giá
            BigDecimal totalPrice = DBUtils.addSpaItemsAndCalculatePrice(
                    0, serviceId, itemIds, bookingDate
            );

            // Lấy lại danh sách item đã chọn
            List<SpaServiceItem> selectedItems = DBUtils.getSpaItemsByIds(itemIds);

            // Set attribute để hiển thị kết quả
            request.setAttribute("spa", spa);
            request.setAttribute("selectedItems", selectedItems);
            request.setAttribute("totalPrice", totalPrice);

            // Forward sang trang kết quả
            request.getRequestDispatcher("bookSpaResult.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("<h3>Lỗi: " + e.getMessage() + "</h3>");
        }
    }
}
