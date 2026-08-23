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
 * Đăng ký dịch vụ Y tế. Dùng chung cho khách đã đăng nhập và khách vãng lai.
 *
 * Bản Guest cũ (MedicalBookingServletGuest + bookMedicalGuest.jsp) từng bị lỗi
 * copy nhầm khiến khách vãng lai không đặt được dịch vụ y tế; gộp một bản
 * loại bỏ hẳn nguy cơ đó.
 */
@WebServlet("/MedicalBookingServlet")
public class MedicalBookingServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(MedicalBookingServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Connection conn = BookingServlet.requireConnection(request);
        try {
            request.setAttribute("allItems", ServiceCatalogDAO.findAllMedicalItems(conn));
            BookingServlet.exposeDraft(request, conn);
            request.getRequestDispatcher("/bookMedical.jsp").forward(request, response);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi tải danh sách dịch vụ y tế", e);
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
            Timestamp admissionDate = HotelServlet.parseDateTime(
                    request.getParameter("admissionDate"), "Ngày nhập viện không hợp lệ.");

            BookingLine line = BookingService.addMedicalLine(conn, bookingId, itemIds, admissionDate);

            request.setAttribute("line", line);
            request.getRequestDispatcher("/bookMedicalResult.jsp").forward(request, response);

        } catch (BookingException e) {
            request.setAttribute("error", e.getMessage());
            try {
                request.setAttribute("allItems", ServiceCatalogDAO.findAllMedicalItems(conn));
            } catch (SQLException ignored) {
                // danh sách rỗng thì JSP đã có nhánh hiển thị riêng
            }
            BookingServlet.exposeDraft(request, conn);
            request.getRequestDispatcher("/bookMedical.jsp").forward(request, response);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi đăng ký dịch vụ y tế", e);
            throw new ServletException(e);
        }
    }
}
