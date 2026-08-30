<%@page import="java.time.LocalDateTime"%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.petweb.model.SpaServiceItem" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Đặt dịch vụ Spa</title>
    <jsp:include page="linkgroup.jsp"/>
    <link rel="stylesheet" href="css/service.css"/>
</head>
<body class="service-page">

<div class="container py-5" style="min-height:100vh;">
    <div class="booking-layout">
    <div class="service-shell theme-spa">
        <div class="service-card">
            <div class="service-stripe"></div>

            <div class="service-head">
                <div class="service-head-icon"><i class="fa-solid fa-spa"></i></div>
                <div>
                    <h1 class="service-title">Đặt dịch vụ Spa</h1>
                    <p class="service-subtitle">Chọn các dịch vụ bạn muốn dành cho bé cưng</p>
                </div>
            </div>

            <div class="service-body">
                <%
                    List<SpaServiceItem> allItems = (List<SpaServiceItem>) request.getAttribute("allItems");
                    LocalDateTime minBooking = LocalDateTime.now().plusDays(1).withSecond(0).withNano(0);
                    String minBookingDate = minBooking.toString().substring(0, 16);
                    String error = (String) request.getAttribute("error");
                %>

                <% if (error != null) { %>
                <div class="service-alert">
                    <i class="fa-solid fa-circle-exclamation"></i> <%= error %>
                </div>
                <% } %>

                <form action="SpaBookingServlet" method="post" data-summary-form>

                    <div class="service-section-label">Danh sách dịch vụ</div>

                    <div class="item-list">
                        <% if (allItems != null && !allItems.isEmpty()) {
                               for (SpaServiceItem item : allItems) { %>
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
                            <i class="fa-solid fa-spa fa-lg mb-2 d-block"></i>
                            Hiện chưa có dịch vụ spa nào.
                        </div>
                        <% } %>
                    </div>

                    <div class="service-section-label">Ngày &amp; giờ đặt lịch</div>
                    <input type="datetime-local" id="bookingDate" name="bookingDate" class="form-control"
                           value="<%= minBookingDate %>" min="<%= minBookingDate %>" required>
                    <div id="bookingDateError" class="service-alert" style="margin-top:10px;margin-bottom:0;display:none;" role="alert">
                        <i class="fa-solid fa-circle-exclamation"></i> Bạn phải đặt lịch trước ít nhất 1 ngày.
                    </div>

                    <div class="service-actions">
                        <a href="<%=request.getContextPath()%>/chooseService" class="btn-back">
                            <i class="fa-solid fa-arrow-left"></i> Quay lại
                        </a>
                        <button type="submit" class="btn btn-service"
                                <%= (allItems == null || allItems.isEmpty()) ? "disabled" : "" %>>
                            Đặt dịch vụ Spa
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
<script>
(function () {
    var form = document.querySelector("form[data-summary-form]");
    var input = document.getElementById("bookingDate");
    var errorEl = document.getElementById("bookingDateError");
    if (!form || !input || !errorEl) return;

    var minMs = new Date(input.min).getTime();

    function isValid() {
        if (!input.value) return false;
        return new Date(input.value).getTime() >= minMs;
    }

    function setError(show) {
        errorEl.style.display = show ? "flex" : "none";
        input.setCustomValidity(show ? "Bạn phải đặt lịch trước ít nhất 1 ngày." : "");
    }

    function validate() {
        var ok = isValid();
        setError(!ok);
        return ok;
    }

    input.addEventListener("input", validate);
    input.addEventListener("change", validate);
    form.addEventListener("submit", function (e) {
        if (!validate()) e.preventDefault();
    });
    validate();
})();
</script>
</body>
</html>
