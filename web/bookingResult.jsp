<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.petweb.model.BookingLine" %>
<%
    BookingLine line = (BookingLine) request.getAttribute("line");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Kết quả đặt phòng</title>
    <jsp:include page="linkgroup.jsp"/>
    <link rel="stylesheet" href="css/service.css"/>
</head>
<body class="service-page">

<div class="container py-5 d-flex align-items-center justify-content-center" style="min-height:100vh;">
    <div class="service-shell theme-hotel">
        <div class="service-card">
            <div class="service-stripe"></div>

            <div class="service-head">
                <div class="service-head-icon"><i class="fa-solid fa-circle-check"></i></div>
                <div>
                    <h1 class="service-title">Đã thêm dịch vụ khách sạn</h1>
                    <p class="service-subtitle">Bạn có thể đặt thêm dịch vụ khác hoặc hoàn tất đơn</p>
                </div>
            </div>

            <div class="service-body">
                <% if (line == null) { %>
                <div class="item-empty">
                    <i class="fa-solid fa-house fa-lg mb-2 d-block"></i>
                    Không tìm thấy thông tin đặt phòng.
                </div>
                <% } else { %>

                <div class="service-section-label">Thông tin đặt phòng</div>
                <div class="mb-3">
                    <div class="result-row">
                        <span>Loại phòng</span>
                        <span class="fw-bold"><%= line.getNote() == null ? line.getRoomCode() : line.getNote() %></span>
                    </div>
                    <div class="result-row">
                        <span>Ngày nhận phòng</span>
                        <span class="fw-bold"><%= line.getFormattedStartAt() %></span>
                    </div>
                    <div class="result-row">
                        <span>Ngày trả phòng</span>
                        <span class="fw-bold"><%= line.getFormattedEndAt() %></span>
                    </div>
                    <div class="result-row">
                        <span>Số ngày ở</span>
                        <span class="fw-bold"><%= line.getQuantity() %> ngày</span>
                    </div>
                </div>

                <div class="result-total bg-blue-tint">
                    <span class="result-total-label text-blue">Tạm tính dịch vụ này</span>
                    <span class="result-total-value text-blue">
                        <%= String.format("%,.0f", line.getLineTotal()) %> đ
                    </span>
                </div>

                <% } %>

                <div class="service-actions">
                    <a href="<%=request.getContextPath()%>/chooseService" class="btn-back">
                        <i class="fa-solid fa-plus"></i> Đặt thêm dịch vụ
                    </a>
                    <form action="BookingServlet" method="post" class="d-flex flex-fill">
                        <input type="hidden" name="action" value="finish">
                        <button type="submit" class="btn btn-service w-100">Hoàn tất &amp; Xem hóa đơn</button>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.min.js"></script>
</body>
</html>
