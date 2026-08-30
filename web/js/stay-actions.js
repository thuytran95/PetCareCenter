/*
 * Xác nhận trước khi hủy phòng của một bé.
 *
 * "Hủy phòng" thực chất hủy CẢ đơn, kể cả những dịch vụ khác nằm chung đơn đó,
 * nên phải nói rõ trước khi khách bấm. Dùng chung cho trang chủ và trang hồ sơ
 * thú cưng để hai nơi hỏi giống hệt nhau.
 */
(function () {
    "use strict";

    document.addEventListener("DOMContentLoaded", function () {
        var forms = document.querySelectorAll(".js-cancel-stay");
        for (var i = 0; i < forms.length; i++) {
            forms[i].addEventListener("submit", function (e) {
                var id = this.getAttribute("data-booking");
                var ok = confirm(
                    "Hủy phòng sẽ hủy toàn bộ đơn #" + id
                    + ", gồm cả những dịch vụ khác trong đơn (nếu có).\n\n"
                    + "Sau khi hủy, bạn có thể đặt lại phòng cho bé vào khoảng thời gian khác.\n\n"
                    + "Bạn chắc chắn muốn hủy?");
                if (!ok) e.preventDefault();
            });
        }
    });
})();

/*
 * Xác nhận trước khi xóa hẳn một đơn đã kết thúc.
 *
 * Khác với "hủy đơn" (đơn vẫn nằm trong lịch sử), xóa là mất hẳn hóa đơn và các
 * dòng dịch vụ, không khôi phục được. Sổ tiêm của bé thì vẫn giữ nguyên.
 */
(function () {
    "use strict";

    document.addEventListener("DOMContentLoaded", function () {
        var forms = document.querySelectorAll(".js-delete-order");
        for (var i = 0; i < forms.length; i++) {
            forms[i].addEventListener("submit", function (e) {
                var id = this.getAttribute("data-booking");
                var ok = confirm(
                    "Xóa hẳn đơn #" + id + " khỏi lịch sử?\n\n"
                    + "Hóa đơn và các dòng dịch vụ của đơn này sẽ mất vĩnh viễn,"
                    + " không khôi phục được.\n\n"
                    + "Sổ tiêm và khám định kỳ của bé vẫn được giữ nguyên.");
                if (!ok) e.preventDefault();
            });
        }
    });
})();
