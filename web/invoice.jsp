<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="com.petweb.model.BookingInfo" %>
<%
    BookingInfo booking = (BookingInfo) request.getAttribute("booking");
    String error = (String) request.getAttribute("errorMessage");
%>

<html>
<head>
    <meta charset="UTF-8">
    <title>Hóa đơn</title>
</head>
<body>
    <h2>HÓA ĐƠN GIAO DỊCH</h2>
    <% if (error != null) { %>
        <p style="color:red;"><%= error %></p>
    <% } else if (booking != null) { %>
        <p><b>Mã Booking:</b> <%= booking.getBookingId() %></p>
        <p><b>Ngày Booking:</b> <%= booking.getBookingDate() %></p>
        <p><b>Trạng thái:</b> <%= booking.getStatus() %></p>
        <p><b>Tổng tiền:</b> <%= booking.getTotalPrice() %> VND</p>
    <% } %>
    <hr>
    <a href="index.jsp">Quay lại trang chủ</a>
</body>
</html>
