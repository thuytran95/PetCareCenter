/*
 * Cập nhật số phòng trống ngay khi khách đổi ngày nhận / trả phòng.
 *
 * Trước đây khách phải chọn phòng, chọn ngày, bấm "Đặt phòng" rồi mới biết
 * hạng đó đã hết chỗ. Giờ trang hỏi máy chủ mỗi lần đổi ngày và hiện luôn
 * "Còn 2/3 phòng" hay "Hết phòng" trên từng hạng, đồng thời khóa những hạng
 * không còn chỗ để khách không chọn nhầm.
 *
 * Con số luôn lấy mới từ máy chủ chứ không tính ở trình duyệt, vì phòng có thể
 * bị người khác đặt mất ngay trong lúc khách đang cân nhắc. Việc chặn thật sự
 * vẫn nằm ở máy chủ lúc bấm đặt — phần này chỉ để khách biết trước.
 */
(function () {
    "use strict";

    var DEBOUNCE_MS = 350;
    var MIN_STAY_MS = 2 * 60 * 60 * 1000;

    function toLocalInputValue(date) {
        var pad = function (n) { return n < 10 ? "0" + n : "" + n; };
        return date.getFullYear() + "-" + pad(date.getMonth() + 1) + "-" + pad(date.getDate())
            + "T" + pad(date.getHours()) + ":" + pad(date.getMinutes());
    }

    function stayTooShort(start, end) {
        return !start || !end || isNaN(start) || isNaN(end) || end - start < MIN_STAY_MS;
    }

    function setup(form) {
        var url = form.getAttribute("data-availability-url");
        var checkIn = form.querySelector("#checkIn");
        var checkOut = form.querySelector("#checkOut");
        var list = form.querySelector("[data-rooms-list]");
        var hint = form.querySelector("[data-rooms-hint]");
        if (!url || !checkIn || !checkOut || !list) return;

        var hintText = hint ? hint.querySelector("[data-rooms-hint-text]") : null;
        var hintIcon = hint ? hint.querySelector("[data-rooms-hint-icon]") : null;
        var timer = null;
        var seq = 0;

        function setHint(text, live) {
            if (!hint) return;
            hint.classList.toggle("rm-hint--live", !!live);
            hint.classList.remove("rm-hint--err");
            if (hintText) hintText.textContent = text;
            if (hintIcon) {
                hintIcon.className = "fa-solid " + (live ? "fa-circle-check" : "fa-circle-info");
            }
        }

        function setError(text) {
            if (!hint) return;
            hint.classList.remove("rm-hint--live");
            hint.classList.add("rm-hint--err");
            if (hintText) hintText.textContent = text;
            if (hintIcon) hintIcon.className = "fa-solid fa-triangle-exclamation";
        }

        function apply(rooms) {
            for (var i = 0; i < rooms.length; i++) {
                var r = rooms[i];
                var option = list.querySelector('[data-room="' + r.roomCode + '"]');
                if (!option) continue;

                var status = option.querySelector("[data-room-status]");
                if (status) status.textContent = r.statusLabel;

                var badge = option.querySelector("[data-room-badge]");
                if (badge) {
                    badge.className = "rm-badge bg-" + r.statusColor + "-tint text-" + r.statusColor;
                }

                option.classList.toggle("item-option--off", r.soldOut);
                var radio = option.querySelector('input[type="radio"]');
                if (radio) {
                    radio.disabled = r.soldOut;
                    // Hạng vừa hết chỗ mà đang được chọn thì bỏ chọn, kèm cập nhật
                    // bảng tạm tính để số tiền không còn dính hạng phòng đó
                    if (r.soldOut && radio.checked) {
                        radio.checked = false;
                        radio.dispatchEvent(new Event("change", { bubbles: true }));
                    }
                }
            }
        }

        function syncCheckOut() {
            if (!checkIn.value) return;
            var start = new Date(checkIn.value);
            if (isNaN(start)) return;

            var minOut = new Date(start.getTime() + MIN_STAY_MS);
            checkOut.min = toLocalInputValue(minOut);

            var current = checkOut.value ? new Date(checkOut.value) : null;
            if (!current || isNaN(current) || stayTooShort(start, current)) {
                checkOut.value = toLocalInputValue(minOut);
            }
        }

        function refresh() {
            var a = checkIn.value, b = checkOut.value;
            if (!a || !b) {
                setHint("Chọn ngày nhận và trả phòng để xem còn bao nhiêu phòng trống.", false);
                return;
            }
            var start = new Date(a), end = new Date(b);
            if (stayTooShort(start, end)) {
                setError("Thời gian lưu trú tối thiểu là 2 giờ. Vui lòng chọn lại ngày trả phòng.");
                return;
            }

            var mine = ++seq;
            setHint("Đang kiểm tra phòng trống…", false);

            fetch(url + "?checkIn=" + encodeURIComponent(a) + "&checkOut=" + encodeURIComponent(b),
                  { headers: { "Accept": "application/json" }, credentials: "same-origin" })
                .then(function (res) {
                    if (!res.ok) throw new Error("HTTP " + res.status);
                    return res.json();
                })
                .then(function (data) {
                    // Bỏ qua kết quả của lần hỏi cũ về muộn hơn lần hỏi mới
                    if (mine !== seq) return;
                    if (!data || !data.rooms) throw new Error("Dữ liệu không hợp lệ");
                    apply(data.rooms);
                    setHint("Số phòng trống bên dưới tính cho đúng khoảng ngày bạn đã chọn.", true);
                })
                .catch(function () {
                    if (mine !== seq) return;
                    setError("Chưa kiểm tra được phòng trống. Bạn vẫn đặt được, hệ thống sẽ kiểm tra lại khi xác nhận.");
                });
        }

        function schedule() {
            clearTimeout(timer);
            timer = setTimeout(refresh, DEBOUNCE_MS);
        }

        function onCheckInChange() {
            syncCheckOut();
            schedule();
        }

        checkIn.addEventListener("change", onCheckInChange);
        checkOut.addEventListener("change", schedule);
        checkIn.addEventListener("input", onCheckInChange);
        checkOut.addEventListener("input", schedule);

        form.addEventListener("submit", function (e) {
            var start = new Date(checkIn.value);
            var end = new Date(checkOut.value);
            if (stayTooShort(start, end)) {
                e.preventDefault();
                setError("Thời gian lưu trú tối thiểu là 2 giờ. Vui lòng chọn lại ngày trả phòng.");
                checkOut.focus();
            }
        });

        syncCheckOut();
        // Ngày có sẵn (quay lại form sau khi bị báo lỗi) thì kiểm tra luôn
        if (checkIn.value && checkOut.value) refresh();
    }

    document.addEventListener("DOMContentLoaded", function () {
        var forms = document.querySelectorAll("[data-availability-url]");
        for (var i = 0; i < forms.length; i++) setup(forms[i]);
    });
})();
