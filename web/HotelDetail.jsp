<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.time.*" %>
<html>
<head><title>Đặt phòng</title></head>
<body>
    <h2>Form đặt phòng</h2>
    <form action="HotelServlet" method="post">
        <input type="hidden" name="serviceId" value="1">
        

        Loại phòng:
        <select name="roomType">
            <option value="vip1">VIP1</option>
            <option value="vip2">VIP2</option>
            <option value="vip3">VIP3</option>
        </select><br><br>

        <%
            LocalDateTime now = LocalDateTime.now();
            String minDateTime = now.toString().substring(0,16);
        %>

        <label>Ngày Check-in: </label>
        <input type="datetime-local" name="checkIn" required min="<%= minDateTime %>"><br><br>

        <label>Ngày Check-out: </label>
        <input type="datetime-local" name="checkOut" required min="<%= minDateTime %>"><br><br>

        <button type="submit">Đặt phòng</button> | 
        <a href="chooseService.jsp">Quay lại đặt dịch vụ</a>
    </form>
</body>
</html>
