<%-- Document : index Created on : Sep 6, 2025, 10:03:17 AM Author : Admin --%>

<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@page import="java.util.List, java.util.Base64, com.petweb.model.Pet, com.petweb.model.Appointment, com.petweb.model.Booking, com.petweb.model.HealthRecord, com.petweb.model.PetStay, java.util.Map" %>
<%
    // Truy cập trực tiếp /index.jsp → chuyển sang servlet gốc để nạp dữ liệu
    if (!Boolean.TRUE.equals(request.getAttribute("fromHomeController"))) {
        response.sendRedirect(request.getContextPath() + "/");
        return;
    }
%>
<!DOCTYPE html>
<html>

<head>
    <base href="${pageContext.request.contextPath}/">
    <meta charset="UTF-8">
    <title>Pet Care Center - Trung tâm chăm sóc thú cưng</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <jsp:include page="linkgroup.jsp"></jsp:include>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css"
        rel="stylesheet">
    <link rel="stylesheet" href="css/header.css">
    <link rel="stylesheet" href="css/footer.css" />
    <link rel="stylesheet" href="css/home.css">
    <link rel="stylesheet" href="css/pet.css">
    <link rel="stylesheet" href="css/health.css">
</head>

<body>
    <jsp:include page="Header.jsp"></jsp:include>
    <section class="banner">
        <span class="banner-blob banner-blob--1" aria-hidden="true"></span>
        <span class="banner-blob banner-blob--2" aria-hidden="true"></span>
        <div class="container position-relative">
            <div class="row align-items-center g-5">
                <div class="col-12 col-lg-6">
                    <span class="banner-eyebrow">
                        <i class="fa-solid fa-paw"></i> Trung tâm chăm sóc thú cưng tận tâm
                    </span>
                    <h1 class="banner-title">
                        Nơi thú cưng được <span class="banner-title-mark">yêu thương</span> trọn vẹn
                    </h1>
                    <div class="banner-content">
                        <p>Trung tâm Chăm sóc thú cưng là nơi gửi gắm yêu thương để thú cưng luôn khỏe mạnh,
                            hạnh phúc và được nâng niu như những người bạn nhỏ trong gia đình.</p>
                    </div>
                    <div class="banner-actions">
                        <a class="btn btn-primary-blue" href="<%=request.getContextPath()%>/BookingServlet">
                            Đặt lịch ngay <i class="fa-solid fa-arrow-right ms-1"></i>
                        </a>
                        <a class="btn btn-outline-blue" href="#dich-vu">Khám phá dịch vụ</a>
                    </div>
                    <ul class="banner-stats">
                        <li>
                            <strong>1.200+</strong>
                            <span>Thú cưng được chăm sóc</span>
                        </li>
                        <li>
                            <strong>15+</strong>
                            <span>Bác sĩ &amp; kỹ thuật viên</span>
                        </li>
                        <li>
                            <strong>4.9/5</strong>
                            <span>Đánh giá từ khách hàng</span>
                        </li>
                    </ul>
                </div>
                <div class="col-12 col-lg-6">
                    <div class="banner-image banner-image-frame">
                        <img class="object-cover" src="image/banner.jpg"
                            alt="Thú cưng được chăm sóc tại trung tâm" />
                        <div class="banner-float-anchor banner-float--rating">
                            <div class="banner-float">
                                <span class="banner-float-icon bg-amber-tint text-amber">
                                    <i class="fa-solid fa-star"></i>
                                </span>
                                <span class="banner-float-text">
                                    <strong>4.9/5 sao</strong>
                                    <span>Hơn 800 lượt đánh giá</span>
                                </span>
                            </div>
                        </div>

                        <%-- Thẻ này bấm được: mở bong bóng liên hệ ngay bên cạnh --%>
                        <div class="banner-float-anchor banner-float--care" data-contact>
                            <button type="button" class="banner-float banner-float-btn"
                                data-contact-toggle aria-expanded="false" aria-controls="contactBubble">
                                <span class="banner-float-icon bg-teal-tint text-teal">
                                    <i class="fa-solid fa-user-doctor"></i>
                                </span>
                                <span class="banner-float-text">
                                    <strong>Bác sĩ trực 24/7</strong>
                                    <span>Bấm để xem liên hệ</span>
                                </span>
                                <i class="fa-solid fa-chevron-up banner-float-caret" aria-hidden="true"></i>
                            </button>

                            <div class="contact-bubble" id="contactBubble" data-contact-bubble hidden>
                                <div class="contact-bubble-head">
                                    <span class="banner-float-icon bg-teal-tint text-teal">
                                        <i class="fa-solid fa-headset"></i>
                                    </span>
                                    <span class="banner-float-text">
                                        <strong>Hỗ trợ 24/7</strong>
                                        <span>Trực cả cuối tuần &amp; ngày lễ</span>
                                    </span>
                                    <button type="button" class="contact-bubble-close" data-contact-close
                                        aria-label="Đóng">
                                        <i class="fa-solid fa-xmark"></i>
                                    </button>
                                </div>

                                <ul class="contact-list">
                                    <li>
                                        <a href="tel:19008386">
                                            <span class="contact-ico bg-teal-tint text-teal">
                                                <i class="fa-solid fa-phone"></i>
                                            </span>
                                            <span class="contact-label">Hotline
                                                <small>1900 1111</small>
                                            </span>
                                        </a>
                                    </li>
                                    <li>
                                        <a href="https://zalo.me/0912345678" target="_blank" rel="noopener">
                                            <span class="contact-ico bg-blue-tint text-blue">
                                                <i class="fa-solid fa-comment-dots"></i>
                                            </span>
                                            <span class="contact-label">Zalo
                                                <small>0111 111 111</small>
                                            </span>
                                        </a>
                                    </li>
                                    <li>
                                        <a href="https://www.facebook.com/petcarecenter.vn" target="_blank"
                                            rel="noopener">
                                            <span class="contact-ico bg-purple-tint text-purple">
                                                <i class="fa-brands fa-facebook-f"></i>
                                            </span>
                                            <span class="contact-label">Facebook
                                                <small>Pet Care Center</small>
                                            </span>
                                        </a>
                                    </li>
                                    <li>
                                        <a href="mailto:hotro@petcarecenter.vn">
                                            <span class="contact-ico bg-amber-tint text-amber">
                                                <i class="fa-regular fa-envelope"></i>
                                            </span>
                                            <span class="contact-label">Email
                                                <small>hotro@petcarecenter.vn</small>
                                            </span>
                                        </a>
                                    </li>
                                </ul>

                                <p class="contact-note">
                                    <i class="fa-solid fa-location-dot"></i>
                                    Địa chỉ: 78 Tô Ngọc Vân, Phường Tây Hồ, Thành phố Hà Nội
                                </p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="banner-trust">
                <div class="banner-trust-item">
                    <span class="banner-trust-icon bg-pink-tint text-pink">
                        <i class="fa-solid fa-heart"></i>
                    </span>
                    <div>
                        <h3>Chăm sóc tận tâm</h3>
                        <p>Đội ngũ giàu kinh nghiệm, yêu thương các bé như thú cưng của chính mình.</p>
                    </div>
                </div>
                <div class="banner-trust-item">
                    <span class="banner-trust-icon bg-blue-tint text-blue">
                        <i class="fa-regular fa-calendar-check"></i>
                    </span>
                    <div>
                        <h3>Đặt lịch online</h3>
                        <p>Chọn dịch vụ và khung giờ phù hợp chỉ trong vài bước, không cần chờ đợi.</p>
                    </div>
                </div>
                <div class="banner-trust-item">
                    <span class="banner-trust-icon bg-purple-tint text-purple">
                        <i class="fa-solid fa-notes-medical"></i>
                    </span>
                    <div>
                        <h3>Hồ sơ sức khỏe</h3>
                        <p>Lưu lại lịch sử khám, tiêm phòng để theo dõi sức khỏe các bé trọn đời.</p>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <%-- Khối chỉ hiện với khách đã đăng nhập; danh sách do HomeController nạp sẵn --%>
    <% List<Pet> myPets = (List<Pet>) request.getAttribute("myPets");
       Map<Integer, List<PetStay>> homeStays = (Map<Integer, List<PetStay>>) request.getAttribute("stays");
       if (myPets != null) {
           String[] homePetColors = {"blue", "pink", "amber", "teal"}; %>
    <section class="my-pets">
        <div class="container">
            <div class="d-flex align-items-center justify-content-between flex-wrap gap-2 mb-4">
                <div>
                    <h2 class="h4 fw-bold mb-1">Thú cưng của bạn</h2>
                    <p class="mb-0" style="color:var(--text-body);font-size:14.5px;">
                        Chọn một bé để đặt lịch chăm sóc ngay
                    </p>
                </div>
                <a class="btn btn-outline-blue fw-semibold px-3 py-2"
                   href="<%=request.getContextPath()%>/petProfile">Quản lý hồ sơ</a>
            </div>

            <% if (myPets.isEmpty()) { %>
            <a class="pet-add-card mx-auto" style="max-width:360px;"
               href="<%=request.getContextPath()%>/addPet">
                <i class="fa-solid fa-paw fa-2x"></i>
                <span class="fw-semibold">Chưa có thú cưng nào, hãy thêm bé đầu tiên!</span>
            </a>
            <% } else { %>
            <%-- Danh sách trượt ngang: nhiều bé thì bấm mũi tên thay vì tràn xuống dòng --%>
            <div class="pet-slider" data-pet-slider>
                <button class="pet-slider-nav pet-slider-nav--prev" type="button"
                        data-pet-prev aria-label="Xem các bé trước đó">
                    <i class="fa-solid fa-chevron-left"></i>
                </button>

                <div class="pet-track" data-pet-track>
                <% int hIdx = 0;
                   for (Pet hp : myPets) {
                       String hc = homePetColors[hIdx % homePetColors.length];
                       hIdx++;
                       List<PetStay> hPetStays = (homeStays == null) ? null : homeStays.get(hp.getPetId());
                       PetStay hStay = (hPetStays == null || hPetStays.isEmpty()) ? null : hPetStays.get(0);
                       int hMoreStays = (hPetStays == null) ? 0 : hPetStays.size() - 1; %>
                <div class="pet-slide">
                    <div class="pet-card d-flex flex-column gap-3">
                        <div class="d-flex align-items-center gap-3">
                            <div class="pet-avatar bg-<%= hc %>-tint text-<%= hc %>">
                                <% if (hp.getPhoto() != null) { %>
                                <img src="data:image/png;base64,<%= Base64.getEncoder().encodeToString(hp.getPhoto()) %>"
                                     alt="<%= hp.getName() %>" />
                                <% } else { %>
                                <i class="fa-solid fa-paw"></i>
                                <% } %>
                            </div>
                            <div>
                                <div class="fw-bold"><%= hp.getName() %></div>
                                <div style="color:var(--text-body);font-size:12.5px;">
                                    <%= hp.getSpecies() == null ? "" : hp.getSpecies() %>
                                </div>
                            </div>
                        </div>
                        <%-- Tình trạng lưu trú, cùng dữ liệu với trang hồ sơ.
                             Bé chưa đặt phòng vẫn có khối cùng chiều cao để các thẻ
                             trong dải trượt không cao thấp lệch nhau. --%>
                        <% if (hStay == null) { %>
                        <div class="pet-stay pet-stay--none">
                            <div class="pet-stay-top">
                                <span class="pet-stay-dot pet-stay-dot--none">
                                    <i class="fa-regular fa-moon"></i>
                                </span>
                                <div class="flex-grow-1">
                                    <div class="pet-stay-state">Chưa đặt phòng</div>
                                    <div class="pet-stay-room">Bé đang ở nhà cùng bạn</div>
                                </div>
                            </div>
                            <div class="pet-stay-actions">
                                <a class="pet-stay-link"
                                   href="<%=request.getContextPath()%>/BookingServlet?petId=<%= hp.getPetId() %>&amp;serviceType=hotel">
                                    Đặt phòng cho bé
                                </a>
                            </div>
                        </div>
                        <% } else { %>
                        <div class="pet-stay pet-stay--<%= hStay.getStateColor() %>">
                            <div class="pet-stay-top">
                                <span class="pet-stay-dot bg-<%= hStay.getStateColor() %>-tint text-<%= hStay.getStateColor() %>">
                                    <i class="fa-solid fa-door-open"></i>
                                </span>
                                <div class="flex-grow-1">
                                    <div class="pet-stay-state"><%= hStay.getStateText() %></div>
                                    <div class="pet-stay-room">
                                        <%= hStay.getRoomName() %> · <%= hStay.getFormattedRange() %>
                                    </div>
                                    <% if (hMoreStays > 0) { %>
                                    <a class="pet-stay-more"
                                       href="<%=request.getContextPath()%>/myBookings?service=hotel&amp;petId=<%= hp.getPetId() %>">
                                        +<%= hMoreStays %> đợt đã đặt nữa
                                    </a>
                                    <% } %>
                                </div>
                            </div>
                            <div class="pet-stay-actions">
                                <a class="pet-stay-link"
                                   href="<%=request.getContextPath()%>/invoice?bookingId=<%= hStay.getBookingId() %>">
                                    Đơn #<%= hStay.getBookingId() %>
                                </a>
                                <% if (hStay.isCheckOutable()) { %>
                                <form action="<%=request.getContextPath()%>/bookingAction" method="post"
                                      class="d-inline">
                                    <input type="hidden" name="action" value="checkout">
                                    <input type="hidden" name="bookingId" value="<%= hStay.getBookingId() %>">
                                    <input type="hidden" name="back" value="index">
                                    <button type="submit" class="pet-stay-btn">
                                        <i class="fa-solid fa-right-from-bracket"></i> Trả phòng
                                    </button>
                                </form>
                                <% } else if (hStay.isCancellable()) { %>
                                <form action="<%=request.getContextPath()%>/bookingAction" method="post"
                                      class="d-inline js-cancel-stay"
                                      data-booking="<%= hStay.getBookingId() %>">
                                    <input type="hidden" name="action" value="cancel">
                                    <input type="hidden" name="bookingId" value="<%= hStay.getBookingId() %>">
                                    <input type="hidden" name="back" value="index">
                                    <button type="submit" class="pet-stay-btn pet-stay-btn--warn">
                                        <i class="fa-solid fa-xmark"></i> Hủy phòng
                                    </button>
                                </form>
                                <% } else if (hStay.isDraft()) { %>
                                <a class="pet-stay-btn"
                                   href="<%=request.getContextPath()%>/chooseService">
                                    <i class="fa-solid fa-arrow-right"></i> Hoàn tất đơn
                                </a>
                                <% } %>
                            </div>
                        </div>
                        <% } %>

                        <div class="pet-menu mt-auto">
                            <a class="pet-menu-item"
                               href="<%=request.getContextPath()%>/BookingServlet?petId=<%= hp.getPetId() %>">
                                <span class="pet-menu-icon bg-<%= hc %>-tint text-<%= hc %>">
                                    <i class="fa-regular fa-calendar-plus"></i>
                                </span>
                                <span class="flex-grow-1">
                                    <span class="pet-menu-title">Đặt lịch cho bé</span>
                                </span>
                                <i class="fa-solid fa-chevron-right pet-menu-go"></i>
                            </a>
                            <a class="pet-menu-item"
                               href="<%=request.getContextPath()%>/petHealth?petId=<%= hp.getPetId() %>">
                                <span class="pet-menu-icon bg-amber-tint text-amber">
                                    <i class="fa-solid fa-syringe"></i>
                                </span>
                                <span class="flex-grow-1">
                                    <span class="pet-menu-title">Khám định kỳ &amp; sổ tiêm</span>
                                </span>
                                <i class="fa-solid fa-chevron-right pet-menu-go"></i>
                            </a>
                        </div>
                    </div>
                </div>
                <% } %>
                </div>

                <button class="pet-slider-nav pet-slider-nav--next" type="button"
                        data-pet-next aria-label="Xem các bé tiếp theo">
                    <i class="fa-solid fa-chevron-right"></i>
                </button>
            </div>
            <% } %>
        </div>
    </section>
    <% } %>

    <%-- Nhắc tiêm phòng / khám định kỳ sắp tới hạn --%>
    <% List<HealthRecord> healthDue = (List<HealthRecord>) request.getAttribute("healthDue");
       if (healthDue != null && !healthDue.isEmpty()) { %>
    <section class="dashboard pb-0">
        <div class="container">
            <h2 class="h5 fw-bold mb-3">
                <i class="fa-solid fa-syringe me-2" style="color:var(--amber);"></i>Nhắc tiêm phòng &amp; khám định kỳ
            </h2>
            <div class="row g-3">
                <% for (HealthRecord hr : healthDue) { %>
                <div class="col-12 col-md-6">
                    <div class="hl-due <%= hr.isOverdue() ? "hl-due--late" : "" %>">
                        <div class="hl-icon bg-<%= hr.getColorName() %>-tint text-<%= hr.getColorName() %>">
                            <i class="fa-solid <%= hr.getIconClass() %>"></i>
                        </div>
                        <div class="flex-grow-1">
                            <div class="fw-bold"><%= hr.getItemName() %></div>
                            <div class="hl-meta">
                                <i class="fa-solid fa-paw"></i> <%= hr.getPetName() %>
                                · Hạn: <%= hr.getFormattedNextDueAt() %>
                            </div>
                            <a class="dash-link"
                               href="<%=request.getContextPath()%>/petHealth?petId=<%= hr.getPetId() %>">
                                Xem sổ sức khỏe
                            </a>
                        </div>
                        <span class="hl-badge <%= hr.isOverdue() ? "hl-badge--late" : "" %>">
                            <%= hr.getDueText() %>
                        </span>
                    </div>
                </div>
                <% } %>
            </div>
        </div>
    </section>
    <% } %>

    <%-- Lịch hẹn sắp tới & hóa đơn gần đây: chỉ hiện với khách đã đăng nhập --%>
    <% List<Appointment> upcoming = (List<Appointment>) request.getAttribute("upcoming");
       List<Booking> recentBookings = (List<Booking>) request.getAttribute("recentBookings");
       if (upcoming != null || recentBookings != null) { %>
    <section class="dashboard">
        <div class="container">
            <div class="row g-4">

                <div class="col-12 col-lg-7">
                    <h2 class="h5 fw-bold mb-3">
                        <i class="fa-regular fa-calendar-check me-2 text-primary-blue"></i>Lịch hẹn sắp tới
                    </h2>

                    <% if (upcoming == null || upcoming.isEmpty()) { %>
                    <div class="dash-empty">
                        <i class="fa-regular fa-calendar"></i>
                        <span>Chưa có lịch hẹn nào sắp tới</span>
                    </div>
                    <% } else {
                           for (Appointment ap : upcoming) { %>
                    <div class="dash-appt <%= ap.isUrgent() ? "dash-appt--urgent" : "" %>">
                        <div class="dash-appt-icon bg-<%= ap.getColorName() %>-tint text-<%= ap.getColorName() %>">
                            <i class="fa-solid <%= ap.getIconClass() %>"></i>
                        </div>
                        <div class="flex-grow-1">
                            <div class="d-flex align-items-center gap-2 flex-wrap">
                                <span class="fw-bold"><%= ap.getServiceLabel() %></span>
                                <span class="dash-pet">
                                    <i class="fa-solid fa-paw"></i> <%= ap.getPetName() %>
                                </span>
                            </div>
                            <div class="dash-appt-time">
                                <%= ap.getFormattedStartAt() %>
                                <% if (ap.getRoomLabel() != null && !ap.getRoomLabel().isBlank()) { %>
                                    · <%= ap.getRoomLabel() %>
                                <% } %>
                            </div>
                        </div>
                        <div class="text-end">
                            <div class="dash-badge <%= ap.isUrgent() ? "dash-badge--urgent" : "" %>">
                                <%= ap.getReminderText() %>
                            </div>
                            <a class="dash-link" href="<%=request.getContextPath()%>/invoice?bookingId=<%= ap.getBookingId() %>">
                                Xem hóa đơn
                            </a>
                        </div>
                    </div>
                    <%     }
                       } %>
                </div>

                <div class="col-12 col-lg-5">
                    <div class="d-flex align-items-center justify-content-between gap-2 mb-3">
                        <h2 class="h5 fw-bold mb-0">
                            <i class="fa-solid fa-receipt me-2" style="color:var(--amber);"></i>Hóa đơn gần đây
                        </h2>
                        <%-- Chỗ này chỉ hiện vài đơn mới nhất, nên phải có lối sang xem đủ --%>
                        <a class="dash-link" href="<%=request.getContextPath()%>/myBookings">
                            Xem tất cả <i class="fa-solid fa-arrow-right"></i>
                        </a>
                    </div>

                    <% if (recentBookings == null || recentBookings.isEmpty()) { %>
                    <div class="dash-empty">
                        <i class="fa-solid fa-receipt"></i>
                        <span>Chưa có hóa đơn nào</span>
                    </div>
                    <% } else {
                           for (Booking bk : recentBookings) { %>
                    <a class="dash-invoice" href="<%=request.getContextPath()%>/invoice?bookingId=<%= bk.getBookingId() %>">
                        <div class="d-flex justify-content-between align-items-start gap-2">
                            <div>
                                <div class="fw-bold">Đơn #<%= bk.getBookingId() %></div>
                                <div class="dash-appt-time">
                                    <%= bk.getPetName() %> · <%= bk.getFormattedCreatedAt() %>
                                </div>
                            </div>
                            <div class="text-end">
                                <div class="dash-amount">
                                    <%= bk.getTotalPrice() == null ? "0" : String.format("%,.0f", bk.getTotalPrice()) %> đ
                                </div>
                                <span class="dash-status dash-status--<%= bk.getStatus().toLowerCase() %>">
                                    <%= bk.getStatus() %>
                                </span>
                            </div>
                        </div>
                    </a>
                    <%     }
                       } %>
                </div>

            </div>
        </div>
    </section>
    <% } %>
    <section class="service" id="dich-vu">
        <div class="container">
            <div class="row">
                <div class="col-12 col-lg-6">
                    <div class="d-flex flex-column  ">
                        <div class="heading">
                            Ngôi nhà thứ hai cho thú cưng của bạn
                        </div>
                        <p>Chúng tôi cung cấp các dịch vụ chăm sóc thú cưng toàn diện – từ sức khỏe đến nghỉ
                            dưỡng</p>
                        <img class="ms-auto" src="image/cat.png" alt="cat" />
                    </div>
                </div>
                <div class="col-12 col-lg-6">
                    <div class="d-flex flex-column gap-4 service-list">
                        <div class="row">
                            <div class="col-12 col-sm-6">
                                <div class="service-item">
                                    <div class="service-item-icon">
                                        <i class="fa-solid fa-paw"></i>
                                    </div>
                                    <div class="service-item-title mb-1 text-primary-blue fw-bold">
                                        Spa thú cưng
                                    </div>
                                    <p class="text-justify">Thư giãn, nâng niu từng phút giây cho các bé thú
                                        cưng</p>
                                    <a class="service-item-link" href="<%=request.getContextPath()%>/service?type=spa">Tìm hiểu thêm <i class="fa-solid fa-arrow-right"></i></a>
                                </div>
                            </div>
                            <div class="col-12 col-sm-6">
                                <div class="service-item">
                                    <div class="service-item-icon">
                                        <i class="fa-solid fa-house"></i>
                                    </div>
                                    <div class="service-item-title mb-1 text-primary-blue fw-bold">
                                        Khách sạn thú cưng
                                    </div>
                                    <p class="text-justify">Ngôi nhà thứ hai ấm áp, an toàn và đầy yêu
                                        thương</p>
                                    <a class="service-item-link" href="<%=request.getContextPath()%>/service?type=hotel">Tìm hiểu thêm <i class="fa-solid fa-arrow-right"></i></a>
                                </div>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-12 col-sm-6">
                                <div class="service-item">
                                    <div class="service-item-icon">
                                        <i class="fa-solid fa-syringe"></i>
                                    </div>
                                    <div class="service-item-title mb-1 text-primary-blue fw-bold">
                                        Tiêm vaccine
                                    </div>
                                    <p class="text-justify">Bảo vệ thú cưng từ những phương pháp phòng chống
                                        bệnh tốt nhất</p>
                                    <a class="service-item-link" href="<%=request.getContextPath()%>/service?type=vaccine">Tìm hiểu thêm <i class="fa-solid fa-arrow-right"></i></a>
                                </div>
                            </div>
                            <div class="col-12 col-sm-6">
                                <div class="service-item">
                                    <div class="service-item-icon">
                                        <i class="fa-solid fa-suitcase-medical"></i>
                                    </div>
                                    <div class="service-item-title mb-1 text-primary-blue fw-bold">
                                        Khám bệnh
                                    </div>
                                    <p class="text-justify">Mang lại sức khỏe, bình an cho thú cưng</p>
                                    <a class="service-item-link" href="<%=request.getContextPath()%>/service?type=medical">Tìm hiểu thêm <i class="fa-solid fa-arrow-right"></i></a>
                                </div>
                            </div>
                        </div>
                    </div>

                </div>
            </div>
        </div>
    </section>

    <%-- Tra cứu hóa đơn nhanh: chỉ hiện với khách chưa đăng nhập.
         myPets chỉ khác null khi HomeController nạp dữ liệu cho khách đã đăng nhập. --%>
    <% if (myPets == null) { %>
    <section class="guest-lookup">
        <div class="container">
            <div class="gl-card">
                <div class="gl-info">
                    <div class="gl-icon"><i class="fa-solid fa-receipt"></i></div>
                    <h2 class="h4 fw-bold mb-2">Tra cứu hóa đơn đặt lịch</h2>
                    <p class="mb-3">
                        Đặt lịch không cần tài khoản? Nhập mã tra cứu trong tin nhắn xác nhận
                        cùng số điện thoại đã dùng lúc đặt để xem hóa đơn, thanh toán hoặc hủy đơn.
                    </p>
                    <div class="gl-note">
                        <i class="fa-solid fa-circle-info"></i>
                        <span>Đã có tài khoản?
                            <a href="<%=request.getContextPath()%>/login">Đăng nhập</a>
                            để xem toàn bộ lịch sử đơn và lịch hẹn.</span>
                    </div>
                </div>

                <form class="gl-form" action="<%=request.getContextPath()%>/lookup" method="post">
                    <label class="gl-label" for="glCode">Mã tra cứu</label>
                    <input class="form-control gl-code" id="glCode" type="text" name="lookupCode"
                           required maxlength="12" placeholder="VD: K7MQ2XPA">

                    <label class="gl-label mt-3" for="glPhone">Số điện thoại khi đặt</label>
                    <input class="form-control" id="glPhone" type="tel" name="phone"
                           required placeholder="VD: 0912345678">

                    <button type="submit" class="btn btn-primary-blue w-100 fw-semibold mt-3">
                        <i class="fa-solid fa-magnifying-glass me-2"></i>Tra cứu ngay
                    </button>
                    <a class="gl-link" href="<%=request.getContextPath()%>/lookup">Hướng dẫn cách tra cứu</a>
                </form>
            </div>
        </div>
    </section>
    <% } %>

    <section id="team">
        <div class="container">
            <div class="heading text-main mb-4">Đội ngũ phát triển</div>
            <div class="row gy-4">
                <div class="col-12 col-md-4">
                    <div class="card member-card text-center p-3">
                        <div class="d-flex flex-column align-items-center">
                            <div class="avatar mb-3">
                                <a href="#" data-bs-toggle="modal" data-bs-target="#modalChien">
                                    <img src="image/nguyenthichien.png" alt="Nguyễn Thị Chiên" class="rounded-circle"
                                        style="width: 84px; height: 84px; object-fit: cover;">
                                </a>
                            </div>
                            <h5 class="mb-1">Nguyễn Thị Chiên</h5>
                            <div class="small">BA</div>
                            <div class="w-100 text-start small">
                                <div class="d-flex align-items-center gap-2 icon-line mb-2">
                                    <i class="bi bi-person-badge"></i>
                                    <span>MSV: K23DTCN529</span>
                                </div>
                                <div class="d-flex align-items-center gap-2 icon-line mb-2">
                                    <i class="bi bi-calendar-event"></i>
                                    <span>13/02/1989</span>
                                </div>
                                <div class="d-flex align-items-center gap-2 icon-line mb-2">
                                    <i class="bi bi-mortarboard"></i>
                                    <span>Lớp: D23TXCN09-K</span>
                                </div>
                                <div class="d-flex align-items-center gap-2 icon-line mb-2">
                                    <i class="bi bi-envelope"></i>
                                    <a class="text-decoration-none text-black text-truncate d-inline-block"
                                        href="mailto:nhansu104@gmail.com">nhansu104@gmail.com</a>
                                </div>
                                <div class="d-flex align-items-center gap-2 icon-line mb-2">
                                    <i class="bi bi-telephone"></i>
                                    <span>0383385868</span>
                                </div>
                            </div>
                        </div>

                    </div>
                </div>
                <div class="col-12 col-md-4">
                    <div class="card member-card text-center p-3">
                        <div class="d-flex flex-column align-items-center">
                            <div class="avatar mb-3">
                                <a href="#" data-bs-toggle="modal" data-bs-target="#modalThuy">
                                    <img src="image/tranthuthuy.png" alt="Trần Thu Thuỷ"
                                        class="rounded-circle"
                                        style="width: 84px; height: 84px; object-fit: cover;">
                                </a>

                            </div>
                            <h5 class="mb-1">Trần Thu Thủy</h5>
                            <div class="small">Frontend</div>                           
                            <div class="w-100 text-start small">
                                <div class="d-flex align-items-center gap-2 icon-line mb-2">
                                    <i class="bi bi-person-badge"></i>
                                    <span>MSV: B23DTCN220</span>
                                </div>
                                <div class="d-flex align-items-center gap-2 icon-line mb-2">
                                    <i class="bi bi-calendar-event"></i>
                                    <span>19/02/1995</span>
                                </div>
                                <div class="d-flex align-items-center gap-2 icon-line mb-2">
                                    <i class="bi bi-mortarboard"></i>
                                    <span>Lớp: D23TXCN06-B</span>
                                </div>
                                <div class="d-flex align-items-center gap-2 icon-line mb-2">
                                    <i class="bi bi-envelope"></i>
                                    <a class="text-decoration-none text-black text-truncate d-inline-block"
                                        href="mailto:thuytt0295@gmail.com">thuytt0295@gmail.com</a>
                                </div>
                                <div class="d-flex align-items-center gap-2 icon-line mb-2">
                                    <i class="bi bi-telephone"></i>
                                    <span>0987341195</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="col-12 col-md-4">
                    <div class="card member-card text-center p-3">
                        <div class="d-flex flex-column align-items-center">
                            <div class="avatar mb-3">
                                <a href="#" data-bs-toggle="modal" data-bs-target="#modalDuyet">
                                    <img src="image/havanduyet.jpg" alt="Hà Văn Duyệt"
                                        class="rounded-circle"
                                        style="width: 84px; height: 84px; object-fit: cover;">
                                </a>
                            </div>
                            <h5 class="mb-1">Hà Văn Duyệt</h5>
                            <div class="small">Backend</div>                           
                            <div class="w-100 text-start small">
                                <div class="d-flex align-items-center gap-2 icon-line mb-2">
                                    <i class="bi bi-person-badge"></i>
                                    <span>MSV: K23DTCN532</span>
                                </div>
                                <div class="d-flex align-items-center gap-2 icon-line mb-2">
                                    <i class="bi bi-calendar-event"></i>
                                    <span>11/01/1998</span>
                                </div>
                                <div class="d-flex align-items-center gap-2 icon-line mb-2">
                                    <i class="bi bi-mortarboard"></i>
                                    <span>Lớp: D23TXCN09-K</span>
                                </div>
                                <div class="d-flex align-items-center gap-2 icon-line mb-2">
                                    <i class="bi bi-envelope"></i>
                                    <a class="text-decoration-none text-black text-truncate d-inline-block"
                                        href="mailto:havanduyet1998st@gmail.com">havanduyet1998st@gmail.com</a>
                                </div>
                                <div class="d-flex align-items-center gap-2 icon-line mb-2">
                                    <i class="bi bi-telephone"></i>
                                    <span>0388335981</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Mỗi thành viên có modal riêng (trước đây cả 4 avatar dùng chung
             1 modal #myModal hardcode nội dung Hoàng Văn Bình -> bấm ai
             cũng ra đúng 1 người). -->
        <div class="modal fade" id="modalChien" tabindex="-1" aria-hidden="true">
            <div class="modal-dialog modal-fullscreen">
                <div class="modal-content">
                    <div class="modal-header">
                        <h1 class="modal-title fs-5">Hoàng Văn Bình</h1>
                        <button type="button" class="btn-close" data-bs-dismiss="modal"
                            aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <img src="image/hoangvanbinh.jpg" alt="Hoàng Văn Bình"
                            style="width: 100%; height: 100%; object-fit: contain;">
                    </div>
                </div>
            </div>
        </div>

        <div class="modal fade" id="modalThuy" tabindex="-1" aria-hidden="true">
            <div class="modal-dialog modal-fullscreen">
                <div class="modal-content">
                    <div class="modal-header">
                        <h1 class="modal-title fs-5">Trần Thu Thủy</h1>
                        <button type="button" class="btn-close" data-bs-dismiss="modal"
                            aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <img src="image/tranthuthuy.png" alt="Trần Thu Thủy"
                            style="width: 100%; height: 100%; object-fit: contain;">
                    </div>
                </div>
            </div>
        </div>

        <div class="modal fade" id="modalTrang" tabindex="-1" aria-hidden="true">
            <div class="modal-dialog modal-fullscreen">
                <div class="modal-content">
                    <div class="modal-header">
                        <h1 class="modal-title fs-5">Trần Thu Trang</h1>
                        <button type="button" class="btn-close" data-bs-dismiss="modal"
                            aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <img src="image/tranthutrang.jpg" alt="Trần Thu Trang"
                            style="width: 100%; height: 100%; object-fit: contain;">
                    </div>
                </div>
            </div>
        </div>

        <div class="modal fade" id="modalDuyet" tabindex="-1" aria-hidden="true">
            <div class="modal-dialog modal-fullscreen">
                <div class="modal-content">
                    <div class="modal-header">
                        <h1 class="modal-title fs-5">Hà Văn Duyệt</h1>
                        <button type="button" class="btn-close" data-bs-dismiss="modal"
                            aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <img src="image/havanduyet.jpg" alt="Hà Văn Duyệt"
                            style="width: 100%; height: 100%; object-fit: contain;">
                    </div>
                </div>
            </div>
        </div>
    </section>

    <jsp:include page="Footer.jsp"></jsp:include>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="js/script.js"></script>
    <script src="js/pet-slider.js"></script>
    <script src="js/stay-actions.js"></script>
    <script src="js/contact-bubble.js"></script>
</body>

</html>
