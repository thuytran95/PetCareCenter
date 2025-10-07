package com.petweb.controller;

import com.petweb.model.HotelDetail;
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
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

@WebServlet("/HotelServletGuest")
public class HotelServletGuest extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        try (Connection conn = ConnectionUtils.getConnection()) {
            // 🔹 Lấy serviceId từ session (cho guest)
            Integer serviceIdObj = (Integer) request.getSession().getAttribute("guestServiceId");
            if (serviceIdObj == null) {
                response.getWriter().println("<h3 style='color:red'>Không tìm thấy guestServiceId trong session!</h3>");
                return;
            }

            int serviceId = serviceIdObj;
            String roomType = request.getParameter("roomType");
            String checkInStr = request.getParameter("checkIn");
            String checkOutStr = request.getParameter("checkOut");
            String priceStr = request.getParameter("price");

            if (roomType == null || checkInStr == null || checkOutStr == null ||
                roomType.isEmpty() || checkInStr.isEmpty() || checkOutStr.isEmpty()) {
                response.getWriter().println("<h3 style='color:red'>Thiếu dữ liệu trong form!</h3>");
                return;
            }

            double price = (priceStr == null || priceStr.isEmpty()) ? 0 : Double.parseDouble(priceStr);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
            Timestamp checkIn = new Timestamp(sdf.parse(checkInStr).getTime());
            Timestamp checkOut = new Timestamp(sdf.parse(checkOutStr).getTime());

            HotelDetail booking = new HotelDetail();
            booking.setRoomType(roomType);
            booking.setCheckIn(checkIn);
            booking.setCheckOut(checkOut);

            long soNgay = booking.getSoNgayO();
            BigDecimal tongTien = booking.tinhTongTien();

            boolean success = DBUtils.bookRoom(conn, serviceId, roomType, checkIn, checkOut, tongTien);

            request.setAttribute("booking", booking);
            request.setAttribute("soNgay", soNgay);
            request.setAttribute("tongTien", tongTien);

            request.getRequestDispatcher("bookingResultGuest.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("<h3 style='color:red'>Có lỗi xảy ra: " + e.getMessage() + "</h3>");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.sendRedirect("HotelDetailGuest.jsp");
    }
}
