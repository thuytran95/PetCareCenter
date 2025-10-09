<%-- Document : booking Created on : Sep 22, 2025, 11:47:47 PM Author : Admin --%>

<%@page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>

    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Đặt lịch hẹn</title>
        <jsp:include page="linkgroup.jsp"></jsp:include>
            <link rel="stylesheet" href="css/common.css" />
            <link rel="stylesheet" href="css/header.css">
            <link rel="stylesheet" href="css/booking.css" />
        </head>
        <body>
        <jsp:include page="Header.jsp"></jsp:include>
        <div style="margin-bottom: 80px;">
            <div class="container">
                <div class="d-flex flex-column justify-content-center align-items-center text-center"
                     style="gap: 36px">
                    <div class="text-primary-blue fw-bold" style="font-size: 40px">Đặt lịch hẹn</div>
                    <p class="mb-5">Đặt lịch hẹn dễ dàng và nhanh chóng với đội ngũ thú y tận tâm, hoạt động 24/7 –
                        chăm sóc sức khỏe toàn diện cho thú cưng của bạn mọi lúc, mọi nơi!</p>
                </div>
                <div class="booking-card">
                    <form action="" class="d-flex flex-column align-items-center gap-4">
                        <div class="row gy-4 mb-4">
                            <div class="col-12 col-md-6">
                                <div class="d-flex gap-2 justify-content-start align-items-center mb-1">
                                    <span class="fw-bold">Thông tin thú cưng</span>
                                    <i class="fa-solid fa-chevron-down"></i>
                                </div>
                                <div class="form-container">
                                    <label class="fw-bold mb-1">Chọn loại thú cưng*</label>
                                    <select class="form-select" name="gender">
                                        <option value="">Chọn loại thú cưng</option>
                                        <option value="F">Chó</option>
                                        <option value="M">Mèo</option>
                                        <option value="M">Khác</option>
                                    </select>
                                </div>
                            </div>
                            <div class="col-12 col-md-6">
                                <div class="d-flex gap-2 justify-content-start align-items-center mb-1">
                                    <span class="fw-bold">Thông tin dịch vụ</span>
                                    <i class="fa-solid fa-chevron-down"></i>
                                </div>
                                <div class="form-container">
                                    <label class="fw-bold mb-1">Chọn loại dịch vụ*</label>
                                    <select class="form-select" name="gender">
                                        <option value="">Chọn loại dịch vụ</option>
                                        <option value="F">Spa</option>
                                        <option value="M">Khách sạn</option>
                                        <option value="M">Khám bệnh</option>
                                    </select>
                                </div>
                            </div>
                            <div class="col-12 col-md-6">
                                <div class="d-flex gap-2 justify-content-start align-items-center mb-1">
                                    <span class="fw-bold">Thời gian hẹn</span>
                                    <i class="fa-solid fa-chevron-down"></i>
                                </div>
                                <div class="form-container">
                                    <label class="fw-bold mb-1">Chọn thời gian hẹn*</label>
                                    <input type="date" class="form-control" />
                                </div>
                            </div>
                            <div class="col-12 col-md-6">
                                <div class="d-flex align-items-end justify-content-center h-100">
                                    <div class="form-container w-100 mt-auto">
                                        <label class="fw-bold mb-1">Chọn giờ hẹn*</label>
                                        <input type="time" class="form-control" />
                                    </div>
                                </div>
                            </div>
                            <div class="col-12 col-md-6">
                                <div class="d-flex gap-2 justify-content-start align-items-center mb-1">
                                    <span class="fw-bold">Thông tin khách hàng</span>
                                    <i class="fa-solid fa-chevron-down"></i>
                                </div>
                                <div class="form-container">
                                    <label class="fw-bold mb-1">Họ và tên*</label>
                                    <input type="text" class="form-control" placeholder="Nhập họ tên" />
                                </div>
                            </div>
                            <div class="col-12 col-md-6">
                                <div class="d-flex align-items-end justify-content-center h-100">
                                    <div class="form-container w-100 mt-auto">
                                        <label class="fw-bold mb-1">Số điện thoại*</label>
                                        <input type="text" class="form-control" placeholder="Nhập số điện thoại" />
                                    </div>
                                </div>
                            </div>
                        </div>
                        <button class="btn btn-primary-blue w-50 mx-auto">Xác nhận đặt lịch</button>
                    </form>
                </div>

                                    <div class="text-center mt-4">
                         <button type="submit" class="btn btn-primary px-4">
                             Xác nhận & chọn dịch vụ
                         </button>
                     </div>
            </form>
        </div>
    </body>

</html>
