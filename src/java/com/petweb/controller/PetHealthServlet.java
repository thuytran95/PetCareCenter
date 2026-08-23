package com.petweb.controller;

import com.petweb.dao.HealthRecordDAO;
import com.petweb.dao.PetDAO;
import com.petweb.model.HealthCalendar;
import com.petweb.model.HealthRecord;
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
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sổ sức khỏe của một thú cưng: lịch sử tiêm phòng, tẩy giun, khám định kỳ
 * và những mục sắp tới hạn phải làm lại.
 *
 * Chỉ chủ nuôi mới xem được sổ của bé mình — kiểm tra qua PetDAO.findByIdAndOwner.
 */
@WebServlet("/petHealth")
public class PetHealthServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(PetHealthServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        UserAccount user = MyUtils.getLoginedUser(request.getSession());
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int petId;
        try {
            petId = Integer.parseInt(request.getParameter("petId"));
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/petProfile");
            return;
        }

        Connection conn = BookingServlet.requireConnection(request);
        try {
            Pet pet = PetDAO.findByIdAndOwner(conn, petId, user.getId());
            if (pet == null) {
                request.getSession().setAttribute("error",
                        "Không tìm thấy thú cưng này trong hồ sơ của bạn.");
                response.sendRedirect(request.getContextPath() + "/petProfile");
                return;
            }

            List<HealthRecord> all = HealthRecordDAO.findByPet(conn, petId);

            // Tách theo loại để trang hiển thị thành từng mục riêng
            List<HealthRecord> vaccines = new ArrayList<>();
            List<HealthRecord> checkups = new ArrayList<>();
            List<HealthRecord> others = new ArrayList<>();
            // Những mục sắp tới hạn / quá hạn, chỉ lấy lần gần nhất của mỗi hạng mục
            List<HealthRecord> due = new ArrayList<>();
            List<String> seen = new ArrayList<>();

            for (HealthRecord r : all) {
                switch (r.getRecordType()) {
                    case HealthRecord.TYPE_VACCINE -> vaccines.add(r);
                    case HealthRecord.TYPE_CHECKUP -> checkups.add(r);
                    case HealthRecord.TYPE_DEWORM -> vaccines.add(r); // tẩy giun xếp cùng nhóm phòng bệnh
                    default -> others.add(r);
                }
                // Danh sách đã sắp xếp mới nhất trước, nên lần đầu gặp một hạng mục
                // chính là lần thực hiện gần nhất của hạng mục đó.
                if (r.hasNextDue() && !seen.contains(r.getItemName())) {
                    seen.add(r.getItemName());
                    if (r.isOverdue() || r.isDueSoon()) {
                        due.add(r);
                    }
                }
            }

            request.setAttribute("calendar", buildCalendar(request.getParameter("ym"), all));

            request.setAttribute("pet", pet);
            request.setAttribute("records", all);
            request.setAttribute("vaccines", vaccines);
            request.setAttribute("checkups", checkups);
            request.setAttribute("others", others);
            request.setAttribute("dueRecords", due);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi tải sổ sức khỏe petId=" + petId, e);
            request.setAttribute("loadError", "Không tải được sổ sức khỏe.");
        }

        request.getRequestDispatcher("/petHealth.jsp").forward(request, response);
    }

    /**
     * Dựng lịch cho tháng mà người dùng đang xem.
     * Tham số {@code ym} có dạng "2026-08"; thiếu hoặc sai định dạng thì mặc
     * định là tháng hiện tại, để đường dẫn hỏng không làm trang chết.
     */
    private static HealthCalendar buildCalendar(String ym, List<HealthRecord> records) {
        YearMonth month;
        try {
            month = (ym == null || ym.isBlank()) ? YearMonth.now() : YearMonth.parse(ym.trim());
        } catch (DateTimeParseException e) {
            month = YearMonth.now();
        }
        return HealthCalendar.build(month, records);
    }
}
