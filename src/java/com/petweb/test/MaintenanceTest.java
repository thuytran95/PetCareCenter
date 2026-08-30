package com.petweb.test;

import com.petweb.dao.BookingDAO;
import com.petweb.model.Booking;
import com.petweb.service.BookingService;
import com.petweb.service.MaintenanceService;

import java.sql.*;
import java.time.LocalDateTime;

/**
 * Kiểm thử các tác vụ nền tự dọn dẹp và tự chuyển trạng thái.
 *
 * Chạy trên CSDL thật nhưng trong một giao dịch bị rollback ở cuối. Mỗi lần chạy
 * tự dựng lấy dữ liệu cần thiết nên không phụ thuộc vào những gì đang có sẵn.
 *
 * Điều quan trọng nhất được khẳng định ở đây: tác vụ nền KHÔNG xóa hóa đơn.
 */
public class MaintenanceTest {

    static int pass = 0, fail = 0;
    static int userId, petId;

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

        userId = scalarInt(conn, "SELECT id FROM user_account ORDER BY id LIMIT 1");
        petId = insertPet(conn, userId);
        check("co du lieu nen de thu", userId > 0 && petId > 0,
                "userId=" + userId + " petId=" + petId);

        // ---------- 1. Dùng lại đơn nháp rỗng ----------
        System.out.println("\n-- Nhom 1: khong de sinh don nhap rong hang loat --");

        int before = countDrafts(conn, petId);
        int first = BookingService.startDraftForUser(conn, userId, petId);
        eq("bam lan dau tao dung mot don nhap", before + 1, countDrafts(conn, petId));

        int second = BookingService.startDraftForUser(conn, userId, petId);
        eq("bam lan hai dung lai don cu", first, second);
        eq("khong sinh them dong nao", before + 1, countDrafts(conn, petId));

        for (int i = 0; i < 5; i++) BookingService.startDraftForUser(conn, userId, petId);
        eq("bam nhieu lan van chi mot don", before + 1, countDrafts(conn, petId));

        // Đơn đã có dịch vụ thì KHÔNG được dùng lại — nó là đơn đang dở của khách
        BookingService.addSpaLine(conn, first, firstSpaItems(conn), soon());
        int third = BookingService.startDraftForUser(conn, userId, petId);
        check("don da co dich vu thi mo don moi chu khong ghi de", third != first,
                "lai dung lai don dang co dich vu");

        // Bé khác thì có đơn riêng
        int otherPet = insertPet(conn, userId);
        int forOther = BookingService.startDraftForUser(conn, userId, otherPet);
        check("moi be co don rieng", forOther != third, "dung chung don giua hai be");

        // ---------- 2. Dọn đơn nháp rỗng ----------
        System.out.println("\n-- Nhom 2: don nhap rong duoc don som --");

        int emptyOld = BookingService.startDraftForUser(conn, userId, insertPet(conn, userId));
        age(conn, emptyOld, MaintenanceService.EMPTY_DRAFT_HOURS + 1);
        check("don rong qua han van con truoc khi quet", exists(conn, emptyOld), "da bien mat");

        MaintenanceService.cleanExpiredDrafts(conn);
        check("don rong qua han bi xoa", !exists(conn, emptyOld), "van con");

        int emptyFresh = BookingService.startDraftForUser(conn, userId, insertPet(conn, userId));
        age(conn, emptyFresh, MaintenanceService.EMPTY_DRAFT_HOURS - 1);
        MaintenanceService.cleanExpiredDrafts(conn);
        check("don rong chua qua han thi giu lai", exists(conn, emptyFresh), "bi xoa som");

        // ---------- 3. Đơn nháp đã chọn dịch vụ được giữ lâu hơn ----------
        System.out.println("\n-- Nhom 3: don nhap da chon dich vu duoc giu lau hon --");

        int started = BookingService.startDraftForUser(conn, userId, insertPet(conn, userId));
        BookingService.addSpaLine(conn, started, firstSpaItems(conn), soon());
        age(conn, started, MaintenanceService.EMPTY_DRAFT_HOURS + 1);
        MaintenanceService.cleanExpiredDrafts(conn);
        check("don da chon dich vu khong bi don theo moc don rong",
                exists(conn, started), "bi xoa som");

        age(conn, started, BookingService.DRAFT_EXPIRE_HOURS + 1);
        MaintenanceService.cleanExpiredDrafts(conn);
        check("qua 24 gio thi don di", !exists(conn, started), "van con");

        // ---------- 4. Hủy khách không đến ----------
        System.out.println("\n-- Nhom 4: huy khach khong den --");

        int noShow = hotelBooking(conn, ts(-3), ts(2));
        BookingService.confirm(conn, noShow);
        MaintenanceService.cancelNoShows(conn);
        eq("qua gio nhan phong chua tra tien thi bi huy",
                Booking.STATUS_CANCELLED, statusOf(conn, noShow));

        int paidLate = hotelBooking(conn, ts(-3), ts(2));
        BookingService.confirm(conn, paidLate);
        BookingService.pay(conn, paidLate);
        MaintenanceService.cancelNoShows(conn);
        eq("don DA THANH TOAN khong bao gio bi huy kieu nay",
                Booking.STATUS_PAID, statusOf(conn, paidLate));

        int inGrace = hotelBooking(conn, ts(0), ts(3));
        BookingService.confirm(conn, inGrace);
        MaintenanceService.cancelNoShows(conn);
        eq("con trong thoi gian an han thi chua huy",
                Booking.STATUS_CONFIRMED, statusOf(conn, inGrace));

        // ---------- 5. Đóng đơn đã phục vụ xong ----------
        System.out.println("\n-- Nhom 5: dong don da phuc vu xong --");

        int done = hotelBooking(conn, ts(-5), ts(-2));
        BookingService.confirm(conn, done);
        BookingService.pay(conn, done);
        MaintenanceService.markCompleted(conn);
        eq("da o xong thi dong don", Booking.STATUS_COMPLETED, statusOf(conn, done));

        int stillHere = hotelBooking(conn, ts(-1), ts(5));
        BookingService.confirm(conn, stillHere);
        BookingService.pay(conn, stillHere);
        MaintenanceService.markCompleted(conn);
        eq("dang o thi chua dong don", Booking.STATUS_PAID, statusOf(conn, stillHere));

        // Đơn rỗng không được coi là "đã phục vụ xong"
        int emptyConfirmed = BookingService.startDraftForUser(conn, userId, insertPet(conn, userId));
        MaintenanceService.markCompleted(conn);
        eq("don rong khong bi coi la da phuc vu xong",
                Booking.STATUS_DRAFT, statusOf(conn, emptyConfirmed));

        // ---------- 6. Hóa đơn không bao giờ bị xóa ----------
        System.out.println("\n-- Nhom 6: hoa don khong bao gio bi xoa --");

        int invoice = hotelBooking(conn, ts(-9), ts(-8));
        BookingService.confirm(conn, invoice);
        BookingService.pay(conn, invoice);
        MaintenanceService.markCompleted(conn);
        age(conn, invoice, 24 * 365);

        MaintenanceService.cleanExpiredDrafts(conn);
        MaintenanceService.cleanOldNotifications(conn);
        check("hoa don da hoan tat van con nguyen du rat cu",
                exists(conn, invoice), "bi xoa mat");
        check("cac dong dich vu cua hoa don van con",
                !BookingDAO.findLines(conn, invoice).isEmpty(), "mat dong dich vu");

        int killed = hotelBooking(conn, ts(80), ts(83));
        BookingService.confirm(conn, killed);
        BookingService.cancel(conn, killed);
        age(conn, killed, 24 * 365);
        MaintenanceService.cleanExpiredDrafts(conn);
        check("don da huy cung khong bi xoa", exists(conn, killed), "bi xoa mat");

        // ---------- 7. Dọn nhật ký thông báo ----------
        System.out.println("\n-- Nhom 7: don nhat ky thong bao --");

        int keep = hotelBooking(conn, ts(90), ts(93));
        BookingService.confirm(conn, keep);
        int notiBefore = countNotifications(conn, keep);
        check("chot don co ghi lai thong bao", notiBefore > 0, "khong co thong bao nao");

        MaintenanceService.cleanOldNotifications(conn);
        eq("thong bao moi khong bi dong vao", notiBefore, countNotifications(conn, keep));

        ageNotifications(conn, keep, MaintenanceService.NOTIFICATION_KEEP_DAYS + 1);
        MaintenanceService.cleanOldNotifications(conn);
        eq("thong bao qua cu bi xoa", 0, countNotifications(conn, keep));
        check("xoa thong bao khong lam mat don", exists(conn, keep), "don bien mat theo");
    }

    // ----- tiện ích -----

    static int hotelBooking(Connection conn, Timestamp in, Timestamp out) throws Exception {
        int id = BookingService.startDraftForUser(conn, userId, insertPet(conn, userId));
        BookingService.addHotelLine(conn, id, "vip1", in, out);
        return id;
    }

    static java.util.List<Integer> firstSpaItems(Connection conn) throws SQLException {
        java.util.List<Integer> ids = new java.util.ArrayList<>();
        ids.add(scalarInt(conn, "SELECT item_id FROM spa_service_item ORDER BY item_id LIMIT 1"));
        return ids;
    }

    static Timestamp soon() {
        return ts(60);
    }

    static Timestamp ts(int daysFromNow) {
        return Timestamp.valueOf(LocalDateTime.now().plusDays(daysFromNow)
                .withHour(9).withMinute(0).withSecond(0).withNano(0));
    }

    /** Đẩy ngày tạo của đơn lùi lại để giả lập đơn đã nằm đó lâu. */
    static void age(Connection c, int bookingId, int hours) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE booking SET created_at = now() - (? || ' hours')::interval"
              + " WHERE booking_id = ?")) {
            ps.setString(1, String.valueOf(hours));
            ps.setInt(2, bookingId);
            ps.executeUpdate();
        }
    }

    static void ageNotifications(Connection c, int bookingId, int days) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE notification SET created_at = now() - (? || ' days')::interval"
              + " WHERE booking_id = ?")) {
            ps.setString(1, String.valueOf(days));
            ps.setInt(2, bookingId);
            ps.executeUpdate();
        }
    }

    static boolean exists(Connection c, int bookingId) throws SQLException {
        return scalarInt(c, "SELECT count(*) FROM booking WHERE booking_id = " + bookingId) > 0;
    }

    static String statusOf(Connection c, int bookingId) throws SQLException {
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT status FROM booking WHERE booking_id = " + bookingId)) {
            return rs.next() ? rs.getString(1) : null;
        }
    }

    static int countDrafts(Connection c, int pet) throws SQLException {
        return scalarInt(c, "SELECT count(*) FROM booking"
                + " WHERE status = 'DRAFT' AND pet_id = " + pet);
    }

    static int countNotifications(Connection c, int bookingId) throws SQLException {
        return scalarInt(c, "SELECT count(*) FROM notification WHERE booking_id = " + bookingId);
    }

    /** Thú cưng tạm, chỉ sống trong giao dịch của lần chạy này. */
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

    static int scalarInt(Connection c, String sql) throws SQLException {
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : -1;
        }
    }
}
