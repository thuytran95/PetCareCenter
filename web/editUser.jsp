<%@page import="java.util.Base64"%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="com.petweb.utils.DBUtils, com.petweb.utils.ConnectionUtils, com.petweb.model.UserAccount" %>
<%@ page import="java.sql.Connection" %>

<%
    String userName = request.getParameter("userName");
    Connection conn = ConnectionUtils.getConnection();
    UserAccount user = DBUtils.findUser(conn, userName);
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Sửa User</title>
</head>
<body>
<h2>Sửa thông tin tài khoản</h2>
<form method="post" action="editUser" enctype="multipart/form-data">
    <input type="hidden" name="id" value="<%= user.getId() %>" />

    <table>
        <tr>
            <td>Ảnh hiện tại:</td>
            <td>
                <% if (user.getAvatar() != null) { %>
                    <img src="data:image/jpeg;base64,<%= Base64.getEncoder().encodeToString(user.getAvatar()) %>" width="120"/>
                <% } else { %>
                    Chưa có ảnh
                <% } %>
            </td>
        </tr>
        <tr>
            <td>Ảnh mới:</td>
            <td><input type="file" name="avatar" accept="image/*" /></td>
        </tr>
        <tr>
            <td>Username:</td>
            <td><input type="text" name="userName" value="<%= user.getUserName() %>" readonly /></td>
        </tr>
        <tr>
            <td>Full Name:</td>
            <td><input type="text" name="fullName" value="<%= user.getFullName() %>" /></td>
        </tr>
        <tr>
            <td>Email:</td>
            <td><input type="text" name="email" value="<%= user.getEmail() %>" /></td>
        </tr>
        <tr>
            <td>Phone:</td>
            <td><input type="text" name="phone" value="<%= user.getPhone() %>" /></td>
        </tr>
        <tr>
            <td>Address:</td>
            <td><input type="text" name="address" value="<%= user.getAddress() %>" /></td>
        </tr>
        <tr>
            <td>Role:</td>
            <td><input type="text" name="role" value="<%= user.getRole() %>" /></td>
        </tr>
        <tr>
            <td>Password:</td>
            <td><input type="password" name="password" value="<%= user.getPassword() %>" /></td>
        </tr>
    </table>

    <button type="submit">Lưu</button>
</form>


 
</body>
</html>
