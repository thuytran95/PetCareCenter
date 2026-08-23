<%@ page contentType="text/html;charset=UTF-8" %>
<%
    // Điểm vào của khách vãng lai: nhập thông tin liên hệ + thú cưng rồi mở đơn nháp.
    // Không tạo tài khoản trong user_account như luồng cũ.
    String error = (String) request.getAttribute("error");
    String serviceType = request.getParameter("serviceType");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Đặt lịch không cần đăng nhập</title>
    <jsp:include page="linkgroup.jsp"/>
    <link rel="stylesheet" href="css/service.css"/>
</head>
<body class="service-page">

<div class="container py-5 d-flex align-items-center justify-content-center" style="min-height:100vh;">
    <div class="service-shell" style="max-width:600px;">
        <div class="service-card">
            <div class="service-stripe"></div>

            <div class="service-head">
                <div class="service-head-icon" style="background:var(--light-blue);color:var(--primary-blue);">
                    <i class="fa-solid fa-paw"></i>
                </div>
                <div>
                    <h1 class="service-title">Đặt lịch nhanh</h1>
                    <p class="service-subtitle">Không cần tạo tài khoản — chỉ cần thông tin liên hệ</p>
                </div>
            </div>

            <div class="service-body">

                <% if (error != null) { %>
                <div class="service-alert">
                    <i class="fa-solid fa-circle-exclamation"></i> <%= error %>
                </div>
                <% } %>

                <form action="<%=request.getContextPath()%>/BookingServlet" method="post">
                    <input type="hidden" name="action" value="startGuest">
                    <% if (serviceType != null && !serviceType.isBlank()) { %>
                    <input type="hidden" name="serviceType" value="<%= serviceType %>">
                    <% } %>

                    <div class="service-section-label">Thông tin liên hệ</div>
                    <div class="row g-3 mb-4">
                        <div class="col-12">
                            <input class="form-control" type="text" name="guestName"
                                   placeholder="Họ và tên người đặt" required>
                        </div>
                        <div class="col-12 col-sm-6">
                            <input class="form-control" type="tel" name="guestPhone"
                                   placeholder="Số điện thoại" required>
                        </div>
                        <div class="col-12 col-sm-6">
                            <input class="form-control" type="email" name="guestEmail"
                                   placeholder="Email (không bắt buộc)">
                        </div>
                    </div>

                    <div class="service-section-label">Thông tin thú cưng</div>
                    <div class="row g-3">
                        <div class="col-12 col-sm-6">
                            <input class="form-control" type="text" name="petName"
                                   placeholder="Tên thú cưng" required>
                        </div>
                        <div class="col-12 col-sm-6">
                            <select class="form-select" name="petSpecies">
                                <option value="">-- Loài --</option>
                                <option value="Chó">Chó</option>
                                <option value="Mèo">Mèo</option>
                                <option value="Khác">Khác</option>
                            </select>
                        </div>
                    </div>

                    <div class="service-actions">
                        <a href="<%=request.getContextPath()%>/home" class="btn-back">
                            <i class="fa-solid fa-arrow-left"></i> Trang chủ
                        </a>
                        <button type="submit" class="btn btn-service">Tiếp tục chọn dịch vụ</button>
                    </div>
                </form>

                <div class="text-center mt-4" style="font-size:13.5px;color:var(--text-body);">
                    Đã đặt lịch trước đó?
                    <a href="<%=request.getContextPath()%>/lookup" class="fw-semibold text-primary-blue">Tra cứu đơn</a>
                    <br/>
                    Đã có tài khoản?
                    <a href="<%=request.getContextPath()%>/login" class="fw-semibold text-primary-blue">Đăng nhập</a>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
