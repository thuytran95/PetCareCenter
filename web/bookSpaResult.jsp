<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.petweb.model.BookingLine" %>
<%@ page import="com.petweb.model.BookingLineItem" %>
<%
    BookingLine line = (BookingLine) request.getAttribute("line");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Đã thêm dịch vụ Spa</title>
    <jsp:include page="linkgroup.jsp"/>
    <link rel="stylesheet" href="css/service.css"/>
</head>
<body class="service-page">

<div class="container py-5 d-flex align-items-center justify-content-center" style="min-height:100vh;">
    <div class="service-shell theme-spa">
        <div class="service-card">
            <div class="service-stripe"></div>

            <div class="service-head">
                <div class="service-head-icon"><i class="fa-solid fa-circle-check"></i></div>
                <div>
                    <h1 class="service-title">Đã thêm dịch vụ Spa</h1>
                    <p class="service-subtitle">Bạn có thể đặt thêm dịch vụ khác hoặc hoàn tất đơn</p>
                </div>
            </div>

            <div class="service-body">
                <% if (line == null || line.getItems().isEmpty()) { %>
                <div class="item-empty">
                    <i class="fa-solid fa-spa fa-lg mb-2 d-block"></i>
                    Bạn chưa chọn dịch vụ nào.
                </div>
                <% } else { %>

                <div class="result-badge mb-3">
                    <i class="fa-regular fa-calendar"></i> Ngày hẹn: <%= line.getFormattedStartAt() %>
                </div>

                <div class="service-section-label">Dịch vụ đã chọn</div>
                <div class="mb-3">
                    <% for (BookingLineItem item : line.getItems()) { %>
                    <div class="result-row">
                        <span><%= item.getItemName() %></span>
                        <span class="fw-bold"><%= String.format("%,.0f", item.getItemPrice()) %> đ</span>
                    </div>
                    <% } %>
                </div>

                <div class="result-total bg-pink-tint">
                    <span class="result-total-label text-pink">Tạm tính dịch vụ này</span>
                    <span class="result-total-value text-pink"><%= String.format("%,.0f", line.getLineTotal()) %> đ</span>
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

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
