package com.petweb.controller;
import jakarta.servlet.http.HttpSession;
import com.petweb.model.Pet;
import com.petweb.model.UserAccount;
import com.petweb.utils.ConnectionUtils;
import com.petweb.utils.DBUtils;
import com.petweb.utils.MyUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.Connection;
import java.time.LocalDate;

@WebServlet("/editPet")
@MultipartConfig(maxFileSize = 5 * 1024 * 1024) // 5MB
public class EditPetServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String petIdStr = request.getParameter("petId");
        if (petIdStr == null || petIdStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/index.jsp");
            return;
        }

        try (Connection conn = ConnectionUtils.getConnection()) {
            int petId = Integer.parseInt(petIdStr);
            Pet pet = DBUtils.getPetById(conn, petId);

            if (pet != null) {
                request.setAttribute("pet", pet);
                request.getRequestDispatcher("/editPet.jsp").forward(request, response);
            } else {
                response.sendRedirect(request.getContextPath() + "/index.jsp");
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String petIdStr = request.getParameter("petId");
        if (petIdStr == null || petIdStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/index.jsp");
            return;
        }

        try (Connection conn = ConnectionUtils.getConnection()) {
            int petId = Integer.parseInt(petIdStr);

            // Lấy pet cũ để giữ nguyên dữ liệu nếu người dùng không nhập
            Pet oldPet = DBUtils.getPetById(conn, petId);

            Pet p = new Pet();
            p.setPetId(petId);
            p.setName(getOrDefault(request.getParameter("name"), oldPet.getName()));
            p.setSpecies(getOrDefault(request.getParameter("species"), oldPet.getSpecies()));
            p.setBreed(getOrDefault(request.getParameter("breed"), oldPet.getBreed()));
            p.setGender(getOrDefault(request.getParameter("gender"), oldPet.getGender()));

            String dobStr = request.getParameter("dob");
            if (dobStr != null && !dobStr.isEmpty()) {
                p.setDob(LocalDate.parse(dobStr));
            } else {
                p.setDob(oldPet.getDob());
            }

            p.setFurColor(getOrDefault(request.getParameter("furColor"), oldPet.getFurColor()));
            p.setIdentifyingMarks(getOrDefault(request.getParameter("identifyingMarks"), oldPet.getIdentifyingMarks()));
            
            HttpSession session = request.getSession();
            UserAccount loginedUser = MyUtils.getLoginedUser(session);
            if (loginedUser == null) {
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }
            p.setUserId(loginedUser.getId());

            Part photoPart = request.getPart("photo");
            if (photoPart != null && photoPart.getSize() > 0) {
                p.setPhoto(photoPart.getInputStream().readAllBytes());
            } else {
                p.setPhoto(oldPet.getPhoto());
            }

            DBUtils.updatePet(conn, p);
            response.sendRedirect(request.getContextPath() + "/index.jsp");

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private String getOrDefault(String value, String defaultVal) {
        return (value != null && !value.isEmpty()) ? value : defaultVal;
    }
}
