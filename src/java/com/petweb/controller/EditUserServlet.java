package com.petweb.controller;

import com.petweb.model.UserAccount;
import com.petweb.utils.DBUtils;
import com.petweb.service.NotificationService;
import com.petweb.utils.MyUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/editUser")
@MultipartConfig
public class EditUserServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(EditUserServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int id;
        try {
            id = Integer.parseInt(request.getParameter("id"));
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Thiếu hoặc sai định dạng id người dùng!");
            request.getRequestDispatcher("/editUser.jsp").forward(request, response);
            return;
        }

        Connection conn = MyUtils.getStoredConnection(request);
        if (conn == null) {
            throw new ServletException("Không có kết nối CSDL cho request này");
        }

        try {
            // editUser.jsp không có input "role" — giữ nguyên role hiện có, không cho client tự đổi quyền qua form này
            UserAccount oldUser = DBUtils.findUser(conn, id);
            if (oldUser == null) {
                request.setAttribute("error", "Không tìm thấy người dùng!");
                request.getRequestDispatcher("/editUser.jsp").forward(request, response);
                return;
            }

            // Số điện thoại: cho phép để trống, nhưng nếu có nhập thì phải dùng được
            // để nhận thông báo — nếu không, báo ngay thay vì lưu im lặng rồi khách
            // thắc mắc vì sao không nhận được tin.
            String phone = clean(request.getParameter("phone"));
            if (phone != null && !NotificationService.isSendablePhone(phone)) {
                request.setAttribute("error",
                        "Số điện thoại \"" + phone + "\" không hợp lệ: cần ít nhất 8 chữ số "
                      + "(ví dụ 0912345678). Bạn có thể để trống nếu chưa muốn nhận thông báo.");
                request.setAttribute("user", oldUser);
                request.getRequestDispatcher("/editUser.jsp").forward(request, response);
                return;
            }

            UserAccount u = new UserAccount();
            u.setId(id);
            // userName và password là bắt buộc: nếu form không gửi lên thì giữ giá trị cũ
            // thay vì ghi NULL làm vi phạm ràng buộc của bảng.
            u.setUserName(orKeep(clean(request.getParameter("userName")), oldUser.getUserName()));
            u.setPassword(orKeep(clean(request.getParameter("password")), oldUser.getPassword()));
            // Các trường còn lại được phép để trống
            u.setFullName(clean(request.getParameter("fullName")));
            u.setEmail(clean(request.getParameter("email")));
            u.setPhone(phone);
            u.setAddress(clean(request.getParameter("address")));
            u.setRole(oldUser.getRole());

            Part avatarPart = request.getPart("avatar");
            if (avatarPart != null && avatarPart.getSize() > 0) {
                u.setAvatar(avatarPart.getInputStream().readAllBytes());
            } else {
                u.setAvatar(oldUser.getAvatar());
            }

            DBUtils.updateUserById(conn, u);

            String encodedUserName = URLEncoder.encode(u.getUserName(), "UTF-8");
            response.sendRedirect(request.getContextPath() + "/editUser.jsp?userName=" + encodedUserName);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi cập nhật hồ sơ user id=" + id, e);
            request.setAttribute("error", "Có lỗi xảy ra, vui lòng thử lại sau!");
            request.getRequestDispatcher("/editUser.jsp").forward(request, response);
        }
    }

    /**
     * Chuẩn hóa giá trị từ form: rỗng hoặc đúng chữ "null" thì coi như không có.
     * Lớp chặn thứ hai sau khi JSP đã được sửa để không in ra chữ "null" nữa.
     */
    /** Giữ giá trị cũ khi giá trị mới không có. */
    private static String orKeep(String newValue, String oldValue) {
        return (newValue != null) ? newValue : oldValue;
    }

    private static String clean(String v) {
        if (v == null) return null;
        String t = v.trim();
        if (t.isEmpty() || t.equalsIgnoreCase("null") || t.equalsIgnoreCase("undefined")) {
            return null;
        }
        return t;
    }
}
