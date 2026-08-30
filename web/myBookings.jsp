<%@page contentType="text/html; charset=UTF-8" %>
<%@page import="java.util.List" %>
<%@page import="com.petweb.model.Booking" %>
<%@page import="com.petweb.model.BookingLine" %>
<%@page import="com.petweb.model.Pet" %>
<%!
    /** Biểu tượng của từng loại dịch vụ. */
    private String lineIcon(BookingLine l) {
        if (l.isHotel()) return "fa-house";
        if (l.isSpa()) return "fa-spa";
        if (l.isMedical()) return "fa-briefcase-medical";
        return "fa-paw";
    }

    private String lineColor(BookingLine l) {
        if (l.isHotel()) return "blue";
        if (l.isSpa()) return "pink";
        if (l.isMedical()) return "teal";
        return "blue";
    }
%>
<%
    // Dữ liệu do MyBookingsServlet nạp sẵn.
    List<Booking> bookings = (List<Booking>) request.getAttribute("bookings");
    Pet pet = (Pet) request.getAttribute("pet");
    String filter = (String) request.getAttribute("filter");
    Boolean hotelOnlyObj = (Boolean) request.getAttribute("hotelOnly");
    boolean hotelOnly = hotelOnlyObj != null && hotelOnlyObj;
    String loadError = (String) request.getAttribute("loadError");

    Integer countAll = (Integer) request.getAttribute("countAll");
    Integer countActive = (Integer) request.getAttribute("countActive");
    Integer countDone = (Integer) request.getAttribute("countDone");
    Integer countHotel = (Integer) request.getAttribute("countHotel");

    String flashMessage = (String) session.getAttribute("message");
    String flashError = (String) session.getAttribute("error");
    session.removeAttribute("message");
    session.removeAttribute("error");

    // Giữ nguyên bộ lọc theo bé khi bấm sang tab khác
    String petQuery = (pet == null) ? "" : "&petId=" + pet.getPetId();
    String base = request.getContextPath() + "/myBookings?";
%>
<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8">
    <title>Đơn của tôi<%= pet == null ? "" : " - " + pet.getName() %></title>
    <jsp:include page="linkgroup.jsp" />
    <link rel="stylesheet" href="css/header.css">
    <link rel="stylesheet" href="css/pet.css">
    <link rel="stylesheet" href="css/orders.css">
</head>

<body>
    <jsp:include page="Header.jsp" />

    <div class="container pb-5 pt-4">

        <div class="d-flex align-items-center justify-content-between flex-wrap gap-3 mb-4">
            <div>
                <h1 class="h3 fw-bold mb-1">
                    <% if (pet != null) { %>
                    Đơn của <%= pet.getName() %>
                    <% } else { %>
                    Đơn của tôi
                    <% } %>
                </h1>
                <p class="mb-0" style="color:var(--text-body);font-size:14.5px;">
                    Toàn bộ lịch đã đặt, kể cả những đợt còn ở phía trước.
                </p>
            </div>
            <div class="d-flex gap-2 flex-wrap">
                <% if (pet != null) { %>
                <a class="btn btn-sm btn-outline-secondary" href="<%= base %>filter=<%= filter %>">
                    <i class="fa-solid fa-list me-1"></i> Xem tất cả các bé
                </a>
                <% } %>
                <a class="btn btn-sm btn-outline-secondary"
                   href="<%=request.getContextPath()%>/petProfile">
                    <i class="fa-solid fa-paw me-1"></i> Hồ sơ thú cưng
                </a>
            </div>
        </div>

        <% if (flashMessage != null) { %>
        <div class="pet-flash pet-flash--ok">
            <i class="fa-solid fa-circle-check"></i> <%= flashMessage %>
        </div>
        <% } %>
        <% if (flashError != null || loadError != null) { %>
        <div class="pet-flash pet-flash--err">
            <i class="fa-solid fa-circle-exclamation"></i>
            <%= flashError != null ? flashError : loadError %>
        </div>
        <% } %>

        <%-- Bộ lọc: giữ nguyên bé đang xem và trạng thái chỉ-xem-đặt-phòng --%>
        <div class="ord-filters">
            <a class="ord-tab <%= "all".equals(filter) ? "ord-tab--on" : "" %>"
               href="<%= base %>filter=all<%= petQuery %><%= hotelOnly ? "&service=hotel" : "" %>">
                Tất cả <span class="ord-count"><%= countAll == null ? 0 : countAll %></span>
            </a>
            <a class="ord-tab <%= "active".equals(filter) ? "ord-tab--on" : "" %>"
               href="<%= base %>filter=active<%= petQuery %><%= hotelOnly ? "&service=hotel" : "" %>">
                Đang hiệu lực <span class="ord-count"><%= countActive == null ? 0 : countActive %></span>
            </a>
            <a class="ord-tab <%= "done".equals(filter) ? "ord-tab--on" : "" %>"
               href="<%= base %>filter=done<%= petQuery %><%= hotelOnly ? "&service=hotel" : "" %>">
                Đã xong / đã hủy <span class="ord-count"><%= countDone == null ? 0 : countDone %></span>
            </a>

            <span class="ord-sep"></span>

            <a class="ord-tab <%= hotelOnly ? "ord-tab--on" : "" %>"
               href="<%= base %>filter=<%= filter %><%= petQuery %><%= hotelOnly ? "" : "&service=hotel" %>">
                <i class="fa-solid fa-house me-1"></i>
                Chỉ đơn có phòng <span class="ord-count"><%= countHotel == null ? 0 : countHotel %></span>
            </a>
        </div>

        <% if (bookings == null || bookings.isEmpty()) { %>
        <div class="ord-empty">
            <i class="fa-regular fa-folder-open fa-2x mb-2"></i>
            <div class="fw-semibold mb-1">Không có đơn nào ở mục này</div>
            <p class="mb-3" style="font-size:13.5px;color:var(--text-body);max-width:420px;">
                Thử chuyển sang mục khác, hoặc đặt lịch mới cho bé của bạn.
            </p>
            <a class="btn btn-primary-blue fw-semibold px-4"
               href="<%=request.getContextPath()%>/petProfile">Đặt lịch cho bé</a>
        </div>
        <% } else { %>

        <div class="ord-list">
            <% for (Booking b : bookings) { %>
            <div class="ord-card">
                <div class="ord-head">
                    <div class="flex-grow-1">
                        <div class="d-flex align-items-center gap-2 flex-wrap">
                            <span class="ord-id">Đơn #<%= b.getBookingId() %></span>
                            <span class="ord-status ord-status--<%= b.getStatusColor() %>">
                                <%= b.getStatusLabel() %>
                            </span>
                            <% if (b.isOverdue()) { %>
                            <span class="ord-status ord-status--pink">Quá hạn</span>
                            <% } %>
                        </div>
                        <div class="ord-meta">
                            <i class="fa-solid fa-paw"></i>
                            <%= b.getPetName() == null ? "Thú cưng" : b.getPetName() %>
                            <% if (b.getLookupCode() != null) { %>
                                · Mã tra cứu <strong><%= b.getLookupCode() %></strong>
                            <% } %>
                        </div>
                    </div>
                    <div class="text-end">
                        <div class="ord-total">
                            <%= b.getTotalPrice() == null ? "0" : String.format("%,.0f", b.getTotalPrice()) %> đ
                        </div>
                    </div>
                </div>

                <ul class="ord-lines">
                    <% for (BookingLine l : b.getLines()) { %>
                    <li class="ord-line">
                        <span class="ord-line-icon bg-<%= lineColor(l) %>-tint text-<%= lineColor(l) %>">
                            <i class="fa-solid <%= lineIcon(l) %>"></i>
                        </span>
                        <div class="flex-grow-1">
                            <div class="fw-semibold"><%= l.getServiceLabel() %>
                                <% if (l.getNote() != null && !l.getNote().isBlank()) { %>
                                <span class="fw-normal" style="color:var(--text-body);">
                                    · <%= l.getNote() %>
                                </span>
                                <% } %>
                            </div>
                            <div class="ord-line-meta">
                                <%= l.getFormattedStartAt() %>
                                <% if (l.isHotel()) { %>
                                    → <%= l.getFormattedEndAt() %> · <%= l.getQuantity() %> ngày
                                <% } %>
                            </div>
                        </div>
                        <div class="ord-line-price">
                            <%= l.getLineTotal() == null ? "0" : String.format("%,.0f", l.getLineTotal()) %> đ
                        </div>
                    </li>
                    <% } %>
                </ul>

                <div class="ord-actions">
                    <a class="btn btn-sm btn-outline-secondary"
                       href="<%=request.getContextPath()%>/invoice?bookingId=<%= b.getBookingId() %>">
                        <i class="fa-regular fa-file-lines me-1"></i> Xem hóa đơn
                    </a>
                    <% if (b.isAwaitingPayment()) { %>
                    <form action="<%=request.getContextPath()%>/bookingAction" method="post" class="d-inline">
                        <input type="hidden" name="action" value="pay">
                        <input type="hidden" name="bookingId" value="<%= b.getBookingId() %>">
                        <input type="hidden" name="back" value="myBookings">
                        <button type="submit" class="btn btn-sm btn-primary-blue fw-semibold">
                            <i class="fa-solid fa-credit-card me-1"></i> Thanh toán
                        </button>
                    </form>
                    <% } %>
                    <% if (b.isCheckOutable()) { %>
                    <form action="<%=request.getContextPath()%>/bookingAction" method="post" class="d-inline">
                        <input type="hidden" name="action" value="checkout">
                        <input type="hidden" name="bookingId" value="<%= b.getBookingId() %>">
                        <input type="hidden" name="back" value="myBookings">
                        <button type="submit" class="pet-stay-btn">
                            <i class="fa-solid fa-right-from-bracket"></i> Trả phòng
                        </button>
                    </form>
                    <% } %>
                    <% if (b.isCancellable()) { %>
                    <form action="<%=request.getContextPath()%>/bookingAction" method="post"
                          class="d-inline js-cancel-stay" data-booking="<%= b.getBookingId() %>">
                        <input type="hidden" name="action" value="cancel">
                        <input type="hidden" name="bookingId" value="<%= b.getBookingId() %>">
                        <input type="hidden" name="back" value="myBookings">
                        <button type="submit" class="pet-stay-btn pet-stay-btn--warn">
                            <i class="fa-solid fa-xmark"></i> Hủy đơn
                        </button>
                    </form>
                    <% } %>
                    <%-- Chỉ đơn đã kết thúc mới xóa được. Đơn còn hiệu lực mà xóa thì
                         phòng bị khóa vô ích và không còn chỗ nào để trả phòng. --%>
                    <% if (b.isCompleted() || b.isCancelled()) { %>
                    <form action="<%=request.getContextPath()%>/bookingAction" method="post"
                          class="d-inline js-delete-order" data-booking="<%= b.getBookingId() %>">
                        <input type="hidden" name="action" value="delete">
                        <input type="hidden" name="bookingId" value="<%= b.getBookingId() %>">
                        <input type="hidden" name="back" value="myBookings">
                        <button type="submit" class="ord-del" title="Xóa đơn khỏi lịch sử">
                            <i class="fa-solid fa-trash"></i> Xóa đơn
                        </button>
                    </form>
                    <% } %>
                </div>
            </div>
            <% } %>
        </div>

        <% } %>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="js/stay-actions.js"></script>
</body>

</html>
