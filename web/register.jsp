<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Đăng ký</title>
        <jsp:include page="linkgroup.jsp"></jsp:include>
            <link rel="stylesheet" href="css/common.css"/>
            <link rel="stylesheet" href="css/form.css"/>
            <link rel="stylesheet" href="css/register.css"/>
        </head>
        <body>
            <div class="register-container">
                <div class="register-container-form">
                    <div class="logo d-flex justify-content-center align-items-center gap-2">
                        <span><i class="fa-solid fa-paw"></i></span>
                        <span>Pet-Care</span>
                    </div> 
                    <form method="post" action="<%=request.getContextPath()%>/login">
                    <div class="form-container form-container-icon d-flex align-items-center mb-4">
                        <span class="icon"><i class="fa-solid fa-user"></i></span>
                        <input class="form-control ps-6" placeholder="Nhập tên đăng nhập" name="userName" type="text" required/>
                    </div>  
                    <div class="form-container form-container-icon d-flex align-items-center mb-1">
                        <span class="icon"><i class="fa-solid fa-lock"></i></span>
                        <input class="form-control ps-6" placeholder="Mật khẩu: tối đa 8 ký tự" name="password" type="password" required />
                    </div>
                    <a href="#" class="text-body link-underline link-underline-opacity-0 d-block text-end mb-5" >Quên mật khẩu?</a>
                    <button class="btn btn-primary-blue w-100" type="submit">Đăng nhập</button>
                </form>
            </div>
        </div>

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
