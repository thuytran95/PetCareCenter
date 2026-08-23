package com.petweb.controller;

import com.petweb.dao.PetDAO;
import com.petweb.model.Pet;
import com.petweb.model.UserAccount;
import com.petweb.utils.MyUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sửa hồ sơ thú cưng.
 *
 * Mọi thao tác đều đi qua PetDAO.findByIdAndOwner / updateOwned nên người dùng
 * không sửa được thú cưng của người khác bằng cách đổi petId trên URL.
 */
@WebServlet("/editPet")
@MultipartConfig(maxFileSize = 5 * 1024 * 1024) // 5MB
public class EditPetServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(EditPetServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        UserAccount user = MyUtils.getLoginedUser(request.getSession());
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Integer petId = parsePetId(request);
        if (petId == null) {
            response.sendRedirect(request.getContextPath() + "/petProfile");
            return;
        }

        Connection conn = BookingServlet.requireConnection(request);
        try {
            Pet pet = PetDAO.findByIdAndOwner(conn, petId, user.getId());
            if (pet == null) {
                request.getSession().setAttribute("error",
                        "Không tìm thấy thú cưng này trong hồ sơ của bạn.");
                response.sendRedirect(request.getContextPath() + "/petProfile");
                return;
            }
            request.setAttribute("pet", pet);
            request.getRequestDispatcher("/editPet.jsp").forward(request, response);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi tải thú cưng petId=" + petId, e);
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        UserAccount user = MyUtils.getLoginedUser(request.getSession());
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Integer petId = parsePetId(request);
        if (petId == null) {
            response.sendRedirect(request.getContextPath() + "/petProfile");
            return;
        }

        Connection conn = BookingServlet.requireConnection(request);
        try {
            Pet oldPet = PetDAO.findByIdAndOwner(conn, petId, user.getId());
            if (oldPet == null) {
                request.getSession().setAttribute("error",
                        "Không tìm thấy thú cưng này trong hồ sơ của bạn.");
                response.sendRedirect(request.getContextPath() + "/petProfile");
                return;
            }

            Pet p = new Pet();
            p.setPetId(petId);
            p.setUserId(user.getId());
            p.setName(orDefault(request.getParameter("name"), oldPet.getName()));
            p.setSpecies(orDefault(request.getParameter("species"), oldPet.getSpecies()));
            p.setBreed(orDefault(request.getParameter("breed"), oldPet.getBreed()));
            p.setGender(orDefault(request.getParameter("gender"), oldPet.getGender()));
            p.setFurColor(orDefault(request.getParameter("furColor"), oldPet.getFurColor()));
            p.setIdentifyingMarks(orDefault(request.getParameter("identifyingMarks"),
                    oldPet.getIdentifyingMarks()));

            String dobStr = request.getParameter("dob");
            if (dobStr != null && !dobStr.isBlank()) {
                try {
                    p.setDob(LocalDate.parse(dobStr));
                } catch (DateTimeParseException e) {
                    request.setAttribute("error", "Ngày sinh không hợp lệ!");
                    request.setAttribute("pet", oldPet);
                    request.getRequestDispatcher("/editPet.jsp").forward(request, response);
                    return;
                }
            } else {
                p.setDob(oldPet.getDob());
            }

            Part photoPart = request.getPart("photo");
            if (photoPart != null && photoPart.getSize() > 0) {
                p.setPhoto(photoPart.getInputStream().readAllBytes());
            } else {
                p.setPhoto(oldPet.getPhoto());
            }

            PetDAO.updateOwned(conn, p, user.getId());
            response.sendRedirect(request.getContextPath() + "/petProfile");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi cập nhật thú cưng petId=" + petId, e);
            throw new ServletException(e);
        }
    }

    private Integer parsePetId(HttpServletRequest request) {
        try {
            return Integer.valueOf(request.getParameter("petId"));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String orDefault(String value, String fallback) {
        return (value != null && !value.isEmpty()) ? value : fallback;
    }
}
