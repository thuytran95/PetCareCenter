<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.petweb.model.Booking" %>
<%@ page import="com.petweb.model.BookingLine" %>
<%!
    /** Biểu tượng của từng loại dịch vụ trong giỏ. */
    private String lineIcon(BookingLine l) {
        if (l.isHotel()) return "fa-house";
        if (l.isSpa()) return "fa-spa";
        if (l.isMedical()) return "fa-briefcase-medical";
        return "fa-paw";
    }

    /** Màu của từng loại dịch vụ, khớp với 3 thẻ chọn phía trên. */
    private String lineColor(BookingLine l) {
        if (l.isHotel()) return "blue";
        if (l.isSpa()) return "pink";
        if (l.isMedical()) return "teal";
        return "blue";
    }
%>
<%
    // Đơn nháp hiện tại do ChooseServiceServlet nạp kèm các dòng dịch vụ đã thêm.
    Integer bookingId = (Integer) session.getAttribute("currentBookingId");
    Booking draft = (Booking) request.getAttribute("draft");
    List<BookingLine> lines = (draft == null) ? null : draft.getLines();
    int lineCount = (lines == null) ? 0 : lines.size();

    String error = (String) request.getAttribute("error");
    if (error == null) {
        error = (String) session.getAttribute("error");
        session.removeAttribute("error");
    }
%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Chọn dịch vụ</title>
        <jsp:include page="linkgroup.jsp"/>
        <link rel="stylesheet" href="css/service.css"/>
        <link rel="stylesheet" href="css/choose.css"/>
    </head>
    <body class="service-page">

        <div class="container py-5">

            <%-- Thanh điều hướng: luôn có lối về trang chủ, không bắt bấm Back trình duyệt --%>
            <div class="cs-topbar">
                <a class="cs-home" href="<%=request.getContextPath()%>/home">
                    <i class="fa-solid fa-arrow-left"></i> Về trang chủ
                </a>
                <div class="cs-steps">
                    <span class="cs-step cs-step--done"><i class="fa-solid fa-check"></i> Chọn thú cưng</span>
                    <span class="cs-step-line"></span>
                    <span class="cs-step cs-step--now">2. Chọn dịch vụ</span>
                    <span class="cs-step-line"></span>
                    <span class="cs-step">3. Hoàn tất &amp; hóa đơn</span>
                </div>
            </div>

            <% if (error != null) { %>
            <div class="service-alert mb-4">
                <i class="fa-solid fa-circle-exclamation"></i> <%= error %>
            </div>
            <% } %>

            <% if (bookingId == null) { %>

            <%-- Không có đơn nào đang mở: chỉ đường thay vì để trang trống --%>
            <div class="cs-empty">
                <div class="cs-empty-icon"><i class="fa-solid fa-paw"></i></div>
                <h1 class="h4 fw-bold mb-2">Chưa có đơn đặt lịch nào đang mở</h1>
                <p class="mb-4">
                    Hãy chọn bé cần chăm sóc trước, rồi bạn có thể thêm nhiều dịch vụ
                    vào cùng một đơn và thanh toán một lần.
                </p>
                <div class="d-flex gap-2 flex-wrap justify-content-center">
                    <a href="<%=request.getContextPath()%>/petProfile" class="btn btn-service text-decoration-none">
                        <i class="fa-solid fa-paw me-2"></i>Chọn thú cưng để đặt lịch
                    </a>
                    <a href="<%=request.getContextPath()%>/home" class="btn-back">
                        <i class="fa-solid fa-house"></i> Về trang chủ
                    </a>
                </div>
            </div>

            <% } else { %>

            <div class="row g-4 align-items-start">

                <%-- Cột trái: chọn dịch vụ --%>
                <div class="col-12 col-lg-7">
                    <div class="service-card">
                        <div class="service-stripe"></div>

                        <div class="service-head">
                            <div class="service-head-icon" style="background:var(--light-blue);color:var(--primary-blue);">
                                <i class="fa-solid fa-paw"></i>
                            </div>
                            <div>
                                <h1 class="service-title">Chọn dịch vụ</h1>
                                <p class="service-subtitle">Bạn có thể thêm nhiều dịch vụ vào cùng một đơn</p>
                            </div>
                        </div>

                        <div class="service-body">
                            <form action="<%=request.getContextPath()%>/BookingServlet" method="post">
                                <input type="hidden" name="action" value="add">

                                <div class="service-section-label">Loại dịch vụ</div>
                                <div class="cs-options">
                                    <label class="cs-option cs-option--blue">
                                        <input type="radio" name="serviceType" value="hotel" class="d-none" required>
                                        <span class="cs-check"><i class="fa-solid fa-check"></i></span>
                                        <span class="cs-icon bg-blue-tint text-blue"><i class="fa-solid fa-house"></i></span>
                                        <span class="cs-name">Khách sạn</span>
                                        <span class="cs-desc">Gửi bé ở lại, tính theo số ngày</span>
                                    </label>
                                    <label class="cs-option cs-option--pink">
                                        <input type="radio" name="serviceType" value="spa" class="d-none" required>
                                        <span class="cs-check"><i class="fa-solid fa-check"></i></span>
                                        <span class="cs-icon bg-pink-tint text-pink"><i class="fa-solid fa-spa"></i></span>
                                        <span class="cs-name">Spa</span>
                                        <span class="cs-desc">Tắm, cắt tỉa, làm đẹp cho bé</span>
                                    </label>
                                    <label class="cs-option cs-option--teal">
                                        <input type="radio" name="serviceType" value="medical" class="d-none" required>
                                        <span class="cs-check"><i class="fa-solid fa-check"></i></span>
                                        <span class="cs-icon bg-teal-tint text-teal"><i class="fa-solid fa-briefcase-medical"></i></span>
                                        <span class="cs-name">Y tế</span>
                                        <span class="cs-desc">Khám, tiêm phòng, tẩy giun</span>
                                    </label>
                                </div>

                                <button type="submit" class="btn btn-service w-100 mt-4">
                                    Tiếp tục <i class="fa-solid fa-arrow-right ms-2"></i>
                                </button>
                            </form>

                            <div class="cs-tip mt-4">
                                <i class="fa-solid fa-lightbulb"></i>
                                <span>
                                    Thêm được bao nhiêu dịch vụ tùy bạn. Chọn xong dịch vụ này,
                                    trang sẽ quay lại đây để bạn thêm tiếp — tất cả gộp vào
                                    <strong>một hóa đơn duy nhất</strong>.
                                </span>
                            </div>
                        </div>
                    </div>
                </div>

                <%-- Cột phải: giỏ dịch vụ của đơn đang mở --%>
                <div class="col-12 col-lg-5">
                    <div class="cs-cart">
                        <div class="cs-cart-head">
                            <div>
                                <div class="cs-cart-title">Đơn #<%= bookingId %></div>
                                <% if (draft != null && draft.getPetName() != null) { %>
                                <div class="cs-cart-pet">
                                    <i class="fa-solid fa-paw"></i> <%= draft.getPetName() %>
                                    <% if (draft.getPetSpecies() != null) { %>
                                        · <%= draft.getPetSpecies() %>
                                    <% } %>
                                </div>
                                <% } %>
                            </div>
                            <span class="cs-cart-count"><%= lineCount %> dịch vụ</span>
                        </div>

                        <% if (lineCount == 0) { %>
                        <div class="cs-cart-empty">
                            <i class="fa-solid fa-cart-shopping"></i>
                            <div class="fw-semibold">Chưa có dịch vụ nào</div>
                            <div style="font-size:12.5px;">Chọn một loại dịch vụ bên cạnh để bắt đầu</div>
                        </div>
                        <% } else { %>
                        <ul class="cs-cart-list">
                            <% for (BookingLine l : lines) { %>
                            <li class="cs-cart-item">
                                <span class="cs-icon bg-<%= lineColor(l) %>-tint text-<%= lineColor(l) %>">
                                    <i class="fa-solid <%= lineIcon(l) %>"></i>
                                </span>
                                <div class="flex-grow-1">
                                    <div class="fw-semibold"><%= l.getServiceLabel() %></div>
                                    <div class="cs-cart-meta">
                                        <%= l.getFormattedStartAt() %>
                                        <% if (l.isHotel()) { %>
                                            → <%= l.getFormattedEndAt() %> · <%= l.getQuantity() %> ngày
                                        <% } else { %>
                                            · <%= l.getQuantity() %> hạng mục
                                        <% } %>
                                    </div>
                                </div>
                                <div class="cs-cart-price">
                                    <%= l.getLineTotal() == null ? "0" : String.format("%,.0f", l.getLineTotal()) %> đ
                                </div>
                            </li>
                            <% } %>
                        </ul>

                        <div class="cs-cart-total">
                            <span>Tạm tính</span>
                            <strong>
                                <%= draft.getTotalPrice() == null ? "0" : String.format("%,.0f", draft.getTotalPrice()) %> đ
                            </strong>
                        </div>
                        <% } %>

                        <form action="<%=request.getContextPath()%>/BookingServlet" method="post" class="mt-3">
                            <input type="hidden" name="action" value="finish">
                            <button type="submit" class="btn btn-service w-100"
                                    <%= lineCount == 0 ? "disabled" : "" %>>
                                <i class="fa-solid fa-receipt me-2"></i>Hoàn tất &amp; xem hóa đơn
                            </button>
                        </form>
                        <% if (lineCount == 0) { %>
                        <div class="cs-cart-note">Cần ít nhất một dịch vụ mới hoàn tất được đơn.</div>
                        <% } %>
                    </div>

                    <div class="cs-help">
                        <div class="cs-help-item">
                            <i class="fa-solid fa-shield-heart"></i>
                            <span>Giá lấy từ bảng giá của trung tâm, chốt ngay lúc bạn thêm dịch vụ.</span>
                        </div>
                        <div class="cs-help-item">
                            <i class="fa-regular fa-clock"></i>
                            <span>Đơn nháp bỏ dở quá 24 giờ sẽ tự hủy, không ảnh hưởng gì tới bạn.</span>
                        </div>
                        <div class="cs-help-item">
                            <i class="fa-solid fa-phone"></i>
                            <span>Cần hỗ trợ? Gọi <strong>1900 6868</strong> trong giờ hành chính.</span>
                        </div>
                    </div>
                </div>
            </div>

            <% } %>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    </body>
</html>
