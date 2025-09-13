<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
    <head><meta charset="UTF-8"><title>Đăng ký</title></head>
    <body>
        <h2>Đăng ký tài khoản</h2>

        <p style="color:red;"><%= request.getAttribute("error") != null ? request.getAttribute("error") : ""%></p>
        <p style="color:green;"><%= request.getAttribute("message") != null ? request.getAttribute("message") : ""%></p>

        <form action="<%=request.getContextPath()%>/register" method="post" enctype="multipart/form-data">
            <table>
                <tr><td>Tên đăng nhập*</td><td><input type="text" name="userName" required></td></tr>
                <tr><td>Mật khẩu*</td><td><input type="password" name="password" required></td></tr>
                <tr>
                    <td>Giới tính</td>
                    <td>
                        <select name="gender">
                            <option value="M">Nam</option>
                            <option value="F">Nữ</option>
                        </select>
                    </td>
                </tr>        <tr><td>Họ tên</td><td><input type="text" name="fullName"></td></tr>
                <tr><td>Email</td><td><input type="email" name="email"></td></tr>
                <tr><td>Điện thoại</td><td><input type="tel" name="phone"></td></tr>
                <tr><td>Địa chỉ</td><td><input type="text" name="address"></td></tr>
                <tr><td>Ảnh đại diện</td><td><input type="file" name="avatar" accept="image/*"></td></tr>
                <tr><td colspan="2"><input type="submit" value="Đăng ký"></td></tr>
            </table>
        </form>

        <p>Đã có tài khoản? <a href="<%=request.getContextPath()%>/login">Đăng nhập</a></p>
    </body>
</html>
