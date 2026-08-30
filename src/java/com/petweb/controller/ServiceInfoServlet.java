package com.petweb.controller;

import com.petweb.dao.ServiceCatalogDAO;
import com.petweb.model.ServicePage;
import com.petweb.service.ServicePageCatalog;
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
 * Trang giới thiệu dịch vụ: /service?type=spa|hotel|vaccine|medical
 *
 * Bốn trang có nội dung hoàn toàn khác nhau (lấy từ ServicePageCatalog) nhưng dùng
 * chung một khuôn hiển thị, nên không phải nhân bản bốn file JSP gần giống nhau.
 *
 * Bảng giá được đọc trực tiếp từ CSDL để trang giới thiệu luôn khớp với số tiền
 * thật lúc đặt lịch, thay vì viết cứng vào nội dung.
 */
@WebServlet("/service")
public class ServiceInfoServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ServiceInfoServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        ServicePage page = ServicePageCatalog.find(request.getParameter("type"));
        if (page == null) {
            // Mã dịch vụ không hợp lệ: đưa về trang chủ thay vì báo lỗi kỹ thuật
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        request.setAttribute("page", page);
        request.setAttribute("otherPages", ServicePageCatalog.all());

        Connection conn = MyUtils.getStoredConnection(request);
        if (conn != null) {
            try {
                switch (page.getPriceSource()) {
                    case ServicePageCatalog.PRICE_SPA ->
                            request.setAttribute("spaItems", ServiceCatalogDAO.findAllSpaItems(conn));
                    case ServicePageCatalog.PRICE_MEDICAL ->
                            request.setAttribute("medicalItems", ServiceCatalogDAO.findAllMedicalItems(conn));
                    case ServicePageCatalog.PRICE_ROOMS ->
                            request.setAttribute("roomTypes", ServiceCatalogDAO.findActiveRoomTypes(conn));
                    default -> { /* không có bảng giá thì trang vẫn hiển thị phần giới thiệu */ }
                }
            } catch (SQLException e) {
                // Không tải được bảng giá thì vẫn cho xem phần giới thiệu
                LOGGER.log(Level.WARNING, "Không tải được bảng giá cho trang " + page.getCode(), e);
            }
        }

        request.getRequestDispatcher("/serviceInfo.jsp").forward(request, response);
    }
}
