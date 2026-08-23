/*
 * Bảng tạm tính realtime cạnh form đặt dịch vụ.
 *
 * Cách hoạt động: mỗi lựa chọn trong form mang sẵn data-price (và data-label),
 * script chỉ cộng lại để hiển thị cho người dùng thấy ngay.
 *
 * LƯU Ý: đây thuần túy là phần hiển thị. Số tiền thật do BookingService tính lại
 * từ bảng giá trong CSDL khi bấm xác nhận, nên có sửa giá trị trong HTML cũng
 * không ảnh hưởng số tiền được lưu.
 */
(function () {
    "use strict";

    var panel = document.getElementById("summaryPanel");
    var form = document.querySelector("form[data-summary-form]");
    if (!panel || !form) return;

    var liveItems = document.getElementById("liveItems");
    var liveEmpty = document.getElementById("liveEmpty");
    var liveSubtotalEl = document.getElementById("liveSubtotal");
    var grandTotalEl = document.getElementById("grandTotal");

    var addedTotal = parseFloat(panel.dataset.addedTotal || "0") || 0;

    function formatVnd(value) {
        return Math.round(value).toLocaleString("vi-VN") + " đ";
    }

    /** Số ngày ở, khớp quy tắc phía máy chủ: trong ngày vẫn tính tròn 1 ngày. */
    function countDays() {
        var checkIn = form.querySelector('input[name="checkIn"]');
        var checkOut = form.querySelector('input[name="checkOut"]');
        if (!checkIn || !checkOut || !checkIn.value || !checkOut.value) return 0;

        var start = new Date(checkIn.value);
        var end = new Date(checkOut.value);
        if (isNaN(start) || isNaN(end) || end <= start) return 0;

        var days = Math.floor((end - start) / 86400000);
        return days <= 0 ? 1 : days;
    }

    function recalc() {
        var rows = [];
        var subtotal = 0;
        var days = countDays();

        form.querySelectorAll("input[data-price]").forEach(function (input) {
            if (!input.checked) return;

            var unit = parseFloat(input.dataset.price) || 0;
            var label = input.dataset.label || "Dịch vụ";
            var amount = unit;
            var meta = "";

            // Phòng khách sạn tính theo số ngày; các dịch vụ khác tính theo lần
            if (input.dataset.perDay === "true") {
                if (days === 0) {
                    meta = " (chọn ngày để tính)";
                    amount = 0;
                } else {
                    meta = " × " + days + " ngày";
                    amount = unit * days;
                }
            }

            rows.push({ label: label + meta, amount: amount });
            subtotal += amount;
        });

        renderRows(rows);
        liveSubtotalEl.textContent = formatVnd(subtotal);
        grandTotalEl.textContent = formatVnd(addedTotal + subtotal);
    }

    function renderRows(rows) {
        var existingList = liveItems.querySelector("ul");
        if (existingList) existingList.remove();

        if (rows.length === 0) {
            if (liveEmpty) liveEmpty.style.display = "";
            return;
        }
        if (liveEmpty) liveEmpty.style.display = "none";

        var ul = document.createElement("ul");
        rows.forEach(function (row) {
            var li = document.createElement("li");
            var name = document.createElement("span");
            var price = document.createElement("span");
            // Dùng textContent để nội dung từ dữ liệu không thể chèn HTML
            name.textContent = row.label;
            price.textContent = formatVnd(row.amount);
            li.appendChild(name);
            li.appendChild(price);
            ul.appendChild(li);
        });
        liveItems.appendChild(ul);
    }

    form.addEventListener("change", recalc);
    form.addEventListener("input", recalc);
    recalc();
})();
