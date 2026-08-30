<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.petweb.model.Booking" %>
<%@ page import="com.petweb.model.BookingLine" %>
<%@ page import="com.petweb.model.BookingLineItem" %>
<%@ page import="com.petweb.model.Notification" %>
<%
    Booking booking = (Booking) request.getAttribute("booking");
    String error = (String) request.getAttribute("errorMessage");
    List<Notification> notifications = (List<Notification>) request.getAttribute("notifications");
    String flashMessage = (String) request.getAttribute("flashMessage");
    String flashError = (String) request.getAttribute("flashError");
    boolean noPhone = Boolean.TRUE.equals(request.getAttribute("noPhoneWarning"));
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Hóa đơn</title>
    <jsp:include page="linkgroup.jsp"/>
    <link rel="stylesheet" href="css/service.css"/>
</head>
<body class="service-page">

<div class="container py-5 d-flex align-items-center justify-content-center" style="min-height:100vh;">
    <div class="service-shell" style="max-width:680px;">
        <div class="service-card">
            <div class="service-stripe"></div>

            <div class="service-head">
                <div class="service-head-icon" style="background:var(--amber-tint);color:var(--amber);">
                    <i class="fa-solid fa-receipt"></i>
                </div>
                <div>
                    <h1 class="service-title">Hóa đơn giao dịch</h1>
                    <p class="service-subtitle">Cảm ơn bạn đã tin tưởng Pet Care Center</p>
                </div>
            </div>

            <div class="service-body">

                <% if (flashMessage != null) { %>
                <div class="service-alert" style="background:var(--teal-tint);color:var(--teal);">
                    <i class="fa-solid fa-circle-check"></i> <%= flashMessage %>
                </div>
                <% } %>
                <% if (flashError != null) { %>
                <div class="service-alert">
                    <i class="fa-solid fa-circle-exclamation"></i> <%= flashError %>
                </div>
                <% } %>

                <% if (error != null) { %>
                <div class="service-alert">
                    <i class="fa-solid fa-circle-exclamation"></i> <%= error %>
                </div>
                <% } else if (booking != null) { %>

                <%-- Trạng thái đơn --%>
                <div class="d-flex align-items-center justify-content-between flex-wrap gap-2 mb-4">
                    <span class="inv-status bg-<%= booking.getStatusColor() %>-tint text-<%= booking.getStatusColor() %>">
                        <i class="fa-solid <%= booking.isPaid() ? "fa-circle-check"
                                              : booking.isCancelled() ? "fa-circle-xmark"
                                              : booking.isOverdue() ? "fa-triangle-exclamation"
                                              : "fa-clock" %>"></i>
                        <%= booking.getStatusLabel() %>
                    </span>
                    <span style="font-size:13px;color:var(--text-body);">Đơn #<%= booking.getBookingId() %></span>
                </div>

                <% if (booking.isOverdue()) { %>
                <div class="service-alert">
                    <i class="fa-solid fa-triangle-exclamation"></i>
                    Đơn này đã qua thời gian dịch vụ mà chưa được thanh toán.
                    Vui lòng liên hệ trung tâm nếu cần hỗ trợ.
                </div>
                <% } %>

                <div class="mb-4">
                    <div class="result-row">
                        <span>Thú cưng</span>
                        <span class="fw-bold">
                            <%= booking.getPetName() %><%= booking.getPetSpecies() == null ? "" : " · " + booking.getPetSpecies() %>
                        </span>
                    </div>
                    <% if (booking.isGuestBooking()) { %>
                    <div class="result-row">
                        <span>Người đặt</span>
                        <span class="fw-bold"><%= booking.getGuestName() %>
                            <%= booking.getGuestPhone() == null ? "" : " · " + booking.getGuestPhone() %></span>
                    </div>
                    <% if (booking.getLookupCode() != null) { %>
                    <div class="result-row">
                        <span>Mã tra cứu</span>
                        <span class="inv-code"><%= booking.getLookupCode() %></span>
                    </div>
                    <% } %>
                    <% } %>
                    <div class="result-row">
                        <span>Ngày đặt</span>
                        <span class="fw-bold"><%= booking.getFormattedCreatedAt() %></span>
                    </div>
                    <% if (booking.isPaid()) { %>
                    <div class="result-row">
                        <span>Ngày thanh toán</span>
                        <span class="fw-bold"><%= booking.getFormattedPaidAt() %></span>
                    </div>
                    <% } %>
                </div>

                <% if (booking.isGuestBooking() && booking.getLookupCode() != null) { %>
                <div class="inv-tip">
                    <i class="fa-solid fa-circle-info"></i>
                    Hãy lưu lại <strong>mã tra cứu</strong> ở trên. Bạn có thể xem lại đơn này bất kỳ lúc nào
                    tại trang <a href="<%=request.getContextPath()%>/lookup">Tra cứu đơn</a>
                    bằng mã đó và số điện thoại đã dùng khi đặt.
                </div>
                <% } %>

                <div class="service-section-label">Chi tiết dịch vụ</div>
                <div class="mb-3">
                    <% for (BookingLine line : booking.getLines()) { %>
                    <div class="invoice-line">
                        <div class="d-flex justify-content-between align-items-start">
                            <div>
                                <div class="fw-bold"><%= line.getServiceLabel() %></div>
                                <div style="font-size:12.5px;color:var(--text-body);">
                                    <% if (line.isHotel()) { %>
                                        <%= line.getNote() == null ? line.getRoomCode() : line.getNote() %>
                                        · <%= line.getFormattedStartAt() %> → <%= line.getFormattedEndAt() %>
                                        (<%= line.getQuantity() %> ngày)
                                    <% } else { %>
                                        <%= line.getFormattedStartAt() %>
                                    <% } %>
                                </div>
                            </div>
                            <div class="fw-bold" style="white-space:nowrap;">
                                <%= String.format("%,.0f", line.getLineTotal()) %> đ
                            </div>
                        </div>
                        <% if (!line.getItems().isEmpty()) { %>
                        <ul class="invoice-sub">
                            <% for (BookingLineItem item : line.getItems()) { %>
                            <li>
                                <span><%= item.getItemName() %></span>
                                <span><%= String.format("%,.0f", item.getItemPrice()) %> đ</span>
                            </li>
                            <% } %>
                        </ul>
                        <% } %>
                    </div>
                    <% } %>
                </div>

                <div class="result-total bg-amber-tint">
                    <span class="result-total-label" style="color:#d6820c;">Tổng thanh toán</span>
                    <span class="result-total-value" style="color:#d6820c;">
                        <%= booking.getTotalPrice() == null ? "0" : String.format("%,.0f", booking.getTotalPrice()) %> đ
                    </span>
                </div>

                <%-- Thanh toán / trả phòng / hủy đơn --%>
                <% if (booking.isAwaitingPayment() || booking.isCancellable()
                       || booking.isCheckOutable()) { %>
                <div class="service-actions">
                    <% if (booking.isAwaitingPayment()) { %>
                    <form action="<%=request.getContextPath()%>/bookingAction" method="post" class="d-flex flex-fill">
                        <input type="hidden" name="action" value="pay">
                        <input type="hidden" name="bookingId" value="<%= booking.getBookingId() %>">
                        <button type="submit" class="btn btn-service w-100"
                                style="background:var(--teal);">
                            <i class="fa-solid fa-credit-card me-1"></i> Thanh toán
                        </button>
                    </form>
                    <% } %>
                    <% if (booking.isCheckOutable()) { %>
                    <form action="<%=request.getContextPath()%>/bookingAction" method="post" class="d-flex flex-fill">
                        <input type="hidden" name="action" value="checkout">
                        <input type="hidden" name="bookingId" value="<%= booking.getBookingId() %>">
                        <button type="submit" class="btn btn-service w-100" style="background:var(--blue);">
                            <i class="fa-solid fa-door-open me-1"></i> Trả phòng
                        </button>
                    </form>
                    <% } %>
                    <% if (booking.isCancellable()) { %>
                    <form action="<%=request.getContextPath()%>/bookingAction" method="post" id="cancelForm">
                        <input type="hidden" name="action" value="cancel">
                        <input type="hidden" name="bookingId" value="<%= booking.getBookingId() %>">
                        <button type="submit" class="btn-back">
                            <i class="fa-solid fa-xmark"></i> Hủy đơn
                        </button>
                    </form>
                    <% } %>
                </div>
                <p class="summary-note">
                    Đây là bản mô phỏng phục vụ đồ án: hệ thống không kết nối cổng thanh toán thật.
                </p>
                <% } %>

                <%-- Vì sao chưa nhận được tin nhắn --%>
                <% if (noPhone) { %>
                <div class="inv-tip" style="border-color:var(--amber);">
                    <i class="fa-solid fa-triangle-exclamation" style="color:var(--amber);"></i>
                    <span>
                        <strong>Chưa gửi được thông báo.</strong>
                        <% if (booking.isGuestBooking()) { %>
                            Số điện thoại trên đơn không hợp lệ nên hệ thống không gửi tin xác nhận.
                        <% } else { %>
                            Hồ sơ của bạn chưa có số điện thoại hợp lệ.
                            <a href="<%=request.getContextPath()%>/editUser.jsp?userName=<%= booking.getUserId() %>">Cập nhật hồ sơ</a>
                            để nhận tin nhắn cho những lần đặt sau.
                        <% } %>
                    </span>
                </div>
                <% } %>

                <%-- Lịch sử thông báo đã gửi --%>
                <% if (notifications != null && !notifications.isEmpty()) { %>
                <div class="service-section-label mt-4">Thông báo đã gửi</div>
                <% for (Notification n : notifications) { %>
                <div class="inv-noti">
                    <div class="inv-noti-icon bg-<%= n.getColorName() %>-tint text-<%= n.getColorName() %>">
                        <i class="fa-solid <%= n.getIconClass() %>"></i>
                    </div>
                    <div class="flex-grow-1">
                        <div class="d-flex justify-content-between gap-2 flex-wrap">
                            <span class="fw-semibold" style="font-size:13px;"><%= n.getEventLabel() %></span>
                            <span style="font-size:11.5px;color:var(--text-input);">
                                SMS → <%= n.getMaskedRecipient() %> · <%= n.getFormattedCreatedAt() %>
                            </span>
                        </div>
                        <div class="inv-noti-body"><%= n.getContent() %></div>
                    </div>
                </div>
                <% } %>
                <% } %>

                <% } else { %>
                <div class="item-empty">
                    <i class="fa-solid fa-receipt fa-lg mb-2 d-block"></i>
                    Không tìm thấy thông tin hóa đơn.
                </div>
                <% } %>

                <div class="service-actions">
                    <a href="<%=request.getContextPath()%>/petProfile" class="btn-back flex-fill justify-content-center">
                        <i class="fa-solid fa-paw"></i> Hồ sơ thú cưng
                    </a>
                    <a href="<%=request.getContextPath()%>/" class="btn btn-service text-center text-decoration-none">
                        Về trang chủ
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
    var cancelForm = document.getElementById("cancelForm");
    if (cancelForm) {
        cancelForm.addEventListener("submit", function (e) {
            if (!confirm("Bạn chắc chắn muốn hủy đơn này?")) e.preventDefault();
        });
    }
</script>
</body>
</html>
