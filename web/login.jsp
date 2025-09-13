<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head><meta charset="UTF-8"><title>Login</title></head>
<body>
<h2>Đăng nhập</h2>

<form method="post" action="<%=request.getContextPath()%>/login">
    <table>
        <tr>
            <td>Tên đăng nhập:</td>
            <td><input type="text" name="userName" /></td>
        </tr>
        <tr>
            <td>Mật khẩu:</td>
            <td><input type="password" name="password" /></td>
        </tr>
        <tr>
            <td>Ghi nhớ:</td>
            <td><input type="checkbox" name="rememberMe" value="Y" /></td>
        </tr>
        <tr>
            <td colspan="2" style="color:red;"><%= request.getAttribute("error") != null ? request.getAttribute("error") : "" %></td>
        </tr>
        <tr>
            <td colspan="2"><input type="submit" value="Đăng nhập"/></td>
        </tr>
    </table>
</form>

<p>Chưa có tài khoản?
    <a href="<%=request.getContextPath()%>/register">Đăng ký ngay</a>
</p>
</body>
</html>
