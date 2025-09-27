<%-- 
    Document   : booking
    Created on : Sep 22, 2025, 11:47:47 PM
    Author     : Admin
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Đặt lịch hẹn</title>
        <jsp:include page="linkgroup.jsp"></jsp:include>
            <link rel="stylesheet" href="css/common.css"/>
            <link rel="stylesheet" href="css/header.css">
            <link rel="stylesheet" href="css/booking.css"/>
        </head>
        <body>
        <jsp:include page="Header.jsp"></jsp:include>
        <
        <div class="py-4">
            <div class="container">
                <div class="d-flex flex-column justify-content-center align-items-center text-center" style="gap: 36px">
                    <div class="text-primary-blue fw-bold" style="font-size: 40px">Đặt lịch hẹn</div>
                    <p class="mb-0">Đặt lịch hẹn dễ dàng và nhanh chóng với đội ngũ thú y tận tâm, hoạt động 24/7 – chăm sóc sức khỏe toàn diện cho thú cưng của bạn mọi lúc, mọi nơi!</p>
                </div>
                <div class="booking-card">
                    <div class="row">
                        <div class="col-12 col-md-6">
                            <div >
                                <div class="d-flex gap-2 justify-content-start align-items-center">
                                    <span>Thông tin thú cưng</span>
                                    <i class="fa-solid fa-chevron-down"></i>
                                </div>
                                <div class="form-container">
                                    <label>Chọn loại thú cưng*</label>
                                    <select class="form-select" name="gender" >
                                        <option value="">Chọn loại thú cưng</option>
                                        <option value="F">Chó</option>
                                        <option value="M">Mèo</option>
                                         <option value="M">Khác</option>
                                    </select>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </body>
</html>
