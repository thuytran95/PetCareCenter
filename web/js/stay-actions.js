/*
 * Xác nhận trước khi hủy phòng của một bé.
 */
(function () {
    "use strict";

    function bind(form) {
        form.addEventListener("submit", function (e) {
            var id = this.getAttribute("data-booking");
            var ok = confirm(
                "Hủy phòng sẽ hủy toàn bộ đơn #" + id
                + ", gồm cả những dịch vụ khác trong đơn (nếu có).\n\n"
                + "Sau khi hủy, bạn có thể đặt lại phòng cho bé vào khoảng thời gian khác.\n\n"
                + "Bạn chắc chắn muốn hủy?"
            );
            if (!ok) e.preventDefault();
        });
    }

    function init(root) {
        var scope = root || document;
        var forms = scope.querySelectorAll(".js-cancel-stay");
        for (var i = 0; i < forms.length; i++) {
            bind(forms[i]);
        }
    }

    window.StayActions = { init: init };
    document.addEventListener("DOMContentLoaded", function () { init(); });
})();
