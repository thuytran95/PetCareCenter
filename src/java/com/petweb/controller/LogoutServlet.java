package com.petweb.controller;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Đăng xuất: hủy session hiện tại rồi quay về trang trước đó (nếu có redirect hợp lệ).
 * Header.jsp gọi bằng POST (form ẩn), nhưng doGet cũng xử lý giống hệt
 * để tránh để lộ trang scaffold mặc định nếu có ai truy cập /logout bằng GET.
 */
@WebServlet(name = "LogoutServlet", urlPatterns = {"/logout"})
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        logout(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        logout(request, response);
    }

    private void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        String redirect = request.getParameter("redirect");
        if (isSafeRedirect(redirect)) {
            response.sendRedirect(request.getContextPath() + redirect);
            return;
        }
        response.sendRedirect(request.getContextPath() + "/");
    }

    private boolean isSafeRedirect(String redirect) {
        if (redirect == null || redirect.isEmpty()) {
            return false;
        }
        if (!redirect.startsWith("/") || redirect.startsWith("//")) {
            return false;
        }
        return !redirect.contains("://");
    }
}
