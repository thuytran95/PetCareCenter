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

    <div class="py-4">
        <div class="container">
            <div class="d-flex flex-column justify-content-center align-items-center text-center" style="gap: 36px">
                <div class="text-primary-blue fw-bold" style="font-size: 40px">Đặt lịch hẹn</div>
                <p class="mb-0">
                    Đặt lịch hẹn nhanh chóng với đội ngũ thú y tận tâm – chăm sóc thú cưng của bạn mọi lúc, mọi nơi!
                </p>
            </div>

            <form action="GuestBookingServlet" method="post" class="booking-card p-4 mt-4">
                <input type="hidden" name="action" value="add">

                <div class="row">
                    <!-- ============ CỘT TRÁI ============ -->
                    <div class="col-12 col-md-6">
                        <div>
                            <div class="d-flex gap-2 justify-content-start align-items-center">
                                <span>🐾 Thông tin thú cưng</span>
                            </div>
                            <div class="form-container">
                                <label>Thú cưng mới *</label>
                                <input type="text" name="newPetName" class="form-control mb-2" placeholder="Tên thú cưng" required>
                                <input type="text" name="newPetType" class="form-control mb-2" placeholder="Loại (VD: Chó, Mèo)">
                                <input type="number" name="newPetAge" class="form-control mb-2" placeholder="Tuổi">
                                <select name="newPetGender" class="form-select mb-2">
                                    <option value="M">Đực</option>
                                    <option value="F">Cái</option>
                                </select>
                            </div>
                        </div>
                    </div>

                    <!-- ============ CỘT PHẢI ============ -->
                    <div class="col-12 col-md-6">
                        <div>
                            <div class="d-flex gap-2 justify-content-start align-items-center">
                                <span>🩺 Thông tin dịch vụ</span>
                            </div>
                            <div class="form-container">
<!--                               <label>Chọn loại dịch vụ *</label>
                                <div class="text-center mt-2">
                                    <a href="chooseServiceGuest.jsp" class="btn btn-outline-primary px-4">
                                        ➕ Chọn dịch vụ
                                    </a>
                                </div>-->


                                <label class="mt-3">Thông tin khách hàng *</label>
                                <input type="text" name="guestName" class="form-control mb-2" placeholder="Họ và tên" required>
                                <input type="text" name="guestPhone" class="form-control mb-2" placeholder="Số điện thoại" required>
                                <input type="email" name="guestEmail" class="form-control mb-2" placeholder="Email (nếu có)">
                            </div>
                        </div>
                    </div>
                </div>

                                    <div class="text-center mt-4">
                         <button type="submit" class="btn btn-primary px-4">
                             Xác nhận & chọn dịch vụ
                         </button>
                     </div>
            </form>
        </div>
    </div>
</body>
</html>
