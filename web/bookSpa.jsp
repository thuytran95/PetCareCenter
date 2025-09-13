<%@page import="java.time.LocalDateTime"%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.petweb.model.SpaServiceItem" %>

<html>
<head>
    <title>Đặt dịch vụ Spa</title>
</head>
<body>
<h2>Chọn dịch vụ Spa</h2>

<%
    List<SpaServiceItem> allItems = (List<SpaServiceItem>) request.getAttribute("allItems");
    LocalDateTime now = LocalDateTime.now();
    String bookingDate = now.toString().substring(0,16); 
%>

<form action="SpaBookingServlet" method="post">
    <table border="1">
        <tr>
            <th>Chọn</th>
            <th>Tên dịch vụ</th>
            <th>Giá</th>
        </tr>
        <% for(SpaServiceItem item : allItems) { %>
        <tr>
            <td><input type="checkbox" name="itemIds" value="<%= item.getItemId() %>"></td>
            <td><%= item.getItemName() %></td>
            <td><%= item.getItemPrice() %></td>
        </tr>
        <% } %>
    </table>
    <label>Ngày đặt (Booking Date): </label>
    <input type="datetime-local" name="bookingDate" value="<%= bookingDate %>" ><br><br>
    <button type="submit">Đặt Spa</button>
    <a href="chooseService.jsp">Quay lại đặt dịch vụ</a>
</form>
</body>
</html>
