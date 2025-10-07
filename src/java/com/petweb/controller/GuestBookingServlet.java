package com.petweb.controller;

import com.petweb.model.Pet;
import com.petweb.model.UserAccount;
import com.petweb.utils.ConnectionUtils;
import com.petweb.utils.DBUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.Connection;

@WebServlet("/GuestBookingServlet")
public class GuestBookingServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        HttpSession session = request.getSession();

        try (Connection conn = ConnectionUtils.getConnection()) {

            // ====================== 1️⃣ KHỞI TẠO GUEST + PET ======================
            if ("add".equals(action)) {

                // --- Lấy thông tin khách ---
                String guestName = request.getParameter("guestName");
                String guestPhone = request.getParameter("guestPhone");
                String guestEmail = request.getParameter("guestEmail");

                // --- Tạo user tạm trong DB ---
                UserAccount guest = DBUtils.createGuestUser(conn, guestName, guestPhone, guestEmail);
                int tempUserId = guest.getId();

                // --- Lấy thông tin thú cưng ---
                String newPetName = request.getParameter("newPetName");
                String newPetType = request.getParameter("newPetType");
                String newPetGender = request.getParameter("newPetGender");

                // --- Gán mặc định nếu để trống ---
                if (newPetName == null || newPetName.trim().isEmpty()) newPetName = "Guest Pet";
                if (newPetType == null || newPetType.trim().isEmpty()) newPetType = "Unknown";
                if (newPetGender == null || newPetGender.trim().isEmpty()) newPetGender = "M"; // ⚠ Sửa từ U → M cho hợp constraint

                // --- Tạo bản ghi pet ---
                Pet pet = new Pet();
                pet.setName(newPetName);
                pet.setSpecies(newPetType);
                pet.setGender(newPetGender);
                pet.setUserId(tempUserId);

                Pet insertedPet = DBUtils.insertPet(conn, pet);
                int petId = insertedPet.getPetId();

                // --- Lưu session ---
                session.setAttribute("tempUserId", tempUserId);
                session.setAttribute("petId", petId);

                // 👉 Chuyển đến trang chọn dịch vụ
                response.sendRedirect(request.getContextPath() + "/chooseServiceGuest.jsp?petId=" + petId);
                return;
            }

            // ====================== 2️⃣ THÊM DỊCH VỤ CHO PET ======================
            if ("service".equals(action)) {
                Integer tempUserId = (Integer) session.getAttribute("tempUserId");
                Integer petId = (Integer) session.getAttribute("petId");

                if (tempUserId == null || petId == null) {
                    response.getWriter().println("<h3>Lỗi: Chưa có thông tin khách hoặc pet trong session!</h3>");
                    return;
                }

                String serviceType = request.getParameter("serviceType");

                // --- Kiểm tra và tạo booking + service ---
                Integer bookingId = (Integer) session.getAttribute("guestBookingId");
                Integer serviceId = (Integer) session.getAttribute("guestServiceId");

                if (bookingId == null || serviceId == null) {
                    bookingId = DBUtils.createBookingInfo(conn, tempUserId, petId);
                    serviceId = DBUtils.createServiceInfo(conn, bookingId, petId);

                    session.setAttribute("guestBookingId", bookingId);
                    session.setAttribute("guestServiceId", serviceId);
                }

                DBUtils.addBookingDetail(conn, serviceId, serviceType, petId);

                // 👉 Điều hướng theo loại dịch vụ
                if ("hotel".equalsIgnoreCase(serviceType)) {
                    response.sendRedirect(request.getContextPath() + "/HotelServletGuest");
                } else if ("spa".equalsIgnoreCase(serviceType)) {
                    response.sendRedirect(request.getContextPath() + "/SpaBookingServletGuest");
                } else if ("medical".equalsIgnoreCase(serviceType)) {
                    response.sendRedirect(request.getContextPath() + "/MedicalBookingServletGuest");
                } else {
                    response.getWriter().println("<h3>Lỗi: Loại dịch vụ không hợp lệ!</h3>");
                }
                return;
            }

            // ====================== 3️⃣ KẾT THÚC BOOKING ======================
            if ("finish".equals(action)) {
                Integer bookingId = (Integer) session.getAttribute("guestBookingId");

                if (bookingId != null) {
                    DBUtils.confirmBooking(conn, bookingId);
                    session.removeAttribute("guestServiceId");
                    session.removeAttribute("guestBookingId");

                    response.sendRedirect(request.getContextPath() + "/invoiceGuest?bookingId=" + bookingId);
                } else {
                    request.getRequestDispatcher("/booking.jsp").forward(request, response);
                }
                return;
            }

            // ====================== 4️⃣ HÀNH ĐỘNG KHÔNG HỢP LỆ ======================
            response.getWriter().println("<h3>Lỗi: Hành động không xác định!</h3>");

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("<h3>Lỗi hệ thống: " + e.getMessage() + "</h3>");
        }
    }
}
