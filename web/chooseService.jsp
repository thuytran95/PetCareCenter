<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
   String petIdParam = request.getParameter("petId");
    Integer petId = null;

    if (petIdParam != null && !petIdParam.isEmpty()) {
        petId = Integer.parseInt(petIdParam);
        session.setAttribute("petId", petId); // 👉 Cập nhật session luôn
    } else {
        petId = (Integer) session.getAttribute("petId");
    }
%>

<html>
<head>
    <title>Chọn Dịch Vụ</title>
</head>
<body>
<h2>Chọn dịch vụ cho thú cưng</h2>

<!-- Form thêm dịch vụ -->
<form action="BookingServlet" method="post">
    <input type="hidden" name="action" value="add">
    <input type="hidden" name="petId" value="<%= petId %>">
    <p><b>Pet ID:</b> <%= petId %></p>

    <label for="serviceType">Loại dịch vụ:</label>
    <select id="serviceType" name="serviceType" required>
        <option value="hotel">Khách sạn thú cưng</option>
        <option value="spa">Spa thú cưng</option>
        <option value="medical">Dịch vụ y tế</option>
    </select><br><br>

    <button type="submit">Thêm dịch vụ</button>
</form>

<br>

<!-- Form kết thúc giao dịch -->
<form action="BookingServlet" method="post">
    <input type="hidden" name="action" value="finish">
    <button type="submit" style="background-color: red; color: white;">Kết thúc giao dịch</button>
</form>

<br>


<%
    String msg = request.getParameter("msg");
    if (msg != null) {
%>
    <p style="color:green;"><%= msg %></p>
<% } %>
</body>
</html>
