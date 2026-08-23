package com.petweb.controller;

import com.petweb.utils.MyUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Màn hình chọn dịch vụ cho đơn đang mở.
 *
 * Trước đây trang này được vào bằng cách chuyển hướng thẳng tới
 * {@code /chooseService.jsp}. Nhưng JDBCFilter chỉ mở kết nối CSDL cho những
 * URL có servlet đăng ký, nên đường đi đó không có kết nối và trang không bao
 * giờ đọc được đơn nháp. Servlet này giữ đúng vai trò đó: nạp đơn nháp kèm các
 * dòng dịch vụ đã thêm rồi mới chuyển sang JSP, để trang hiện được giỏ dịch vụ
 * và tạm tính.
 */
@WebServlet("/chooseService")
public class ChooseServiceServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        BookingServlet.exposeDraft(request, MyUtils.getStoredConnection(request));
        request.getRequestDispatcher("/chooseService.jsp").forward(request, response);
    }
}
