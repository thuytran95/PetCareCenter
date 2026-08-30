package com.petweb.controller;

import com.petweb.dao.BookingDAO;
import com.petweb.dao.PetDAO;
import com.petweb.model.Booking;
import com.petweb.model.BookingLine;
import com.petweb.model.Pet;
import com.petweb.model.UserAccount;
import com.petweb.utils.MyUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Danh sách TẤT CẢ đơn đặt lịch của khách đang đăng nhập.
 *
 * Trang chủ chỉ hiện vài hóa đơn gần đây và thẻ thú cưng chỉ hiện đợt lưu trú
 * sắp tới nhất, nên trước đây không có chỗ nào xem được toàn bộ. Đây là chỗ đó.
 *
 * Lọc được theo bé ({@code petId}) và theo nhóm ({@code filter}):
 * đang hiệu lực / đã xong / tất cả; thêm {@code service=hotel} để chỉ xem đơn
 * có đặt phòng.
 */
@WebServlet("/myBookings")
public class MyBookingsServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(MyBookingsServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        UserAccount user = MyUtils.getLoginedUser(request.getSession());
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Connection conn = BookingServlet.requireConnection(request);
        Integer petId = parsePetId(request.getParameter("petId"));
        String filter = normalizeFilter(request.getParameter("filter"));
        boolean hotelOnly = "hotel".equals(request.getParameter("service"));

        try {
            // Chỉ lấy đơn của bé thuộc về chính khách này — đổi petId trên URL
            // không xem được đơn của người khác.
            Pet pet = null;
            if (petId != null) {
                pet = PetDAO.findByIdAndOwner(conn, petId, user.getId());
                if (pet == null) {
                    request.getSession().setAttribute("error",
                            "Không tìm thấy thú cưng này trong hồ sơ của bạn.");
                    response.sendRedirect(request.getContextPath() + "/petProfile");
                    return;
                }
            }

            List<Booking> all = BookingDAO.findHistory(conn, user.getId(), petId);
            List<Booking> shown = new ArrayList<>();
            int active = 0, finished = 0, hotelCount = 0;

            for (Booking b : all) {
                boolean isActive = b.isConfirmed() || b.isPaid();
                if (isActive) active++; else finished++;
                if (hasHotel(b)) hotelCount++;

                if (hotelOnly && !hasHotel(b)) continue;
                if ("active".equals(filter) && !isActive) continue;
                if ("done".equals(filter) && isActive) continue;
                shown.add(b);
            }

            request.setAttribute("pet", pet);
            request.setAttribute("bookings", shown);
            request.setAttribute("filter", filter);
            request.setAttribute("hotelOnly", hotelOnly);
            request.setAttribute("countAll", all.size());
            request.setAttribute("countActive", active);
            request.setAttribute("countDone", finished);
            request.setAttribute("countHotel", hotelCount);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi tải danh sách đơn của userId=" + user.getId(), e);
            request.setAttribute("loadError", "Không tải được danh sách đơn.");
        }

        request.getRequestDispatcher("/myBookings.jsp").forward(request, response);
    }

    private static boolean hasHotel(Booking b) {
        for (BookingLine l : b.getLines()) {
            if (l.isHotel()) return true;
        }
        return false;
    }

    /** Giá trị lạ coi như không lọc, không phải lỗi. */
    private static String normalizeFilter(String raw) {
        if ("active".equals(raw) || "done".equals(raw)) return raw;
        return "all";
    }

    private static Integer parsePetId(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return Integer.valueOf(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
