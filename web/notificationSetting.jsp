<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@page import="java.util.List" %>
<%@page import="com.petweb.model.Notification" %>
<%
    // Danh sách do NotificationServlet nạp.
    // Trang này trước đây chỉ có 2 ô checkbox trang trí, không lưu gì;
    // giờ hiển thị lịch sử thông báo thật của khách.
    List<Notification> notifications = (List<Notification>) request.getAttribute("notifications");
    String loadError = (String) request.getAttribute("loadError");
%>
<!DOCTYPE html>
<html>

<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Thông báo của tôi</title>
    <jsp:include page="linkgroup.jsp"></jsp:include>
    <link rel="stylesheet" href="css/common.css" />
    <link rel="stylesheet" href="css/profile.css" />
    <link rel="stylesheet" href="css/header.css" />
    <link rel="stylesheet" href="css/pet.css" />
</head>

<body>
    <jsp:include page="Header.jsp" />

    <div class="container py-5">
        <div class="row">
            <div class="col-12 col-sm-4">
                <jsp:include page="setting-common.jsp"></jsp:include>
            </div>

            <div class="col-12 col-sm-8">
                <h1 class="h4 fw-bold mb-1">Thông báo của tôi</h1>
                <p style="color:var(--text-body);font-size:14px;">
                    Lịch sử tin nhắn trung tâm đã gửi tới số điện thoại của bạn.
                </p>

                <div class="noti-note">
                    <i class="fa-solid fa-circle-info"></i>
                    <span>
                        Đây là bản <strong>mô phỏng</strong> phục vụ đồ án: hệ thống ghi lại nội dung tin nhắn
                        thay vì gửi SMS thật qua nhà mạng.
                    </span>
                </div>

                <% if (loadError != null) { %>
                <div class="pet-flash pet-flash--err">
                    <i class="fa-solid fa-circle-exclamation"></i> <%= loadError %>
                </div>
                <% } %>

                <% if (notifications == null || notifications.isEmpty()) { %>
                <div class="pet-add-card" style="min-height:180px;">
                    <i class="fa-regular fa-bell fa-2x"></i>
                    <span class="fw-semibold">Chưa có thông báo nào</span>
                    <span style="font-size:12.5px;">Thông báo sẽ xuất hiện sau khi bạn đặt lịch.</span>
                </div>
                <% } else { %>
                <div class="noti-list">
                    <% for (Notification n : notifications) { %>
                    <div class="noti-item">
                        <div class="noti-icon bg-<%= n.getColorName() %>-tint text-<%= n.getColorName() %>">
                            <i class="fa-solid <%= n.getIconClass() %>"></i>
                        </div>
                        <div class="flex-grow-1">
                            <div class="d-flex justify-content-between align-items-start gap-2 flex-wrap">
                                <span class="fw-bold" style="font-size:14px;"><%= n.getEventLabel() %></span>
                                <span class="noti-meta">
                                    <i class="fa-solid fa-comment-sms"></i>
                                    <%= n.getMaskedRecipient() %> · <%= n.getFormattedCreatedAt() %>
                                </span>
                            </div>
                            <div class="noti-content"><%= n.getContent() %></div>
                            <% if (n.getBookingId() != null) { %>
                            <a class="noti-link"
                               href="<%=request.getContextPath()%>/invoice?bookingId=<%= n.getBookingId() %>">
                                Xem đơn #<%= n.getBookingId() %> <i class="fa-solid fa-arrow-right"></i>
                            </a>
                            <% } %>
                        </div>
                    </div>
                    <% } %>
                </div>
                <% } %>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>

</html>
