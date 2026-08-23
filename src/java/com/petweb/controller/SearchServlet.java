package com.petweb.controller;

import com.petweb.dao.PetDAO;
import com.petweb.dao.ServiceCatalogDAO;
import com.petweb.model.*;
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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tìm kiếm cho ô tìm kiếm trên thanh điều hướng.
 *
 * Trước đây ô đó chỉ là hình trang trí, không gửi đi đâu. Giờ nó tìm trong
 * ba nhóm dữ liệu mà khách thực sự cần tra:
 *   1. Trang dịch vụ  (spa / khách sạn / vaccine / khám bệnh)
 *   2. Hạng mục dịch vụ trong bảng giá (spa + y tế + loại phòng)
 *   3. Thú cưng của CHÍNH người đang đăng nhập
 *
 * Lọc ngay trong Java thay vì viết thêm câu SQL LIKE: bảng giá chỉ vài chục
 * dòng nên đơn giản hơn và tránh phát sinh truy vấn mới cho một tính năng phụ.
 */
@WebServlet("/search")
public class SearchServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(SearchServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String rawQuery = request.getParameter("q");
        String query = (rawQuery == null) ? "" : rawQuery.trim();
        request.setAttribute("query", query);

        if (query.isEmpty()) {
            request.getRequestDispatcher("/search.jsp").forward(request, response);
            return;
        }

        String needle = normalize(query);
        Connection conn = BookingServlet.requireConnection(request);

        // 1. Trang dịch vụ
        List<ServicePage> pages = new ArrayList<>();
        for (ServicePage p : ServicePageCatalog.all()) {
            if (matches(needle, p.getTitle(), p.getTagline(), p.getIntro())) {
                pages.add(p);
            }
        }
        request.setAttribute("resultPages", pages);

        try {
            // 2. Hạng mục trong bảng giá
            List<SpaServiceItem> spa = new ArrayList<>();
            for (SpaServiceItem it : ServiceCatalogDAO.findAllSpaItems(conn)) {
                if (matches(needle, it.getItemName())) spa.add(it);
            }
            request.setAttribute("resultSpa", spa);

            List<MedicalServiceItem> med = new ArrayList<>();
            for (MedicalServiceItem it : ServiceCatalogDAO.findAllMedicalItems(conn)) {
                if (matches(needle, it.getItemName())) med.add(it);
            }
            request.setAttribute("resultMedical", med);

            List<RoomType> rooms = new ArrayList<>();
            for (RoomType r : ServiceCatalogDAO.findActiveRoomTypes(conn)) {
                if (matches(needle, r.getRoomName(), r.getDescription())) rooms.add(r);
            }
            request.setAttribute("resultRooms", rooms);

            // 3. Thú cưng của chính người đang đăng nhập
            UserAccount user = MyUtils.getLoginedUser(request.getSession());
            if (user != null) {
                List<Pet> pets = new ArrayList<>();
                for (Pet p : PetDAO.findByOwner(conn, user.getId())) {
                    if (matches(needle, p.getName(), p.getSpecies(), p.getBreed())) pets.add(p);
                }
                request.setAttribute("resultPets", pets);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Lỗi khi tìm kiếm với từ khóa: " + query, e);
            request.setAttribute("error", "Không thực hiện được tìm kiếm, vui lòng thử lại.");
        }

        request.getRequestDispatcher("/search.jsp").forward(request, response);
    }

    /** Có trường nào chứa từ khóa không (bỏ qua hoa/thường và dấu tiếng Việt). */
    private boolean matches(String needle, String... fields) {
        for (String f : fields) {
            if (f != null && normalize(f).contains(needle)) return true;
        }
        return false;
    }

    /**
     * Chuẩn hóa để tìm "kham benh" cũng ra "Khám bệnh":
     * tách dấu bằng Unicode NFD rồi bỏ các ký tự dấu.
     */
    private String normalize(String s) {
        String noAccent = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replace('đ', 'd').replace('Đ', 'D');
        return noAccent.toLowerCase();
    }
}
