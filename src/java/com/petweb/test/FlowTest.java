package com.petweb.test;

import com.petweb.dao.BookingDAO;
import com.petweb.dao.HealthRecordDAO;
import com.petweb.dao.NotificationDAO;
import com.petweb.dao.ServiceCatalogDAO;
import com.petweb.model.Booking;
import com.petweb.model.BookingLine;
import com.petweb.model.RoomAvailability;
import com.petweb.service.BookingService;
import com.petweb.service.PetService;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Đi trọn hai luồng chính từ đầu đến cuối, đúng thứ tự người dùng thật sẽ bấm.
 *
 * Khác với các bộ kiểm thử kia (mỗi bộ soi kỹ một quy tắc), bộ này kiểm tra các
 * bước NỐI VỚI NHAU có đúng không: tiền có khớp qua từng bước, phòng có được
 * giữ rồi nhả đúng lúc, thông báo có được gửi, sổ sức khỏe có được ghi.
 *
 * Chạy trên CSDL thật trong một giao dịch bị rollback ở cuối.
 */
public class FlowTest {

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
                userId = scalar(conn, "SELECT id FROM user_account ORDER BY id LIMIT 1");
                check("co tai khoan de thu", userId > 0, "khong co user");
                userFlow(conn);
                guestFlow(conn);
                mixedFlow(conn);
            } finally {
                conn.rollback();
                System.out.println("\n(da rollback, CSDL khong doi)");
            }
        }
        System.out.println("\n===== " + pass + " dat / " + fail + " hong =====");
        if (fail > 0) System.exit(1);
    }

    /** Luồng khách đã đăng nhập: đặt → chốt → trả tiền → trả phòng → xóa đơn. */
    static void userFlow(Connection conn) throws Exception {
        System.out.println("\n===== LUONG KHACH DA DANG NHAP =====");

        System.out.println("\n-- Buoc 1: mo don cho be --");
        int pet = insertPet(conn, userId);
        int order = BookingService.startDraftForUser(conn, userId, pet);
        Booking draft = BookingDAO.findByIdWithLines(conn, order);
        eq("don moi o trang thai nhap", Booking.STATUS_DRAFT, draft.getStatus());
        eq("don gan dung be", (Integer) pet, draft.getPetId());
        check("da chup lai ten be tren don", draft.getPetName() != null, "thieu ten be");
        eq("don chua co dich vu nao", 0, draft.getLines().size());

        System.out.println("\n-- Buoc 2: them phong khach san --");
        Timestamp in = hours(240);   // 10 ngày nữa
        Timestamp out = hours(312);  // ở 3 ngày
        int freeBefore = freeRooms(conn, "vip2", in, out);
        BookingLine room = BookingService.addHotelLine(conn, order, "vip2", in, out);
        eq("tinh dung so ngay o", 3, room.getQuantity());
        eq("gia phong lay tu bang gia nhan so ngay",
                priceOf(conn, "vip2").multiply(BigDecimal.valueOf(3)),
                room.getLineTotal());
        eq("them mot dong dich vu", 1, BookingDAO.findLines(conn, order).size());
        eq("don nhap da giu mot phong", freeBefore - 1, freeRooms(conn, "vip2", in, out));

        System.out.println("\n-- Buoc 3: them spa vao cung don --");
        BookingLine spa = BookingService.addSpaLine(conn, order, firstSpa(conn), hours(250));
        eq("gio co hai dong dich vu", 2, BookingDAO.findLines(conn, order).size());

        BigDecimal expected = room.getLineTotal().add(spa.getLineTotal());
        eq("tong tien khop tong cac dong", 0,
                expected.compareTo(BookingDAO.sumLineTotals(conn, order)));

        System.out.println("\n-- Buoc 4: chot don --");
        Booking confirmed = BookingService.confirm(conn, order);
        eq("don chuyen sang da xac nhan", Booking.STATUS_CONFIRMED, confirmed.getStatus());
        eq("tong tien tren don khop tong cac dong", 0,
                expected.compareTo(confirmed.getTotalPrice()));
        check("khach dang nhap thi khong can ma tra cuu",
                confirmed.getLookupCode() == null, "lai sinh ma");
        check("da ghi thong bao xac nhan",
                NotificationDAO.existsForBooking(conn, order, "BOOKING_CONFIRMED"),
                "khong co thong bao");
        check("don da chot van giu phong",
                freeRooms(conn, "vip2", in, out) == freeBefore - 1, "phong bi nha som");

        System.out.println("\n-- Buoc 5: thanh toan --");
        Booking paid = BookingService.pay(conn, order);
        eq("don chuyen sang da thanh toan", Booking.STATUS_PAID, paid.getStatus());
        check("da ghi thong bao thanh toan",
                NotificationDAO.existsForBooking(conn, order, "PAYMENT"), "khong co thong bao");
        check("tra tien roi van giu phong",
                freeRooms(conn, "vip2", in, out) == freeBefore - 1, "phong bi nha oan");
        check("don khong the thanh toan hai lan", payFails(conn, order), "tra tien duoc lan hai");

        System.out.println("\n-- Buoc 6: don hien dung cho tren giao dien --");
        check("don nam trong lich su cua be",
                BookingDAO.findHistory(conn, userId, pet).stream()
                        .anyMatch(b -> b.getBookingId() == order), "khong thay");
        eq("the thu cung dem duoc mot don", 1, BookingDAO.countBookingsForPet(conn, pet));
        check("dot luu tru hien tren ho so",
                BookingDAO.findCurrentStaysByOwner(conn, userId).stream()
                        .anyMatch(s -> s.getBookingId() == order), "khong thay");
        check("be dang co don nen chua xoa ho so duoc",
                deleteePetFails(conn, pet), "xoa duoc");

        System.out.println("\n-- Buoc 7: khong dat chong lan cho cung be --");
        int again = BookingService.startDraftForUser(conn, userId, pet);
        check("dat trung khoang thoi gian bi chan",
                addRoomFails(conn, again, "vip1", hours(250), hours(300)), "khong chan");
        check("dat khoang khac thi duoc",
                !addRoomFails(conn, again, "vip1", hours(400), hours(420)), "bi chan oan");
        BookingService.cancel(conn, BookingService.confirm(conn, again).getBookingId());

        System.out.println("\n-- Buoc 8: tra phong som --");
        // Kéo đợt lưu trú về hiện tại để trả phòng được
        shiftLine(conn, order, hours(-24), hours(48));
        Booking done = BookingService.checkOut(conn, order);
        eq("don chuyen sang hoan tat", Booking.STATUS_COMPLETED, done.getStatus());
        check("nhung ngay con lai da duoc nha ra",
                freeRooms(conn, "vip2", hours(1), hours(48)) == totalRooms(conn, "vip2"),
                "phong van bi giu");

        System.out.println("\n-- Buoc 9: don xong roi thi don dep duoc --");
        eq("be het don hieu luc", 0, BookingDAO.countActiveBookingsForPet(conn, pet));
        BookingService.deleteFinished(conn, order, userId);
        check("don da bi xoa khoi lich su",
                BookingDAO.findById(conn, order) == null, "van con");
        // Buoc 7 da tao them mot don roi huy, don do van nam trong lich su,
        // nen sau khi xoa don chinh thi con dung mot don.
        eq("chi con lai don da huy o buoc 7", 1, BookingDAO.countBookingsForPet(conn, pet));
        check("don vua xoa khong con trong lich su cua be",
                BookingDAO.findHistory(conn, userId, pet).stream()
                        .noneMatch(x -> x.getBookingId() == order), "van thay");
        eq("gio xoa duoc ho so be", 1, PetService.deleteOwned(conn, pet, userId));
    }

    /** Luồng khách vãng lai: đặt → chốt lấy mã → tra cứu → thanh toán. */
    static void guestFlow(Connection conn) throws Exception {
        System.out.println("\n===== LUONG KHACH VANG LAI =====");

        System.out.println("\n-- Buoc 1: mo don khong can tai khoan --");
        String phone = "0912345678";
        int order = BookingService.startDraftForGuest(conn, "Khach Vang Lai", phone,
                null, "Be Cua Khach", "Mèo");
        Booking b = BookingDAO.findByIdWithLines(conn, order);
        check("khong tao tai khoan ao", b.getUserId() == null, "lai gan user");
        check("khong gan ho so thu cung", b.getPetId() == null, "lai gan pet");
        eq("luu ten be tren don", "Be Cua Khach", b.getPetName());

        System.out.println("\n-- Buoc 2: chon dich vu y te --");
        BookingService.addMedicalLine(conn, order, firstVaccine(conn), hours(-48));
        eq("da them mot dong", 1, BookingDAO.findLines(conn, order).size());

        System.out.println("\n-- Buoc 3: chot don va nhan ma tra cuu --");
        Booking confirmed = BookingService.confirm(conn, order);
        String code = confirmed.getLookupCode();
        check("khach vang lai duoc cap ma tra cuu", code != null && !code.isBlank(),
                "ma=" + code);
        eq("ma tra cuu dai 8 ky tu", 8, code == null ? 0 : code.length());
        check("da gui thong bao kem ma",
                NotificationDAO.existsForBooking(conn, order, "BOOKING_CONFIRMED"),
                "khong co thong bao");

        System.out.println("\n-- Buoc 4: tra cuu lai don --");
        Booking found = BookingService.lookupGuestBooking(conn, code, phone);
        eq("tra cuu ra dung don", order, found.getBookingId());
        check("go so dien thoai co dau cach van tra duoc",
                BookingService.lookupGuestBooking(conn, code, "0912 345 678")
                        .getBookingId() == order, "khong tra duoc");
        check("ma dung nhung sai so dien thoai thi khong ra",
                lookupFails(conn, code, "0900000000"), "van tra ra");
        check("sai ma thi khong ra", lookupFails(conn, "SAIMA123", phone), "van tra ra");
        check("thieu ma hoac so thi bao loi", lookupFails(conn, null, phone), "khong bao loi");

        System.out.println("\n-- Buoc 5: thanh toan --");
        BookingService.pay(conn, order);
        eq("don da thanh toan", Booking.STATUS_PAID,
                BookingDAO.findById(conn, order).getStatus());

        System.out.println("\n-- Buoc 6: gioi han cua khach vang lai --");
        eq("khong ghi so suc khoe vi khong co ho so be", 0,
                scalar(conn, "SELECT count(*) FROM pet_health_record WHERE booking_id = " + order));
        check("khong xoa duoc don cua khach vang lai bang tai khoan khac",
                deleteOrderFails(conn, order, userId), "xoa duoc");
    }

    /** Vài chỗ hai luồng gặp nhau, dễ sai nếu chỉ thử riêng từng luồng. */
    static void mixedFlow(Connection conn) throws Exception {
        System.out.println("\n===== CHO HAI LUONG GAP NHAU =====");

        System.out.println("\n-- Phong dung chung giua hai loai khach --");
        Timestamp in = hours(1000);
        Timestamp out = hours(1024);
        int total = totalRooms(conn, "vip3");

        List<Integer> orders = new ArrayList<>();
        for (int i = 0; i < total; i++) {
            int o = BookingService.startDraftForGuest(conn, "Khach " + i, "091234567" + i,
                    null, "Be " + i, "Chó");
            BookingService.addHotelLine(conn, o, "vip3", in, out);
            BookingService.confirm(conn, o);
            orders.add(o);
        }
        eq("khach vang lai lap day hang phong", 0, freeRooms(conn, "vip3", in, out));

        int pet = insertPet(conn, userId);
        int mine = BookingService.startDraftForUser(conn, userId, pet);
        check("khach dang nhap khong chen duoc vao hang da het",
                addRoomFails(conn, mine, "vip3", in, out), "van dat duoc");

        BookingService.cancel(conn, orders.get(0));
        eq("huy mot don khach vang lai thi trong ra mot phong", 1,
                freeRooms(conn, "vip3", in, out));
        check("gio khach dang nhap dat duoc",
                !addRoomFails(conn, mine, "vip3", in, out), "van bi chan");

        System.out.println("\n-- So suc khoe chi ghi cho be co ho so --");
        int hPet = insertPet(conn, userId);
        int hOrder = BookingService.startDraftForUser(conn, userId, hPet);
        BookingService.addMedicalLine(conn, hOrder, firstVaccine(conn), hours(-24));
        BookingService.confirm(conn, hOrder);
        BookingService.pay(conn, hOrder);
        check("thanh toan don y te thi ghi so", HealthRecordDAO.countByPet(conn, hPet) > 0,
                "so trong");
        check("muc vua ghi co ngay can lam lai",
                HealthRecordDAO.findByPet(conn, hPet).get(0).hasNextDue(), "khong co han");
    }

    // ----- tiện ích -----

    static int freeRooms(Connection c, String code, Timestamp in, Timestamp out)
            throws SQLException {
        for (RoomAvailability r : ServiceCatalogDAO.findAvailability(c, in, out, null)) {
            if (code.equals(r.getRoomCode())) return r.getFreeRooms();
        }
        return -1;
    }

    static int totalRooms(Connection c, String code) throws SQLException {
        return ServiceCatalogDAO.findRoomType(c, code).getTotalRooms();
    }

    static BigDecimal priceOf(Connection c, String code) throws SQLException {
        return ServiceCatalogDAO.findRoomType(c, code).getPricePerDay();
    }

    static List<Integer> firstSpa(Connection c) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        ids.add(scalar(c, "SELECT item_id FROM spa_service_item ORDER BY item_id LIMIT 1"));
        return ids;
    }

    static List<Integer> firstVaccine(Connection c) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        ids.add(scalar(c, "SELECT item_id FROM medical_service_item"
                + " WHERE category = " + q("VACCINE") + " ORDER BY item_id LIMIT 1"));
        return ids;
    }

    /** Dời một đợt lưu trú sang khoảng thời gian khác, để thử bước trả phòng. */
    static void shiftLine(Connection c, int bookingId, Timestamp in, Timestamp out)
            throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE booking_line SET start_at = ?, end_at = ?"
              + " WHERE booking_id = ? AND service_type = " + q("HOTEL"))) {
            ps.setTimestamp(1, in);
            ps.setTimestamp(2, out);
            ps.setInt(3, bookingId);
            ps.executeUpdate();
        }
    }

    static boolean payFails(Connection c, int id) {
        try { BookingService.pay(c, id); return false; } catch (Exception e) { return true; }
    }

    static boolean addRoomFails(Connection c, int id, String room, Timestamp in, Timestamp out) {
        try { BookingService.addHotelLine(c, id, room, in, out); return false; }
        catch (Exception e) { return true; }
    }

    static boolean lookupFails(Connection c, String code, String phone) {
        try { BookingService.lookupGuestBooking(c, code, phone); return false; }
        catch (Exception e) { return true; }
    }

    static boolean deleteePetFails(Connection c, int petId) {
        try { PetService.deleteOwned(c, petId, userId); return false; }
        catch (Exception e) { return true; }
    }

    static boolean deleteOrderFails(Connection c, int bookingId, int asUser) {
        try { BookingService.deleteFinished(c, bookingId, asUser); return false; }
        catch (Exception e) { return true; }
    }

    static String q(String s) {
        char quote = (char) 39;
        return quote + s + quote;
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
