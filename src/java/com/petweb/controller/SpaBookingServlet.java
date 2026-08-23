package com.petweb.controller;

import com.petweb.dao.ServiceCatalogDAO;
import com.petweb.model.BookingLine;
import com.petweb.service.BookingException;
import com.petweb.service.BookingService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Đặt dịch vụ Spa. Dùng chung cho khách đã đăng nhập và khách vãng lai.
 * Giá từng hạng mục do BookingService lấy từ bảng giá rồi chụp lại vào đơn,
 * nên số tiền không phụ thuộc dữ liệu trình duyệt gửi lên.
 */
@WebServlet("/SpaBookingServlet")
public class SpaBookingServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(SpaBookingServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Connection conn = BookingServlet.requireConnection(request);
        try {
            request.setAttribute("allItems", ServiceCatalogDAO.findAllSpaItems(conn));
            BookingServlet.exposeDraft(request, conn);
            request.getRequestDispatcher("/bookSpa.jsp").forward(request, response);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi tải danh sách dịch vụ spa", e);
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        Connection conn = BookingServlet.requireConnection(request);

        try {
            int bookingId = BookingServlet.requireDraftId(session);

            List<Integer> itemIds = ServletParams.parseIds(request.getParameterValues("itemIds"));
            Timestamp bookingDate = HotelServlet.parseDateTime(
                    request.getParameter("bookingDate"), "Ngày đặt lịch không hợp lệ.");

            BookingLine line = BookingService.addSpaLine(conn, bookingId, itemIds, bookingDate);

            request.setAttribute("line", line);
            request.getRequestDispatcher("/bookSpaResult.jsp").forward(request, response);

        } catch (BookingException e) {
            request.setAttribute("error", e.getMessage());
            try {
                request.setAttribute("allItems", ServiceCatalogDAO.findAllSpaItems(conn));
            } catch (SQLException ignored) {
                // danh sách rỗng thì JSP đã có nhánh hiển thị riêng
            }
            BookingServlet.exposeDraft(request, conn);
            request.getRequestDispatcher("/bookSpa.jsp").forward(request, response);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi đặt dịch vụ spa", e);
            throw new ServletException(e);
        }
    }
}
