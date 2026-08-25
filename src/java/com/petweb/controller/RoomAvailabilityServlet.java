package com.petweb.controller;

import com.petweb.dao.ServiceCatalogDAO;
import com.petweb.model.RoomAvailability;
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
import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Trả về tình trạng phòng trống dưới dạng JSON để trang đặt phòng cập nhật
 * ngay khi khách đổi ngày, không phải bấm "Đặt phòng" rồi mới biết hết chỗ.
 *
 * Chỉ đọc, không thay đổi gì, nên gọi bao nhiêu lần cũng an toàn. Số liệu luôn
 * tính lại từ CSDL chứ không nhớ đệm, vì phòng có thể bị người khác đặt mất
 * ngay trong lúc khách đang chọn ngày.
 */
@WebServlet("/roomAvailability")
public class RoomAvailabilityServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(RoomAvailabilityServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        // Số phòng trống thay đổi liên tục, không được để trình duyệt dùng bản cũ
        response.setHeader("Cache-Control", "no-store");

        Connection conn = MyUtils.getStoredConnection(request);
        Timestamp checkIn = parseOrNull(request.getParameter("checkIn"));
        Timestamp checkOut = parseOrNull(request.getParameter("checkOut"));

        // Bỏ qua chính đơn đang mở: dòng khách đã thêm không nên tự chặn khách
        Integer currentBooking = (Integer) request.getSession()
                .getAttribute(BookingServlet.SESSION_BOOKING_ID);

        try {
            List<RoomAvailability> rooms =
                    ServiceCatalogDAO.findAvailability(conn, checkIn, checkOut, currentBooking);
            try (PrintWriter out = response.getWriter()) {
                out.print(toJson(rooms));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi tra tình trạng phòng trống", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = response.getWriter()) {
                out.print("{\"error\":\"Không tra được tình trạng phòng.\"}");
            }
        }
    }

    /**
     * Dựng JSON bằng tay — dự án không có thư viện JSON nào và cấu trúc ở đây
     * chỉ vài trường cố định, thêm hẳn một thư viện là không đáng.
     */
    private static String toJson(List<RoomAvailability> rooms) {
        StringBuilder sb = new StringBuilder("{\"rooms\":[");
        for (int i = 0; i < rooms.size(); i++) {
            RoomAvailability r = rooms.get(i);
            if (i > 0) sb.append(',');
            sb.append('{')
              .append("\"roomCode\":\"").append(escape(r.getRoomCode())).append("\",")
              .append("\"totalRooms\":").append(r.getTotalRooms()).append(',')
              .append("\"freeRooms\":").append(r.getFreeRooms()).append(',')
              .append("\"soldOut\":").append(r.isSoldOut()).append(',')
              .append("\"lowStock\":").append(r.isLowStock()).append(',')
              .append("\"windowKnown\":").append(r.isWindowKnown()).append(',')
              .append("\"statusLabel\":\"").append(escape(r.getStatusLabel())).append("\",")
              .append("\"statusColor\":\"").append(escape(r.getStatusColor())).append('"')
              .append('}');
        }
        return sb.append("]}").toString();
    }

    /** Thoát các ký tự làm hỏng chuỗi JSON. */
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

    /** Giá trị từ input datetime-local; thiếu hoặc sai định dạng thì coi như chưa chọn. */
    private static Timestamp parseOrNull(String raw) {
        try {
            return HotelServlet.parseDateTime(raw, "");
        } catch (Exception e) {
            return null;
        }
    }
}
