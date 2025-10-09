<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.petweb.model.MedicalServiceItem" %>
<%@ page import="java.time.LocalDateTime" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Đăng ký dịch vụ Y tế</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">

<div class="container py-5">
    <div class="row justify-content-center">
        <div class="col-lg-8 col-md-10">
            <div class="card shadow-sm">
                <div class="card-header bg-danger text-white text-center">
                    <h4 class="mb-0">🩺 Đăng ký dịch vụ Y tế cho thú cưng</h4>
                </div>
                <div class="card-body">

                    <%
                        Integer medicalId = (Integer) request.getAttribute("medicalId");
                        List<MedicalServiceItem> allItems = (List<MedicalServiceItem>) request.getAttribute("allItems");

                        LocalDateTime now = LocalDateTime.now();
                        String admissionDate = now.toString().substring(0,16);

                        if (allItems == null || allItems.isEmpty()) {
                            response.sendRedirect("chooseServiceGuest.jsp");
                            return;
                        }
                    %>

                    <!-- Form đăng ký -->
                    <form action="MedicalBookingServletGuest" method="post">
                        <div class="table-responsive mb-4">
                            <table class="table table-bordered align-middle">
                                <thead class="table-light">
                                    <tr>
                                        <th>Chọn</th>
                                        <th>Tên dịch vụ</th>
                                        <th>Giá (VNĐ)</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <% for (MedicalServiceItem item : allItems) { %>
                                    <tr>
                                        <td class="text-center">
                                            <input type="checkbox" name="itemIds" value="<%= item.getItemId() %>">
                                        </td>
                                        <td><%= item.getItemName() %></td>
                                        <td class="text-danger fw-bold"><%= item.getItemPrice() %></td>
                                    </tr>
                                    <% } %>
                                </tbody>
                            </table>
                        </div>

                        <!-- Ngày nhập viện -->
                        <div class="mb-3">
                            <label for="admissionDate" class="form-label fw-bold">📅 Ngày nhập viện:</label>
                            <input type="datetime-local" id="admissionDate" name="admissionDate"
                                   class="form-control" value="<%= admissionDate %>" required>
                        </div>

                        <!-- Nút hành động -->
                        <div class="d-flex justify-content-between">
                            <a href="chooseServiceGuest.jsp" class="btn btn-secondary">⬅ Quay lại</a>
                            <button type="submit" class="btn btn-danger">Đăng ký</button>
                        </div>
                    </form>

                </div>
            </div>
        </div>
    </div>
</div>

</body>
</html>
