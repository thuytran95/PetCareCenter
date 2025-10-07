<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="com.petweb.model.BookingInfo" %>
<%
    BookingInfo booking = (BookingInfo) request.getAttribute("booking");
    String error = (String) request.getAttribute("errorMessage");
%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Hóa đơn</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">

<div class="container py-5">
    <div class="row justify-content-center">
        <div class="col-lg-6 col-md-8">
            <div class="card shadow-sm">
                <div class="card-header bg-dark text-white text-center">
                    <h4 class="mb-0">🧾 HÓA ĐƠN GIAO DỊCH</h4>
                </div>
                <div class="card-body">

                    <% if (error != null) { %>
                        <div class="alert alert-danger text-center">
                            <%= error %>
                        </div>
                    <% } else if (booking != null) { %>

                        <table class="table table-bordered">
                            <tr>
                                <th class="bg-light">Mã Booking</th>
                                <td><%= booking.getBookingId() %></td>
                            </tr>
                          
                            <tr>
                                <th class="bg-light">Trạng thái</th>
                                <td><%= booking.getStatus() %></td>
                            </tr>
                            <tr>
                                <th class="bg-light">Tổng tiền</th>
                                <td class="fw-bold text-danger"><%= booking.getTotalPrice() %> VND</td>
                            </tr>
                        </table>

                    <% } else { %>
                        <div class="alert alert-warning text-center">
                            Không tìm thấy thông tin hóa đơn.
                        </div>
                    <% } %>

                </div>
                <div class="card-footer text-center">
                    <a href="booking.jsp" class="btn btn-secondary">⬅ Quay lại</a>
                </div>
            </div>
        </div>
    </div>
</div>

</body>
</html>
