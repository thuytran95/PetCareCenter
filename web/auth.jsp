<%-- 
    Document   : auth
    Created on : Sep 14, 2025, 5:10:56 PM
    Author     : Admin
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Pet Care Center - Trung tâm chăm sóc thú cưng</title>
        <jsp:include page="linkgroup.jsp"></jsp:include>        
            <link rel="stylesheet" href="css/header.css"/>
            <link rel="stylesheet" href="css/auth.css"/>
        </head>
        <body>
            <div class="d-flex flex-column auth">
                <div class="container-fluid ">
                    <div class="header">
                        <div class="d-flex gap-1 align-items-center justify-content-center" style="width: fit-content">
                            <a href="#">
                                <img src="${pageContext.request.contextPath}/image/logo.svg" alt="logo" style="height: 48px" /> 
                        </a> 
                        <div class="header-brand-name">Pet-Care</div>
                    </div>
                </div>

            </div>
            <div class="auth-container">
                <div class="container">
                    <div class="row">
                        <div class="col-6">
                            <div class="auth-content">
                                <div class="heading">
                                    Một tài khoản cho bạn, ngàn tiện ích cho thú cưng
                                </div> 
                                <p>Trở thành thành viên của hội yêu thú cưng tại trung tâm, bạn sẽ có thể nhận được thông báo các chương tình ưu đã mới sớm nhất,
                                    theo dõi chi tiết sổ sức khỏe của thú cưng và vô vàn tiện ích khác.</p>
                                <p>
                                    Hãy tham gia ngay thôi!
                                </p>
                                <div class="d-flex gap-2">
                                    <a class="btn btn-outline-blue px-5" href="<%=request.getContextPath()%>/login">Đăng nhập</a>
                                    <a class="btn btn-primary-blue px-5" href="<%=request.getContextPath()%>/register">Đăng ký</a>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>  
        </div>
    </body>
</html>
