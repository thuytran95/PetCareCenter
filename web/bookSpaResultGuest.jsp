<%@page import="com.petweb.model.SpaDetail"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.petweb.model.SpaServiceItem" %>
<%@ page import="java.math.BigDecimal" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Kết quả đặt Spa</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">

<div class="container py-5">
    <div class="row justify-content-center">
        <div class="col-lg-8 col-md-10">
            <div class="card shadow-sm">
                <div class="card-header bg-success text-white text-center">
                    <h4 class="mb-0">✅ Kết quả đặt dịch vụ Spa</h4>
                </div>
                <div class="card-body">

                    <%
                        List<SpaServiceItem> selectedItems = (List<SpaServiceItem>) request.getAttribute("selectedItems");
                        BigDecimal totalPrice = (BigDecimal) request.getAttribute("totalPrice");
                        SpaDetail spa = (SpaDetail) request.getAttribute("spa");
                        if(selectedItems == null || selectedItems.isEmpty()) {
                    %>
                        <div class="alert alert-warning text-center">
                            Bạn chưa chọn dịch vụ nào.
                        </div>
                    <%
                        } else {
                    %>

                        <!-- Bảng dịch vụ -->
                        <div class="table-responsive mb-4">
                            <table class="table table-bordered align-middle">
                                <thead class="table-light">
                                    <tr>
                                        <th>STT</th>
                                        <th>Tên dịch vụ</th>
                                        <th>Giá (VNĐ)</th>
                                    </tr>
                                </thead>
                                <tbody>
                                <%
                                    int index = 1;
                                    for(SpaServiceItem item : selectedItems) {
                                %>
                                    <tr>
                                        <td><%= index++ %></td>
                                        <td><%= item.getItemName() %></td>
                                        <td class="text-danger fw-bold"><%= item.getItemPrice() %></td>
                                    </tr>
                                <% } %>
                                    <tr class="table-success">
                                        <td colspan="2" class="fw-bold text-end">Tổng cộng</td>
                                        <td class="fw-bold text-danger"><%= totalPrice %> VNĐ</td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>

                        <!-- Ngày hẹn -->
                        <p><b>📅 Ngày hẹn:</b> <span class="text-primary"><%= spa.getFormattedbookingDate() %></span></p>

                    <% } %>

                    <!-- Quay lại -->
                    <div class="d-flex justify-content-end mt-4">
                        <a href="chooseServiceGuest.jsp" class="btn btn-secondary">⬅ Quay lại đặt dịch vụ</a>
                    </div>

                </div>
            </div>
        </div>
    </div>
</div>

</body>
</html>
