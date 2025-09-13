package com.petweb.controller;

import java.io.IOException;
import java.sql.Connection;

import com.petweb.utils.ConnectionUtils;
import com.petweb.utils.DBUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 *
 * @author Duyet
 */
@WebServlet("/BookingServlet")
public class BookingServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Hiển thị giao diện chọn dịch vụ
        request.getRequestDispatcher("chooseService.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("userId");

        if (userId == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        try (Connection conn = ConnectionUtils.getConnection()) {
            if ("add".equals(action)) {
                int petId = Integer.parseInt(request.getParameter("petId"));
                String serviceType = request.getParameter("serviceType");

                // Kiểm tra bookingId và serviceId hiện tại trong session
                Integer bookingId = (Integer) session.getAttribute("currentBookingId");
                Integer serviceId = (Integer) session.getAttribute("serviceId");
                
                if (bookingId == null || serviceId == null) {
                    // Tạo booking mới
                    bookingId = DBUtils.createBookingInfo(conn, userId, petId);

                    // Tạo service mới
                    serviceId = DBUtils.createServiceInfo(conn, bookingId, petId);

                    // Lưu vào session
                    session.setAttribute("currentBookingId", bookingId);
                    session.setAttribute("serviceId", serviceId);
                    session.setAttribute("petId", petId);
                }

                // Thêm dịch vụ chi tiết
                DBUtils.addBookingDetail(conn, serviceId, serviceType, petId);

                // 👉 Điều hướng theo loại dịch vụ
                if ("hotel".equalsIgnoreCase(serviceType)) {
                    response.sendRedirect(request.getContextPath() + "/HotelServlet");
                } else if ("spa".equalsIgnoreCase(serviceType)) {
                    response.sendRedirect(request.getContextPath() + "/SpaBookingServlet");
                } else if ("medical".equalsIgnoreCase(serviceType)) {
                    response.sendRedirect(request.getContextPath() + "/MedicalBookingServlet");
                } else {
                    response.getWriter().println("<h3>Lỗi: Chọn dịch vụ hợp lệ!</h3>");
                }

            } else if ("finish".equals(action)) {
                Integer bookingId = (Integer) session.getAttribute("currentBookingId");

                if (bookingId != null) {
                    DBUtils.confirmBooking(conn, bookingId);

                    session.removeAttribute("serviceId");
                    session.removeAttribute("currentBookingId");

                    // ✅ redirect về servlet InvoiceServlet
                    response.sendRedirect(request.getContextPath() + "/invoice?bookingId=" + bookingId);
                } else {
                    response.getWriter().println("<h3>Lỗi: Chưa có booking trong session!</h3>");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("<h3>Lỗi: " + e.getMessage() + "</h3>");
        }
    }
}
