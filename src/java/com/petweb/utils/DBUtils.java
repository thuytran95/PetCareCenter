package com.petweb.utils;

import com.petweb.model.UserAccount;
import com.petweb.model.BookingInfo;
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
   public static Pet insertPet(Connection conn, Pet p) throws SQLException {
    String sql = """
        INSERT INTO pet (name, species, breed, gender, dob, fur_color, identifying_marks, user_id, photo)
        VALUES (?,?,?,?,?,?,?,?,?)
        RETURNING pet_id
    """;

    try (PreparedStatement pstm = conn.prepareStatement(sql)) {
        pstm.setString(1, p.getName());
        pstm.setString(2, p.getSpecies());
        pstm.setString(3, p.getBreed());
        pstm.setString(4, p.getGender());

        if (p.getDob() != null) {
            pstm.setDate(5, java.sql.Date.valueOf(p.getDob()));
        } else {
            pstm.setNull(5, java.sql.Types.DATE);
        }

        pstm.setString(6, p.getFurColor());
        pstm.setString(7, p.getIdentifyingMarks());
        pstm.setInt(8, p.getUserId());
        pstm.setBytes(9, p.getPhoto());

        try (ResultSet rs = pstm.executeQuery()) {
            if (rs.next()) {
                p.setPetId(rs.getInt("pet_id"));
            }
        }
    }
   
    return p;
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

    // Cập nhật user theo userName (nếu bạn muốn)
    public static void updateUserByUserName(Connection conn, UserAccount u) throws SQLException {
        String sql = """
            UPDATE user_account
            SET gender=?, password=?, email=?, phone=?, address=?, avatar=?, full_name=?
            WHERE user_name=?
        """;
        try (PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setString(1, u.getGender());
            pstm.setString(2, u.getPassword());
            pstm.setString(3, u.getEmail());
            pstm.setString(4, u.getPhone());
            pstm.setString(5, u.getAddress());
            pstm.setBytes(6, u.getAvatar());
            pstm.setString(7, u.getFullName());
            pstm.setString(8, u.getUserName());
            pstm.executeUpdate();
        }
    }
    
 public static int updatePet(Connection conn, Pet pet) throws SQLException {
        String sql = """
            UPDATE pet
            SET name=?, species=?, breed=?, gender=?, dob=?, fur_color=?, identifying_marks=?,user_id=?, photo=?
            WHERE pet_id=?
        """;
        try (PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setString(1, pet.getName());
            pstm.setString(2, pet.getSpecies());
            pstm.setString(3, pet.getBreed());
            pstm.setString(4, pet.getGender());
            if (pet.getDob() != null) pstm.setDate(5, Date.valueOf(pet.getDob())); else pstm.setNull(5, Types.DATE);
            pstm.setString(6, pet.getFurColor());
            pstm.setString(7, pet.getIdentifyingMarks());
            pstm.setInt(8, pet.getUserId());
            pstm.setBytes(9, pet.getPhoto());
            pstm.setInt(10, pet.getPetId());

            return pstm.executeUpdate();
        }
    }


    // Xóa user theo userName
    public static void deleteUser(Connection conn, String userName) throws SQLException {
        try (PreparedStatement pstm = conn.prepareStatement("DELETE FROM user_account WHERE user_name=?")) {
            pstm.setString(1, userName);
            pstm.executeUpdate();
        }
    }
    
     public static int deletePet(Connection conn, int petId) throws SQLException {
             try (PreparedStatement pstm = conn.prepareStatement("DELETE FROM pet WHERE pet_id=?")) {
                    pstm.setInt(1, petId);
                    return pstm.executeUpdate();
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
 public static Pet getPetById(Connection conn, int petId) throws SQLException {
    String sql = "SELECT * FROM pet WHERE pet_id = ?";
    try (PreparedStatement pstm = conn.prepareStatement(sql)) {
        pstm.setInt(1, petId);
        try (ResultSet rs = pstm.executeQuery()) {
            if (rs.next()) {
                return mapRowPet(rs);
            }
        }
    }
    return null;
}

public static boolean bookRoom(Connection conn, int serviceId, String roomType, Timestamp checkIn, Timestamp checkOut, BigDecimal tongTien) throws SQLException {
    String sql = "INSERT INTO hotel_detail (service_id, room_type, check_in, check_out, hotel_price,status) VALUES (?,?,?,?,?,'busy')";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, serviceId);
        ps.setString(2, roomType);
        ps.setTimestamp(3, checkIn);
        ps.setTimestamp(4, checkOut);
        ps.setBigDecimal(5, tongTien);
        return ps.executeUpdate() > 0;
    }
}
   

public static List<SpaServiceItem> getSpaAllItems() throws Exception {
        List<SpaServiceItem> list = new ArrayList<>();
        try (Connection conn = ConnectionUtils.getConnection()) {
            String sql = "SELECT item_id, item_name, item_price FROM spa_service_item";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while(rs.next()) {
                    list.add(new SpaServiceItem(
                        rs.getInt("item_id"),
                        rs.getString("item_name"),
                        rs.getBigDecimal("item_price")
                    ));
                }
            }
        }
        return list;
    }

    // Lấy danh sách item theo ID
    public static List<SpaServiceItem> getSpaItemsByIds(String[] itemIds) throws Exception {
        List<SpaServiceItem> list = new ArrayList<>();
        if(itemIds == null || itemIds.length == 0) return list;

        try(Connection conn = ConnectionUtils.getConnection()) {
            String sql = "SELECT item_id, item_name, item_price FROM spa_service_item WHERE item_id = ?";
            try(PreparedStatement ps = conn.prepareStatement(sql)) {
                for(String idStr : itemIds) {
                    int itemId = Integer.parseInt(idStr);
                    ps.setInt(1, itemId);
                    try(ResultSet rs = ps.executeQuery()) {
                        if(rs.next()) {
                            list.add(new SpaServiceItem(
                                rs.getInt("item_id"),
                                rs.getString("item_name"),
                                rs.getBigDecimal("item_price")
                            ));
                        }
                    }
                }
            }
        }
        return list;
    }

  public static BigDecimal addSpaItemsAndCalculatePrice(
        int spaId, int serviceId, String[] itemIds, Timestamp bookingDate) throws Exception {

    if(itemIds == null || itemIds.length == 0) return BigDecimal.ZERO;
    BigDecimal totalPrice = BigDecimal.ZERO;

    try(Connection conn = ConnectionUtils.getConnection()) {
        conn.setAutoCommit(false);
        try {
            // Nếu spaId = 0 thì insert mới
            if (spaId == 0) {
                String insertSql = "INSERT INTO spa_detail (booking_date, service_id, spa_price, status) " +
                                   "VALUES (?, ?, 0, 'busy') RETURNING spa_id";
                try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
                    psInsert.setTimestamp(1, bookingDate);
                    psInsert.setInt(2, serviceId);
                    try (ResultSet rs = psInsert.executeQuery()) {
                        if (rs.next()) {
                            spaId = rs.getInt(1);
                        }
                    }
                }
            } else {
                // update booking_date
                String updateSql = "UPDATE spa_detail SET booking_date=?, status='busy' WHERE spa_id=?";
                try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                    psUpdate.setTimestamp(1, bookingDate);
                    psUpdate.setInt(2, spaId);
                    psUpdate.executeUpdate();
                }
                // Xoá item cũ (tránh duplicate)
                String deleteSql = "DELETE FROM spa_detail_item WHERE spa_id=?";
                try (PreparedStatement psDel = conn.prepareStatement(deleteSql)) {
                    psDel.setInt(1, spaId);
                    psDel.executeUpdate();
                }
            }

            // Thêm spa_detail_item
            String sqlInsert = "INSERT INTO spa_detail_item(spa_id, item_id) VALUES (?, ?)";
            for(String idStr : itemIds) {
                int itemId = Integer.parseInt(idStr);
                BigDecimal itemPrice = getSpaItemPrice(conn, itemId);
                totalPrice = totalPrice.add(itemPrice);

                try (PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
                    ps.setInt(1, spaId);
                    ps.setInt(2, itemId);
                    ps.executeUpdate();
                }
            }

            // Cập nhật tổng giá
            String sqlUpdate = "UPDATE spa_detail SET spa_price=? WHERE spa_id=?";
            try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                ps.setBigDecimal(1, totalPrice);
                ps.setInt(2, spaId);
                ps.executeUpdate();
            }

            conn.commit();
        } catch(Exception ex) {
            conn.rollback();
            throw ex;
        }
    }
    return totalPrice;
}


    private static BigDecimal getSpaItemPrice(Connection conn, int itemId) throws Exception {
        String sql = "SELECT item_price FROM spa_service_item WHERE item_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next()) return rs.getBigDecimal("item_price");
            }
        }
        return BigDecimal.ZERO;
    }

    public static MedicalServiceItem getMedicalItemById(int itemId) throws Exception {
        String sql = "SELECT * FROM medical_service_item WHERE item_id = ?";
        try (Connection conn = ConnectionUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new MedicalServiceItem(
                        rs.getInt("item_id"),
                        rs.getString("item_name"),
                        rs.getBigDecimal("item_price")
                    );
                }
            }
        }
        return null; // không tìm thấy
    }

    // Thêm các item vào medical_detail + tính tổng giá
    public static BigDecimal addMedicalItemsAndCalculatePrice(int medicalId,int serviceId, String[] itemIds, Timestamp admissionDate) throws Exception {
    if(itemIds == null || itemIds.length == 0) return BigDecimal.ZERO;

    BigDecimal totalPrice = BigDecimal.ZERO;

    try(Connection conn = ConnectionUtils.getConnection()) {
        conn.setAutoCommit(false);

        // Nếu medicalId = 0 thì insert mới
        if (medicalId == 0) {
            String insertSql = "INSERT INTO medical_detail (admission_date, service_id, medical_price, status) " +
                               "VALUES (?, ?, 0, 'busy') RETURNING medical_id";
            try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
                psInsert.setTimestamp(1, admissionDate);
                psInsert.setInt(2, serviceId); // service_id giả định = 3
                try (ResultSet rs = psInsert.executeQuery()) {
                    if (rs.next()) {
                        medicalId = rs.getInt(1);
                    }
                }
            }
        } else {
            // Update lại admission_date và status
            String updateSql = "UPDATE medical_detail SET admission_date=?, status='busy' WHERE medical_id=?";
            try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                psUpdate.setTimestamp(1, admissionDate);
                psUpdate.setInt(2, medicalId);
                psUpdate.executeUpdate();
            }
        }

        // Thêm medical_detail_item (lấy giá từ DB trong cùng transaction)
        String sqlItem = "SELECT item_price FROM medical_service_item WHERE item_id=?";
        String sqlInsert = "INSERT INTO medical_detail_item(medical_id, item_id, price) VALUES (?, ?, ?)";
        for (String idStr : itemIds) {
            int itemId = Integer.parseInt(idStr);

            try (PreparedStatement ps1 = conn.prepareStatement(sqlItem)) {
                ps1.setInt(1, itemId);
                try (ResultSet rs = ps1.executeQuery()) {
                    if (rs.next()) {
                        BigDecimal itemPrice = rs.getBigDecimal("item_price");
                        totalPrice = totalPrice.add(itemPrice);

                        try (PreparedStatement ps2 = conn.prepareStatement(sqlInsert)) {
                            ps2.setInt(1, medicalId);
                            ps2.setInt(2, itemId);
                            ps2.setBigDecimal(3, itemPrice);
                            ps2.executeUpdate();
                        }
                    }
                }
            }
        }

        // Cập nhật tổng giá
        String sqlUpdate = "UPDATE medical_detail SET medical_price=? WHERE medical_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
            ps.setBigDecimal(1, totalPrice);
            ps.setInt(2, medicalId);
            ps.executeUpdate();
        }

        conn.commit();
    }

    return totalPrice;
}

    public static List<MedicalServiceItem> getAllMedicalItems() throws Exception {
    List<MedicalServiceItem> list = new ArrayList<>();
    String sql = "SELECT * FROM medical_service_item";

    try (Connection conn = ConnectionUtils.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            list.add(new MedicalServiceItem(
                rs.getInt("item_id"),
                rs.getString("item_name"),
                rs.getBigDecimal("item_price")
            ));
        }
    }
    return list;
}
public static int createServiceInfo(Connection conn, int bookingId, int petId) throws SQLException {
    String sql = "INSERT INTO service_info(booking_id, pet_id, status_info) " +
                 "VALUES(?, ?, 'PENDING') RETURNING service_id";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, bookingId);
        ps.setInt(2, petId);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("service_id");
            }
        }
    }
    return 0;
}

public static int createBookingInfo(Connection conn, int userId, int petId) throws SQLException {
    String sql = "INSERT INTO booking_info (user_id, pet_id, total_price, status_info) " +
                 "VALUES (?, ?, 0, 'PENDING') RETURNING booking_id";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, userId);
        ps.setInt(2, petId);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt("booking_id");
        }
    }
    return 0;
}


// Cập nhật tổng tiền vào booking_info
public static void updateBookingTotal(Connection conn, int bookingId, BigDecimal totalPrice) throws SQLException {
    String sql = "UPDATE booking_info SET total_price=? WHERE booking_id=?";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setBigDecimal(1, totalPrice);
        ps.setInt(2, bookingId);
        ps.executeUpdate();
    }
}

// Lấy thông tin booking kèm chi tiết dịch vụ
public static BookingInfo getBookingInfoById(Connection conn, int bookingId) throws SQLException {
    String sql = "SELECT * FROM booking_info WHERE booking_id=?";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, bookingId);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                BookingInfo b = new BookingInfo();
                b.setBookingId(rs.getInt("booking_id"));
                b.setUserId(rs.getInt("user_id"));
                b.setTotalPrice(rs.getBigDecimal("total_price"));
                b.setStatus(rs.getString("status"));
                return b;
            }
        }
    }
    return null;
}

// Lấy tổng tiền từ các dịch vụ liên quan đến booking
public static BigDecimal calculateTotalFromServices(Connection conn, int bookingId) throws SQLException {
    BigDecimal total = BigDecimal.ZERO;

    // Hotel
    String sqlHotel = "SELECT SUM(hotel_price) AS sum_price " +
                      "FROM hotel_detail " +
                      "WHERE service_id IN (SELECT service_id FROM service_info WHERE booking_id = ?)";
    try (PreparedStatement ps = conn.prepareStatement(sqlHotel)) {
        ps.setInt(1, bookingId);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next() && rs.getBigDecimal("sum_price") != null) {
                total = total.add(rs.getBigDecimal("sum_price"));
            }
        }
    }

    // Spa
    String sqlSpa = "SELECT SUM(spa_price) AS sum_price " +
                    "FROM spa_detail " +
                    "WHERE service_id IN (SELECT service_id FROM service_info WHERE booking_id = ?)";
    try (PreparedStatement ps = conn.prepareStatement(sqlSpa)) {
        ps.setInt(1, bookingId);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next() && rs.getBigDecimal("sum_price") != null) {
                total = total.add(rs.getBigDecimal("sum_price"));
            }
        }
    }

    // Medical
    String sqlMedical = "SELECT SUM(medical_price) AS sum_price " +
                        "FROM medical_detail " +
                        "WHERE service_id IN (SELECT service_id FROM service_info WHERE booking_id = ?)";
    try (PreparedStatement ps = conn.prepareStatement(sqlMedical)) {
        ps.setInt(1, bookingId);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next() && rs.getBigDecimal("sum_price") != null) {
                total = total.add(rs.getBigDecimal("sum_price"));
            }
        }
    }
    String updateSql = "UPDATE booking_info SET total_price = ? WHERE booking_id = ?";
    try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
        ps.setBigDecimal(1, total);
        ps.setInt(2, bookingId);
        ps.executeUpdate();
    }

    return total;
}

public static void confirmBooking(Connection conn, int serviceId) throws SQLException {
    // Update service_info thành CONFIRMED
    String sqlService = "UPDATE service_info SET status_info = 'CONFIRMED' WHERE service_id = ?";
    try (PreparedStatement ps = conn.prepareStatement(sqlService)) {
        ps.setInt(1, serviceId);
        ps.executeUpdate();
    }

    // Tìm booking_id gắn với service_id này
    String sqlFindBooking = "SELECT booking_id FROM service_info WHERE service_id = ?";
    int bookingId = -1;
    try (PreparedStatement ps = conn.prepareStatement(sqlFindBooking)) {
        ps.setInt(1, serviceId);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                bookingId = rs.getInt("booking_id");
            }
        }
    }

    if (bookingId != -1) {
        // Update booking_info thành CONFIRMED
        String sqlBooking = "UPDATE booking_info SET status_info = 'CONFIRMED' WHERE booking_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlBooking)) {
            ps.setInt(1, bookingId);
            ps.executeUpdate();
        }
    }
}
public static void addBookingDetail(Connection conn, int serviceId, String serviceType, int petId) throws SQLException {
    String sql = "INSERT INTO booking_detail (service_id, service_type, price, check_in, check_out, note, total_price) " +
                 "VALUES (?, ?, ?, ?, ?, ?, ?)";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, serviceId);
        ps.setString(2, serviceType);
        ps.setBigDecimal(3, BigDecimal.valueOf(0.0)); // price default
        ps.setTimestamp(4, null); // check_in
        ps.setTimestamp(5, null); // check_out
        ps.setString(6, null);    // note
        ps.setBigDecimal(7, BigDecimal.valueOf(0.0)); // total_price default = price
        ps.executeUpdate();
    }
}
}