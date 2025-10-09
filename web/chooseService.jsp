<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String petIdParam = request.getParameter("petId");
    Integer petId = null;

    if (petIdParam != null && !petIdParam.isEmpty()) {
        petId = Integer.parseInt(petIdParam);
        session.setAttribute("petId", petId); // 👉 Cập nhật session luôn
    } else {
        petId = (Integer) session.getAttribute("petId");
    }


%>

<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Chọn Dịch Vụ</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
        <link rel="stylesheet" href="css/common.css"/>
    </head>
    <body class="bg-light">

        <div class="container py-5">
            <div class="row justify-content-center">
                <div class="col-md-8 col-lg-6">
                    <div class="card shadow-sm">
                        <div class="card-header bg-primary-blue text-white text-center">
                            <h4 class="mb-0">Chọn dịch vụ cho thú cưng</h4>
                        </div>
                        <div class="card-body">

                            <!-- Thông tin Pet -->
                            <p><b>Pet ID:</b> <span class="text-primary-blue"><%= petId%></span></p>

                            <!-- Form thêm dịch vụ -->
                            <form action="BookingServlet" method="post" class="mb-4">
                                <input type="hidden" name="action" value="add">
                                <input type="hidden" name="petId" value="<%= petId%>">

                                <div class="mb-3">
                                    <label for="serviceType" class="form-label">Loại dịch vụ</label>
                                    <select id="serviceType" name="serviceType" class="form-select" required>
                                        <option value="hotel">🏨 Khách sạn thú cưng</option>
                                        <option value="spa">💆 Spa thú cưng</option>
                                        <option value="medical">💉 Dịch vụ y tế</option>
                                    </select>
                                </div>

                                <div class="d-grid">
                                    <button type="submit" class="btn btn-success">+ Thêm dịch vụ</button>
                                </div>
                            </form>

                            <!-- Form kết thúc giao dịch -->
                            <form action="BookingServlet" method="post">
                                <input type="hidden" name="action" value="finish">
                                <div class="d-grid">
                                    <button type="submit" class="btn btn-danger">Kết thúc giao dịch</button>
                                </div>
                            </form>

                            <!-- Thông báo -->
                            <%
                                String msg = request.getParameter("msg");
                                if (msg != null) {
                            %>
                            <div class="alert alert-success mt-3"><%= msg%></div>
                            <% }%>

                        </div>
                    </div>
                </div>
            </div>
        </div>
        <script src="https://cdn.jsdelivr.net/npm/@popperjs/core@2.11.8/dist/umd/popper.min.js"
                integrity="sha384-I7E8VVD/ismYTF4hNIPjVp/Zjvgyol6VFvRkX/vR+Vc4jQkC+hVqc2pM8ODewa9r"
        crossorigin="anonymous"></script>
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.min.js"></script>
    </body>
</html>
