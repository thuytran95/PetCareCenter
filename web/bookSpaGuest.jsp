<%@page import="java.time.LocalDateTime"%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.petweb.model.SpaServiceItem" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Đặt dịch vụ Spa</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">

<div class="container py-5">
    <div class="row justify-content-center">
        <div class="col-lg-8 col-md-10">
            <div class="card shadow-sm">
                <div class="card-header bg-info text-white text-center">
                    <h4 class="mb-0">🐾 Đặt dịch vụ Spa cho thú cưng</h4>
                </div>
                <div class="card-body">

                    <%
                        List<SpaServiceItem> allItems = (List<SpaServiceItem>) request.getAttribute("allItems");
                        LocalDateTime now = LocalDateTime.now();
                        String bookingDate = now.toString().substring(0,16); 
                    %>

                    <!-- Form -->
                    <form action="SpaBookingServletGuest" method="post">

                        <!-- Bảng dịch vụ -->
                        <div class="table-responsive mb-4">
                            <table class="table table-bordered align-middle text-center">
                                <thead class="table-light">
                                    <tr>
                                        <th>Chọn</th>
                                        <th>Tên dịch vụ</th>
                                        <th>Giá (VNĐ)</th>
                                    </tr>
                                </thead>
                                <tbody>
                                <% if (allItems != null) {
                                       for(SpaServiceItem item : allItems) { %>
                                    <tr>
                                        <td>
                                            <input type="checkbox" class="form-check-input" name="itemIds" value="<%= item.getItemId() %>">
                                        </td>
                                        <td><%= item.getItemName() %></td>
                                        <td class="text-danger fw-bold"><%= item.getItemPrice() %></td>
                                    </tr>
                                <%    }
                                   } else { %>
                                    <tr>
                                        <td colspan="3" class="text-muted">Không có dịch vụ nào.</td>
                                    </tr>
                                <% } %>
                                </tbody>
                            </table>
                        </div>

                        <!-- Ngày đặt -->
                        <div class="mb-3">
                            <label class="form-label fw-bold">Ngày đặt (Booking Date):</label>
                            <input type="datetime-local" name="bookingDate" class="form-control" value="<%= bookingDate %>" required>
                        </div>

                        <!-- Buttons -->
                        <div class="d-flex justify-content-between">
                            <a href="chooseServiceGuest.jsp" class="btn btn-secondary">⬅ Quay lại</a>
                            <button type="submit" class="btn btn-success">Đặt Spa ✅</button>
                        </div>

                    </form>

                </div>
            </div>
        </div>
    </div>
</div>

</body>
</html>
