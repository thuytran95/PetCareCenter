package com.petweb.test;

import com.petweb.dao.HealthRecordDAO;
import com.petweb.model.Booking;
import com.petweb.model.HealthCalendar;
import com.petweb.model.HealthRecord;
import com.petweb.service.BookingService;
import com.petweb.service.HealthRecordService;

import jakarta.servlet.annotation.WebServlet;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Kiểm thử tính năng sổ sức khỏe / sổ tiêm.
 * Chạy trên CSDL thật nhưng trong một giao dịch bị rollback ở cuối,
 * nên không để lại dấu vết nào.
 */
public class HealthRecordTest {

    static int pass = 0, fail = 0;

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

        // ---------- 0. Dữ liệu nền ----------
        System.out.println("\n-- Nhom 0: du lieu nen cua migration_v4 --");

        check("cot category ton tai tren medical_service_item",
                columnExists(conn, "medical_service_item", "category"), "khong thay cot");
        check("cot repeat_months ton tai tren medical_service_item",
                columnExists(conn, "medical_service_item", "repeat_months"), "khong thay cot");
        check("bang pet_health_record ton tai",
                tableExists(conn, "pet_health_record"), "khong thay bang");

        check("moi hang muc y te deu da duoc phan loai",
                scalarInt(conn, "SELECT count(*) FROM medical_service_item WHERE category IS NULL") == 0,
                "con hang muc chua co category");
        check("category chi nhan 4 gia tri hop le",
                scalarInt(conn, "SELECT count(*) FROM medical_service_item"
                        + " WHERE category NOT IN ('VACCINE','CHECKUP','DEWORM','OTHER')") == 0,
                "co gia tri la");

        int vaccineId = scalarInt(conn,
                "SELECT item_id FROM medical_service_item"
                + " WHERE category = 'VACCINE' AND repeat_months IS NOT NULL ORDER BY item_id LIMIT 1");
        check("co it nhat mot mui tiem co chu ky nhac lai", vaccineId > 0, "khong tim thay");

        int repeatMonths = scalarInt(conn,
                "SELECT repeat_months FROM medical_service_item WHERE item_id = " + vaccineId);
        check("chu ky nhac lai la so duong", repeatMonths > 0, "repeat_months=" + repeatMonths);

        // ---------- 1. computeNextDue ----------
        System.out.println("\n-- Nhom 1: tinh ngay can lam lai --");

        Timestamp base = Timestamp.valueOf(LocalDateTime.of(2026, 1, 15, 9, 0));
        eq("cong 12 thang",
                Timestamp.valueOf(LocalDateTime.of(2027, 1, 15, 9, 0)),
                HealthRecordService.computeNextDue(base, 12));
        eq("cong 3 thang",
                Timestamp.valueOf(LocalDateTime.of(2026, 4, 15, 9, 0)),
                HealthRecordService.computeNextDue(base, 3));
        eq("chu ky null -> khong co han", null,
                HealthRecordService.computeNextDue(base, null));
        eq("chu ky 0 -> khong co han", null,
                HealthRecordService.computeNextDue(base, 0));
        eq("ngay thuc hien null -> khong co han", null,
                HealthRecordService.computeNextDue(null, 12));
        eq("cuoi thang khong tran sang thang sau",
                Timestamp.valueOf(LocalDateTime.of(2026, 2, 28, 8, 0)),
                HealthRecordService.computeNextDue(
                        Timestamp.valueOf(LocalDateTime.of(2026, 1, 31, 8, 0)), 1));

        // ---------- 2. Ghi sổ khi thanh toán ----------
        System.out.println("\n-- Nhom 2: thanh toan don y te thi tu ghi so --");

        int userId = scalarInt(conn, "SELECT id FROM user_account ORDER BY id LIMIT 1");
        // Tao rieng mot thu cung cho lan chay nay. Neu dung thu cung co san thi
        // ket qua phu thuoc vao so suc khoe that cua no: chi can no da co mot ban
        // ghi cung hang muc voi ngay thuc hien moi hon la truy van nhac han se
        // chon ban ghi kia, va bai kiem thu that bai du tinh nang van dung.
        int petId = insertScratchPet(conn, userId);
        check("tao duoc thu cung rieng de thu", userId > 0 && petId > 0,
                "userId=" + userId + " petId=" + petId);
        eq("thu cung moi tao co so suc khoe trong", 0, HealthRecordDAO.countByPet(conn, petId));

        int before = HealthRecordDAO.countByPet(conn, petId);

        int bookingId = BookingService.startDraftForUser(conn, userId, petId);
        Timestamp admission = Timestamp.valueOf(LocalDateTime.now().withNano(0));
        List<Integer> items = new ArrayList<>();
        items.add(vaccineId);
        BookingService.addMedicalLine(conn, bookingId, items, admission);
        BookingService.confirm(conn, bookingId);

        check("chua thanh toan thi chua ghi so",
                HealthRecordDAO.countByPet(conn, petId) == before,
                "so da co them dong truoc khi thanh toan");

        Booking paid = BookingService.pay(conn, bookingId);
        eq("don chuyen sang PAID", Booking.STATUS_PAID, paid.getStatus());

        int after = HealthRecordDAO.countByPet(conn, petId);
        eq("thanh toan ghi them dung 1 dong vao so", before + 1, after);

        List<HealthRecord> recs = HealthRecordDAO.findByPet(conn, petId);
        HealthRecord newest = null;
        for (HealthRecord r : recs) {
            if (r.getBookingId() != null && r.getBookingId() == bookingId) { newest = r; break; }
        }
        check("tim duoc dong vua ghi", newest != null, "khong thay dong cua don #" + bookingId);

        if (newest != null) {
            eq("dong so tro dung thu cung", petId, newest.getPetId());
            eq("phan loai la VACCINE", HealthRecord.TYPE_VACCINE, newest.getRecordType());
            eq("hang muc dung", (Integer) vaccineId, newest.getItemId());
            check("co ten hang muc", newest.getItemName() != null && !newest.getItemName().isBlank(),
                    "ten rong");
            eq("ngay thuc hien = ngay nhap vien", admission, newest.getPerformedAt());
            check("co ngay can lam lai", newest.hasNextDue(), "next_due_at null");
            eq("ngay lam lai = ngay thuc hien + chu ky",
                    HealthRecordService.computeNextDue(admission, repeatMonths),
                    newest.getNextDueAt());
            check("chua toi han nen khong qua han", !newest.isOverdue(),
                    "bi coi la qua han: " + newest.getFormattedNextDueAt());
            check("nhan loai hien thi duoc",
                    newest.getTypeLabel() != null && !newest.getTypeLabel().isBlank(), "nhan rong");
            check("co ten bieu tuong va mau",
                    newest.getIconClass() != null && newest.getColorName() != null, "thieu");
        }

        // ---------- 3. Không sinh dòng trùng ----------
        System.out.println("\n-- Nhom 3: goi lai khong sinh ban ghi trung --");

        int again = HealthRecordService.recordFromBooking(conn, paid);
        eq("ghi lai lan 2 khong them dong nao", 0, again);
        eq("so dong trong so giu nguyen", after, HealthRecordDAO.countByPet(conn, petId));

        // ---------- 4. Khách vãng lai ----------
        System.out.println("\n-- Nhom 4: khach vang lai khong co so --");

        int gId = BookingService.startDraftForGuest(conn, "Khach Thu", "0912345678",
                null, "Be Thu", "Cho");
        List<Integer> gItems = new ArrayList<>();
        gItems.add(vaccineId);
        BookingService.addMedicalLine(conn, gId, gItems, admission);
        BookingService.confirm(conn, gId);
        Booking gPaid = BookingService.pay(conn, gId);

        check("don khach vang lai khong gan thu cung", gPaid.getPetId() == null,
                "petId=" + gPaid.getPetId());
        eq("khong ghi so cho don khong co ho so thu cung", 0,
                HealthRecordService.recordFromBooking(conn, gPaid));
        eq("recordFromBooking(null) tra ve 0", 0,
                HealthRecordService.recordFromBooking(conn, null));

        // ---------- 5. Nhắc hạn trên trang chủ ----------
        System.out.println("\n-- Nhom 5: truy van nhac han cho trang chu --");

        // Kéo hạn của dòng vừa ghi về gần hôm nay để nó phải xuất hiện trong danh sách nhắc
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE pet_health_record SET next_due_at = now() + interval '3 days'"
                + " WHERE booking_id = ?")) {
            ps.setInt(1, bookingId);
            ps.executeUpdate();
        }

        List<HealthRecord> due = HealthRecordDAO.findUpcomingDueByOwner(conn, userId, 30, 10);
        boolean found = false;
        for (HealthRecord r : due) {
            if (r.getBookingId() != null && r.getBookingId() == bookingId) { found = true; break; }
        }
        check("muc sap toi han xuat hien trong danh sach nhac", found, "khong thay");
        check("moi muc nhac deu kem ten thu cung",
                due.stream().allMatch(r -> r.getPetName() != null && !r.getPetName().isBlank()),
                "co muc thieu ten be");
        check("moi muc nhac deu co han", due.stream().allMatch(HealthRecord::hasNextDue),
                "co muc khong co han");

        List<HealthRecord> narrow = HealthRecordDAO.findUpcomingDueByOwner(conn, userId, 1, 10);
        check("thu hep cua so nhac thi bo muc con 3 ngay nua",
                narrow.stream().noneMatch(r -> r.getBookingId() != null && r.getBookingId() == bookingId),
                "van con trong danh sach");

        check("gioi han so dong co hieu luc",
                HealthRecordDAO.findUpcomingDueByOwner(conn, userId, 3650, 1).size() <= 1,
                "tra ve nhieu hon gioi han");

        check("nguoi dung khac khong thay muc cua be nay",
                HealthRecordDAO.findUpcomingDueByOwner(conn, -1, 3650, 10).isEmpty(),
                "lo du lieu sang tai khoan khac");

        // ---------- 6. Quá hạn ----------
        System.out.println("\n-- Nhom 6: nhan biet qua han --");

        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE pet_health_record SET next_due_at = now() - interval '10 days'"
                + " WHERE booking_id = ?")) {
            ps.setInt(1, bookingId);
            ps.executeUpdate();
        }
        HealthRecord late = null;
        for (HealthRecord r : HealthRecordDAO.findByPet(conn, petId)) {
            if (r.getBookingId() != null && r.getBookingId() == bookingId) { late = r; break; }
        }
        check("dong qua han bi danh dau qua han", late != null && late.isOverdue(),
                "khong nhan ra qua han");
        check("chu hien thi noi ro da qua han",
                late != null && late.getDueText() != null
                        && late.getDueText().toLowerCase().contains("quá"),
                "dueText=" + (late == null ? "null" : late.getDueText()));
        check("muc qua han van nam trong danh sach nhac",
                HealthRecordDAO.findUpcomingDueByOwner(conn, userId, 0, 10).stream()
                        .anyMatch(r -> r.getBookingId() != null && r.getBookingId() == bookingId),
                "muc qua han bi bo sot");

        // ---------- 7. Lọc theo loại ----------
        System.out.println("\n-- Nhom 7: loc theo loai --");

        check("loc VACCINE tra ve dung loai",
                HealthRecordDAO.findByPetAndType(conn, petId, HealthRecord.TYPE_VACCINE).stream()
                        .allMatch(r -> HealthRecord.TYPE_VACCINE.equals(r.getRecordType())),
                "lan loai khac");
        check("loc CHECKUP khong tra ve mui tiem",
                HealthRecordDAO.findByPetAndType(conn, petId, HealthRecord.TYPE_CHECKUP).stream()
                        .noneMatch(r -> HealthRecord.TYPE_VACCINE.equals(r.getRecordType())),
                "lan mui tiem");
        check("thu cung khong ton tai thi so rong",
                HealthRecordDAO.findByPet(conn, -1).isEmpty(), "tra ve du lieu la");
        eq("dem so cua thu cung khong ton tai", 0, HealthRecordDAO.countByPet(conn, -1));

        // ---------- 8. Lịch tháng ----------
        System.out.println("\n-- Nhom 8: lich thang --");
        checkCalendar(HealthRecordDAO.findByPet(conn, petId));

        // ---------- 9. Định tuyến ----------
        System.out.println("\n-- Nhom 9: dinh tuyen trang co doc CSDL --");
        checkRouting();
    }

    /**
     * JDBCFilter chỉ mở kết nối CSDL cho URL có servlet đăng ký. Trang nào cần
     * đọc dữ liệu mà lại vào bằng đường dẫn ".jsp" thì sẽ im lặng mất dữ liệu,
     * nên ở đây kiểm tra các trang đó đều có ánh xạ servlet đúng.
     */
    static void checkRouting() {
        checkMapping("com.petweb.controller.ChooseServiceServlet", "/chooseService");
        checkMapping("com.petweb.controller.PetHealthServlet", "/petHealth");
        checkMapping("com.petweb.controller.HomeController", "");
        checkMapping("com.petweb.controller.BookingLookupServlet", "/lookup");
    }

    static void checkMapping(String className, String url) {
        try {
            Class<?> c = Class.forName(className);
            WebServlet ann = c.getAnnotation(WebServlet.class);
            if (ann == null) {
                check(className + " co @WebServlet", false, "thieu chu thich");
                return;
            }
            List<String> urls = new ArrayList<>();
            urls.addAll(Arrays.asList(ann.value()));
            urls.addAll(Arrays.asList(ann.urlPatterns()));
            check(className.substring(className.lastIndexOf('.') + 1) + " anh xa " + url,
                    urls.contains(url), "cac anh xa hien co: " + urls);
        } catch (ClassNotFoundException e) {
            check(className + " ton tai", false, "khong tim thay lop");
        }
    }

    /** Kiểm tra lưới lịch dựng đúng ngày tháng và gắn đúng bản ghi vào ô. */
    static void checkCalendar(List<HealthRecord> recs) {

        // Tháng 8/2026 bắt đầu vào thứ Bảy -> 5 o dem, tong 5 + 31 = 36 o
        YearMonth aug = YearMonth.of(2026, 8);
        HealthCalendar cal = HealthCalendar.build(aug, recs);
        eq("thang 8/2026 co dung so o", 5 + 31, cal.getDays().size());

        long blanks = cal.getDays().stream().filter(HealthCalendar.Day::isBlank).count();
        eq("dem dung 5 o trong dau thang", 5L, blanks);

        // Tháng 2/2026 bắt đầu vào Chủ Nhật -> 6 ô đệm, 28 ngày
        HealthCalendar feb = HealthCalendar.build(YearMonth.of(2026, 2), recs);
        eq("thang 2/2026 co dung so o", 6 + 28, feb.getDays().size());
        eq("thang 2/2026 co 28 ngay",
                28L, feb.getDays().stream().filter(d -> !d.isBlank()).count());

        // Tháng nhuận
        eq("thang 2/2028 co 29 ngay", 29L,
                HealthCalendar.build(YearMonth.of(2028, 2), recs).getDays().stream()
                        .filter(d -> !d.isBlank()).count());

        eq("nhan thang hien thi dung", "Tháng 8 / 2026", cal.getLabel());
        eq("ma thang dung dinh dang URL", "2026-08", cal.getYm());
        eq("lui mot thang", "2026-07", cal.getPrevYm());
        eq("tien mot thang", "2026-09", cal.getNextYm());
        eq("sang nam moi tu thang 12", "2027-01",
                HealthCalendar.build(YearMonth.of(2026, 12), recs).getNextYm());

        check("hang tieu de co 7 cot", HealthCalendar.WEEKDAY_LABELS.length == 7,
                "so cot=" + HealthCalendar.WEEKDAY_LABELS.length);
        eq("cot dau tien la Thu Hai", "T2", HealthCalendar.WEEKDAY_LABELS[0]);
        eq("cot cuoi cung la Chu Nhat", "CN", HealthCalendar.WEEKDAY_LABELS[6]);

        // Ngày thực hiện của các bản ghi phải rơi đúng vào ô ngày của nó
        boolean placedRight = true;
        int placed = 0;
        for (HealthRecord r : recs) {
            if (r.getPerformedAt() == null) continue;
            LocalDate d = r.getPerformedAt().toLocalDateTime().toLocalDate();
            HealthCalendar c = HealthCalendar.build(YearMonth.from(d), recs);
            HealthCalendar.Day cell = null;
            for (HealthCalendar.Day x : c.getDays()) {
                if (!x.isBlank() && x.getDayOfMonth() == d.getDayOfMonth()) { cell = x; break; }
            }
            if (cell == null || !cell.getDone().contains(r)) placedRight = false;
            else placed++;
        }
        check("moi ban ghi roi dung o ngay da thuc hien", placedRight,
                "co ban ghi bi dat sai o");
        check("da dat duoc it nhat mot ban ghi vao lich", placed > 0,
                "khong co ban ghi nao duoc dat");

        // Tháng không có gì thì lịch rỗng nhưng vẫn dựng được lưới
        HealthCalendar far = HealthCalendar.build(YearMonth.of(1990, 1), recs);
        check("thang khong co du lieu duoc coi la rong", far.isEmpty(), "bao la co du lieu");
        eq("thang rong van dem duoc so lan da lam", 0, far.getDoneCount());
        eq("thang rong van dem duoc so muc den han", 0, far.getDueCount());
        check("thang rong van co du o ngay",
                far.getDays().stream().filter(d -> !d.isBlank()).count() == 31,
                "thieu o ngay");
        check("thang rong khong o nao duoc danh dau",
                far.getDays().stream().noneMatch(HealthCalendar.Day::hasAnything),
                "co o bi danh dau");

        // Không có bản ghi nào thì vẫn không nổ
        HealthCalendar none = HealthCalendar.build(aug, null);
        check("danh sach null van dung duoc lich", none.isEmpty() && none.getDays().size() == 36,
                "lich hong khi khong co du lieu");

        // Ô đệm không được coi là hôm nay hay cuối tuần
        HealthCalendar.Day blank = cal.getDays().get(0);
        check("o dem khong phai ngay that", blank.isBlank(), "bi coi la ngay that");
        check("o dem khong phai hom nay", !blank.isToday(), "bi coi la hom nay");
        eq("o dem khong co so ngay", 0, blank.getDayOfMonth());
        eq("o dem khong co ghi chu", "", blank.getTooltip());

        // Đúng một ô được đánh dấu là hôm nay, và chỉ ở tháng hiện tại
        HealthCalendar thisMonth = HealthCalendar.build(YearMonth.now(), recs);
        eq("thang hien tai co dung mot o hom nay", 1L,
                thisMonth.getDays().stream().filter(HealthCalendar.Day::isToday).count());
        eq("thang khac khong co o hom nay", 0L,
                far.getDays().stream().filter(HealthCalendar.Day::isToday).count());
    }

    // ----- tiện ích -----

    /**
     * Tạo một thú cưng tạm cho chủ đã cho, chỉ sống trong giao dịch của lần chạy
     * này rồi bị rollback. Nhờ vậy bài kiểm thử không dựa vào hồ sơ có sẵn trong
     * CSDL và cho kết quả như nhau ở mọi máy.
     */
    static int insertScratchPet(Connection c, int userId) throws SQLException {
        String sql = "INSERT INTO pet (name, species, user_id) VALUES (?,?,?) RETURNING pet_id";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "Be Kiem Thu");
            ps.setString(2, "Chó");
            ps.setInt(3, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    static boolean tableExists(Connection c, String t) throws SQLException {
        return scalarInt(c, "SELECT count(*) FROM information_schema.tables"
                + " WHERE table_name = '" + t + "'") > 0;
    }

    static boolean columnExists(Connection c, String t, String col) throws SQLException {
        return scalarInt(c, "SELECT count(*) FROM information_schema.columns"
                + " WHERE table_name = '" + t + "' AND column_name = '" + col + "'") > 0;
    }

    static int scalarInt(Connection c, String sql) throws SQLException {
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : -1;
        }
    }
}
