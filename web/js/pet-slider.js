/*
 * Danh sách thú cưng trượt ngang ở trang chủ.
 *
 * Khi số bé còn ít, mọi thẻ nằm vừa trong khung nên hai mũi tên bị ẩn đi và
 * phần này trông y hệt một hàng thẻ bình thường. Chỉ khi danh sách dài hơn
 * khung — thường là từ bé thứ 5 trở đi trên màn hình rộng — mũi tên mới hiện.
 *
 * Mỗi lần bấm trượt đúng một "trang" bằng chiều rộng khung, và mũi tên ở đầu
 * hoặc cuối danh sách sẽ bị làm mờ để người dùng biết đã hết chỗ để trượt.
 */
(function () {
    "use strict";

    function setup(slider) {
        var track = slider.querySelector("[data-pet-track]");
        var prev = slider.querySelector("[data-pet-prev]");
        var next = slider.querySelector("[data-pet-next]");
        if (!track || !prev || !next) return;

        // Trừ 1px cho sai số làm tròn của trình duyệt khi tính chiều rộng
        function scrollable() {
            return track.scrollWidth - track.clientWidth > 1;
        }

        function refresh() {
            slider.classList.toggle("pet-slider--scrollable", scrollable());
            prev.disabled = track.scrollLeft <= 1;
            next.disabled = track.scrollLeft >= track.scrollWidth - track.clientWidth - 1;
        }

        function page(direction) {
            // Trượt gần trọn một khung, chừa lại chút để thấy thẻ kế bên
            var step = Math.max(track.clientWidth * 0.9, 200);
            track.scrollBy({ left: direction * step, behavior: "smooth" });
        }

        prev.addEventListener("click", function () { page(-1); });
        next.addEventListener("click", function () { page(1); });
        track.addEventListener("scroll", refresh, { passive: true });
        window.addEventListener("resize", refresh);

        // Ảnh thú cưng tải xong có thể làm đổi chiều cao/rộng, nên tính lại
        window.addEventListener("load", refresh);

        refresh();
    }

    document.addEventListener("DOMContentLoaded", function () {
        var sliders = document.querySelectorAll("[data-pet-slider]");
        for (var i = 0; i < sliders.length; i++) {
            setup(sliders[i]);
        }
    });
})();
