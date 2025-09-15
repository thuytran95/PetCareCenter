<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Login</title>
        <jsp:include page="linkgroup.jsp"></jsp:include>
            <link rel="stylesheet" href="css/common.css"/>
            <link rel="stylesheet" href="css/form.css"/>
            <link rel="stylesheet" href="css/login.css"/>
        </head>
        <body>
            <div class="login-container">
                <div class="login-container-form">
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
                <div>Bạn chưa chưa là thành viên của trung tâm? <a class="link-underline link-underline-opacity-0 text-primary-blue" href="<%=request.getContextPath()%>/register">Đăng ký</a></div>
            </div>
        </div>
    </body>
</html>
