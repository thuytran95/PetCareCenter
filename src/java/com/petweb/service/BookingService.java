package com.petweb.service;

import com.petweb.dao.BookingDAO;
import com.petweb.dao.PetDAO;
import com.petweb.dao.NotificationDAO;
import com.petweb.dao.ServiceCatalogDAO;
import com.petweb.model.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.security.SecureRandom;

/**
 * Toàn bộ quy tắc nghiệp vụ đặt lịch nằm ở đây — Servlet chỉ đọc tham số và hiển thị.
 *
 * Nguyên tắc:
 *  1. Giá LUÔN lấy từ bảng giá trong DB, không bao giờ nhận từ trình duyệt.
 *  2. Mỗi lần khách thêm dịch vụ = thêm một dòng (booking_line) vào đơn nháp.
 *  3. Tổng tiền được tính lại từ các dòng khi xác nhận, không cộng dồn thủ công.
 *  4. Chỉ đơn ở trạng thái DRAFT mới được sửa.
 */
public class BookingService {

    /** Đơn nháp bỏ dở quá số giờ này sẽ bị dọn (dùng bởi AutoStatusListener). */
    /** Giữ nguyên tên cũ cho code sẵn có, giá trị lấy từ nơi quy tắc thực sự sống. */
    public static final int DRAFT_EXPIRE_HOURS = ServiceCatalogDAO.DRAFT_HOLD_HOURS;

    // =====================================================================
    // Tạo đơn nháp
    // =====================================================================

    /**
     * Mở đơn nháp cho khách đã đăng nhập, dựa trên một thú cưng của chính họ.
     * Kiểm tra quyền sở hữu ngay tại đây nên không ai đặt lịch hộ thú cưng người khác
     * bằng cách đổi petId trên URL.
     */
    public static int startDraftForUser(Connection conn, int userId, int petId)
            throws SQLException, BookingException {

        Pet pet = PetDAO.findByIdAndOwner(conn, petId, userId);
        if (pet == null) {
            throw new BookingException("Không tìm thấy thú cưng này trong hồ sơ của bạn.");
        }

        // Khách hay bấm "Đặt lịch" rồi thoát ra, bấm lại, thoát tiếp. Nếu mỗi
        // lần đều tạo một dòng mới thì CSDL đầy đơn rỗng. Đơn rỗng chưa giữ chỗ
        // và chưa có gì để mất, nên dùng lại chính nó thay vì đẻ thêm dòng.
        Integer existing = BookingDAO.findEmptyDraft(conn, userId, pet.getPetId());
        if (existing != null) {
            BookingDAO.touchDraft(conn, existing);
            return existing;
        }
        return BookingDAO.insertDraftForUser(conn, userId, pet.getPetId(), pet.getName(), pet.getSpecies());
    }

    /** Mở đơn nháp cho khách vãng lai — không tạo tài khoản, thông tin lưu thẳng trên đơn. */
    public static int startDraftForGuest(Connection conn, String guestName, String guestPhone,
                                         String guestEmail, String petName, String petSpecies)
            throws SQLException, BookingException {

        if (isBlank(guestName)) throw new BookingException("Vui lòng nhập tên người đặt lịch.");
        if (isBlank(guestPhone)) throw new BookingException("Vui lòng nhập số điện thoại liên hệ.");
        if (isBlank(petName)) throw new BookingException("Vui lòng nhập tên thú cưng.");

        return BookingDAO.insertDraftForGuest(conn, guestName.trim(), guestPhone.trim(),
                isBlank(guestEmail) ? null : guestEmail.trim(),
                petName.trim(), isBlank(petSpecies) ? null : petSpecies.trim());
    }

    // =====================================================================
    // Thêm dòng dịch vụ
    // =====================================================================

    /**
     * Thêm dịch vụ khách sạn.
     * Giá = giá/ngày của loại phòng × số ngày ở, và chỉ thêm được khi loại phòng
     * đó còn chỗ trong đúng khoảng thời gian yêu cầu.
     */
    public static BookingLine addHotelLine(Connection conn, int bookingId, String roomCode,
                                           Timestamp checkIn, Timestamp checkOut)
            throws SQLException, BookingException {

        Booking booking = requireDraft(conn, bookingId);

        if (checkIn == null || checkOut == null) {
            throw new BookingException("Vui lòng chọn đầy đủ ngày nhận và trả phòng.");
        }
        if (!checkOut.after(checkIn)) {
            throw new BookingException("Ngày trả phòng phải sau ngày nhận phòng.");
        }
        long stayMs = checkOut.getTime() - checkIn.getTime();
        if (stayMs < 2L * 60 * 60 * 1000) {
            throw new BookingException("Thời gian lưu trú tối thiểu là 2 giờ.");
        }

        RoomType room = ServiceCatalogDAO.findRoomType(conn, roomCode);
        if (room == null || !room.isActive()) {
            throw new BookingException("Loại phòng không hợp lệ.");
        }

        // Một con vật chỉ ở được một phòng tại một thời điểm, nên bé đang có đợt
        // lưu trú chồng lấn thì phải trả phòng trước rồi mới đặt tiếp được.
        requirePetFree(conn, booking, checkIn, checkOut);

        // Còn phòng trống không? Đếm các đơn khác đang chiếm loại phòng này trong cùng khoảng.
        int busy = ServiceCatalogDAO.countOverlappingRooms(conn, roomCode, checkIn, checkOut, bookingId);
        if (busy >= room.getTotalRooms()) {
            throw new BookingException("Đã hết " + room.getRoomName()
                    + " trong khoảng thời gian bạn chọn. Vui lòng chọn loại phòng khác hoặc đổi ngày.");
        }

        long days = countDays(checkIn, checkOut);
        BigDecimal total = room.getPricePerDay().multiply(BigDecimal.valueOf(days));

        BookingLine line = new BookingLine();
        line.setBookingId(booking.getBookingId());
        line.setServiceType(BookingLine.TYPE_HOTEL);
        line.setStartAt(checkIn);
        line.setEndAt(checkOut);
        line.setRoomCode(room.getRoomCode());
        line.setQuantity((int) days);
        line.setLineTotal(total);
        line.setNote(room.getRoomName());

        BookingDAO.insertLine(conn, line);
        refreshTotal(conn, bookingId);
        return line;
    }

    /** Thêm dịch vụ spa: giá lấy từ spa_service_item rồi chụp lại vào đơn. */
    public static BookingLine addSpaLine(Connection conn, int bookingId,
                                         List<Integer> itemIds, Timestamp bookingDate)
            throws SQLException, BookingException {

        Booking booking = requireDraft(conn, bookingId);
        requireDate(bookingDate, "Vui lòng chọn ngày giờ đặt lịch.");
        requireAdvanceBooking(bookingDate, 1, "Bạn phải đặt lịch trước ít nhất 1 ngày.");
        requireItems(itemIds);

        Map<Integer, SpaServiceItem> found = ServiceCatalogDAO.findSpaItemsByIds(conn, itemIds);
        ensureAllFound(itemIds, found.keySet().size());

        List<BookingLineItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Integer id : itemIds) {
            SpaServiceItem src = found.get(id);
            if (src == null) continue;
            items.add(new BookingLineItem(src.getItemId(), src.getItemName(), src.getItemPrice()));
            total = total.add(src.getItemPrice());
        }

        return addItemLine(conn, booking, BookingLine.TYPE_SPA, bookingDate, items, total);
    }

    /** Thêm dịch vụ y tế: giá lấy từ medical_service_item rồi chụp lại vào đơn. */
    public static BookingLine addMedicalLine(Connection conn, int bookingId,
                                             List<Integer> itemIds, Timestamp admissionDate)
            throws SQLException, BookingException {

        Booking booking = requireDraft(conn, bookingId);
        requireDate(admissionDate, "Vui lòng chọn ngày giờ nhập viện.");
        requireItems(itemIds);

        Map<Integer, MedicalServiceItem> found = ServiceCatalogDAO.findMedicalItemsByIds(conn, itemIds);
        ensureAllFound(itemIds, found.keySet().size());

        List<BookingLineItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Integer id : itemIds) {
            MedicalServiceItem src = found.get(id);
            if (src == null) continue;
            items.add(new BookingLineItem(src.getItemId(), src.getItemName(), src.getItemPrice()));
            total = total.add(src.getItemPrice());
        }

        return addItemLine(conn, booking, BookingLine.TYPE_MEDICAL, admissionDate, items, total);
    }

    private static BookingLine addItemLine(Connection conn, Booking booking, String type,
                                           Timestamp startAt, List<BookingLineItem> items,
                                           BigDecimal total) throws SQLException {
        BookingLine line = new BookingLine();
        line.setBookingId(booking.getBookingId());
        line.setServiceType(type);
        line.setStartAt(startAt);
        line.setQuantity(items.size());
        line.setLineTotal(total);

        int lineId = BookingDAO.insertLine(conn, line);
        BookingDAO.insertLineItems(conn, lineId, items);
        line.setItems(items);

        refreshTotal(conn, booking.getBookingId());
        return line;
    }

    // =====================================================================
    // Chốt đơn / đọc hóa đơn
    // =====================================================================

    /**
     * Chốt đơn: tính lại tổng tiền từ các dòng rồi chuyển DRAFT → CONFIRMED.
     * Đơn không có dòng nào thì không cho chốt.
     */
    public static Booking confirm(Connection conn, int bookingId)
            throws SQLException, BookingException {

        Booking booking = requireDraft(conn, bookingId);
        List<BookingLine> lines = BookingDAO.findLines(conn, bookingId);
        if (lines.isEmpty()) {
            throw new BookingException("Đơn chưa có dịch vụ nào, vui lòng chọn ít nhất một dịch vụ.");
        }

        BigDecimal total = BookingDAO.sumLineTotals(conn, bookingId);
        BookingDAO.updateTotal(conn, bookingId, total);
        BookingDAO.updateStatus(conn, bookingId, Booking.STATUS_CONFIRMED);

        // Khách vãng lai không có tài khoản nên cần mã để tra cứu lại đơn về sau
        if (booking.isGuestBooking() && booking.getLookupCode() == null) {
            String code = generateLookupCode(conn);
            BookingDAO.updateLookupCode(conn, bookingId, code);
            booking.setLookupCode(code);
        }

        booking.setTotalPrice(total);
        booking.setStatus(Booking.STATUS_CONFIRMED);
        booking.setLines(lines);

        attachContactPhone(conn, booking);
        NotificationService.sendBookingConfirmed(conn, booking);
        return booking;
    }

    /**
     * Đọc đơn đầy đủ để hiển thị hóa đơn. CHỈ ĐỌC — không sửa gì trong DB,
     * khác hàm calculateTotalFromServices cũ vốn ghi lại total_price ngay trong request GET.
     */
    public static Booking loadInvoice(Connection conn, int bookingId)
            throws SQLException, BookingException {

        Booking booking = BookingDAO.findByIdWithLines(conn, bookingId);
        if (booking == null) {
            throw new BookingException("Không tìm thấy đơn đặt lịch.");
        }
        return booking;
    }

    /** Đơn nháp hiện tại còn dùng được không (tồn tại và vẫn ở trạng thái DRAFT). */
    public static Booking findUsableDraft(Connection conn, Integer bookingId) throws SQLException {
        if (bookingId == null) return null;
        Booking b = BookingDAO.findById(conn, bookingId);
        return (b != null && Booking.STATUS_DRAFT.equals(b.getStatus())) ? b : null;
    }

    /**
     * Thanh toán đơn — MÔ PHỎNG, không có cổng thanh toán thật.
     * Chỉ chuyển trạng thái CONFIRMED → PAID và gửi tin xác nhận.
     */
    public static Booking pay(Connection conn, int bookingId)
            throws SQLException, BookingException {

        Booking booking = BookingDAO.findByIdWithLines(conn, bookingId);
        if (booking == null) {
            throw new BookingException("Không tìm thấy đơn đặt lịch.");
        }
        if (booking.isPaid()) {
            throw new BookingException("Đơn này đã được thanh toán rồi.");
        }
        if (!booking.isAwaitingPayment()) {
            throw new BookingException("Đơn ở trạng thái hiện tại không thể thanh toán.");
        }

        int rows = BookingDAO.markPaid(conn, bookingId);
        if (rows == 0) {
            // Ai đó vừa đổi trạng thái giữa chừng
            throw new BookingException("Đơn vừa thay đổi trạng thái, vui lòng tải lại trang.");
        }

        booking.setStatus(Booking.STATUS_PAID);
        attachContactPhone(conn, booking);
        NotificationService.sendPaymentReceived(conn, booking);

        // Dịch vụ y tế đã thanh toán được ghi vào sổ sức khỏe của bé, kèm ngày
        // cần làm lại. Ghi sổ hỏng cũng không được làm hỏng việc thanh toán.
        HealthRecordService.recordFromBooking(conn, booking);
        return booking;
    }

    /** Hủy đơn. Đơn đã hoàn tất hoặc đã hủy thì không hủy lại được. */
    public static Booking cancel(Connection conn, int bookingId)
            throws SQLException, BookingException {

        Booking booking = BookingDAO.findByIdWithLines(conn, bookingId);
        if (booking == null) {
            throw new BookingException("Không tìm thấy đơn đặt lịch.");
        }
        if (booking.isCancelled()) {
            throw new BookingException("Đơn này đã được hủy trước đó.");
        }
        if (!booking.isCancellable()) {
            throw new BookingException("Đơn ở trạng thái hiện tại không thể hủy.");
        }

        int rows = BookingDAO.markCancelled(conn, bookingId);
        if (rows == 0) {
            throw new BookingException("Đơn vừa thay đổi trạng thái, vui lòng tải lại trang.");
        }

        booking.setStatus(Booking.STATUS_CANCELLED);
        attachContactPhone(conn, booking);
        NotificationService.sendBookingCancelled(conn, booking);
        return booking;
    }

    /**
     * Trả phòng: kết thúc đợt lưu trú và nhả phòng ra ngay.
     *
     * Trả phòng sớm hơn dự kiến là chuyện thường — bé được đón về trước hạn.
     * Khi đó phần thời gian còn lại phải trống ngay cho khách khác đặt, nên
     * ngoài việc đóng đơn, các dòng khách sạn cũng được rút ngắn về đúng thời
     * điểm trả phòng. Tiền đã chốt trên hóa đơn thì giữ nguyên, không tính lại,
     * đúng như thông lệ của khách sạn.
     */
    public static Booking checkOut(Connection conn, int bookingId)
            throws SQLException, BookingException {

        Booking booking = BookingDAO.findByIdWithLines(conn, bookingId);
        if (booking == null) {
            throw new BookingException("Không tìm thấy đơn đặt lịch.");
        }
        if (booking.isCompleted()) {
            throw new BookingException("Đơn này đã trả phòng rồi.");
        }
        if (!booking.isConfirmed() && !booking.isPaid()) {
            throw new BookingException("Chỉ đơn đã xác nhận hoặc đã thanh toán mới trả phòng được.");
        }
        if (!hasHotelLine(booking)) {
            throw new BookingException("Đơn này không có dịch vụ lưu trú để trả phòng.");
        }

        Timestamp now = new Timestamp(System.currentTimeMillis());
        BookingDAO.shortenHotelStay(conn, bookingId, now);

        int rows = BookingDAO.markCompleted(conn, bookingId);
        if (rows == 0) {
            throw new BookingException("Đơn vừa thay đổi trạng thái, vui lòng tải lại trang.");
        }

        booking.setStatus(Booking.STATUS_COMPLETED);
        return booking;
    }

    /**
     * Mỗi bé chỉ ở một phòng tại một thời điểm.
     *
     * Quy tắc chỉ cấm HAI ĐỢT CHỒNG LẤN nhau, chứ không cấm đặt trước cho lần
     * sau: bé đang ở tới ngày 5 thì chủ vẫn đặt được phòng cho ngày 20, vì hai
     * khoảng đó không giao nhau và bé hoàn toàn có thể ở cả hai. Cấm rộng hơn
     * thế sẽ chặn luôn việc giữ chỗ trước, vốn là nhu cầu bình thường.
     *
     * Đơn của khách vãng lai không gắn hồ sơ thú cưng nên không kiểm tra được —
     * cùng một cái tên có thể là hai con vật khác nhau của hai người khác nhau.
     */
    private static void requirePetFree(Connection conn, Booking booking,
                                       Timestamp checkIn, Timestamp checkOut)
            throws SQLException, BookingException {

        if (booking.getPetId() == null) return;

        PetStay clash = BookingDAO.findOverlappingStayForPet(
                conn, booking.getPetId(), checkIn, checkOut);
        if (clash == null) return;

        String who = booking.getPetName() == null ? "Bé" : booking.getPetName();
        if (clash.getBookingId() == booking.getBookingId()) {
            throw new BookingException(who + " đã có phòng trong khoảng thời gian này ở"
                    + " ngay đơn hiện tại (" + clash.getRoomName() + ", "
                    + clash.getFormattedRange() + "). Mỗi bé chỉ ở được một phòng.");
        }
        throw new BookingException(who + " đang có phòng " + clash.getRoomName()
                + " từ " + clash.getFormattedRange() + " (đơn #" + clash.getBookingId()
                + "). Mỗi bé chỉ ở được một phòng tại một thời điểm — hãy trả phòng"
                + " trước, hoặc chọn khoảng thời gian khác.");
    }

    private static boolean hasHotelLine(Booking booking) {
        for (BookingLine l : booking.getLines()) {
            if (l.isHotel()) return true;
        }
        return false;
    }

    /**
     * Xóa hẳn một đơn đã kết thúc khỏi lịch sử của chủ tài khoản.
     *
     * Chỉ áp dụng cho đơn ĐÃ HOÀN TẤT hoặc ĐÃ HỦY — đơn còn hiệu lực mà xóa thì
     * phòng sẽ bị khóa vô ích và không còn chỗ nào để trả phòng hay hủy.
     *
     * Đây là thao tác KHÔNG khôi phục được: hóa đơn và các dòng dịch vụ mất hẳn.
     * Sổ sức khỏe của bé vẫn còn nguyên, chỉ mất đường dẫn về đơn.
     *
     * Không cho xóa đơn của khách vãng lai: đơn đó không thuộc tài khoản nào,
     * và người có mã tra cứu không nên xóa được chứng từ.
     */
    public static Booking deleteFinished(Connection conn, int bookingId, int userId)
            throws SQLException, BookingException {

        Booking booking = BookingDAO.findByIdWithLines(conn, bookingId);
        if (booking == null) {
            throw new BookingException("Không tìm thấy đơn đặt lịch.");
        }
        if (booking.getUserId() == null || booking.getUserId() != userId) {
            throw new BookingException("Đơn này không thuộc tài khoản của bạn.");
        }
        if (!booking.isCompleted() && !booking.isCancelled()) {
            throw new BookingException("Chỉ xóa được đơn đã hoàn tất hoặc đã hủy."
                    + " Đơn đang còn hiệu lực thì hãy trả phòng hoặc hủy trước.");
        }

        int rows = BookingDAO.deleteFinished(conn, bookingId, userId);
        if (rows == 0) {
            throw new BookingException("Đơn vừa thay đổi trạng thái, vui lòng tải lại trang.");
        }
        return booking;
    }

    /** Tra cứu đơn cho khách vãng lai bằng mã tra cứu + số điện thoại đã dùng khi đặt. */
    public static Booking lookupGuestBooking(Connection conn, String code, String phone)
            throws SQLException, BookingException {

        if (isBlank(code) || isBlank(phone)) {
            throw new BookingException("Vui lòng nhập cả mã tra cứu và số điện thoại.");
        }
        Booking booking = BookingDAO.findByLookupCode(conn,
                code.trim().toUpperCase(), phone.trim());
        if (booking == null) {
            throw new BookingException(
                    "Không tìm thấy đơn nào khớp mã tra cứu và số điện thoại này. "
                  + "Kiểm tra lại mã trong tin nhắn xác nhận; nếu bạn đặt lịch lúc đang "
                  + "đăng nhập thì đơn đó không có mã tra cứu, hãy đăng nhập để xem.");
        }
        booking.setLines(BookingDAO.findLines(conn, booking.getBookingId()));
        return booking;
    }

    /**
     * Sinh mã tra cứu ngắn, dễ đọc qua điện thoại.
     * Bỏ các ký tự dễ nhầm (O/0, I/1) và thử lại nếu trùng.
     */
    private static String generateLookupCode(Connection conn) throws SQLException {
        final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        SecureRandom rnd = new SecureRandom();
        for (int attempt = 0; attempt < 10; attempt++) {
            StringBuilder sb = new StringBuilder(8);
            for (int i = 0; i < 8; i++) {
                sb.append(ALPHABET.charAt(rnd.nextInt(ALPHABET.length())));
            }
            String code = sb.toString();
            if (!BookingDAO.lookupCodeExists(conn, code)) {
                return code;
            }
        }
        // Xác suất tới đây gần như bằng 0; dùng mốc thời gian làm phương án dự phòng
        return "B" + System.currentTimeMillis() % 10000000L;
    }

    // =====================================================================
    // Tiện ích nội bộ
    // =====================================================================

    /**
     * Gán số điện thoại nhận thông báo cho đơn.
     * Khách vãng lai đã có guest_phone; khách đã đăng nhập thì lấy từ hồ sơ tài khoản.
     */
    public static void attachContactPhone(Connection conn, Booking booking) {
        if (booking.getUserId() == null) return; // khách vãng lai: dùng luôn guest_phone
        try {
            UserAccount owner = com.petweb.utils.DBUtils.findUser(conn, booking.getUserId());
            if (owner != null) booking.setContactPhone(owner.getPhone());
        } catch (SQLException e) {
            // Không lấy được số thì bỏ qua gửi tin, đơn vẫn hợp lệ
        }
    }

    /** Cập nhật tổng tiền của đơn theo các dòng hiện có. */
    private static void refreshTotal(Connection conn, int bookingId) throws SQLException {
        BookingDAO.updateTotal(conn, bookingId, BookingDAO.sumLineTotals(conn, bookingId));
    }

    private static Booking requireDraft(Connection conn, int bookingId)
            throws SQLException, BookingException {
        Booking booking = BookingDAO.findById(conn, bookingId);
        if (booking == null) {
            throw new BookingException("Không tìm thấy đơn đặt lịch, vui lòng bắt đầu lại.");
        }
        if (!Booking.STATUS_DRAFT.equals(booking.getStatus())) {
            throw new BookingException("Đơn này đã được chốt, không thể thay đổi.");
        }
        return booking;
    }

    private static void requireDate(Timestamp ts, String message) throws BookingException {
        if (ts == null) throw new BookingException(message);
    }

    /** Ngày đặt phải cách hiện tại ít nhất {@code days} ngày. */
    private static void requireAdvanceBooking(Timestamp ts, int days, String message)
            throws BookingException {
        if (ts == null) return;
        LocalDateTime earliest = LocalDateTime.now().plusDays(days);
        if (ts.toLocalDateTime().isBefore(earliest)) {
            throw new BookingException(message);
        }
    }

    private static void requireItems(List<Integer> itemIds) throws BookingException {
        if (itemIds == null || itemIds.isEmpty()) {
            throw new BookingException("Vui lòng chọn ít nhất một dịch vụ.");
        }
    }

    /** Chặn trường hợp client gửi id không tồn tại để né tiền. */
    private static void ensureAllFound(List<Integer> requested, int foundCount) throws BookingException {
        if (foundCount != requested.size()) {
            throw new BookingException("Có dịch vụ không còn tồn tại, vui lòng chọn lại.");
        }
    }

    /** Số ngày ở; ở trong ngày vẫn tính tròn 1 ngày. */
    private static long countDays(Timestamp checkIn, Timestamp checkOut) {
        LocalDateTime in = checkIn.toLocalDateTime();
        LocalDateTime out = checkOut.toLocalDateTime();
        long days = Duration.between(in, out).toDays();
        return days <= 0 ? 1 : days;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
