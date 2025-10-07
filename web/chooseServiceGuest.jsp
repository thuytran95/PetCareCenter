<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
   String petIdParam = request.getParameter("petId");
   Integer petId = null;

   if (petIdParam != null && !petIdParam.isEmpty()) {
       petId = Integer.parseInt(petIdParam);
       session.setAttribute("petId", petId); // lưu luôn vào session
   } else {
       petId = (Integer) session.getAttribute("petId");
   }
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Chọn Dịch Vụ Cho Pet</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">

<div class="container py-5">
    <div class="row justify-content-center">
        <div class="col-md-8 col-lg-6">
            <div class="card shadow-sm">
                <div class="card-header bg-primary text-white text-center">
                    <h4 class="mb-0">Chọn dịch vụ cho thú cưng</h4>
                </div>
                <div class="card-body">

                    <p><b>🐾 Pet ID:</b> <span class="text-primary"><%= petId %></span></p>

                    <!-- 🧾 Form chọn loại dịch vụ -->
                    <form action="GuestBookingServlet" method="post" class="mb-4">
                        <input type="hidden" name="action" value="service">
                        <input type="hidden" name="petId" value="<%= petId %>">

                        <div class="mb-3">
                            <label for="serviceType" class="form-label">Loại dịch vụ</label>
                            <select id="serviceType" name="serviceType" class="form-select" required>
                                <option value="hotel">🏨 Khách sạn thú cưng</option>
                                <option value="spa">💆 Spa thú cưng</option>
                                <option value="medical">💉 Dịch vụ y tế</option>
                            </select>
                        </div>

                        <div class="d-grid">
                            <button type="submit" class="btn btn-success">+ Đặt dịch vụ</button>
                        </div>
                    </form>

                    <!-- 🧾 Form kết thúc booking -->
                    <form action="GuestBookingServlet" method="post">
                        <input type="hidden" name="action" value="finish">
                        <div class="d-grid">
                            <button type="submit" class="btn btn-danger">Kết thúc giao dịch</button>
                        </div>
                    </form>

                    <% 
                        String msg = request.getParameter("msg");
                        if (msg != null) {
                    %>
                        <div class="alert alert-info mt-3"><%= msg %></div>
                    <% } %>

                </div>
            </div>
        </div>
    </div>
</div>

</body>
</html>
