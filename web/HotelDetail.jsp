<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.petweb.model.RoomType" %>
<%@ page import="java.time.LocalDateTime" %>
<%
    // Bảng giá phòng do HotelServlet nạp từ bảng room_type — không còn viết cứng trong JSP.
    List<RoomType> roomTypes = (List<RoomType>) request.getAttribute("roomTypes");
    String minDateTime = LocalDateTime.now().withSecond(0).withNano(0).toString().substring(0, 16);
    String error = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Đặt phòng khách sạn thú cưng</title>
    <jsp:include page="linkgroup.jsp"/>
    <link rel="stylesheet" href="css/service.css"/>
</head>
<body class="service-page">

<div class="container py-5" style="min-height:100vh;">
    <div class="booking-layout">
    <div class="service-shell theme-hotel">
        <div class="service-card">
            <div class="service-stripe"></div>

            <div class="service-head">
                <div class="service-head-icon"><i class="fa-solid fa-house"></i></div>
                <div>
                    <h1 class="service-title">Đặt phòng khách sạn</h1>
                    <p class="service-subtitle">Ngôi nhà thứ hai ấm áp và an toàn cho bé cưng</p>
                </div>
            </div>

            <div class="service-body">

                <% if (error != null) { %>
                <div class="service-alert">
                    <i class="fa-solid fa-circle-exclamation"></i> <%= error %>
                </div>
                <% } %>

                <form action="HotelServlet" method="post" data-summary-form>

                    <div class="service-section-label">Loại phòng</div>
                    <div class="item-list">
                        <% if (roomTypes != null && !roomTypes.isEmpty()) {
                               for (RoomType r : roomTypes) { %>
                        <label class="item-option">
                            <input type="radio" name="roomType" value="<%= r.getRoomCode() %>" required
                                   data-price="<%= r.getPricePerDay() %>"
                                   data-per-day="true"
                                   data-label="<%= r.getRoomName() %>">
                            <span class="item-name">
                                <%= r.getRoomName() %>
                                <span class="d-block fw-normal" style="font-size:12.5px;color:var(--text-body);">
                                    <%= r.getDescription() == null ? "" : r.getDescription() %>
                                </span>
                            </span>
                            <span class="item-price"><%= String.format("%,.0f", r.getPricePerDay()) %> đ / ngày</span>
                        </label>
                        <%     }
                           } else { %>
                        <div class="item-empty">
                            <i class="fa-solid fa-house fa-lg mb-2 d-block"></i>
                            Hiện chưa có loại phòng nào.
                        </div>
                        <% } %>
                    </div>

                    <div class="row g-3">
                        <div class="col-12 col-sm-6">
                            <div class="service-section-label">Ngày nhận phòng</div>
                            <input type="datetime-local" id="checkIn" name="checkIn" class="form-control"
                                   required min="<%= minDateTime %>">
                        </div>
                        <div class="col-12 col-sm-6">
                            <div class="service-section-label">Ngày trả phòng</div>
                            <input type="datetime-local" id="checkOut" name="checkOut" class="form-control"
                                   required min="<%= minDateTime %>">
                        </div>
                    </div>

                    <div class="service-actions">
                        <a href="<%=request.getContextPath()%>/chooseService" class="btn-back">
                            <i class="fa-solid fa-arrow-left"></i> Quay lại
                        </a>
                        <button type="submit" class="btn btn-service"
                                <%= (roomTypes == null || roomTypes.isEmpty()) ? "disabled" : "" %>>
                            Đặt phòng
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>

        <jsp:include page="bookingSummary.jsp"/>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.min.js"></script>
<script src="js/booking-summary.js"></script>
</body>
</html>
