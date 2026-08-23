<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Base64" %>
<%@ page import="com.petweb.model.ServicePage" %>
<%@ page import="com.petweb.model.SpaServiceItem" %>
<%@ page import="com.petweb.model.MedicalServiceItem" %>
<%@ page import="com.petweb.model.RoomType" %>
<%@ page import="com.petweb.model.Pet" %>
<%
    // Kết quả do SearchServlet nạp.
    String query = (String) request.getAttribute("query");
    String error = (String) request.getAttribute("error");
    List<ServicePage> resultPages = (List<ServicePage>) request.getAttribute("resultPages");
    List<SpaServiceItem> resultSpa = (List<SpaServiceItem>) request.getAttribute("resultSpa");
    List<MedicalServiceItem> resultMedical = (List<MedicalServiceItem>) request.getAttribute("resultMedical");
    List<RoomType> resultRooms = (List<RoomType>) request.getAttribute("resultRooms");
    List<Pet> resultPets = (List<Pet>) request.getAttribute("resultPets");

    int total = 0;
    if (resultPages != null) total += resultPages.size();
    if (resultSpa != null) total += resultSpa.size();
    if (resultMedical != null) total += resultMedical.size();
    if (resultRooms != null) total += resultRooms.size();
    if (resultPets != null) total += resultPets.size();

    boolean hasQuery = query != null && !query.isEmpty();
%>
<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8">
    <title><%= hasQuery ? "Tìm: " + query : "Tìm kiếm" %> - Pet Care Center</title>
    <jsp:include page="linkgroup.jsp"></jsp:include>
    <link rel="stylesheet" href="css/header.css">
    <link rel="stylesheet" href="css/footer.css" />
    <link rel="stylesheet" href="css/pet.css">
    <link rel="stylesheet" href="css/serviceInfo.css">
</head>

<body>
    <jsp:include page="Header.jsp"></jsp:include>

    <div class="container py-5" style="min-height:60vh;">

        <h1 class="h4 fw-bold mb-1">
            <% if (hasQuery) { %>
                Kết quả cho &ldquo;<%= query %>&rdquo;
            <% } else { %>
                Tìm kiếm
            <% } %>
        </h1>
        <p style="color:var(--text-body);font-size:14px;">
            <% if (!hasQuery) { %>
                Nhập tên dịch vụ hoặc tên thú cưng của bạn vào ô tìm kiếm phía trên.
            <% } else { %>
                Tìm thấy <strong><%= total %></strong> kết quả.
            <% } %>
        </p>

        <% if (error != null) { %>
        <div class="pet-flash pet-flash--err">
            <i class="fa-solid fa-circle-exclamation"></i> <%= error %>
        </div>
        <% } %>

        <% if (hasQuery && total == 0) { %>
        <div class="pet-add-card mx-auto" style="max-width:420px;min-height:200px;">
            <i class="fa-solid fa-magnifying-glass fa-2x"></i>
            <span class="fw-semibold">Không tìm thấy kết quả nào</span>
            <span style="font-size:12.5px;">Thử từ khóa khác, ví dụ "spa", "tiêm phòng", "khách sạn".</span>
        </div>
        <% } %>

        <%-- Thú cưng của tôi --%>
        <% if (resultPets != null && !resultPets.isEmpty()) { %>
        <h2 class="h6 fw-bold text-uppercase mt-4 mb-3"
            style="letter-spacing:.04em;color:var(--text-body);">Thú cưng của bạn</h2>
        <div class="row g-3">
            <% for (Pet p : resultPets) { %>
            <div class="col-12 col-sm-6 col-lg-4">
                <div class="pet-card d-flex align-items-center gap-3">
                    <div class="pet-avatar bg-blue-tint text-blue">
                        <% if (p.getPhoto() != null) { %>
                        <img src="data:image/png;base64,<%= Base64.getEncoder().encodeToString(p.getPhoto()) %>"
                             alt="<%= p.getName() %>" />
                        <% } else { %>
                        <i class="fa-solid fa-paw"></i>
                        <% } %>
                    </div>
                    <div class="flex-grow-1">
                        <div class="fw-bold"><%= p.getName() %></div>
                        <div style="color:var(--text-body);font-size:12.5px;">
                            <%= p.getSpecies() == null ? "" : p.getSpecies() %>
                        </div>
                    </div>
                    <a class="btn btn-sm bg-blue-tint text-blue fw-semibold"
                       href="<%=request.getContextPath()%>/BookingServlet?petId=<%= p.getPetId() %>">Đặt lịch</a>
                </div>
            </div>
            <% } %>
        </div>
        <% } %>

        <%-- Trang dịch vụ --%>
        <% if (resultPages != null && !resultPages.isEmpty()) { %>
        <h2 class="h6 fw-bold text-uppercase mt-5 mb-3"
            style="letter-spacing:.04em;color:var(--text-body);">Dịch vụ</h2>
        <div class="row g-3">
            <% for (ServicePage sp : resultPages) { %>
            <div class="col-12 col-sm-6 col-lg-4">
                <a class="svc-other" href="<%=request.getContextPath()%>/service?type=<%= sp.getCode() %>">
                    <div class="svc-other-icon bg-<%= sp.getColorName() %>-tint text-<%= sp.getColorName() %>">
                        <i class="fa-solid <%= sp.getIconClass() %>"></i>
                    </div>
                    <div>
                        <div class="fw-bold"><%= sp.getTitle() %></div>
                        <div class="svc-other-tagline"><%= sp.getTagline() %></div>
                    </div>
                    <i class="fa-solid fa-arrow-right ms-auto" style="color:var(--text-input);"></i>
                </a>
            </div>
            <% } %>
        </div>
        <% } %>

        <%-- Hạng mục trong bảng giá --%>
        <% boolean hasItems = (resultSpa != null && !resultSpa.isEmpty())
                           || (resultMedical != null && !resultMedical.isEmpty())
                           || (resultRooms != null && !resultRooms.isEmpty());
           if (hasItems) { %>
        <h2 class="h6 fw-bold text-uppercase mt-5 mb-3"
            style="letter-spacing:.04em;color:var(--text-body);">Hạng mục &amp; bảng giá</h2>
        <div class="svc-price-card">
            <% if (resultRooms != null) for (RoomType r : resultRooms) { %>
            <div class="svc-price-row">
                <div>
                    <div class="fw-semibold"><%= r.getRoomName() %></div>
                    <div class="svc-price-desc">Khách sạn ·
                        <a href="<%=request.getContextPath()%>/service?type=hotel">xem dịch vụ</a>
                    </div>
                </div>
                <div class="svc-price-value text-blue">
                    <%= String.format("%,.0f", r.getPricePerDay()) %> đ<span>/ngày</span>
                </div>
            </div>
            <% } %>

            <% if (resultSpa != null) for (SpaServiceItem it : resultSpa) { %>
            <div class="svc-price-row">
                <div>
                    <div class="fw-semibold"><%= it.getItemName() %></div>
                    <div class="svc-price-desc">Spa ·
                        <a href="<%=request.getContextPath()%>/service?type=spa">xem dịch vụ</a>
                    </div>
                </div>
                <div class="svc-price-value text-pink">
                    <%= String.format("%,.0f", it.getItemPrice()) %> đ
                </div>
            </div>
            <% } %>

            <% if (resultMedical != null) for (MedicalServiceItem it : resultMedical) { %>
            <div class="svc-price-row">
                <div>
                    <div class="fw-semibold"><%= it.getItemName() %></div>
                    <div class="svc-price-desc">Y tế ·
                        <a href="<%=request.getContextPath()%>/service?type=medical">xem dịch vụ</a>
                    </div>
                </div>
                <div class="svc-price-value text-teal">
                    <%= String.format("%,.0f", it.getItemPrice()) %> đ
                </div>
            </div>
            <% } %>
        </div>
        <% } %>

    </div>

    <jsp:include page="Footer.jsp"></jsp:include>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>

</html>
