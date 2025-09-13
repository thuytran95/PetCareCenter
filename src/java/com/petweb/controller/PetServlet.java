package com.petweb.controller;

import com.petweb.model.Pet;
import com.petweb.utils.ConnectionUtils;
import com.petweb.utils.DBUtils;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PetServlet extends HttpServlet {

    @Override
protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
    String action = request.getParameter("action");

    try (Connection conn = ConnectionUtils.getConnection()) {

        if ("edit".equals(action)) {
            // Lấy petId từ request
            int petId = Integer.parseInt(request.getParameter("petId"));

            // Gọi DBUtils để lấy Pet từ DB
            Pet pet = DBUtils.getPetById(conn, petId);

            // Set vào request
            request.setAttribute("pet", pet);

            // Forward sang editPet.jsp
            request.getRequestDispatcher("editPet.jsp").forward(request, response);
            return;
        }

        // Nếu không có action, ví dụ hiển thị danh sách pet
       
        request.getRequestDispatcher("index.jsp").forward(request, response);

    } catch (Exception e) {
        throw new ServletException(e);
    }
}

    @Override
protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

    request.setCharacterEncoding("UTF-8");
    String action = request.getParameter("action");

    try (Connection conn = ConnectionUtils.getConnection()) {

        if ("insert".equals(action)) {
            String name = request.getParameter("name");
            String species = request.getParameter("species");
            String breed = request.getParameter("breed");
            String dobStr = request.getParameter("dob");
            String furColor = request.getParameter("furColor");
            String userIdStr = request.getParameter("userId");

            java.time.LocalDate dob = null;
            if (dobStr != null && !dobStr.isEmpty()) {
                dob = java.time.LocalDate.parse(dobStr);
            }

            int userId = Integer.parseInt(userIdStr);

            // Xử lý upload file ảnh
            Part photoPart = request.getPart("photo");
            byte[] photoBytes = null;
            if (photoPart != null && photoPart.getSize() > 0) {
                try (java.io.InputStream is = photoPart.getInputStream()) {
                    photoBytes = is.readAllBytes();
                }
            }

            Pet pet = new Pet();
            pet.setName(name);
            pet.setSpecies(species);
            pet.setBreed(breed);
            pet.setDob(dob);
            pet.setFurColor(furColor);
            pet.setUserId(userId);
            pet.setPhoto(photoBytes);

            DBUtils.insertPet(conn, pet);
        }

        else if ("update".equals(action)) {
            String petIdStr = request.getParameter("petId");
            int petId = Integer.parseInt(petIdStr);

            String name = request.getParameter("name");
            String species = request.getParameter("species");
            String breed = request.getParameter("breed");
            String dobStr = request.getParameter("dob");
            String furColor = request.getParameter("furColor");
            String userIdStr = request.getParameter("userId");

            java.time.LocalDate dob = null;
            if (dobStr != null && !dobStr.isEmpty()) {
                dob = java.time.LocalDate.parse(dobStr);
            }

            int userId = Integer.parseInt(userIdStr);

            Part photoPart = request.getPart("photo");
            byte[] photoBytes = null;
            if (photoPart != null && photoPart.getSize() > 0) {
                try (java.io.InputStream is = photoPart.getInputStream()) {
                    photoBytes = is.readAllBytes();
                }
            }

            Pet pet = new Pet();
            pet.setPetId(petId);
            pet.setName(name);
            pet.setSpecies(species);
            pet.setBreed(breed);
            pet.setDob(dob);
            pet.setFurColor(furColor);
            pet.setUserId(userId);
            pet.setPhoto(photoBytes);

            DBUtils.updatePet(conn, pet);
        }

        response.sendRedirect(request.getContextPath() + "/pet");

    } catch (SQLException | NumberFormatException e) {
        throw new ServletException(e);
    }   catch (ClassNotFoundException ex) {
            Logger.getLogger(PetServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
}  
}
