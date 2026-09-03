package com.petweb.controller;

import com.petweb.dao.BookingDAO;
import com.petweb.dao.PetDAO;
import com.petweb.model.Pet;
import com.petweb.model.PetStay;
import com.petweb.model.UserAccount;
import com.petweb.service.PetService;
import com.petweb.utils.MyUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Hồ sơ thú cưng của người dùng đang đăng nhập.
 *
 * Phải là Servlet chứ không thể để petProfile.jsp tự truy vấn: JDBCFilter chỉ mở
 * kết nối cho các URL trỏ tới servlet, nên JSP gọi thẳng sẽ không có Connection.
 * Đặt ở đây cũng là nơi duy nhất kiểm tra đăng nhập cho màn hình này.
 */
@WebServlet("/petProfile")
public class PetProfileServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(PetProfileServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        UserAccount user = MyUtils.getLoginedUser(request.getSession());
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Connection conn = BookingServlet.requireConnection(request);
        try {
            List<Pet> pets = PetDAO.findByOwner(conn, user.getId());
            request.setAttribute("pets", pets);

            // Toàn bộ đợt lưu trú còn hiệu lực, gom theo bé. Một bé có thể có
            // nhiều đợt đặt trước, nên lấy cả danh sách chứ không chỉ đợt đầu.
            request.setAttribute("stays", BookingDAO.groupStaysByPet(
                    BookingDAO.findCurrentStaysByOwner(conn, user.getId())));

            // Bé còn đơn hiệu lực thì không xóa được, và xóa bé là mất luôn sổ
            // sức khỏe. Đưa hai con số này ra để thẻ báo trước, thay vì để người
            // dùng bấm xóa rồi mới nhận thông báo từ chối.
            Map<Integer, Integer> activeOrders = new HashMap<>();
            Map<Integer, Integer> healthCounts = new HashMap<>();
            Map<Integer, Integer> orderCounts = new HashMap<>();
            for (Pet p : pets) {
                activeOrders.put(p.getPetId(),
                        BookingDAO.countActiveBookingsForPet(conn, p.getPetId()));
                healthCounts.put(p.getPetId(),
                        PetService.healthRecordsAtRisk(conn, p.getPetId()));
                // Tổng số đơn đã đặt, để thẻ dẫn thẳng sang danh sách đơn của bé
                orderCounts.put(p.getPetId(),
                        BookingDAO.countBookingsForPet(conn, p.getPetId()));
            }
            request.setAttribute("activeOrders", activeOrders);
            request.setAttribute("healthCounts", healthCounts);
            request.setAttribute("orderCounts", orderCounts);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi tải danh sách thú cưng của userId=" + user.getId(), e);
            request.setAttribute("loadError", "Không tải được danh sách thú cưng.");
        }
        request.getRequestDispatcher("/petProfile.jsp").forward(request, response);
    }
}
