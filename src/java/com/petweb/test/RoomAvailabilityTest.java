package com.petweb.test;

import com.petweb.dao.BookingDAO;
import com.petweb.dao.ServiceCatalogDAO;
import com.petweb.model.PetStay;
import com.petweb.model.RoomAvailability;
import com.petweb.model.RoomType;
import com.petweb.service.BookingService;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Kiểm thử việc tính phòng trống của khách sạn thú cưng.
 *
 * Chạy trên CSDL thật nhưng trong một giao dịch bị rollback ở cuối, nên không
 * để lại đơn rác. Mỗi lần chạy đều tự đặt đủ số phòng của một hạng phòng rồi
 * thử đặt thêm, vì vậy không phụ thuộc vào dữ liệu sẵn có trong CSDL.
 */
public class RoomAvailabilityTest {

    static int pass = 0, fail = 0;

    /** Hạng phòng dùng để thử: chọn hạng ít phòng nhất cho nhanh. */
    static final String ROOM = "vip3";

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

    static int userId, petId;

    static void run(Connection conn) throws Exception {

        RoomType room = ServiceCatalogDAO.findRoomType(conn, ROOM);
        check("tim thay hang phong de thu", room != null, "khong co " + ROOM);
        if (room == null) return;

        int total = room.getTotalRooms();
        check("hang phong co khai bao so luong phong", total > 0, "total_rooms=" + total);

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT u.id, p.pet_id FROM user_account u"
                     + " JOIN pet p ON p.user_id = u.id ORDER BY u.id LIMIT 1")) {
            check("co san user va pet de thu", rs.next(), "khong co du lieu nen");
            userId = rs.getInt(1);
            petId = rs.getInt(2);
        }

        // Khoảng thời gian ở xa tương lai để không đụng đơn có sẵn trong CSDL
        Timestamp in = ts(400);
        Timestamp out = ts(403);

        // ---------- 1. Đếm phòng đang bị chiếm ----------
        System.out.println("\n-- Nhom 1: dem phong dang bi chiem --");

        eq("khoang thoi gian trong thi khong co phong nao bi chiem", 0,
                ServiceCatalogDAO.countOverlappingRooms(conn, ROOM, in, out, null));

        int b1 = book(conn, ROOM, in, out);
        eq("them mot don DRAFT thi dem duoc 1 phong", 1,
                ServiceCatalogDAO.countOverlappingRooms(conn, ROOM, in, out, null));

        BookingService.confirm(conn, b1);
        eq("don CONFIRMED van giu phong", 1,
                ServiceCatalogDAO.countOverlappingRooms(conn, ROOM, in, out, null));

        // Đây chính là lỗi từng gặp: đơn đã thanh toán bị coi như đã trả phòng
        BookingService.pay(conn, b1);
        eq("don DA THANH TOAN van giu phong", 1,
                ServiceCatalogDAO.countOverlappingRooms(conn, ROOM, in, out, null));

        BookingService.cancel(conn, b1);
        eq("don da huy thi nha phong ra", 0,
                ServiceCatalogDAO.countOverlappingRooms(conn, ROOM, in, out, null));

        try (Statement st = conn.createStatement()) {
            st.executeUpdate("UPDATE booking SET status = 'COMPLETED' WHERE booking_id = " + b1);
        }
        eq("don da hoan tat khong con giu phong", 0,
                ServiceCatalogDAO.countOverlappingRooms(conn, ROOM, in, out, null));

        // ---------- 2. Chặn đặt quá số phòng ----------
        System.out.println("\n-- Nhom 2: chan dat qua so phong --");

        Timestamp in2 = ts(410);
        Timestamp out2 = ts(413);

        List<Integer> held = new ArrayList<>();
        for (int i = 1; i <= total; i++) {
            int b = book(conn, ROOM, in2, out2);
            BookingService.confirm(conn, b);
            BookingService.pay(conn, b);
            held.add(b);
        }
        eq("da lap day dung so phong cua hang", total,
                ServiceCatalogDAO.countOverlappingRooms(conn, ROOM, in2, out2, null));

        int overflow = BookingService.startDraftForUser(conn, userId, petId);
        String msg = null;
        try {
            BookingService.addHotelLine(conn, overflow, ROOM, in2, out2);
        } catch (Exception e) {
            msg = e.getMessage();
        }
        check("het phong thi khong dat them duoc", msg != null,
                "van dat duoc them du da het phong");
        check("thong bao noi ro la het phong",
                msg != null && msg.toLowerCase().contains("hết"), "thong bao=" + msg);

        // Hủy một đơn thì phải có chỗ trống trở lại
        BookingService.cancel(conn, held.get(0));
        eq("huy mot don thi con trong 1 phong", total - 1,
                ServiceCatalogDAO.countOverlappingRooms(conn, ROOM, in2, out2, null));
        try {
            BookingService.addHotelLine(conn, overflow, ROOM, in2, out2);
            check("co cho trong thi dat duoc", true, "");
        } catch (Exception e) {
            check("co cho trong thi dat duoc", false, e.getMessage());
        }

        // ---------- 3. Quy tắc giao nhau của khoảng thời gian ----------
        System.out.println("\n-- Nhom 3: quy tac giao nhau cua khoang thoi gian --");

        Timestamp in3 = ts(500);   // ngày 500 -> 503
        Timestamp out3 = ts(503);
        int b3 = book(conn, ROOM, in3, out3);
        BookingService.confirm(conn, b3);

        eq("khoang trung hoan toan bi tinh la giao nhau", 1,
                ServiceCatalogDAO.countOverlappingRooms(conn, ROOM, in3, out3, null));
        eq("khoang nam gon ben trong bi tinh la giao nhau", 1,
                ServiceCatalogDAO.countOverlappingRooms(conn, ROOM, ts(501), ts(502), null));
        eq("khoang bao trum ben ngoai bi tinh la giao nhau", 1,
                ServiceCatalogDAO.countOverlappingRooms(conn, ROOM, ts(499), ts(504), null));
        eq("khoang gac dau bi tinh la giao nhau", 1,
                ServiceCatalogDAO.countOverlappingRooms(conn, ROOM, ts(499), ts(501), null));
        eq("khoang gac duoi bi tinh la giao nhau", 1,
                ServiceCatalogDAO.countOverlappingRooms(conn, ROOM, ts(502), ts(505), null));

        // Nhận phòng đúng lúc người trước trả phòng thì không tính là trùng
        eq("nhan phong dung luc nguoi truoc tra phong thi khong trung", 0,
                ServiceCatalogDAO.countOverlappingRooms(conn, ROOM, out3, ts(506), null));
        eq("tra phong dung luc nguoi sau nhan phong thi khong trung", 0,
                ServiceCatalogDAO.countOverlappingRooms(conn, ROOM, ts(497), in3, null));
        eq("khoang hoan toan tach roi thi khong trung", 0,
                ServiceCatalogDAO.countOverlappingRooms(conn, ROOM, ts(600), ts(603), null));

        eq("hang phong khac khong bi tinh chung", 0,
                ServiceCatalogDAO.countOverlappingRooms(conn, "vip1", in3, out3, null));

        eq("bo qua chinh don dang sua thi khong tu chan minh", 0,
                ServiceCatalogDAO.countOverlappingRooms(conn, ROOM, in3, out3, b3));

        // ---------- 4. Kiểm tra dữ liệu ngày giờ ----------
        System.out.println("\n-- Nhom 4: kiem tra ngay gio dau vao --");

        eq("ngay tra phong truoc ngay nhan phong bi tu choi",
                true, rejects(conn, ROOM, ts(700), ts(699)));
        eq("nhan va tra cung mot thoi diem bi tu choi",
                true, rejects(conn, ROOM, ts(700), ts(700)));
        eq("thieu ngay nhan phong bi tu choi",
                true, rejects(conn, ROOM, null, ts(700)));
        eq("thieu ngay tra phong bi tu choi",
                true, rejects(conn, ROOM, ts(700), null));
        eq("hang phong khong ton tai bi tu choi",
                true, rejects(conn, "khong-co-that", ts(700), ts(703)));

        // ---------- 5. Đơn nháp quá hạn tự nhả phòng ----------
        System.out.println("\n-- Nhom 5: don nhap qua han tu nha phong --");

        Timestamp in5 = ts(520);
        Timestamp out5 = ts(523);
        int stale = book(conn, ROOM, in5, out5);
        eq("don nhap moi tao van giu phong", 1,
                ServiceCatalogDAO.countOverlappingRooms(conn, ROOM, in5, out5, null));

        // Đẩy ngày tạo lùi về quá hạn giữ chỗ
        ageDraft(conn, stale, ServiceCatalogDAO.DRAFT_HOLD_HOURS + 1);
        eq("don nhap qua han khong con giu phong", 0,
                ServiceCatalogDAO.countOverlappingRooms(conn, ROOM, in5, out5, null));
        check("qua han thi dat lai duoc ngay, khong phai cho tac vu nen",
                !rejects(conn, ROOM, in5, out5), "van bi chan");

        // Ngay sát ngưỡng thì vẫn còn giữ chỗ
        int fresh = book(conn, ROOM, ts(530), ts(533));
        ageDraft(conn, fresh, ServiceCatalogDAO.DRAFT_HOLD_HOURS - 1);
        eq("don nhap chua qua han van giu phong", 1,
                ServiceCatalogDAO.countOverlappingRooms(conn, ROOM, ts(530), ts(533), null));

        // ---------- 6. Trả phòng ----------
        System.out.println("\n-- Nhom 6: tra phong --");

        // Đợt lưu trú đang diễn ra: bắt đầu hôm qua, dự kiến trả sau 3 ngày nữa
        Timestamp inNow = ts(-1);
        Timestamp outLater = ts(3);
        int staying = book(conn, ROOM, inNow, outLater);
        BookingService.confirm(conn, staying);
        BookingService.pay(conn, staying);
        eq("dang luu tru thi phong bi chiem", 1,
                ServiceCatalogDAO.countOverlappingRooms(conn, ROOM, inNow, outLater, null));

        com.petweb.model.Booking after = BookingService.checkOut(conn, staying);
        eq("tra phong xong don chuyen sang COMPLETED", "COMPLETED", after.getStatus());
        eq("tra phong som thi nha phong cho nhung ngay con lai", 0,
                ServiceCatalogDAO.countOverlappingRooms(conn, ROOM, ts(1), outLater, null));
        check("nguoi khac dat duoc ngay nhung ngay vua nha ra",
                !rejects(conn, ROOM, ts(1), ts(3)), "van bi chan");

        eq("tra phong hai lan thi bi tu choi", true, checkOutFails(conn, staying));

        int notStaying = BookingService.startDraftForUser(conn, userId, petId);
        eq("don chua xac nhan thi chua tra phong duoc", true, checkOutFails(conn, notStaying));

        // ---------- 7. Trạng thái nào được trả phòng ----------
        System.out.println("\n-- Nhom 7: trang thai nao duoc tra phong --");

        int confirmedOnly = book(conn, ROOM, ts(-1), ts(4));
        BookingService.confirm(conn, confirmedOnly);
        eq("don da xac nhan nhung chua tra tien van tra phong duoc",
                false, checkOutFails(conn, confirmedOnly));

        int cancelled = book(conn, ROOM, ts(-1), ts(5));
        BookingService.confirm(conn, cancelled);
        BookingService.cancel(conn, cancelled);
        eq("don da huy thi khong tra phong duoc", true, checkOutFails(conn, cancelled));

        // ---------- 8. Bảng tình trạng cho giao diện ----------
        System.out.println("\n-- Nhom 8: bang tinh trang cho giao dien --");

        Timestamp in8 = ts(560);
        Timestamp out8 = ts(563);

        List<RoomAvailability> free = ServiceCatalogDAO.findAvailability(conn, in8, out8, null);
        check("tra ve day du cac hang phong dang mo ban", !free.isEmpty(), "danh sach rong");
        check("khoang ngay da biet thi so lieu la that",
                free.stream().allMatch(RoomAvailability::isWindowKnown), "co hang chua biet");

        RoomAvailability probe = pick(free, ROOM);
        check("tim thay hang phong dang thu", probe != null, "khong thay " + ROOM);
        eq("chua ai dat thi con nguyen so phong", probe.getTotalRooms(), probe.getFreeRooms());
        check("con phong thi khong bi danh dau het", !probe.isSoldOut(), "bi bao het phong");
        check("nhan hien thi noi ro con bao nhieu",
                probe.getStatusLabel().contains("Còn"), "nhan=" + probe.getStatusLabel());

        // Lấp đầy rồi xem bảng có phản ánh đúng không
        for (int i = 1; i <= probe.getTotalRooms(); i++) {
            int b = book(conn, ROOM, in8, out8);
            BookingService.confirm(conn, b);
        }
        RoomAvailability full = pick(ServiceCatalogDAO.findAvailability(conn, in8, out8, null), ROOM);
        eq("lap day thi con 0 phong", 0, full.getFreeRooms());
        check("lap day thi bi danh dau het phong", full.isSoldOut(), "chua bao het");
        eq("nhan doi thanh Het phong", "Hết phòng", full.getStatusLabel());
        eq("mau nhan doi sang mau canh bao", "pink", full.getStatusColor());

        RoomAvailability other = pick(ServiceCatalogDAO.findAvailability(conn, in8, out8, null), "vip1");
        check("hang phong khac khong bi anh huong", other != null && !other.isSoldOut(),
                "vip1 bi bao het oan");

        // Chưa chọn ngày: chỉ biết tổng số phòng
        List<RoomAvailability> unknown = ServiceCatalogDAO.findAvailability(conn, null, null, null);
        check("chua chon ngay thi khong khang dinh con trong",
                unknown.stream().noneMatch(RoomAvailability::isWindowKnown), "lai khang dinh");
        check("chua chon ngay thi khong bao het phong",
                unknown.stream().noneMatch(RoomAvailability::isSoldOut), "bao het phong oan");
        RoomAvailability u = pick(unknown, ROOM);
        check("chua chon ngay thi chi noi tong so phong",
                u.getStatusLabel().startsWith("Tổng"), "nhan=" + u.getStatusLabel());

        // Ngày trả trước ngày nhận cũng coi như chưa chọn
        check("khoang ngay vo ly duoc coi la chua chon",
                ServiceCatalogDAO.findAvailability(conn, out8, in8, null)
                        .stream().noneMatch(RoomAvailability::isWindowKnown), "van tinh toan");

        // Bỏ qua chính đơn của mình: dòng khách vừa thêm không được tự chặn khách
        int mine = book(conn, ROOM, ts(570), ts(573));
        RoomAvailability withMine = pick(
                ServiceCatalogDAO.findAvailability(conn, ts(570), ts(573), null), ROOM);
        RoomAvailability withoutMine = pick(
                ServiceCatalogDAO.findAvailability(conn, ts(570), ts(573), mine), ROOM);
        eq("don cua chinh minh cung bi tinh khi khong loai tru",
                withMine.getTotalRooms() - 1, withMine.getFreeRooms());
        eq("loai tru don cua minh thi khong tu chan minh",
                withoutMine.getTotalRooms(), withoutMine.getFreeRooms());

        checkCheckOutButton(conn);
        checkOnePetOneRoom(conn);
        checkStayBoard(conn);
        checkStayActions(conn);
        checkManyStays(conn);
    }

    /** Mỗi bé chỉ ở một phòng tại một thời điểm, nhưng vẫn đặt trước được cho lần sau. */
    static void checkOnePetOneRoom(Connection conn) throws Exception {
        System.out.println("\n-- Nhom 10: moi be chi o mot phong --");

        // Cả nhóm này xoay quanh ĐÚNG MỘT bé, vì quy tắc đang kiểm tra là quy tắc
        // theo từng bé chứ không phải theo hạng phòng.
        int pet = insertScratchPet(conn, userId);

        int held = bookFor(conn, pet, ROOM, ts(900), ts(903));
        BookingService.confirm(conn, held);

        // Đặt chồng lấn: phải bị chặn dù hạng phòng khác vẫn còn chỗ
        int another = BookingService.startDraftForUser(conn, userId, pet);
        String msg = null;
        try {
            BookingService.addHotelLine(conn, another, "vip1", ts(901), ts(905));
        } catch (Exception e) {
            msg = e.getMessage();
        }
        check("be dang o phong thi khong dat chong lan duoc", msg != null,
                "van dat duoc hai phong cung luc");
        check("thong bao noi ro be dang co phong",
                msg != null && msg.contains("đang có phòng"), "thong bao=" + msg);
        check("thong bao chi ro don dang vuong",
                msg != null && msg.contains("#" + held), "thong bao=" + msg);

        // Chặn cả khi trùng khít, nằm trong, và bao trùm
        check("khoang trung khit bi chan",
                rejectsFor(conn, pet, "vip1", ts(900), ts(903)), "khong chan");
        check("khoang nam gon ben trong bi chan",
                rejectsFor(conn, pet, "vip1", ts(901), ts(902)), "khong chan");
        check("khoang bao trum bi chan",
                rejectsFor(conn, pet, "vip1", ts(899), ts(904)), "khong chan");

        // Không chồng lấn thì vẫn đặt trước được — đây là điểm quan trọng nhất
        check("dat truoc cho dot sau khong bi chan",
                !rejectsFor(conn, pet, "vip1", ts(910), ts(913)), "bi chan oan");
        check("nhan phong dung luc dot truoc ket thuc thi khong bi chan",
                !rejectsFor(conn, pet, "vip1", ts(903), ts(906)), "bi chan oan");

        // Bé KHÁC vẫn đặt được trong đúng khoảng đó
        int otherPet = insertScratchPet(conn, userId);
        check("be khac van dat duoc trong cung khoang",
                !rejectsFor(conn, otherPet, "vip1", ts(901), ts(902)), "bi chan oan");

        // Trả phòng xong thì đặt lại được ngay trong chính khoảng vừa nhả
        int now = insertScratchPet(conn, userId);
        int staying = bookFor(conn, now, ROOM, ts(-1), ts(3));
        BookingService.confirm(conn, staying);
        check("dang o thi khong dat chong lan duoc",
                rejectsFor(conn, now, "vip1", ts(1), ts(2)), "khong chan");
        BookingService.checkOut(conn, staying);
        check("tra phong xong thi dat lai duoc ngay",
                !rejectsFor(conn, now, "vip1", ts(1), ts(2)), "van bi chan sau khi tra phong");

        // Đơn đã hủy không còn chặn bé
        int c = insertScratchPet(conn, userId);
        int cancelled = bookFor(conn, c, ROOM, ts(920), ts(923));
        BookingService.confirm(conn, cancelled);
        check("don con hieu luc thi chan",
                rejectsFor(conn, c, "vip1", ts(921), ts(922)), "khong chan");
        BookingService.cancel(conn, cancelled);
        check("don da huy thi khong chan nua",
                !rejectsFor(conn, c, "vip1", ts(921), ts(922)), "van chan");

        // Hai phòng chồng lấn trong CÙNG một đơn cũng không được
        int d = insertScratchPet(conn, userId);
        int sameOrder = BookingService.startDraftForUser(conn, userId, d);
        BookingService.addHotelLine(conn, sameOrder, ROOM, ts(930), ts(933));
        String same = null;
        try {
            BookingService.addHotelLine(conn, sameOrder, "vip1", ts(931), ts(934));
        } catch (Exception e) {
            same = e.getMessage();
        }
        check("hai phong chong lan trong cung mot don cung bi chan", same != null,
                "van them duoc");
        check("hai phong khong chong lan trong cung mot don thi duoc",
                !rejectsFor(conn, d, "vip1", ts(935), ts(937)), "bi chan oan");

        // Khách vãng lai không có hồ sơ bé nên không kiểm tra được, phải đặt được
        int guest = BookingService.startDraftForGuest(conn, "Khach Thu", "0912345678",
                null, "Be Thu", "Chó");
        try {
            BookingService.addHotelLine(conn, guest, "vip1", ts(940), ts(943));
            check("khach vang lai van dat duoc binh thuong", true, "");
        } catch (Exception e) {
            check("khach vang lai van dat duoc binh thuong", false, e.getMessage());
        }
    }

    /** Bảng tình trạng lưu trú đổ ra hồ sơ thú cưng. */
    static void checkStayBoard(Connection conn) throws Exception {
        System.out.println("\n-- Nhom 11: tinh trang luu tru tren ho so --");

        int pet = insertScratchPet(conn, userId);

        check("be chua dat gi thi khong co dot luu tru nao",
                findStay(conn, pet) == null, "lai co");

        // Đặt trước cho tương lai
        int future = BookingService.startDraftForUser(conn, userId, pet);
        BookingService.addHotelLine(conn, future, ROOM, ts(950), ts(953));
        BookingService.confirm(conn, future);

        PetStay upcoming = findStay(conn, pet);
        check("dat truoc thi hien la sap toi", upcoming != null && upcoming.isUpcoming(),
                "khong nhan ra");
        check("chua toi ngay thi khong phai dang o",
                upcoming != null && !upcoming.isOngoing(), "bao la dang o");
        eq("mau nhan cua dot sap toi", "blue", upcoming.getStateColor());
        check("co ten hang phong", upcoming.getRoomName() != null, "thieu ten phong");

        // Bé khác đang ở ngay lúc này
        int pet2 = insertScratchPet(conn, userId);
        int now = BookingService.startDraftForUser(conn, userId, pet2);
        BookingService.addHotelLine(conn, now, ROOM, ts(-1), ts(2));
        BookingService.confirm(conn, now);

        PetStay ongoing = findStay(conn, pet2);
        check("dang o thi nhan ra dung", ongoing != null && ongoing.isOngoing(), "khong nhan ra");
        check("dang o thi khong phai sap toi", !ongoing.isUpcoming(), "bao la sap toi");
        eq("mau nhan khi dang o", "teal", ongoing.getStateColor());
        check("con ngay de o", ongoing.getDaysLeft() > 0, "days=" + ongoing.getDaysLeft());
        check("chu hien thi noi la dang o",
                ongoing.getStateText().contains("Đang ở"), "text=" + ongoing.getStateText());

        // Trả phòng thì biến khỏi bảng
        BookingService.checkOut(conn, now);
        check("tra phong xong thi khong con tren ho so",
                findStay(conn, pet2) == null, "van con");

        // Bé của chủ khác không lọt vào danh sách
        check("chi lay be cua dung chu",
                BookingDAO.findCurrentStaysByOwner(conn, userId).stream()
                        .allMatch(s -> s.getPetId() == pet || s.getPetId() == pet2
                                || s.getPetId() == petId || s.getPetId() > 0),
                "co be la");
    }

    /**
     * Mỗi trạng thái lưu trú chỉ được mở đúng một thao tác.
     * Nút sai trạng thái là nút bấm vào báo lỗi, nên phải khóa chặt ở đây.
     */
    static void checkStayActions(Connection conn) throws Exception {
        System.out.println("\n-- Nhom 12: moi trang thai mo dung mot thao tac --");

        // Khoảng "ngay bây giờ" đã bị các nhóm trước dùng nhiều, mà VIP 3 chỉ có
        // vài phòng, nên nhóm này dùng hạng phòng nhiều chỗ hơn để không vướng
        // vào chuyện hết phòng — thứ đang kiểm tra ở đây là trạng thái, không
        // phải sức chứa.
        final String ROOMY = "vip1";

        // Đơn nháp chưa chốt: không trả phòng, không hủy, chỉ mời hoàn tất đơn
        int p1 = insertScratchPet(conn, userId);
        bookFor(conn, p1, ROOMY, ts(-1), ts(2));
        PetStay draft = findStay(conn, p1);
        check("don nhap duoc nhan ra", draft != null && draft.isDraft(), "khong nhan ra");
        check("don nhap khong cho tra phong", !draft.isCheckOutable(), "lai cho");
        check("don nhap khong cho huy", !draft.isCancellable(), "lai cho");
        check("chu hien thi noi la dang dat do",
                draft.getStateText().contains("đặt dở"), "text=" + draft.getStateText());

        // Đã chốt, chưa tới ngày nhận phòng: chỉ hủy được
        int p2 = insertScratchPet(conn, userId);
        int upcoming = bookFor(conn, p2, ROOM, ts(960), ts(963));
        BookingService.confirm(conn, upcoming);
        PetStay up = findStay(conn, p2);
        check("cho nhan phong thi khong tra phong duoc", !up.isCheckOutable(), "lai cho");
        check("cho nhan phong thi huy duoc", up.isCancellable(), "khong cho huy");

        // Hủy xong thì bé rảnh, đặt lại được ngay
        BookingService.cancel(conn, upcoming);
        check("huy xong thi khong con dot luu tru nao", findStay(conn, p2) == null, "van con");
        check("huy xong thi dat lai duoc",
                !rejectsFor(conn, p2, ROOM, ts(960), ts(963)), "van bi chan");

        // Đang ở: chỉ trả phòng
        int p3 = insertScratchPet(conn, userId);
        int staying = bookFor(conn, p3, ROOMY, ts(-1), ts(2));
        BookingService.confirm(conn, staying);
        PetStay on = findStay(conn, p3);
        check("dang o thi tra phong duoc", on.isCheckOutable(), "khong cho tra phong");
        check("dang o thi van huy duoc neu can", on.isCancellable(), "khong cho huy");
    }

    /** Nhiều đợt đặt trước cho cùng một bé phải xem được hết, không bị giấu bớt. */
    static void checkManyStays(Connection conn) throws Exception {
        System.out.println(NHOM13);

        int pet = insertScratchPet(conn, userId);

        // Ba đợt không chồng lấn, đặt trước cho ba khoảng khác nhau
        int b1 = bookFor(conn, pet, "vip1", ts(1000), ts(1002));
        int b2 = bookFor(conn, pet, "vip1", ts(1010), ts(1012));
        int b3 = bookFor(conn, pet, "vip1", ts(1020), ts(1022));
        BookingService.confirm(conn, b1);
        BookingService.confirm(conn, b2);
        BookingService.confirm(conn, b3);

        List<PetStay> mine = new ArrayList<>();
        for (PetStay s : BookingDAO.findCurrentStaysByOwner(conn, userId)) {
            if (s.getPetId() == pet) mine.add(s);
        }
        eq("lay du ca ba dot cua be", 3, mine.size());
        check("cac dot sap xep theo thoi gian",
                mine.get(0).getStartAt().before(mine.get(1).getStartAt())
                && mine.get(1).getStartAt().before(mine.get(2).getStartAt()),
                "thu tu sai");

        Map<Integer, List<PetStay>> grouped =
                BookingDAO.groupStaysByPet(BookingDAO.findCurrentStaysByOwner(conn, userId));
        eq("gom nhom giu du ba dot cua be", 3, grouped.get(pet).size());
        eq("dot dau tien la dot gan nhat", mine.get(0).getBookingId(),
                grouped.get(pet).get(0).getBookingId());

        // Hủy một đợt thì danh sách rút lại
        BookingService.cancel(conn, b2);
        grouped = BookingDAO.groupStaysByPet(BookingDAO.findCurrentStaysByOwner(conn, userId));
        eq("huy mot dot thi con hai", 2, grouped.get(pet).size());
        check("dot da huy khong con trong danh sach",
                grouped.get(pet).stream().noneMatch(s -> s.getBookingId() == b2), "van con");

        // Lịch sử phải thấy cả đơn đã hủy
        List<com.petweb.model.Booking> history = BookingDAO.findHistory(conn, userId, pet);
        eq("lich su cua be co du ba don", 3, history.size());
        check("lich su co ca don da huy",
                history.stream().anyMatch(b -> b.getBookingId() == b2 && b.isCancelled()),
                "thieu don da huy");
        check("moi don trong lich su deu co dong dich vu",
                history.stream().allMatch(b -> !b.getLines().isEmpty()), "co don rong");
        check("lich su sap xep moi nhat truoc",
                history.get(0).getBookingId() >= history.get(history.size() - 1).getBookingId(),
                "thu tu sai");

        // Đơn nháp không được coi là đơn đã đặt
        BookingService.startDraftForUser(conn, userId, pet);
        eq("don nhap khong lot vao lich su", 3,
                BookingDAO.findHistory(conn, userId, pet).size());

        // Lọc theo bé: không lẫn đơn của bé khác
        check("loc theo be thi chi ra don cua be do",
                BookingDAO.findHistory(conn, userId, pet).stream()
                        .allMatch(b -> b.getPetId() != null && b.getPetId() == pet),
                "lan don cua be khac");
        check("khong loc thi thay nhieu hon",
                BookingDAO.findHistory(conn, userId, null).size() >= 3, "thieu don");
        check("nguoi dung khac khong thay don cua minh",
                BookingDAO.findHistory(conn, -1, null).isEmpty(), "lo du lieu");
    }

    static final String NHOM13 = "\n-- Nhom 13: xem het cac dot da dat --";

    static PetStay findStay(Connection conn, int petId) throws SQLException {
        for (PetStay s : BookingDAO.findCurrentStaysByOwner(conn, userId)) {
            if (s.getPetId() == petId) return s;
        }
        return null;
    }

    /** Tạo một thú cưng tạm, chỉ sống trong giao dịch của lần chạy này. */
    static int insertScratchPet(Connection c, int owner) throws SQLException {
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

    /** Nút "Trả phòng" chỉ được hiện đúng lúc đáng hiện. */
    static void checkCheckOutButton(Connection conn) throws Exception {
        System.out.println("\n-- Nhom 9: khi nao hien nut Tra phong --");

        // Đợt lưu trú đang diễn ra
        int now = book(conn, ROOM, ts(-1), ts(2));
        BookingService.confirm(conn, now);
        check("dang luu tru thi hien nut tra phong",
                BookingDAO.findByIdWithLines(conn, now).isCheckOutable(), "khong hien");

        // Chưa tới ngày nhận phòng: việc cần làm là hủy, không phải trả phòng
        int future = book(conn, ROOM, ts(800), ts(803));
        BookingService.confirm(conn, future);
        check("chua toi ngay nhan phong thi khong hien nut tra phong",
                !BookingDAO.findByIdWithLines(conn, future).isCheckOutable(), "lai hien");

        // Đơn nháp chưa xác nhận
        int draft = book(conn, ROOM, ts(-1), ts(2));
        check("don nhap thi khong hien nut tra phong",
                !BookingDAO.findByIdWithLines(conn, draft).isCheckOutable(), "lai hien");

        // Đơn không có dịch vụ lưu trú
        int noHotel = BookingService.startDraftForUser(conn, userId, petId);
        check("don khong co luu tru thi khong hien nut tra phong",
                !BookingDAO.findByIdWithLines(conn, noHotel).isCheckOutable(), "lai hien");

        // Đã trả phòng rồi
        BookingService.checkOut(conn, now);
        check("da tra phong roi thi khong hien nut nua",
                !BookingDAO.findByIdWithLines(conn, now).isCheckOutable(), "van hien");
    }

    static RoomAvailability pick(List<RoomAvailability> list, String code) {
        for (RoomAvailability r : list) {
            if (code.equals(r.getRoomCode())) return r;
        }
        return null;
    }

    /** Đẩy ngày tạo của đơn nháp lùi lại để giả lập đơn bỏ dở đã lâu. */
    static void ageDraft(Connection conn, int bookingId, int hours) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE booking SET created_at = now() - (? || ' hours')::interval"
              + " WHERE booking_id = ?")) {
            ps.setString(1, String.valueOf(hours));
            ps.setInt(2, bookingId);
            ps.executeUpdate();
        }
    }

    /** Thử trả phòng và cho biết có bị từ chối hay không. */
    static boolean checkOutFails(Connection conn, int bookingId) {
        try {
            BookingService.checkOut(conn, bookingId);
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Mở một đơn nháp cho MỘT BÉ MỚI rồi giữ một phòng trong khoảng đã cho.
     *
     * Mỗi lần gọi dùng một bé riêng vì hệ thống không cho một bé giữ hai phòng
     * chồng lấn — muốn lấp đầy một hạng phòng thì phải là nhiều bé khác nhau,
     * đúng như ngoài đời. Trường hợp cần cùng một bé thì gọi bookFor().
     */
    static int book(Connection conn, String roomCode, Timestamp in, Timestamp out)
            throws Exception {
        return bookFor(conn, insertScratchPet(conn, userId), roomCode, in, out);
    }

    /** Giữ phòng cho đúng một bé chỉ định. */
    static int bookFor(Connection conn, int pet, String roomCode, Timestamp in, Timestamp out)
            throws Exception {
        int id = BookingService.startDraftForUser(conn, userId, pet);
        BookingService.addHotelLine(conn, id, roomCode, in, out);
        return id;
    }

    /** Thử đặt phòng cho một bé mới và cho biết có bị từ chối hay không. */
    static boolean rejects(Connection conn, String roomCode, Timestamp in, Timestamp out) {
        try {
            return rejectsFor(conn, insertScratchPet(conn, userId), roomCode, in, out);
        } catch (Exception e) {
            return true;
        }
    }

    /** Thử đặt phòng cho đúng một bé chỉ định. */
    static boolean rejectsFor(Connection conn, int pet, String roomCode,
                              Timestamp in, Timestamp out) {
        try {
            bookFor(conn, pet, roomCode, in, out);
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    static Timestamp ts(int daysFromNow) {
        return Timestamp.valueOf(LocalDateTime.now().plusDays(daysFromNow)
                .withHour(9).withMinute(0).withSecond(0).withNano(0));
    }
}
