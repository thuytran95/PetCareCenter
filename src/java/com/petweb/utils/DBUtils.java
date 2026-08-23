package com.petweb.utils;

import com.petweb.model.UserAccount;
import com.petweb.model.MedicalServiceItem;
import com.petweb.model.Pet;

import com.petweb.model.SpaServiceItem;
import java.math.BigDecimal;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBUtils {

    // Đọc user theo userName + password (đăng nhập)
    public static UserAccount findUser(Connection conn, String userName, String password) throws SQLException {
        String sql = """
            SELECT id, user_name, gender, password, email, phone, address, avatar, full_name,role
            FROM user_account
            WHERE user_name = ? AND password = ?
        """;
        try (PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setString(1, userName);
            pstm.setString(2, password);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    // Đọc user theo userName
    public static UserAccount findUser(Connection conn, String userName) throws SQLException {
        String sql = """
            SELECT id, user_name, gender, password, email, phone, address, avatar, full_name,role
            FROM user_account
            WHERE user_name = ?
        """;
        try (PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setString(1, userName);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }
    
       // Đọc user theo userName
    public static UserAccount findUser(Connection conn, int id) throws SQLException {
        String sql = """
            SELECT id, user_name, gender, password, email, phone, address, avatar, full_name,role
            FROM user_account
            WHERE id = ?
        """;
        try (PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setInt(1, id);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }


    
    public static UserAccount register(Connection conn, UserAccount u) throws SQLException {
        // Kiểm tra trùng username / email / phone
        if (existsByUserName(conn, u.getUserName())) {
            throw new SQLException("Tên đăng nhập đã tồn tại!");
        }
        if (u.getEmail() != null && existsByEmail(conn, u.getEmail())) {
            throw new SQLException("Email đã được sử dụng!");
        }
        if (u.getPhone() != null && existsByPhone(conn, u.getPhone())) {
            throw new SQLException("Số điện thoại đã được sử dụng!");
        }

        String sql = """
            INSERT INTO user_account (user_name, gender, password, email, phone, address, avatar, full_name,role)
            VALUES (?,?,?,?,?,?,?,?,?)
            RETURNING id
        """;
        try (PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setString(1, u.getUserName());
            pstm.setString(2, u.getGender());
            pstm.setString(3, u.getPassword());
            pstm.setString(4, u.getEmail());
            pstm.setString(5, u.getPhone());
            pstm.setString(6, u.getAddress());
            pstm.setBytes(7, u.getAvatar());
            pstm.setString(8, u.getFullName());
            pstm.setString(9,u.getRole());

            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    u.setId(rs.getInt("id"));
                }
            }
        }
        return u;
    }


    // Cập nhật user theo id (khuyến nghị)
    public static void updateUserById(Connection conn, UserAccount u) throws SQLException {
        String sql = """
            UPDATE user_account
            SET user_name=?, gender=?, password=?, email=?, phone=?, address=?, avatar=?, full_name=?
            WHERE id=?
        """;
        try (PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setString(1, u.getUserName());
            pstm.setString(2, u.getGender());
            pstm.setString(3, u.getPassword());
            pstm.setString(4, u.getEmail());
            pstm.setString(5, u.getPhone());
            pstm.setString(6, u.getAddress());
            pstm.setBytes(7, u.getAvatar());
            pstm.setString(8, u.getFullName());
            pstm.setInt(9, u.getId());
            
            pstm.executeUpdate();
        }
    }

    // Danh sách user
    public static List<UserAccount> queryUsers(Connection conn) throws SQLException {
        String sql = """
            SELECT id, user_name, gender, password, email, phone, address, avatar, full_name,role
            FROM user_account
            ORDER BY id
        """;
        List<UserAccount> list = new ArrayList<>();
        try (PreparedStatement pstm = conn.prepareStatement(sql);
             ResultSet rs = pstm.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public static List<Pet> queryPets(Connection conn,int userId) throws SQLException{
         String sql = """
        SELECT pet_id, name, species, breed, dob, fur_color, user_id, photo, identifying_marks
        FROM pet
        WHERE user_id = ?
        ORDER BY pet_id
    """;
    List<Pet> list = new ArrayList<>();
    try (PreparedStatement pstm = conn.prepareStatement(sql)) {
        pstm.setInt(1, userId);
        try (ResultSet rs = pstm.executeQuery()) {
            while(rs.next()) {
                list.add(mapRowPet(rs));
            }
        }
    }
    return list;
    }
    // --- Helpers ---
    private static boolean existsByUserName(Connection conn, String userName) throws SQLException {
        try (PreparedStatement p = conn.prepareStatement("SELECT 1 FROM user_account WHERE user_name=?")) {
            p.setString(1, userName);
            try (ResultSet rs = p.executeQuery()) { return rs.next(); }
        }
    }

    private static boolean existsByEmail(Connection conn, String email) throws SQLException {
        try (PreparedStatement p = conn.prepareStatement("SELECT 1 FROM user_account WHERE email=?")) {
            p.setString(1, email);
            try (ResultSet rs = p.executeQuery()) { return rs.next(); }
        }
    }

    private static boolean existsByPhone(Connection conn, String phone) throws SQLException {
        try (PreparedStatement p = conn.prepareStatement("SELECT 1 FROM user_account WHERE phone=?")) {
            p.setString(1, phone);
            try (ResultSet rs = p.executeQuery()) { return rs.next(); }
        }
    }

    private static UserAccount mapRow(ResultSet rs) throws SQLException {
        UserAccount u = new UserAccount();
        u.setId(rs.getInt("id"));
        u.setUserName(rs.getString("user_name"));
        u.setGender(rs.getString("gender"));
        u.setPassword(rs.getString("password"));
        u.setEmail(rs.getString("email"));
        u.setPhone(rs.getString("phone"));
        u.setAddress(rs.getString("address"));
        u.setAvatar(rs.getBytes("avatar"));
        u.setFullName(rs.getString("full_name"));
        u.setRole(rs.getString("role"));
        return u;
    }

   private static Pet mapRowPet(ResultSet rs) throws SQLException {
    Pet p = new Pet();
    p.setPetId(rs.getInt("pet_id"));
    p.setName(rs.getString("name"));
    p.setSpecies(rs.getString("species"));
    p.setBreed(rs.getString("breed"));

    // chỉ đọc gender nếu cột tồn tại
    try {
        p.setGender(rs.getString("gender"));
    } catch (SQLException e) {
        p.setGender(null);
    }

    Date dob = rs.getDate("dob");
    if (dob != null) {
        p.setDob(dob.toLocalDate());
    }

    p.setFurColor(rs.getString("fur_color"));
    p.setIdentifyingMarks(rs.getString("identifying_marks"));
    p.setUserId(rs.getInt("user_id"));
    p.setPhoto(rs.getBytes("photo"));
    return p;
}

    public static void updatePassword(Connection conn, int userId, String newPassword) throws SQLException {
    String sql = "UPDATE user_account SET password = ? WHERE id = ?";
    try (PreparedStatement pstm = conn.prepareStatement(sql)) {
        pstm.setString(1, newPassword);
        pstm.setInt(2, userId);
        pstm.executeUpdate();
    }


}
 }
