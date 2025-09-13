<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Đổi mật khẩu</title>
</head>
<body>
<h2>Đổi mật khẩu</h2>

<form action="changePassword" method="post">
    <label>Mật khẩu cũ:</label><br>
    <input type="password" name="oldPassword" required><br><br>

    <label>Mật khẩu mới:</label><br>
    <input type="password" name="newPassword" required><br><br>

    <button type="submit">Xác nhận</button>
</form>

<br>
<a href="index.jsp">Quay lại trang chủ</a>

<% String error = (String) request.getAttribute("errorMessage"); %>
<% String success = (String) request.getAttribute("successMessage"); %>

<% if (error != null) { %>
    <p style="color:red;"><%= error %></p>
<% } %>
<% if (success != null) { %>
    <p style="color:green;"><%= success %></p>
<% } %>
</body>
</html>
