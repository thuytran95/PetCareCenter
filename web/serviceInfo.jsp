<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.petweb.model.ServicePage" %>
<%@ page import="com.petweb.model.SpaServiceItem" %>
<%@ page import="com.petweb.model.MedicalServiceItem" %>
<%@ page import="com.petweb.model.RoomType" %>
<%
    // Nội dung do ServiceInfoServlet nạp; bảng giá đọc thẳng từ CSDL nên luôn khớp
    // với số tiền thật lúc đặt lịch.
    ServicePage svc = (ServicePage) request.getAttribute("page");
    List<ServicePage> otherPages = (List<ServicePage>) request.getAttribute("otherPages");
    List<SpaServiceItem> spaItems = (List<SpaServiceItem>) request.getAttribute("spaItems");
    List<MedicalServiceItem> medicalItems = (List<MedicalServiceItem>) request.getAttribute("medicalItems");
    List<RoomType> roomTypes = (List<RoomType>) request.getAttribute("roomTypes");

    String c = svc.getColorName();
    String bookUrl = request.getContextPath() + "/BookingServlet?serviceType=" + svc.getBookingType();
%>
<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8">
    <title><%= svc.getTitle() %> - Pet Care Center</title>
    <jsp:include page="linkgroup.jsp"></jsp:include>
    <link rel="stylesheet" href="css/header.css">
    <link rel="stylesheet" href="css/footer.css" />
    <link rel="stylesheet" href="css/pet.css">
    <link rel="stylesheet" href="css/serviceInfo.css">
</head>

<body>
    <jsp:include page="Header.jsp"></jsp:include>

    <%-- Phần mở đầu --%>
    <section class="svc-hero svc-hero--<%= c %>">
        <div class="container">
            <div class="row align-items-center g-4">
                <div class="col-12 col-lg-7">
                    <div class="svc-hero-icon bg-<%= c %>-tint text-<%= c %>">
                        <i class="fa-solid <%= svc.getIconClass() %>"></i>
                    </div>
                    <h1 class="svc-hero-title"><%= svc.getTitle() %></h1>
                    <p class="svc-hero-tagline"><%= svc.getTagline() %></p>
                    <p class="svc-hero-intro"><%= svc.getIntro() %></p>
                    <div class="d-flex flex-wrap gap-3 mt-4">
                        <a class="btn btn-svc btn-svc--<%= c %>" href="<%= bookUrl %>">
                            Đặt lịch ngay <i class="fa-solid fa-arrow-right ms-1"></i>
                        </a>
                        <a class="btn btn-svc-outline" href="#bang-gia">Xem bảng giá</a>
                    </div>
                </div>
                <div class="col-12 col-lg-5 d-none d-lg-block">
                    <div class="svc-hero-art bg-<%= c %>-tint text-<%= c %>">
                        <i class="fa-solid <%= svc.getIconClass() %>"></i>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <%-- Điểm nổi bật --%>
    <section class="svc-section">
        <div class="container">
            <h2 class="svc-section-title">Vì sao chọn dịch vụ này</h2>
            <div class="row g-3">
                <% for (ServicePage.Highlight h : svc.getHighlights()) { %>
                <div class="col-12 col-sm-6 col-lg-3">
                    <div class="svc-highlight">
                        <div class="svc-highlight-icon bg-<%= c %>-tint text-<%= c %>">
                            <i class="fa-solid <%= h.getIcon() %>"></i>
                        </div>
                        <div class="fw-bold mb-1"><%= h.getTitle() %></div>
                        <p class="mb-0"><%= h.getDescription() %></p>
                    </div>
                </div>
                <% } %>
            </div>
        </div>
    </section>

    <%-- Quy trình --%>
    <section class="svc-section svc-section--alt">
        <div class="container">
            <h2 class="svc-section-title">Quy trình thực hiện</h2>
            <ol class="svc-steps">
                <% int stepNo = 1;
                   for (String step : svc.getSteps()) { %>
                <li>
                    <span class="svc-step-no bg-<%= c %>-tint text-<%= c %>"><%= stepNo++ %></span>
                    <span><%= step %></span>
                </li>
                <% } %>
            </ol>
        </div>
    </section>

    <%-- Bảng giá lấy từ CSDL --%>
    <section class="svc-section" id="bang-gia">
        <div class="container">
            <h2 class="svc-section-title">Bảng giá</h2>
            <p class="svc-price-note"><%= svc.getPriceNote() %></p>

            <div class="svc-price-card">
                <% if (roomTypes != null && !roomTypes.isEmpty()) { %>
                    <% for (RoomType r : roomTypes) { %>
                    <div class="svc-price-row">
                        <div>
                            <div class="fw-semibold"><%= r.getRoomName() %></div>
                            <div class="svc-price-desc"><%= r.getDescription() == null ? "" : r.getDescription() %></div>
                        </div>
                        <div class="svc-price-value text-<%= c %>">
                            <%= String.format("%,.0f", r.getPricePerDay()) %> đ<span>/ngày</span>
                        </div>
                    </div>
                    <% } %>

                <% } else if (spaItems != null && !spaItems.isEmpty()) { %>
                    <% for (SpaServiceItem it : spaItems) { %>
                    <div class="svc-price-row">
                        <div class="fw-semibold"><%= it.getItemName() %></div>
                        <div class="svc-price-value text-<%= c %>">
                            <%= String.format("%,.0f", it.getItemPrice()) %> đ
                        </div>
                    </div>
                    <% } %>

                <% } else if (medicalItems != null && !medicalItems.isEmpty()) { %>
                    <% for (MedicalServiceItem it : medicalItems) { %>
                    <div class="svc-price-row">
                        <div class="fw-semibold"><%= it.getItemName() %></div>
                        <div class="svc-price-value text-<%= c %>">
                            <%= String.format("%,.0f", it.getItemPrice()) %> đ
                        </div>
                    </div>
                    <% } %>

                <% } else { %>
                    <div class="svc-price-empty">
                        <i class="fa-solid fa-tag mb-2 d-block"></i>
                        Bảng giá đang được cập nhật, vui lòng liên hệ trung tâm.
                    </div>
                <% } %>
            </div>
        </div>
    </section>

    <%-- Câu hỏi thường gặp --%>
    <section class="svc-section svc-section--alt">
        <div class="container">
            <h2 class="svc-section-title">Câu hỏi thường gặp</h2>
            <div class="accordion svc-faq" id="faqAccordion">
                <% int faqNo = 0;
                   for (ServicePage.Faq f : svc.getFaqs()) {
                       faqNo++;
                       String fid = "faq" + faqNo; %>
                <div class="accordion-item">
                    <h3 class="accordion-header">
                        <button class="accordion-button <%= faqNo == 1 ? "" : "collapsed" %>" type="button"
                                data-bs-toggle="collapse" data-bs-target="#<%= fid %>">
                            <%= f.getQuestion() %>
                        </button>
                    </h3>
                    <div id="<%= fid %>" class="accordion-collapse collapse <%= faqNo == 1 ? "show" : "" %>"
                         data-bs-parent="#faqAccordion">
                        <div class="accordion-body"><%= f.getAnswer() %></div>
                    </div>
                </div>
                <% } %>
            </div>
        </div>
    </section>

    <%-- Kêu gọi đặt lịch --%>
    <section class="svc-section">
        <div class="container">
            <div class="svc-cta bg-<%= c %>-tint">
                <div>
                    <h2 class="h4 fw-bold mb-1">Sẵn sàng đặt lịch cho bé?</h2>
                    <p class="mb-0" style="color:var(--text-body);font-size:14.5px;">
                        Chọn thú cưng và thời gian phù hợp, chỉ mất chưa tới một phút.
                    </p>
                </div>
                <a class="btn btn-svc btn-svc--<%= c %>" href="<%= bookUrl %>">
                    Đặt lịch ngay <i class="fa-solid fa-arrow-right ms-1"></i>
                </a>
            </div>
        </div>
    </section>

    <%-- Các dịch vụ khác --%>
    <section class="svc-section svc-section--alt">
        <div class="container">
            <h2 class="svc-section-title">Dịch vụ khác</h2>
            <div class="row g-3">
                <% for (ServicePage other : otherPages) {
                       if (other.getCode().equals(svc.getCode())) continue; %>
                <div class="col-12 col-sm-6 col-lg-4">
                    <a class="svc-other" href="<%=request.getContextPath()%>/service?type=<%= other.getCode() %>">
                        <div class="svc-other-icon bg-<%= other.getColorName() %>-tint text-<%= other.getColorName() %>">
                            <i class="fa-solid <%= other.getIconClass() %>"></i>
                        </div>
                        <div>
                            <div class="fw-bold"><%= other.getTitle() %></div>
                            <div class="svc-other-tagline"><%= other.getTagline() %></div>
                        </div>
                        <i class="fa-solid fa-arrow-right ms-auto" style="color:var(--text-input);"></i>
                    </a>
                </div>
                <% } %>
            </div>
        </div>
    </section>

    <jsp:include page="Footer.jsp"></jsp:include>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.min.js"></script>
</body>

</html>
