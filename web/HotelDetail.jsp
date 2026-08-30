<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.petweb.model.RoomAvailability" %>
<%@ page import="java.time.LocalDateTime" %>
<%
    // Bảng giá kèm tình trạng còn trống do HotelServlet nạp từ CSDL.
    List<RoomAvailability> rooms = (List<RoomAvailability>) request.getAttribute("rooms");
    boolean windowKnown = rooms != null && !rooms.isEmpty() && rooms.get(0).isWindowKnown();
    String prevCheckIn = (String) request.getAttribute("checkIn");
    String prevCheckOut = (String) request.getAttribute("checkOut");
    LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
    String minDateTime = now.toString().substring(0, 16);
    String defaultCheckIn = minDateTime;
    String defaultCheckOut = now.plusHours(2).toString().substring(0, 16);
    String checkInValue = prevCheckIn != null && !prevCheckIn.isBlank() ? prevCheckIn : defaultCheckIn;
    String checkOutValue = prevCheckOut != null && !prevCheckOut.isBlank() ? prevCheckOut : defaultCheckOut;
    String error = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Đặt phòng khách sạn thú cưng</title>
    <jsp:include page="linkgroup.jsp"/>
    <link rel="stylesheet" href="css/service.css"/>
    <link rel="stylesheet" href="css/rooms.css"/>
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

                <form action="HotelServlet" method="post" data-summary-form
                      data-availability-url="<%=request.getContextPath()%>/roomAvailability">

                    <%-- Chọn ngày trước: số phòng trống chỉ có nghĩa khi gắn với một khoảng ngày --%>
                    <div class="service-section-label">Thời gian lưu trú</div>
                    <div class="row g-3 mb-2">
                        <div class="col-12 col-sm-6">
                            <label class="rm-label" for="checkIn">Ngày nhận phòng</label>
                            <input type="datetime-local" id="checkIn" name="checkIn" class="form-control"
                                   required min="<%= minDateTime %>"
                                   value="<%= checkInValue %>">
                        </div>
                        <div class="col-12 col-sm-6">
                            <label class="rm-label" for="checkOut">Ngày trả phòng</label>
                            <input type="datetime-local" id="checkOut" name="checkOut" class="form-control"
                                   required min="<%= defaultCheckOut %>"
                                   value="<%= checkOutValue %>">
                        </div>
                    </div>

                    <div class="rm-hint <%= windowKnown ? "rm-hint--live" : "" %>" data-rooms-hint>
                        <i class="fa-solid <%= windowKnown ? "fa-circle-check" : "fa-circle-info" %>"
                           data-rooms-hint-icon></i>
                        <span data-rooms-hint-text>
                            <% if (windowKnown) { %>
                            Số phòng trống bên dưới tính cho đúng khoảng ngày bạn đã chọn.
                            <% } else { %>
                            Chọn ngày nhận và trả phòng để xem còn bao nhiêu phòng trống.
                            <% } %>
                        </span>
                    </div>

                    <div class="service-section-label mt-4">Loại phòng</div>
                    <div class="item-list" data-rooms-list>
                        <% if (rooms != null && !rooms.isEmpty()) {
                               for (RoomAvailability r : rooms) { %>
                        <label class="item-option <%= r.isSoldOut() ? "item-option--off" : "" %>"
                               data-room="<%= r.getRoomCode() %>">
                            <input type="radio" name="roomType" value="<%= r.getRoomCode() %>" required
                                   data-price="<%= r.getPricePerDay() %>"
                                   data-per-day="true"
                                   data-label="<%= r.getRoomName() %>"
                                   <%= r.isSoldOut() ? "disabled" : "" %>>
                            <span class="item-name">
                                <%= r.getRoomName() %>
                                <span class="d-block fw-normal" style="font-size:12.5px;color:var(--text-body);">
                                    <%= r.getDescription() == null ? "" : r.getDescription() %>
                                </span>
                                <span class="rm-badge bg-<%= r.getStatusColor() %>-tint text-<%= r.getStatusColor() %>"
                                      data-room-badge>
                                    <i class="fa-solid fa-door-open"></i>
                                    <span data-room-status><%= r.getStatusLabel() %></span>
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

                    <div class="service-actions">
                        <a href="<%=request.getContextPath()%>/chooseService" class="btn-back">
                            <i class="fa-solid fa-arrow-left"></i> Quay lại
                        </a>
                        <button type="submit" class="btn btn-service"
                                <%= (rooms == null || rooms.isEmpty()) ? "disabled" : "" %>>
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

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="js/booking-summary.js"></script>
<script src="js/room-availability.js"></script>
</body>
</html>
