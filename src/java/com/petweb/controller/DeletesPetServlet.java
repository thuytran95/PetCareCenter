package com.petweb.controller;

import com.petweb.service.BookingException;
import com.petweb.service.PetService;
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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Xóa thú cưng.
 *
 * Quy tắc (kiểm tra quyền sở hữu và chặn bé còn đơn đang hiệu lực) nằm ở PetService;
 * servlet chỉ đọc tham số và hiển thị kết quả.
 * Tham số vẫn giữ tên "petid" (chữ thường) cho khớp các link sẵn có trong JSP.
 */
@WebServlet("/deletePet")
public class DeletesPetServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(DeletesPetServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        UserAccount user = MyUtils.getLoginedUser(request.getSession());
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String petIdStr = request.getParameter("petid");
        int petId;
        try {
            petId = Integer.parseInt(petIdStr);
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("error", "Mã thú cưng không hợp lệ!");
            response.sendRedirect(request.getContextPath() + "/petProfile");
            return;
        }

        Connection conn = BookingServlet.requireConnection(request);
        try {
            int rows = PetService.deleteOwned(conn, petId, user.getId());
            if (rows > 0) {
                request.getSession().setAttribute("message", "Đã xóa thú cưng.");
            } else {
                request.getSession().setAttribute("error",
                        "Không tìm thấy thú cưng này trong hồ sơ của bạn.");
            }
        } catch (BookingException e) {
            // Bé còn đơn đang hiệu lực: đây là từ chối có chủ ý, không phải sự cố
            request.getSession().setAttribute("error", e.getMessage());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi xóa thú cưng petId=" + petId, e);
            request.getSession().setAttribute("error",
                    "Không thể xóa thú cưng vì đang có dữ liệu liên quan.");
        }

        response.sendRedirect(request.getContextPath() + "/petProfile");
    }
}
