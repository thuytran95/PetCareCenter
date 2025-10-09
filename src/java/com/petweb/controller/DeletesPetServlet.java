package com.petweb.controller;

import com.petweb.utils.ConnectionUtils;
import com.petweb.utils.DBUtils;
import java.sql.Connection;
import java.sql.SQLException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/deletePet")
public class DeletesPetServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String petIdStr = request.getParameter("petid");
        if (petIdStr == null || petIdStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/index.jsp");
            return;
        }

        try (Connection conn = ConnectionUtils.getConnection()) {
            int petId = Integer.parseInt(petIdStr);
            int rows = DBUtils.deletePet(conn, petId);

            if (rows > 0) {
                request.getSession().setAttribute("message", "Xóa Pet thành công!");
            } else {
                request.getSession().setAttribute("error", "Không tìm thấy Pet để xóa!");
            }

        } catch (SQLException e) {
            Logger.getLogger(DeletesPetServlet.class.getName()).log(Level.SEVERE, null, e);
            request.getSession().setAttribute("error", "Lỗi khi xóa Pet: " + e.getMessage());
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DeletesPetServlet.class.getName()).log(Level.SEVERE, null, ex);
            request.getSession().setAttribute("error", "Không thể kết nối CSDL: " + ex.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/petProfile.jsp");
    }
}
