/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.petweb.controller;

import com.petweb.model.BookingInfo;
import com.petweb.utils.ConnectionUtils;
import com.petweb.utils.DBUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;

/**
 *
 * @author Duyet
 */
@WebServlet("/invoiceGuest")
public class InvoiceServletGuest extends HttpServlet{
     @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idStr = request.getParameter("bookingId");
        if (idStr == null || idStr.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu bookingId");
            return;
        }

        try (Connection conn = ConnectionUtils.getConnection()) {
            int bookingId = Integer.parseInt(idStr);

            BookingInfo booking = DBUtils.getBookingInfoById(conn, bookingId);

            if (booking == null) {
                request.setAttribute("errorMessage", "Không tìm thấy thông tin booking!");
            } else {
                BigDecimal total = DBUtils.calculateTotalFromServices(conn, bookingId);
                booking.setTotalPrice(total);
                request.setAttribute("booking", booking);
            }

            request.getRequestDispatcher("invoiceGuest.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "bookingId không hợp lệ");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
