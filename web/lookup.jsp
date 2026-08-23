<%@ page contentType="text/html;charset=UTF-8" %>
<%
    // Trang tra cứu đơn cho khách vãng lai: mã tra cứu + số điện thoại đã dùng khi đặt.
    String error = (String) request.getAttribute("error");
    String prevCode = (String) request.getAttribute("lookupCode");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Tra cứu đơn đặt lịch</title>
    <jsp:include page="linkgroup.jsp"/>
    <link rel="stylesheet" href="css/service.css"/>
</head>
<body class="service-page">

<div class="container py-5 d-flex align-items-center justify-content-center" style="min-height:100vh;">
    <div class="service-shell" style="max-width:520px;">
        <div class="service-card">
            <div class="service-stripe"></div>

            <div class="service-head">
                <div class="service-head-icon" style="background:var(--blue-tint);color:var(--blue);">
                    <i class="fa-solid fa-magnifying-glass"></i>
                </div>
                <div>
                    <h1 class="service-title">Tra cứu đơn đặt lịch</h1>
                    <p class="service-subtitle">Dành cho khách đặt lịch không cần tài khoản</p>
                </div>
            </div>

            <div class="service-body">

                <% if (error != null) { %>
                <div class="service-alert">
                    <i class="fa-solid fa-circle-exclamation"></i> <%= error %>
                </div>
                <% } %>

                <div class="inv-tip">
                    <i class="fa-solid fa-circle-info"></i>
                    <span>
                        <strong>Trang này chỉ dành cho đơn đặt KHÔNG đăng nhập.</strong><br/>
                        Mã tra cứu gồm 8 ký tự (chữ in hoa và số, ví dụ <code>DWJA8CPW</code>),
                        được gửi kèm trong tin nhắn xác nhận ngay sau khi bạn chốt đơn.
                        Cần nhập đúng cả mã và số điện thoại đã dùng lúc đặt.
                    </span>
                </div>

                <div class="inv-tip" style="border-color:var(--primary-blue);">
                    <i class="fa-solid fa-user"></i>
                    <span>
                        Nếu bạn đặt lịch <strong>khi đang đăng nhập</strong> thì đơn đó không có mã tra cứu.
                        Hãy <a href="<%=request.getContextPath()%>/login">đăng nhập</a>
                        rồi xem ở mục &ldquo;Hóa đơn gần đây&rdquo; trên trang chủ.
                    </span>
                </div>

                <form action="<%=request.getContextPath()%>/lookup" method="post">
                    <div class="service-section-label">Mã tra cứu</div>
                    <input class="form-control mb-3" type="text" name="lookupCode" required
                           maxlength="12" placeholder="VD: K7MQ2XPA"
                           style="text-transform:uppercase;letter-spacing:.12em;font-weight:600;"
                           value="<%= prevCode == null ? "" : prevCode %>">

                    <div class="service-section-label">Số điện thoại khi đặt</div>
                    <input class="form-control" type="tel" name="phone" required
                           placeholder="VD: 0912345678">
                    <small style="color:var(--text-body);font-size:12px;">
                        Gõ có dấu cách hay dấu gạch đều được.
                    </small>

                    <div class="service-actions">
                        <a href="<%=request.getContextPath()%>/home" class="btn-back">
                            <i class="fa-solid fa-arrow-left"></i> Trang chủ
                        </a>
                        <button type="submit" class="btn btn-service">Tra cứu</button>
                    </div>
                </form>

                <div class="text-center mt-4" style="font-size:13.5px;color:var(--text-body);">
                    Bạn có tài khoản?
                    <a href="<%=request.getContextPath()%>/login" class="fw-semibold text-primary-blue">Đăng nhập</a>
                    để xem toàn bộ lịch sử đơn.
                </div>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
