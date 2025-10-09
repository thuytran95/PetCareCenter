<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.petweb.model.HotelDetail" %>
<%
    HotelDetail booking = (HotelDetail) request.getAttribute("booking");
    long soNgay = (long) request.getAttribute("soNgay");
    java.math.BigDecimal tongTien = (java.math.BigDecimal) request.getAttribute("tongTien");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Kết quả đặt phòng</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">

<div class="container py-5">
    <div class="card shadow-lg border-0">
        <div class="card-header bg-success text-white text-center">
            <h3 class="mb-0">✅ Đặt phòng thành công!</h3>
        </div>
        <div class="card-body p-4">
            <p><b>Loại phòng:</b> <%= booking.getRoomType() %></p>
            <p><b>Check-in:</b> <%= booking.getFormattedCheckIn() %></p>
            <p><b>Check-out:</b> <%= booking.getFormattedCheckOut() %></p>
            <p><b>Số ngày ở:</b> <%= soNgay %></p>
            <p class="fs-5 text-danger"><b>Tổng tiền: <%= tongTien.toPlainString() %> VND</b></p>

            <div class="mt-4 d-flex justify-content-center">
                <a href="chooseServiceGuest.jsp" class="btn btn-primary">
                    ⬅ Về đặt dịch vụ khác
                </a>
            </div>
        </div>
    </div>
</div>

</body>
</html>
