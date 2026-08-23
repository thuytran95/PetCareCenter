<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.petweb.model.MedicalServiceItem" %>
<%@ page import="java.time.LocalDateTime" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Đăng ký dịch vụ Y tế</title>
    <jsp:include page="linkgroup.jsp"/>
    <link rel="stylesheet" href="css/service.css"/>
</head>
<body class="service-page">

<div class="container py-5" style="min-height:100vh;">
    <div class="booking-layout">
    <div class="service-shell theme-medical">
        <div class="service-card">
            <div class="service-stripe"></div>

            <div class="service-head">
                <div class="service-head-icon"><i class="fa-solid fa-briefcase-medical"></i></div>
                <div>
                    <h1 class="service-title">Đăng ký dịch vụ Y tế</h1>
                    <p class="service-subtitle">Khám bệnh, tiêm phòng và chăm sóc sức khỏe cho thú cưng</p>
                </div>
            </div>

            <div class="service-body">
                <%
                    List<MedicalServiceItem> allItems = (List<MedicalServiceItem>) request.getAttribute("allItems");
                    LocalDateTime now = LocalDateTime.now();
                    String admissionDate = now.toString().substring(0,16);
                    String error = (String) request.getAttribute("error");
                %>

                <% if (error != null) { %>
                <div class="service-alert">
                    <i class="fa-solid fa-circle-exclamation"></i> <%= error %>
                </div>
                <% } %>

                <form action="MedicalBookingServlet" method="post" data-summary-form>

                    <div class="service-section-label">Danh sách dịch vụ</div>

                    <div class="item-list">
                        <% if (allItems != null && !allItems.isEmpty()) {
                               for (MedicalServiceItem item : allItems) { %>
                        <label class="item-option">
                            <input type="checkbox" name="itemIds" value="<%= item.getItemId() %>"
                                   data-price="<%= item.getItemPrice() %>"
                                   data-label="<%= item.getItemName() %>">
                            <span class="item-name"><%= item.getItemName() %></span>
                            <span class="item-price"><%= String.format("%,.0f", item.getItemPrice()) %> đ</span>
                        </label>
                        <%     }
                           } else { %>
                        <div class="item-empty">
                            <i class="fa-solid fa-briefcase-medical fa-lg mb-2 d-block"></i>
                            Hiện chưa có dịch vụ y tế nào.
                        </div>
                        <% } %>
                    </div>

                    <div class="service-section-label">Ngày &amp; giờ nhập viện</div>
                    <input type="datetime-local" id="admissionDate" name="admissionDate" class="form-control"
                           value="<%= admissionDate %>" min="<%= admissionDate %>" required>

                    <div class="service-actions">
                        <a href="<%=request.getContextPath()%>/chooseService" class="btn-back">
                            <i class="fa-solid fa-arrow-left"></i> Quay lại
                        </a>
                        <button type="submit" class="btn btn-service"
                                <%= (allItems == null || allItems.isEmpty()) ? "disabled" : "" %>>
                            Đăng ký dịch vụ
                        </button>
                    </div>

                </form>
            </div>
        </div>
    </div>

        <jsp:include page="bookingSummary.jsp"/>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="js/booking-summary.js"></script>
</body>
</html>
