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
 * Thêm thú cưng mới cho người dùng đang đăng nhập.
 *
 * Chủ sở hữu lấy từ session, KHÔNG lấy từ tham số userId ẩn trong form như bản cũ
 * (trước đây người dùng có thể sửa giá trị đó để gán thú cưng cho tài khoản khác).
 */
@WebServlet("/addPet")
@MultipartConfig(maxFileSize = 5 * 1024 * 1024) // 5MB
public class AddPetServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(AddPetServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (MyUtils.getLoginedUser(request.getSession()) == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        request.getRequestDispatcher("/addPet.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        UserAccount user = MyUtils.getLoginedUser(request.getSession());
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String name = request.getParameter("name");
        String species = request.getParameter("species");

        if (name == null || name.isBlank() || species == null || species.isBlank()) {
            request.setAttribute("error", "Vui lòng nhập đầy đủ tên và loài thú cưng!");
            request.getRequestDispatcher("/addPet.jsp").forward(request, response);
            return;
        }

        LocalDate dob = null;
        String dobStr = request.getParameter("dob");
        if (dobStr != null && !dobStr.isBlank()) {
            try {
                dob = LocalDate.parse(dobStr);
            } catch (DateTimeParseException e) {
                request.setAttribute("error", "Ngày sinh không hợp lệ!");
                request.getRequestDispatcher("/addPet.jsp").forward(request, response);
                return;
            }
        }

        Connection conn = BookingServlet.requireConnection(request);
        try {
            Pet p = new Pet();
            p.setName(name.trim());
            p.setSpecies(species.trim());
            p.setBreed(request.getParameter("breed"));
            p.setGender(request.getParameter("gender"));
            p.setDob(dob);
            p.setFurColor(request.getParameter("furColor"));
            p.setIdentifyingMarks(request.getParameter("identifyingMarks"));
            p.setUserId(user.getId()); // chủ sở hữu lấy từ phiên đăng nhập

            Part photoPart = request.getPart("photo");
            if (photoPart != null && photoPart.getSize() > 0) {
                p.setPhoto(photoPart.getInputStream().readAllBytes());
            }

            PetDAO.insert(conn, p);
            response.sendRedirect(request.getContextPath() + "/petProfile");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi thêm thú cưng cho userId=" + user.getId(), e);
            request.setAttribute("error", "Có lỗi xảy ra, vui lòng thử lại sau!");
            request.getRequestDispatcher("/addPet.jsp").forward(request, response);
        }
    }
}
