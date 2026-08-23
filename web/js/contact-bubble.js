/*
 * Bong bóng liên hệ ở thẻ "Bác sĩ trực 24/7" trên trang chủ.
 *
 * Thẻ là một <button>, bấm vào thì bong bóng hiện ra ngay phía trên với
 * hotline, Zalo, Facebook và email. Bong bóng đóng lại khi bấm ra ngoài,
 * bấm nút X, hoặc nhấn phím Esc — và khi đang mở thì thẻ ngừng trôi lên
 * xuống để người dùng còn bấm trúng các đường dẫn bên trong.
 */
(function () {
    "use strict";

    var wrap = document.querySelector("[data-contact]");
    if (!wrap) return;

    var toggle = wrap.querySelector("[data-contact-toggle]");
    var bubble = wrap.querySelector("[data-contact-bubble]");
    var closeBtn = wrap.querySelector("[data-contact-close]");
    if (!toggle || !bubble) return;

    function isOpen() {
        return !bubble.hasAttribute("hidden");
    }

    function open() {
        bubble.removeAttribute("hidden");
        wrap.classList.add("is-open");
        toggle.setAttribute("aria-expanded", "true");
    }

    function close() {
        bubble.setAttribute("hidden", "");
        wrap.classList.remove("is-open");
        toggle.setAttribute("aria-expanded", "false");
    }

    toggle.addEventListener("click", function (e) {
        e.stopPropagation();
        if (isOpen()) {
            close();
        } else {
            open();
        }
    });

    if (closeBtn) {
        closeBtn.addEventListener("click", function () {
            close();
            toggle.focus();
        });
    }

    // Bấm bên trong bong bóng thì giữ nguyên, chỉ bấm ra ngoài mới đóng
    bubble.addEventListener("click", function (e) {
        e.stopPropagation();
    });

    document.addEventListener("click", function () {
        if (isOpen()) close();
    });

    document.addEventListener("keydown", function (e) {
        if ((e.key === "Escape" || e.key === "Esc") && isOpen()) {
            close();
            toggle.focus();
        }
    });
})();
