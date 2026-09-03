package com.petweb.service;

import com.petweb.dao.BookingDAO;
import com.petweb.dao.HealthRecordDAO;
import com.petweb.dao.PetDAO;
import com.petweb.model.Pet;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Quy tắc nghiệp vụ quanh hồ sơ thú cưng.
 *
 * Hiện chỉ có một quy tắc, nhưng là quy tắc quan trọng: KHÔNG xóa hồ sơ của bé
 * đang có lịch. Khóa ngoại của CSDL không ngăn được việc này — booking.pet_id
 * khai báo ON DELETE SET NULL nên lệnh xóa vẫn chạy trót lọt, để lại hậu quả mà
 * người dùng không hề thấy:
 *
 *  - Đơn đang hiệu lực thành mồ côi: pet_id bằng NULL, đơn biến mất khỏi hồ sơ
 *    thú cưng và khỏi bảng nhắc lịch, dù tiền đã trả.
 *  - Bé đang ở khách sạn thì phòng VẪN bị chiếm tới hết hạn, mà không còn nút
 *    "Trả phòng" nào để nhả ra.
 *  - Sổ sức khỏe bị xóa theo (pet_health_record khai báo ON DELETE CASCADE) —
 *    toàn bộ lịch sử tiêm phòng mất vĩnh viễn, không khôi phục được.
 */
public class PetService {

    private PetService() {
    }

    /**
     * Xóa hồ sơ một bé của chính chủ nuôi.
     *
     * @return số dòng bị xóa (0 nghĩa là không tìm thấy bé trong hồ sơ người này)
     * @throws BookingException khi bé còn đơn đang hiệu lực
     */
    public static int deleteOwned(Connection conn, int petId, int userId)
            throws SQLException, BookingException {

        Pet pet = PetDAO.findByIdAndOwner(conn, petId, userId);
        if (pet == null) {
            throw new BookingException("Không tìm thấy thú cưng này trong hồ sơ của bạn.");
        }

        int active = BookingDAO.countActiveBookingsForPet(conn, petId);
        if (active > 0) {
            String name = pet.getName() == null ? "Bé" : pet.getName();
            throw new BookingException(name + " đang có " + active + " đơn còn hiệu lực."
                    + " Hãy trả phòng hoặc hủy các đơn đó trước, rồi mới xóa hồ sơ được."
                    + " Xóa ngay bây giờ sẽ khiến đơn mất chủ và phòng bị khóa vô ích.");
        }

        return PetDAO.deleteOwned(conn, petId, userId);
    }

    /**
     * Số ghi chép trong sổ sức khỏe sẽ mất nếu xóa bé.
     * Dùng để cảnh báo trước khi xóa, vì phần dữ liệu này không khôi phục được.
     */
    public static int healthRecordsAtRisk(Connection conn, int petId) throws SQLException {
        return HealthRecordDAO.countByPet(conn, petId);
    }
}
