package com.petweb.dao;

import com.petweb.model.Pet;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Truy cập bảng pet. Mọi phương thức nhận Connection từ bên ngoài (JDBCFilter quản lý
 * vòng đời và transaction cho cả request) nên DAO không tự mở/đóng kết nối.
 */
public class PetDAO {

    private static final String COLUMNS =
            "pet_id, name, species, breed, gender, dob, fur_color, identifying_marks, user_id, photo";

    /**
     * Lấy thú cưng theo id KÈM kiểm tra chủ sở hữu.
     * Đây là hàm nên dùng ở mọi luồng sửa/xóa/đặt lịch: trả về null khi con vật
     * không thuộc về userId, nhờ đó người dùng không thao tác được lên thú cưng của người khác
     * chỉ bằng cách đổi số trên URL.
     */
    public static Pet findByIdAndOwner(Connection conn, int petId, int userId) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM pet WHERE pet_id = ? AND user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, petId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    /** Lấy thú cưng theo id, không kiểm tra chủ. Chỉ dùng cho màn hình quản trị. */
    public static Pet findById(Connection conn, int petId) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM pet WHERE pet_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, petId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    /** Danh sách thú cưng của đúng một chủ. */
    public static List<Pet> findByOwner(Connection conn, int userId) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM pet WHERE user_id = ? ORDER BY pet_id";
        List<Pet> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public static Pet insert(Connection conn, Pet p) throws SQLException {
        String sql = """
            INSERT INTO pet (name, species, breed, gender, dob, fur_color, identifying_marks, user_id, photo)
            VALUES (?,?,?,?,?,?,?,?,?)
            RETURNING pet_id
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getSpecies());
            ps.setString(3, p.getBreed());
            ps.setString(4, p.getGender());
            if (p.getDob() != null) ps.setDate(5, Date.valueOf(p.getDob()));
            else ps.setNull(5, Types.DATE);
            ps.setString(6, p.getFurColor());
            ps.setString(7, p.getIdentifyingMarks());
            ps.setInt(8, p.getUserId());
            ps.setBytes(9, p.getPhoto());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) p.setPetId(rs.getInt("pet_id"));
            }
        }
        return p;
    }

    /** Cập nhật kèm điều kiện chủ sở hữu; trả về số dòng đổi (0 = không phải chủ hoặc không tồn tại). */
    public static int updateOwned(Connection conn, Pet pet, int userId) throws SQLException {
        String sql = """
            UPDATE pet
            SET name=?, species=?, breed=?, gender=?, dob=?, fur_color=?, identifying_marks=?, photo=?
            WHERE pet_id=? AND user_id=?
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pet.getName());
            ps.setString(2, pet.getSpecies());
            ps.setString(3, pet.getBreed());
            ps.setString(4, pet.getGender());
            if (pet.getDob() != null) ps.setDate(5, Date.valueOf(pet.getDob()));
            else ps.setNull(5, Types.DATE);
            ps.setString(6, pet.getFurColor());
            ps.setString(7, pet.getIdentifyingMarks());
            ps.setBytes(8, pet.getPhoto());
            ps.setInt(9, pet.getPetId());
            ps.setInt(10, userId);
            return ps.executeUpdate();
        }
    }

    /** Xóa kèm điều kiện chủ sở hữu; trả về số dòng đã xóa. */
    public static int deleteOwned(Connection conn, int petId, int userId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM pet WHERE pet_id=? AND user_id=?")) {
            ps.setInt(1, petId);
            ps.setInt(2, userId);
            return ps.executeUpdate();
        }
    }

    private static Pet mapRow(ResultSet rs) throws SQLException {
        Pet p = new Pet();
        p.setPetId(rs.getInt("pet_id"));
        p.setName(rs.getString("name"));
        p.setSpecies(rs.getString("species"));
        p.setBreed(rs.getString("breed"));
        p.setGender(rs.getString("gender"));
        Date dob = rs.getDate("dob");
        if (dob != null) p.setDob(dob.toLocalDate());
        p.setFurColor(rs.getString("fur_color"));
        p.setIdentifyingMarks(rs.getString("identifying_marks"));
        p.setUserId(rs.getInt("user_id"));
        p.setPhoto(rs.getBytes("photo"));
        return p;
    }
}
