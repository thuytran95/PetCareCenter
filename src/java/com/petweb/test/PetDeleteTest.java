package com.petweb.test;

import com.petweb.dao.BookingDAO;
import com.petweb.dao.HealthRecordDAO;
import com.petweb.dao.PetDAO;
import com.petweb.dao.ServiceCatalogDAO;
import com.petweb.service.BookingService;
import com.petweb.service.PetService;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Kiểm thử quy tắc xóa hồ sơ thú cưng.
 *
 * Vì sao cần quy tắc này: khóa ngoại của CSDL KHÔNG ngăn được việc xóa. Cột
 * booking.pet_id khai báo ON DELETE SET NULL nên lệnh xóa vẫn chạy trót lọt và
 * để lại đơn mồ côi cùng căn phòng bị khóa mà không ai trả được; còn bảng
 * pet_health_record khai báo ON DELETE CASCADE nên sổ tiêm mất vĩnh viễn.
 *
 * Chạy trên CSDL thật trong một giao dịch bị rollback ở cuối.
 */
public class PetDeleteTest {

    static int pass = 0, fail = 0;
    static int userId;

    static void check(String name, boolean ok, String detail) {
        if (ok) { pass++; System.out.println("  [OK]   " + name); }
        else    { fail++; System.out.println("  [FAIL] " + name + " -> " + detail); }
    }

    static void eq(String name, Object expect, Object actual) {
        boolean ok = (expect == null) ? actual == null : expect.equals(actual);
        check(name, ok, "mong doi=" + expect + " nhung nhan=" + actual);
    }

    public static void main(String[] args) throws Exception {
        Class.forName("org.postgresql.Driver");
        try (Connection conn = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/petcare", "postgres", "1")) {
            conn.setAutoCommit(false);
            try {
                run(conn);
            } finally {
                conn.rollback();
                System.out.println("\n(da rollback, CSDL khong doi)");
            }
        }
        System.out.println("\n===== " + pass + " dat / " + fail + " hong =====");
        if (fail > 0) System.exit(1);
    }

    static void run(Connection conn) throws Exception {

        userId = scalar(conn, "SELECT id FROM user_account ORDER BY id LIMIT 1");
        check("co tai khoan de thu", userId > 0, "khong co user");

        // ---------- 1. Bé rảnh thì xóa được ----------
        System.out.println("\n-- Nhom 1: be ranh thi xoa duoc --");

        int free = insertPet(conn, userId);
        eq("be moi tao khong co don nao", 0,
                BookingDAO.countActiveBookingsForPet(conn, free));
        eq("xoa duoc", 1, PetService.deleteOwned(conn, free, userId));
        check("be da bien mat khoi ho so",
                PetDAO.findByIdAndOwner(conn, free, userId) == null, "van con");

        // ---------- 2. Bé đang ở khách sạn thì không xóa được ----------
        System.out.println("\n-- Nhom 2: be dang o khach san --");

        int staying = insertPet(conn, userId);
        Timestamp in = hours(-24);
        Timestamp out = hours(72);
        int booking = BookingService.startDraftForUser(conn, userId, staying);
        BookingService.addHotelLine(conn, booking, "vip1", in, out);
        BookingService.confirm(conn, booking);
        BookingService.pay(conn, booking);

        eq("dem duoc mot don dang hieu luc", 1,
                BookingDAO.countActiveBookingsForPet(conn, staying));

        int busyRooms = ServiceCatalogDAO.countOverlappingRooms(conn, "vip1", in, out, null);
        String msg = deleteFails(conn, staying);
        check("khong xoa duoc be dang co don", msg != null, "van xoa duoc");
        check("thong bao noi ro so don con hieu luc",
                msg != null && msg.contains("1 đơn"), "thong bao=" + msg);
        check("thong bao chi cach xu ly",
                msg != null && msg.contains("trả phòng"), "thong bao=" + msg);

        check("be van con nguyen trong ho so",
                PetDAO.findByIdAndOwner(conn, staying, userId) != null, "bi xoa mat");
        eq("don van gan dung be", staying,
                scalar(conn, "SELECT pet_id FROM booking WHERE booking_id = " + booking));
        eq("so phong bi chiem khong doi", busyRooms,
                ServiceCatalogDAO.countOverlappingRooms(conn, "vip1", in, out, null));

        // Trả phòng xong thì xóa được
        BookingService.checkOut(conn, staying > 0 ? booking : booking);
        eq("tra phong xong thi khong con don hieu luc", 0,
                BookingDAO.countActiveBookingsForPet(conn, staying));
        eq("luc nay xoa duoc", 1, PetService.deleteOwned(conn, staying, userId));

        // ---------- 3. Các trạng thái đơn khác ----------
        System.out.println("\n-- Nhom 3: trang thai don nao thi chan --");

        int confirmed = withBooking(conn, "CONFIRMED");
        check("don da xac nhan thi chan", deleteFails(conn, confirmed) != null, "van xoa duoc");

        int cancelled = withBooking(conn, "CANCELLED");
        eq("don da huy thi khong chan", 0,
                BookingDAO.countActiveBookingsForPet(conn, cancelled));
        eq("xoa duoc be chi con don da huy", 1,
                PetService.deleteOwned(conn, cancelled, userId));

        int completed = withBooking(conn, "COMPLETED");
        eq("don da hoan tat thi khong chan", 0,
                BookingDAO.countActiveBookingsForPet(conn, completed));

        // Đơn nháp RỖNG không giữ chỗ nên không được chặn việc xóa
        int emptyDraft = insertPet(conn, userId);
        BookingService.startDraftForUser(conn, userId, emptyDraft);
        eq("don nhap rong khong chan viec xoa", 0,
                BookingDAO.countActiveBookingsForPet(conn, emptyDraft));
        eq("xoa duoc be chi co don nhap rong", 1,
                PetService.deleteOwned(conn, emptyDraft, userId));

        // Đơn nháp ĐÃ chọn dịch vụ thì đang giữ chỗ, phải chặn
        int liveDraft = insertPet(conn, userId);
        int d = BookingService.startDraftForUser(conn, userId, liveDraft);
        BookingService.addHotelLine(conn, d, "vip1", hours(500), hours(520));
        eq("don nhap da chon dich vu thi chan", 1,
                BookingDAO.countActiveBookingsForPet(conn, liveDraft));
        check("khong xoa duoc", deleteFails(conn, liveDraft) != null, "van xoa duoc");

        // Đơn nháp quá hạn giữ chỗ thì thôi, không chặn nữa
        ageBooking(conn, d, ServiceCatalogDAO.DRAFT_HOLD_HOURS + 1);
        eq("don nhap qua han khong con chan", 0,
                BookingDAO.countActiveBookingsForPet(conn, liveDraft));

        // ---------- 3b. Đếm đơn hiện trên thẻ ----------
        System.out.println("\n-- Nhom 3b: dem don hien tren the thu cung --");

        int counted = insertPet(conn, userId);
        eq("be moi chua co don nao", 0, BookingDAO.countBookingsForPet(conn, counted));

        int c1 = BookingService.startDraftForUser(conn, userId, counted);
        BookingService.addHotelLine(conn, c1, "vip1", hours(700), hours(720));
        eq("don nhap chua chot thi chua tinh", 0,
                BookingDAO.countBookingsForPet(conn, counted));

        BookingService.confirm(conn, c1);
        eq("chot don thi dem duoc 1", 1, BookingDAO.countBookingsForPet(conn, counted));

        int c2 = BookingService.startDraftForUser(conn, userId, counted);
        BookingService.addHotelLine(conn, c2, "vip1", hours(800), hours(820));
        BookingService.confirm(conn, c2);
        eq("them don thu hai", 2, BookingDAO.countBookingsForPet(conn, counted));

        BookingService.cancel(conn, c2);
        eq("don da huy van nam trong lich su", 2,
                BookingDAO.countBookingsForPet(conn, counted));

        eq("be khac khong bi tinh chung", 0,
                BookingDAO.countBookingsForPet(conn, insertPet(conn, userId)));
        eq("be khong ton tai thi bang 0", 0, BookingDAO.countBookingsForPet(conn, -1));

        // ---------- 3c. Xóa hẳn đơn đã kết thúc ----------
        System.out.println(NHOM3C);

        int owner2 = scalar(conn, "SELECT id FROM user_account WHERE id <> " + userId
                + " ORDER BY id LIMIT 1");

        // Đơn đã hủy: xóa được, dòng dịch vụ xóa theo
        int delPet = insertPet(conn, userId);
        int delOrder = BookingService.startDraftForUser(conn, userId, delPet);
        BookingService.addHotelLine(conn, delOrder, "vip1", hours(900), hours(920));
        BookingService.confirm(conn, delOrder);
        BookingService.cancel(conn, delOrder);
        check("truoc khi xoa, don co dong dich vu",
                !BookingDAO.findLines(conn, delOrder).isEmpty(), "khong co dong nao");
        BookingService.deleteFinished(conn, delOrder, userId);
        check("don da huy bi xoa han",
                BookingDAO.findById(conn, delOrder) == null, "van con");
        eq("dong dich vu cua don cung bi xoa theo", 0,
                BookingDAO.findLines(conn, delOrder).size());

        // Đơn đã hoàn tất: xóa được, và sổ sức khỏe của bé PHẢI còn nguyên
        int hPet = insertPet(conn, userId);
        int hOrder = BookingService.startDraftForUser(conn, userId, hPet);
        List<Integer> vac = new ArrayList<>();
        vac.add(scalar(conn, "SELECT item_id FROM medical_service_item"
                + " WHERE category = " + Q + "VACCINE" + Q + " ORDER BY item_id LIMIT 1"));
        BookingService.addMedicalLine(conn, hOrder, vac, hours(-72));
        BookingService.confirm(conn, hOrder);
        BookingService.pay(conn, hOrder);
        int bookRows = HealthRecordDAO.countByPet(conn, hPet);
        check("da ghi so suc khoe truoc khi xoa", bookRows > 0, "so ghi chep=" + bookRows);

        setStatus(conn, hOrder, "COMPLETED");
        BookingService.deleteFinished(conn, hOrder, userId);
        check("don da hoan tat bi xoa han",
                BookingDAO.findById(conn, hOrder) == null, "van con");
        eq("SO TIEM CUA BE VAN CON NGUYEN", bookRows, HealthRecordDAO.countByPet(conn, hPet));
        eq("ghi chep chi mat duong dan toi don", 0,
                scalar(conn, "SELECT count(*) FROM pet_health_record"
                        + " WHERE pet_id = " + hPet + " AND booking_id IS NOT NULL"));

        // Đơn còn hiệu lực thì KHÔNG xóa được
        int livePet = insertPet(conn, userId);
        int live = BookingService.startDraftForUser(conn, userId, livePet);
        BookingService.addHotelLine(conn, live, "vip1", hours(950), hours(960));
        BookingService.confirm(conn, live);
        String m1 = deleteOrderFails(conn, live, userId);
        check("don da xac nhan thi khong xoa duoc", m1 != null, "xoa duoc");
        check("thong bao chi ro chi xoa duoc don da ket thuc",
                m1 != null && m1.contains("đã hoàn tất"), "thong bao=" + m1);

        BookingService.pay(conn, live);
        check("don da thanh toan cung khong xoa duoc",
                deleteOrderFails(conn, live, userId) != null, "xoa duoc");
        check("don van con nguyen", BookingDAO.findById(conn, live) != null, "bi xoa mat");

        // Đơn nháp cũng không xóa qua đường này
        int draftPet = insertPet(conn, userId);
        int draftOrder = BookingService.startDraftForUser(conn, userId, draftPet);
        check("don nhap khong xoa qua duong nay",
                deleteOrderFails(conn, draftOrder, userId) != null, "xoa duoc");

        // Không xóa được đơn của người khác
        if (owner2 > 0) {
            int othersPet = insertPet(conn, owner2);
            int othersOrder = BookingService.startDraftForUser(conn, owner2, othersPet);
            BookingService.addHotelLine(conn, othersOrder, "vip1", hours(970), hours(980));
            BookingService.confirm(conn, othersOrder);
            BookingService.cancel(conn, othersOrder);
            check("khong xoa duoc don cua nguoi khac",
                    deleteOrderFails(conn, othersOrder, userId) != null, "xoa duoc");
            check("don cua nguoi khac van con",
                    BookingDAO.findById(conn, othersOrder) != null, "bi xoa mat");
        }

        check("don khong ton tai thi bao khong tim thay",
                deleteOrderFails(conn, -999, userId) != null, "khong bao gi");

        // ---------- 4. Quyền sở hữu ----------
        System.out.println("\n-- Nhom 4: quyen so huu --");

        int mine = insertPet(conn, userId);
        check("nguoi khac khong xoa duoc be cua minh",
                deleteFailsAs(conn, mine, -1) != null, "xoa duoc be nguoi khac");
        check("be van con", PetDAO.findByIdAndOwner(conn, mine, userId) != null, "bi xoa mat");
        check("be khong ton tai thi bao khong tim thay",
                deleteFails(conn, -12345) != null, "khong bao gi");

        // ---------- 5. Cảnh báo mất sổ sức khỏe ----------
        System.out.println("\n-- Nhom 5: canh bao mat so suc khoe --");

        int withBook = insertPet(conn, userId);
        eq("be moi thi so suc khoe trong", 0,
                PetService.healthRecordsAtRisk(conn, withBook));

        int med = BookingService.startDraftForUser(conn, userId, withBook);
        List<Integer> items = new ArrayList<>();
        items.add(scalar(conn, "SELECT item_id FROM medical_service_item"
                + " WHERE category = 'VACCINE' ORDER BY item_id LIMIT 1"));
        BookingService.addMedicalLine(conn, med, items, hours(-48));
        BookingService.confirm(conn, med);
        BookingService.pay(conn, med);

        int atRisk = PetService.healthRecordsAtRisk(conn, withBook);
        check("da co ghi chep trong so suc khoe", atRisk > 0, "so ghi chep=" + atRisk);
        eq("con so canh bao khop voi so ghi chep that",
                HealthRecordDAO.countByPet(conn, withBook), atRisk);

        // Đơn y tế đã qua thời gian nên tự hết hiệu lực, xóa được — và khi đó
        // sổ sức khỏe mất thật, đúng như lời cảnh báo.
        eq("don y te da qua thi khong chan", 0,
                BookingDAO.countActiveBookingsForPet(conn, withBook));
        eq("xoa duoc", 1, PetService.deleteOwned(conn, withBook, userId));
        eq("so suc khoe bi xoa theo dung nhu canh bao", 0,
                scalar(conn, "SELECT count(*) FROM pet_health_record WHERE pet_id = " + withBook));
    }

    static final String NHOM3C = "\n-- Nhom 3c: xoa han don da ket thuc --";

    /** Dấu nháy đơn trong câu SQL, viết tách ra cho dễ đọc. */
    static final String Q = "" + (char) 39;

    /** Thử xóa đơn và trả về thông báo từ chối, hoặc null nếu xóa được. */
    static String deleteOrderFails(Connection conn, int bookingId, int asUser) {
        try {
            BookingService.deleteFinished(conn, bookingId, asUser);
            return null;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    static void setStatus(Connection c, int bookingId, String status) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE booking SET status = ? WHERE booking_id = ?")) {
            ps.setString(1, status);
            ps.setInt(2, bookingId);
            ps.executeUpdate();
        }
    }

    // ----- tiện ích -----

    /** Bé kèm một đơn khách sạn đã chuyển sang trạng thái chỉ định. */
    static int withBooking(Connection conn, String status) throws Exception {
        int pet = insertPet(conn, userId);
        int b = BookingService.startDraftForUser(conn, userId, pet);
        BookingService.addHotelLine(conn, b, "vip1", hours(600), hours(620));
        BookingService.confirm(conn, b);
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE booking SET status = ? WHERE booking_id = ?")) {
            ps.setString(1, status);
            ps.setInt(2, b);
            ps.executeUpdate();
        }
        return pet;
    }

    /** Thử xóa và trả về thông báo từ chối, hoặc null nếu xóa được. */
    static String deleteFails(Connection conn, int petId) {
        return deleteFailsAs(conn, petId, userId);
    }

    static String deleteFailsAs(Connection conn, int petId, int asUser) {
        try {
            PetService.deleteOwned(conn, petId, asUser);
            return null;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    static void ageBooking(Connection c, int bookingId, int hours) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE booking SET created_at = now() - (? || ' hours')::interval"
              + " WHERE booking_id = ?")) {
            ps.setString(1, String.valueOf(hours));
            ps.setInt(2, bookingId);
            ps.executeUpdate();
        }
    }

    static Timestamp hours(int h) {
        return Timestamp.valueOf(LocalDateTime.now().plusHours(h).withNano(0));
    }

    static int insertPet(Connection c, int owner) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO pet (name, species, user_id) VALUES (?,?,?) RETURNING pet_id")) {
            ps.setString(1, "Be Kiem Thu");
            ps.setString(2, "Chó");
            ps.setInt(3, owner);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    static int scalar(Connection c, String sql) throws SQLException {
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : -1;
        }
    }
}
