package com.petweb.controller;
import jakarta.servlet.http.HttpSession;
import com.petweb.model.Pet;
import com.petweb.utils.ConnectionUtils;
import com.petweb.utils.DBUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.Connection;
import java.time.LocalDate;

@WebServlet("/addPet")
@MultipartConfig(maxFileSize = 5 * 1024 * 1024) // 5MB
public class AddPetServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Hiển thị form thêm pet
        request.getRequestDispatcher("/addPet.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try (Connection conn = ConnectionUtils.getConnection()) {
            Pet p = new Pet();

            p.setName(request.getParameter("name"));
            p.setSpecies(request.getParameter("species"));
            p.setBreed(request.getParameter("breed"));
            p.setGender(request.getParameter("gender"));

            String dobStr = request.getParameter("dob");
            if (dobStr != null && !dobStr.isEmpty()) {
                p.setDob(LocalDate.parse(dobStr));
            }

            p.setFurColor(request.getParameter("furColor"));
            p.setIdentifyingMarks(request.getParameter("identifyingMarks"));


            int userId = Integer.parseInt(request.getParameter("userId"));
            p.setUserId(userId);

            Part photoPart = request.getPart("photo");
            if (photoPart != null && photoPart.getSize() > 0) {
                p.setPhoto(photoPart.getInputStream().readAllBytes());
            }

            DBUtils.insertPet(conn, p);

            response.sendRedirect(request.getContextPath() + "/petProfile.jsp");

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
