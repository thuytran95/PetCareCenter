/*
 * Danh sách thú cưng trượt ngang ở trang chủ.
 */
(function () {
    "use strict";

    function setup(slider) {
        var track = slider.querySelector("[data-pet-track]");
        var prev = slider.querySelector("[data-pet-prev]");
        var next = slider.querySelector("[data-pet-next]");
        if (!track || !prev || !next) return;

        function scrollable() {
            return track.scrollWidth - track.clientWidth > 1;
        }

        function refresh() {
            slider.classList.toggle("pet-slider--scrollable", scrollable());
            prev.disabled = track.scrollLeft <= 1;
            next.disabled = track.scrollLeft >= track.scrollWidth - track.clientWidth - 1;
        }

        function page(direction) {
            var step = Math.max(track.clientWidth * 0.9, 200);
            track.scrollBy({ left: direction * step, behavior: "smooth" });
        }

        prev.addEventListener("click", function () { page(-1); });
        next.addEventListener("click", function () { page(1); });
        track.addEventListener("scroll", refresh, { passive: true });
        window.addEventListener("resize", refresh);
        window.addEventListener("load", refresh);
        refresh();
    }

    function init(root) {
        var scope = root || document;
        var sliders = scope.querySelectorAll("[data-pet-slider]");
        for (var i = 0; i < sliders.length; i++) {
            setup(sliders[i]);
        }
    }

    window.PetSlider = { init: init };
    document.addEventListener("DOMContentLoaded", function () { init(); });
})();
