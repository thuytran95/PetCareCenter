<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.time.*" %>
<%
    LocalDateTime now = LocalDateTime.now();
    String minDateTime = now.toString().substring(0,16);
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Đặt phòng khách sạn thú cưng</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">

<div class="container py-5">
    <div class="card shadow-lg border-0 rounded-3">
        <div class="card-header bg-primary text-white text-center">
            <h3 class="mb-0">🐾 Đặt phòng khách sạn thú cưng</h3>
        </div>
        <div class="card-body p-4">
            <form action="HotelServletGuest" method="post" class="needs-validation" novalidate>
               

                <!-- Loại phòng -->
                <div class="mb-3">
                    <label for="roomType" class="form-label fw-bold">Loại phòng</label>
                    <select id="roomType" name="roomType" class="form-select" required>
                        <option value="vip1">VIP 1</option>
                        <option value="vip2">VIP 2</option>
                        <option value="vip3">VIP 3</option>
                    </select>
                </div>

                <!-- Check-in -->
                <div class="mb-3">
                    <label for="checkIn" class="form-label fw-bold">Ngày Check-in</label>
                    <input type="datetime-local" id="checkIn" name="checkIn" class="form-control" required min="<%= minDateTime %>">
                </div>

                <!-- Check-out -->
                <div class="mb-4">
                    <label for="checkOut" class="form-label fw-bold">Ngày Check-out</label>
                    <input type="datetime-local" id="checkOut" name="checkOut" class="form-control" required min="<%= minDateTime %>">
                </div>

                <!-- Nút hành động -->
                <div class="d-flex justify-content-between">
                    <a href="chooseServiceGuest.jsp" class="btn btn-outline-secondary">
                        ⬅ Quay lại dịch vụ
                    </a>
                    <button type="submit" class="btn btn-success">
                        ✅ Đặt phòng
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

</body>
</html>
