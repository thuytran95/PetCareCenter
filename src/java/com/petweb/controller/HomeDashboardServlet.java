package com.petweb.controller;

import com.petweb.dao.BookingDAO;
import com.petweb.dao.HealthRecordDAO;
import com.petweb.dao.PetDAO;
import com.petweb.model.Appointment;
import com.petweb.model.Booking;
import com.petweb.model.HealthRecord;
import com.petweb.model.Pet;
import com.petweb.model.PetStay;
import com.petweb.model.UserAccount;
import com.petweb.utils.MyUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * API bảng tin trang chủ: trả JSON cho khách đã đăng nhập.
 * Trang chủ gọi endpoint này khi load và sau khi đăng nhập thành công.
 */
@WebServlet("/api/homeDashboard")
public class HomeDashboardServlet extends HttpServlet {

    private static final int MAX_UPCOMING = 4;
    private static final int MAX_RECENT_INVOICES = 3;
    private static final int HEALTH_DUE_WITHIN_DAYS = 30;
    private static final int MAX_HEALTH_DUE = 4;

    private static final Logger LOGGER = Logger.getLogger(HomeDashboardServlet.class.getName());
    private static final String[] PET_COLORS = {"blue", "pink", "amber", "teal"};

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");

        UserAccount user = MyUtils.getLoginedUser(request.getSession());
        if (user == null) {
            write(response, "{\"loggedIn\":false}");
            return;
        }

        Connection conn = MyUtils.getStoredConnection(request);
        if (conn == null) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            write(response, "{\"loggedIn\":true,\"error\":\"Không có kết nối CSDL.\"}");
            return;
        }

        try {
            List<Pet> pets = PetDAO.findByOwner(conn, user.getId());
            Map<Integer, List<PetStay>> stays = BookingDAO.groupStaysByPet(
                    BookingDAO.findCurrentStaysByOwner(conn, user.getId()));
            List<Appointment> upcoming =
                    BookingDAO.findUpcomingAppointments(conn, user.getId(), MAX_UPCOMING);
            List<Booking> recentBookings =
                    BookingDAO.findRecentByUser(conn, user.getId(), MAX_RECENT_INVOICES);
            List<HealthRecord> healthDue = HealthRecordDAO.findUpcomingDueByOwner(
                    conn, user.getId(), HEALTH_DUE_WITHIN_DAYS, MAX_HEALTH_DUE);

            write(response, toJson(pets, stays, upcoming, recentBookings, healthDue));
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING,
                    "Không tải được bảng tin trang chủ cho userId=" + user.getId(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            write(response, "{\"loggedIn\":true,\"error\":\"Không tải được dữ liệu trang chủ.\"}");
        }
    }

    private static void write(HttpServletResponse response, String json) throws IOException {
        try (PrintWriter out = response.getWriter()) {
            out.print(json);
        }
    }

    private static String toJson(List<Pet> pets, Map<Integer, List<PetStay>> stays,
                                 List<Appointment> upcoming, List<Booking> recentBookings,
                                 List<HealthRecord> healthDue) {
        if (pets == null) pets = Collections.emptyList();
        if (stays == null) stays = Collections.emptyMap();
        if (upcoming == null) upcoming = Collections.emptyList();
        if (recentBookings == null) recentBookings = Collections.emptyList();
        if (healthDue == null) healthDue = Collections.emptyList();

        StringBuilder sb = new StringBuilder(4096);
        sb.append("{\"loggedIn\":true,\"pets\":[");
        for (int i = 0; i < pets.size(); i++) {
            if (i > 0) sb.append(',');
            appendPet(sb, pets.get(i), stays, i);
        }
        sb.append("],\"healthDue\":[");
        for (int i = 0; i < healthDue.size(); i++) {
            if (i > 0) sb.append(',');
            appendHealthDue(sb, healthDue.get(i));
        }
        sb.append("],\"upcoming\":[");
        for (int i = 0; i < upcoming.size(); i++) {
            if (i > 0) sb.append(',');
            appendAppointment(sb, upcoming.get(i));
        }
        sb.append("],\"recentBookings\":[");
        for (int i = 0; i < recentBookings.size(); i++) {
            if (i > 0) sb.append(',');
            appendBooking(sb, recentBookings.get(i));
        }
        return sb.append("]}").toString();
    }

    private static void appendPet(StringBuilder sb, Pet pet, Map<Integer, List<PetStay>> stays, int index) {
        List<PetStay> petStays = stays.get(pet.getPetId());
        PetStay stay = (petStays == null || petStays.isEmpty()) ? null : petStays.get(0);
        int moreStays = (petStays == null) ? 0 : Math.max(0, petStays.size() - 1);
        String color = PET_COLORS[index % PET_COLORS.length];
        byte[] photo = pet.getPhoto();

        sb.append('{')
          .append("\"petId\":").append(pet.getPetId()).append(',')
          .append("\"name\":\"").append(escape(pet.getName())).append("\",")
          .append("\"species\":\"").append(escape(pet.getSpecies())).append("\",")
          .append("\"color\":\"").append(color).append("\",")
          .append("\"photoBase64\":");
        if (photo == null || photo.length == 0) {
            sb.append("null");
        } else {
            sb.append('"').append(escape(Base64.getEncoder().encodeToString(photo))).append('"');
        }
        sb.append(",\"stay\":");
        if (stay == null) {
            sb.append("null");
        } else {
            sb.append('{')
              .append("\"bookingId\":").append(stay.getBookingId()).append(',')
              .append("\"stateColor\":\"").append(escape(stay.getStateColor())).append("\",")
              .append("\"stateText\":\"").append(escape(stay.getStateText())).append("\",")
              .append("\"roomName\":\"").append(escape(stay.getRoomName())).append("\",")
              .append("\"formattedRange\":\"").append(escape(stay.getFormattedRange())).append("\",")
              .append("\"moreStays\":").append(moreStays).append(',')
              .append("\"checkOutable\":").append(stay.isCheckOutable()).append(',')
              .append("\"cancellable\":").append(stay.isCancellable()).append(',')
              .append("\"draft\":").append(stay.isDraft())
              .append('}');
        }
        sb.append('}');
    }

    private static void appendHealthDue(StringBuilder sb, HealthRecord hr) {
        sb.append('{')
          .append("\"petId\":").append(hr.getPetId()).append(',')
          .append("\"petName\":\"").append(escape(hr.getPetName())).append("\",")
          .append("\"itemName\":\"").append(escape(hr.getItemName())).append("\",")
          .append("\"formattedNextDueAt\":\"").append(escape(hr.getFormattedNextDueAt())).append("\",")
          .append("\"dueText\":\"").append(escape(hr.getDueText())).append("\",")
          .append("\"colorName\":\"").append(escape(hr.getColorName())).append("\",")
          .append("\"iconClass\":\"").append(escape(hr.getIconClass())).append("\",")
          .append("\"overdue\":").append(hr.isOverdue())
          .append('}');
    }

    private static void appendAppointment(StringBuilder sb, Appointment ap) {
        sb.append('{')
          .append("\"bookingId\":").append(ap.getBookingId()).append(',')
          .append("\"serviceLabel\":\"").append(escape(ap.getServiceLabel())).append("\",")
          .append("\"petName\":\"").append(escape(ap.getPetName())).append("\",")
          .append("\"formattedStartAt\":\"").append(escape(ap.getFormattedStartAt())).append("\",")
          .append("\"roomLabel\":\"").append(escape(ap.getRoomLabel())).append("\",")
          .append("\"reminderText\":\"").append(escape(ap.getReminderText())).append("\",")
          .append("\"colorName\":\"").append(escape(ap.getColorName())).append("\",")
          .append("\"iconClass\":\"").append(escape(ap.getIconClass())).append("\",")
          .append("\"urgent\":").append(ap.isUrgent())
          .append('}');
    }

    private static void appendBooking(StringBuilder sb, Booking bk) {
        String amount = bk.getTotalPrice() == null ? "0"
                : String.format("%,.0f", bk.getTotalPrice());
        sb.append('{')
          .append("\"bookingId\":").append(bk.getBookingId()).append(',')
          .append("\"petName\":\"").append(escape(bk.getPetName())).append("\",")
          .append("\"formattedCreatedAt\":\"").append(escape(bk.getFormattedCreatedAt())).append("\",")
          .append("\"amount\":\"").append(escape(amount)).append("\",")
          .append("\"status\":\"").append(escape(bk.getStatus())).append('"')
          .append('}');
    }

    private static String escape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"'  -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        return sb.toString();
    }
}
