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
                    <div class="logo d-flex flex-column justify-content-center align-items-center gap-2 text-center">
                        <span style="font-size: 32px;"><i class="fa-solid fa-paw"></i></span>
                        <span>Pet-Care</span>
                    </div>
                    <h1 class="h5 fw-bold text-center mt-4 mb-1">Chào mừng trở lại</h1>
                    <p class="text-center mb-4" style="color: var(--text-body); font-size: 14px;">Đăng nhập để tiếp tục chăm sóc thú cưng của bạn</p>

                    <% if (request.getAttribute("error") != null) { %>
                    <div class="d-flex align-items-center gap-2 rounded-4 px-3 py-2 mb-4" style="background: var(--pink-tint); color: var(--pink); font-size: 13.5px; font-weight: 600;">
                        <i class="fa-solid fa-circle-exclamation"></i> <%= request.getAttribute("error")%>
                    </div>
                    <% } %>

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
                    <button class="btn btn-primary-blue w-100 py-3 fw-semibold" type="submit">Đăng nhập</button>
                </form>
                <div class="text-center mt-4" style="font-size: 13.5px; color: var(--text-body); line-height: 1.9;">
                    Bạn chưa là thành viên của trung tâm? <a class="fw-semibold text-primary-blue" href="<%=request.getContextPath()%>/register">Đăng ký</a><br/>
                    hoặc <a class="fw-semibold text-primary-blue" href="<%=request.getContextPath()%>/booking.jsp">đặt lịch không cần đăng nhập</a><br/>
                    <a class="fw-semibold text-primary-blue" href="<%=request.getContextPath()%>/lookup">Tra cứu đơn đã đặt</a>
                </div>
            </div>
        </div>
    </body>
</html>
