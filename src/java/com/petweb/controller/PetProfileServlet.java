package com.petweb.controller;

import com.petweb.dao.BookingDAO;
import com.petweb.dao.PetDAO;
import com.petweb.model.PetStay;
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
            request.setAttribute("pets", PetDAO.findByOwner(conn, user.getId()));

            // Toàn bộ đợt lưu trú còn hiệu lực, gom theo bé. Một bé có thể có
            // nhiều đợt đặt trước, nên lấy cả danh sách chứ không chỉ đợt đầu.
            request.setAttribute("stays", BookingDAO.groupStaysByPet(
                    BookingDAO.findCurrentStaysByOwner(conn, user.getId())));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi tải danh sách thú cưng của userId=" + user.getId(), e);
            request.setAttribute("loadError", "Không tải được danh sách thú cưng.");
        }
        request.getRequestDispatcher("/petProfile.jsp").forward(request, response);
    }
}
