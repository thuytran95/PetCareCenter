package com.petweb.controller;

import com.petweb.utils.DBUtils;
import com.petweb.utils.MyUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet("/deleteUser")
public class DeleteUserServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String userName = request.getParameter("userName");
        Connection conn = MyUtils.getStoredConnection(request);

        try {
            if (userName != null && !userName.isEmpty()) {
                DBUtils.deleteUser(conn, userName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Quay lại danh sách user sau khi xóa
        response.sendRedirect(request.getContextPath() + "/index.jsp");
    }
}
