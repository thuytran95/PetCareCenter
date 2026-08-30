/*
 * Trang chủ: kiểm tra đăng nhập qua API rồi hiển thị bảng tin hoặc form tra cứu.
 * Gọi lại khi vào / hoặc sau khi đăng nhập thành công (redirect về trang chủ).
 */
(function () {
    "use strict";

    var PET_COLORS = ["blue", "pink", "amber", "teal"];

    function ctx() {
        return document.body.getAttribute("data-context-path") || "";
    }

    function esc(text) {
        if (text == null) return "";
        return String(text)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;");
    }

    function setVisibility(loggedIn) {
        var member = document.getElementById("home-member-area");
        var guest = document.getElementById("home-guest-area");
        if (member) member.hidden = !loggedIn;
        if (guest) guest.hidden = loggedIn;
        document.body.setAttribute("data-home-logged-in", loggedIn ? "true" : "false");
    }

    function renderStay(pet, stay) {
        var cp = ctx();
        if (!stay) {
            return ""
                + '<div class="pet-stay pet-stay--none">'
                + '  <div class="pet-stay-top">'
                + '    <span class="pet-stay-dot pet-stay-dot--none"><i class="fa-regular fa-moon"></i></span>'
                + '    <div class="flex-grow-1">'
                + '      <div class="pet-stay-state">Chưa đặt phòng</div>'
                + '      <div class="pet-stay-room">Bé đang ở nhà cùng bạn</div>'
                + "    </div>"
                + "  </div>"
                + '  <div class="pet-stay-actions">'
                + '    <a class="pet-stay-link" href="' + cp + '/BookingServlet?petId=' + pet.petId + '&amp;serviceType=hotel">Đặt phòng cho bé</a>'
                + "  </div>"
                + "</div>";
        }

        var moreLink = stay.moreStays > 0
            ? '<a class="pet-stay-more" href="' + cp + "/myBookings?service=hotel&amp;petId=" + pet.petId + '">+' + stay.moreStays + " đợt đã đặt nữa</a>"
            : "";

        var actions = '<a class="pet-stay-link" href="' + cp + "/invoice?bookingId=" + stay.bookingId + '">Đơn #' + stay.bookingId + "</a>";
        if (stay.checkOutable) {
            actions += ""
                + '<form action="' + cp + '/bookingAction" method="post" class="d-inline">'
                + '  <input type="hidden" name="action" value="checkout">'
                + '  <input type="hidden" name="bookingId" value="' + stay.bookingId + '">'
                + '  <input type="hidden" name="back" value="index">'
                + '  <button type="submit" class="pet-stay-btn"><i class="fa-solid fa-right-from-bracket"></i> Trả phòng</button>'
                + "</form>";
        } else if (stay.cancellable) {
            actions += ""
                + '<form action="' + cp + '/bookingAction" method="post" class="d-inline js-cancel-stay" data-booking="' + stay.bookingId + '">'
                + '  <input type="hidden" name="action" value="cancel">'
                + '  <input type="hidden" name="bookingId" value="' + stay.bookingId + '">'
                + '  <input type="hidden" name="back" value="index">'
                + '  <button type="submit" class="pet-stay-btn pet-stay-btn--warn"><i class="fa-solid fa-xmark"></i> Hủy phòng</button>'
                + "</form>";
        } else if (stay.draft) {
            actions += '<a class="pet-stay-btn" href="' + cp + '/chooseService"><i class="fa-solid fa-arrow-right"></i> Hoàn tất đơn</a>';
        }

        return ""
            + '<div class="pet-stay pet-stay--' + esc(stay.stateColor) + '">'
            + '  <div class="pet-stay-top">'
            + '    <span class="pet-stay-dot bg-' + esc(stay.stateColor) + "-tint text-" + esc(stay.stateColor) + '"><i class="fa-solid fa-door-open"></i></span>'
            + '    <div class="flex-grow-1">'
            + '      <div class="pet-stay-state">' + esc(stay.stateText) + "</div>"
            + '      <div class="pet-stay-room">' + esc(stay.roomName) + " · " + esc(stay.formattedRange) + "</div>"
            + moreLink
            + "    </div>"
            + "  </div>"
            + '  <div class="pet-stay-actions">' + actions + "</div>"
            + "</div>";
    }

    function renderPetCard(pet) {
        var cp = ctx();
        var color = pet.color || PET_COLORS[0];
        var avatar = pet.photoBase64
            ? '<img src="data:image/png;base64,' + pet.photoBase64 + '" alt="' + esc(pet.name) + '" />'
            : '<i class="fa-solid fa-paw"></i>';

        return ""
            + '<div class="pet-slide">'
            + '  <div class="pet-card d-flex flex-column gap-3">'
            + '    <div class="d-flex align-items-center gap-3">'
            + '      <div class="pet-avatar bg-' + color + "-tint text-" + color + '">' + avatar + "</div>"
            + "      <div>"
            + '        <div class="fw-bold">' + esc(pet.name) + "</div>"
            + '        <div style="color:var(--text-body);font-size:12.5px;">' + esc(pet.species || "") + "</div>"
            + "      </div>"
            + "    </div>"
            + renderStay(pet, pet.stay)
            + '    <div class="pet-menu mt-auto">'
            + '      <a class="pet-menu-item" href="' + cp + "/BookingServlet?petId=" + pet.petId + '">'
            + '        <span class="pet-menu-icon bg-' + color + "-tint text-" + color + '"><i class="fa-regular fa-calendar-plus"></i></span>'
            + '        <span class="flex-grow-1"><span class="pet-menu-title">Đặt lịch cho bé</span></span>'
            + '        <i class="fa-solid fa-chevron-right pet-menu-go"></i>'
            + "      </a>"
            + '      <a class="pet-menu-item" href="' + cp + "/petHealth?petId=" + pet.petId + '">'
            + '        <span class="pet-menu-icon bg-amber-tint text-amber"><i class="fa-solid fa-syringe"></i></span>'
            + '        <span class="flex-grow-1"><span class="pet-menu-title">Khám định kỳ &amp; sổ tiêm</span></span>'
            + '        <i class="fa-solid fa-chevron-right pet-menu-go"></i>'
            + "      </a>"
            + "    </div>"
            + "  </div>"
            + "</div>";
    }

    function renderPets(pets) {
        var cp = ctx();
        var body = "";

        if (!pets || pets.length === 0) {
            body = ""
                + '<a class="pet-add-card mx-auto" style="max-width:360px;" href="' + cp + '/addPet">'
                + '  <i class="fa-solid fa-paw fa-2x"></i>'
                + "  <span class=\"fw-semibold\">Chưa có thú cưng nào, hãy thêm bé đầu tiên!</span>"
                + "</a>";
        } else {
            var slides = pets.map(renderPetCard).join("");
            body = ""
                + '<div class="pet-slider" data-pet-slider>'
                + '  <button class="pet-slider-nav pet-slider-nav--prev" type="button" data-pet-prev aria-label="Xem các bé trước đó">'
                + '    <i class="fa-solid fa-chevron-left"></i>'
                + "  </button>"
                + '  <div class="pet-track" data-pet-track>' + slides + "</div>"
                + '  <button class="pet-slider-nav pet-slider-nav--next" type="button" data-pet-next aria-label="Xem các bé tiếp theo">'
                + '    <i class="fa-solid fa-chevron-right"></i>'
                + "  </button>"
                + "</div>";
        }

        return ""
            + '<section class="my-pets">'
            + '  <div class="container">'
            + '    <div class="d-flex align-items-center justify-content-between flex-wrap gap-2 mb-4">'
            + "      <div>"
            + '        <h2 class="h4 fw-bold mb-1">Thú cưng của bạn</h2>'
            + '        <p class="mb-0" style="color:var(--text-body);font-size:14.5px;">Chọn một bé để đặt lịch chăm sóc ngay</p>'
            + "      </div>"
            + '      <a class="btn btn-outline-blue fw-semibold px-3 py-2" href="' + cp + '/petProfile">Quản lý hồ sơ</a>'
            + "    </div>"
            + body
            + "  </div>"
            + "</section>";
    }

    function renderHealthDue(items) {
        if (!items || items.length === 0) return "";

        var cards = items.map(function (hr) {
            var cp = ctx();
            return ""
                + '<div class="col-12 col-md-6">'
                + '  <div class="hl-due' + (hr.overdue ? " hl-due--late" : "") + '">'
                + '    <div class="hl-icon bg-' + esc(hr.colorName) + "-tint text-" + esc(hr.colorName) + '">'
                + '      <i class="fa-solid ' + esc(hr.iconClass) + '"></i>'
                + "    </div>"
                + '    <div class="flex-grow-1">'
                + '      <div class="fw-bold">' + esc(hr.itemName) + "</div>"
                + '      <div class="hl-meta"><i class="fa-solid fa-paw"></i> ' + esc(hr.petName) + " · Hạn: " + esc(hr.formattedNextDueAt) + "</div>"
                + '      <a class="dash-link" href="' + cp + "/petHealth?petId=" + hr.petId + '">Xem sổ sức khỏe</a>'
                + "    </div>"
                + '    <span class="hl-badge' + (hr.overdue ? " hl-badge--late" : "") + '">' + esc(hr.dueText) + "</span>"
                + "  </div>"
                + "</div>";
        }).join("");

        return ""
            + '<section class="dashboard pb-0">'
            + '  <div class="container">'
            + '    <h2 class="h5 fw-bold mb-3"><i class="fa-solid fa-syringe me-2" style="color:var(--amber);"></i>Nhắc tiêm phòng &amp; khám định kỳ</h2>'
            + '    <div class="row g-3">' + cards + "</div>"
            + "  </div>"
            + "</section>";
    }

    function renderAppointments(upcoming, recentBookings) {
        var cp = ctx();
        var apptBody;

        if (!upcoming || upcoming.length === 0) {
            apptBody = ""
                + '<div class="dash-empty">'
                + '  <i class="fa-regular fa-calendar"></i>'
                + "  <span>Chưa có lịch hẹn nào sắp tới</span>"
                + "</div>";
        } else {
            apptBody = upcoming.map(function (ap) {
                var room = ap.roomLabel ? " · " + esc(ap.roomLabel) : "";
                return ""
                    + '<div class="dash-appt' + (ap.urgent ? " dash-appt--urgent" : "") + '">'
                    + '  <div class="dash-appt-icon bg-' + esc(ap.colorName) + "-tint text-" + esc(ap.colorName) + '">'
                    + '    <i class="fa-solid ' + esc(ap.iconClass) + '"></i>'
                    + "  </div>"
                    + '  <div class="flex-grow-1">'
                    + '    <div class="d-flex align-items-center gap-2 flex-wrap">'
                    + '      <span class="fw-bold">' + esc(ap.serviceLabel) + "</span>"
                    + '      <span class="dash-pet"><i class="fa-solid fa-paw"></i> ' + esc(ap.petName) + "</span>"
                    + "    </div>"
                    + '    <div class="dash-appt-time">' + esc(ap.formattedStartAt) + room + "</div>"
                    + "  </div>"
                    + '  <div class="text-end">'
                    + '    <div class="dash-badge' + (ap.urgent ? " dash-badge--urgent" : "") + '">' + esc(ap.reminderText) + "</div>"
                    + '    <a class="dash-link" href="' + cp + "/invoice?bookingId=" + ap.bookingId + '">Xem hóa đơn</a>'
                    + "  </div>"
                    + "</div>";
            }).join("");
        }

        var invoiceBody;
        if (!recentBookings || recentBookings.length === 0) {
            invoiceBody = ""
                + '<div class="dash-empty">'
                + '  <i class="fa-solid fa-receipt"></i>'
                + "  <span>Chưa có hóa đơn nào</span>"
                + "</div>";
        } else {
            invoiceBody = recentBookings.map(function (bk) {
                return ""
                    + '<a class="dash-invoice" href="' + cp + "/invoice?bookingId=" + bk.bookingId + '">'
                    + '  <div class="d-flex justify-content-between align-items-start gap-2">'
                    + "    <div>"
                    + '      <div class="fw-bold">Đơn #' + bk.bookingId + "</div>"
                    + '      <div class="dash-appt-time">' + esc(bk.petName) + " · " + esc(bk.formattedCreatedAt) + "</div>"
                    + "    </div>"
                    + '    <div class="text-end">'
                    + '      <div class="dash-amount">' + esc(bk.amount) + " đ</div>"
                    + '      <span class="dash-status dash-status--' + esc((bk.status || "").toLowerCase()) + '">' + esc(bk.status) + "</span>"
                    + "    </div>"
                    + "  </div>"
                    + "</a>";
            }).join("");
        }

        return ""
            + '<section class="dashboard">'
            + '  <div class="container">'
            + '    <div class="row g-4">'
            + '      <div class="col-12 col-lg-7">'
            + '        <h2 class="h5 fw-bold mb-3"><i class="fa-regular fa-calendar-check me-2 text-primary-blue"></i>Lịch hẹn sắp tới</h2>'
            + apptBody
            + "      </div>"
            + '      <div class="col-12 col-lg-5">'
            + '        <div class="d-flex align-items-center justify-content-between gap-2 mb-3">'
            + '          <h2 class="h5 fw-bold mb-0"><i class="fa-solid fa-receipt me-2" style="color:var(--amber);"></i>Hóa đơn gần đây</h2>'
            + '          <a class="dash-link" href="' + cp + '/myBookings">Xem tất cả <i class="fa-solid fa-arrow-right"></i></a>'
            + "        </div>"
            + invoiceBody
            + "      </div>"
            + "    </div>"
            + "  </div>"
            + "</section>";
    }

    function renderDashboard(data) {
        var petsMount = document.getElementById("home-pets-mount");
        var healthMount = document.getElementById("home-health-mount");
        var apptMount = document.getElementById("home-appt-mount");
        if (!petsMount || !healthMount || !apptMount) return;

        petsMount.innerHTML = renderPets(data.pets || []);
        healthMount.innerHTML = renderHealthDue(data.healthDue || []);
        apptMount.innerHTML = renderAppointments(data.upcoming || [], data.recentBookings || []);

        if (window.PetSlider && window.PetSlider.init) {
            window.PetSlider.init(document.getElementById("home-member-area"));
        }
        if (window.StayActions && window.StayActions.init) {
            window.StayActions.init(document.getElementById("home-member-area"));
        }
    }

    function loadDashboard() {
        return fetch(ctx() + "/api/homeDashboard", {
            credentials: "same-origin",
            headers: { Accept: "application/json" }
        })
            .then(function (res) { return res.json(); })
            .then(function (data) {
                if (!data.loggedIn) {
                    setVisibility(false);
                    return;
                }
                setVisibility(true);
                if (data.error) {
                    renderDashboard({ pets: [], upcoming: [], recentBookings: [], healthDue: [] });
                    return;
                }
                renderDashboard(data);
            })
            .catch(function () {
                var hint = document.body.getAttribute("data-home-logged-in") === "true";
                setVisibility(hint);
                if (hint) {
                    renderDashboard({ pets: [], upcoming: [], recentBookings: [], healthDue: [] });
                }
            });
    }

    document.addEventListener("DOMContentLoaded", loadDashboard);
    window.HomeDashboard = { reload: loadDashboard };
})();
