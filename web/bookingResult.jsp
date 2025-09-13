<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.petweb.model.HotelDetail" %>
<html>
<head><title>Kết quả đặt phòng</title></head>
<body>
    <h2>Kết quả đặt phòng</h2>
    <%
        HotelDetail booking = (HotelDetail) request.getAttribute("booking");
        long soNgay = (long) request.getAttribute("soNgay");
        java.math.BigDecimal tongTien = (java.math.BigDecimal) request.getAttribute("tongTien");
    %>

    <p>Loại phòng: <%= booking.getRoomType() %></p>
    <p>Check-in: <%= booking.getFormattedCheckIn() %></p>
    <p>Check-out: <%= booking.getFormattedCheckOut() %></p>
    <p>Số ngày ở: <%= soNgay %></p>
    <p><b>Tổng tiền: <%= tongTien.toPlainString() %> VND</b></p>

    <a href="chooseService.jsp">Về đặt dịch vụ</a>
</body>
</html>
