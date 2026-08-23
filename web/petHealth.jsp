<%@page contentType="text/html; charset=UTF-8" %>
<%@page import="java.util.List" %>
<%@page import="java.util.Base64" %>
<%@page import="com.petweb.model.Pet" %>
<%@page import="com.petweb.model.HealthRecord" %>
<%@page import="com.petweb.model.HealthCalendar" %>
<%
    // Dữ liệu do PetHealthServlet nạp sẵn.
    Pet pet = (Pet) request.getAttribute("pet");
    List<HealthRecord> records  = (List<HealthRecord>) request.getAttribute("records");
    List<HealthRecord> vaccines = (List<HealthRecord>) request.getAttribute("vaccines");
    List<HealthRecord> checkups = (List<HealthRecord>) request.getAttribute("checkups");
    List<HealthRecord> others   = (List<HealthRecord>) request.getAttribute("others");
    List<HealthRecord> dueList  = (List<HealthRecord>) request.getAttribute("dueRecords");
    HealthCalendar cal = (HealthCalendar) request.getAttribute("calendar");
    String loadError = (String) request.getAttribute("loadError");
%>
<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8">
    <title>Sổ sức khỏe<%= pet == null ? "" : " - " + pet.getName() %></title>
    <jsp:include page="linkgroup.jsp" />
    <link rel="stylesheet" href="css/common.css" />
    <link rel="stylesheet" href="css/header.css">
    <link rel="stylesheet" href="css/pet.css">
    <link rel="stylesheet" href="css/health.css">
</head>

<body>
    <jsp:include page="Header.jsp" />

    <div class="container py-5">

        <% if (loadError != null) { %>
        <div class="pet-flash pet-flash--err">
            <i class="fa-solid fa-circle-exclamation"></i> <%= loadError %>
        </div>
        <% } %>

        <% if (pet != null) { %>

        <%-- Thông tin bé --%>
        <div class="hl-head">
            <div class="hl-avatar bg-teal-tint text-teal">
                <% if (pet.getPhoto() != null) { %>
                <img src="data:image/png;base64,<%= Base64.getEncoder().encodeToString(pet.getPhoto()) %>"
                     alt="<%= pet.getName() %>" />
                <% } else { %>
                <i class="fa-solid fa-paw fa-lg"></i>
                <% } %>
            </div>
            <div class="flex-grow-1">
                <h1 class="h4 fw-bold mb-1">Sổ sức khỏe của <%= pet.getName() %></h1>
                <div style="color:var(--text-body);font-size:14px;">
                    <%= pet.getSpecies() == null ? "" : pet.getSpecies() %><%= pet.getBreed() == null ? "" : " · " + pet.getBreed() %>
                    · <%= records == null ? 0 : records.size() %> mục đã ghi
                </div>
            </div>
            <div class="d-flex gap-2 flex-wrap">
                <a class="btn btn-sm btn-outline-secondary" href="<%=request.getContextPath()%>/petProfile">
                    <i class="fa-solid fa-arrow-left me-1"></i> Hồ sơ
                </a>
                <a class="btn btn-sm btn-primary-blue fw-semibold"
                   href="<%=request.getContextPath()%>/BookingServlet?petId=<%= pet.getPetId() %>&serviceType=medical">
                    <i class="fa-solid fa-syringe me-1"></i> Đặt lịch y tế
                </a>
            </div>
        </div>

        <%-- Nhắc: mục sắp tới hạn hoặc quá hạn --%>
        <% if (dueList != null && !dueList.isEmpty()) { %>
        <h2 class="hl-section">
            <i class="fa-solid fa-bell me-2" style="color:var(--amber);"></i>Cần làm lại
        </h2>
        <div class="row g-3 mb-4">
            <% for (HealthRecord r : dueList) { %>
            <div class="col-12 col-md-6">
                <div class="hl-due <%= r.isOverdue() ? "hl-due--late" : "" %>">
                    <div class="hl-icon bg-<%= r.getColorName() %>-tint text-<%= r.getColorName() %>">
                        <i class="fa-solid <%= r.getIconClass() %>"></i>
                    </div>
                    <div class="flex-grow-1">
                        <div class="fw-bold"><%= r.getItemName() %></div>
                        <div class="hl-meta">
                            Lần gần nhất: <%= r.getFormattedPerformedAt() %>
                            · Hạn: <%= r.getFormattedNextDueAt() %>
                        </div>
                    </div>
                    <span class="hl-badge <%= r.isOverdue() ? "hl-badge--late" : "" %>">
                        <%= r.getDueText() %>
                    </span>
                </div>
            </div>
            <% } %>
        </div>
        <% } %>

        <%-- Chưa có gì trong sổ --%>
        <% if (records == null || records.isEmpty()) { %>
        <div class="hl-empty">
            <i class="fa-solid fa-notes-medical fa-2x mb-2"></i>
            <div class="fw-semibold">Sổ sức khỏe còn trống</div>
            <p class="mb-3" style="font-size:13px;color:var(--text-body);max-width:440px;">
                Các mũi tiêm, lần tẩy giun và khám sức khỏe sẽ được ghi vào đây tự động
                sau khi bạn đặt lịch dịch vụ y tế và hoàn tất thanh toán.
            </p>
            <a class="btn btn-primary-blue fw-semibold px-4"
               href="<%=request.getContextPath()%>/BookingServlet?petId=<%= pet.getPetId() %>&serviceType=medical">
                Đặt lịch y tế cho bé
            </a>
        </div>
        <% } else { %>

        <%-- Lịch tháng: nhìn một phát thấy ngay bé đã khám ngày nào, sắp tới hạn ngày nào --%>
        <% if (cal != null) { %>
        <h2 class="hl-section">
            <i class="fa-regular fa-calendar-days me-2" style="color:var(--primary-blue);"></i>Lịch khám &amp; tiêm
        </h2>
        <div class="hl-cal mb-4">
            <div class="hl-cal-bar">
                <a class="hl-cal-nav"
                   href="<%=request.getContextPath()%>/petHealth?petId=<%= pet.getPetId() %>&amp;ym=<%= cal.getPrevYm() %>"
                   title="Tháng trước"><i class="fa-solid fa-chevron-left"></i></a>
                <div class="hl-cal-title"><%= cal.getLabel() %></div>
                <a class="hl-cal-nav"
                   href="<%=request.getContextPath()%>/petHealth?petId=<%= pet.getPetId() %>&amp;ym=<%= cal.getNextYm() %>"
                   title="Tháng sau"><i class="fa-solid fa-chevron-right"></i></a>
            </div>

            <div class="hl-cal-grid hl-cal-head">
                <% for (String w : HealthCalendar.WEEKDAY_LABELS) { %>
                <div class="hl-cal-wd"><%= w %></div>
                <% } %>
            </div>

            <div class="hl-cal-grid">
                <% for (HealthCalendar.Day d : cal.getDays()) {
                       if (d.isBlank()) { %>
                <div class="hl-cal-cell hl-cal-cell--blank"></div>
                <%     continue;
                       }
                       String cls = "hl-cal-cell";
                       if (d.isToday()) cls += " hl-cal-cell--today";
                       if (d.hasAnything()) cls += " hl-cal-cell--mark";
                       if (d.isOverdue()) cls += " hl-cal-cell--late";
                       if (d.isWeekend()) cls += " hl-cal-cell--wk"; %>
                <div class="<%= cls %>" title="<%= d.getTooltip() %>">
                    <span class="hl-cal-num"><%= d.getDayOfMonth() %></span>
                    <div class="hl-cal-dots">
                        <% for (HealthRecord r : d.getDone()) { %>
                        <span class="hl-cal-dot bg-<%= r.getColorName() %>-tint text-<%= r.getColorName() %>">
                            <i class="fa-solid <%= r.getIconClass() %>"></i>
                        </span>
                        <% } %>
                        <% for (HealthRecord r : d.getDue()) { %>
                        <span class="hl-cal-dot hl-cal-dot--due"><i class="fa-regular fa-bell"></i></span>
                        <% } %>
                    </div>
                </div>
                <% } %>
            </div>

            <div class="hl-cal-legend">
                <span><span class="hl-cal-dot bg-amber-tint text-amber"><i class="fa-solid fa-syringe"></i></span> Tiêm phòng</span>
                <span><span class="hl-cal-dot bg-teal-tint text-teal"><i class="fa-solid fa-stethoscope"></i></span> Khám sức khỏe</span>
                <span><span class="hl-cal-dot bg-pink-tint text-pink"><i class="fa-solid fa-shield-virus"></i></span> Tẩy giun</span>
                <span><span class="hl-cal-dot hl-cal-dot--due"><i class="fa-regular fa-bell"></i></span> Đến hạn làm lại</span>
            </div>
        </div>

        <div class="hl-cal-sum">
            <% if (cal.isEmpty()) { %>
            <i class="fa-regular fa-face-smile"></i>
            Tháng này bé không có lịch khám hay mũi tiêm nào. Bấm mũi tên để xem tháng khác.
            <% } else { %>
            <i class="fa-solid fa-circle-info"></i>
            Tháng này có <strong><%= cal.getDoneCount() %></strong> lần đã thực hiện
            và <strong><%= cal.getDueCount() %></strong> mục đến hạn.
            <% } %>
        </div>
        <% } %>

        <%-- Sổ tiêm phòng & tẩy giun --%>
        <% if (vaccines != null && !vaccines.isEmpty()) { %>
        <h2 class="hl-section">
            <i class="fa-solid fa-syringe me-2" style="color:var(--amber);"></i>Sổ tiêm phòng &amp; tẩy giun
        </h2>
        <div class="hl-timeline mb-4">
            <% for (HealthRecord r : vaccines) { %>
            <div class="hl-item">
                <div class="hl-dot bg-<%= r.getColorName() %>-tint text-<%= r.getColorName() %>">
                    <i class="fa-solid <%= r.getIconClass() %>"></i>
                </div>
                <div class="hl-body">
                    <div class="d-flex justify-content-between align-items-start gap-2 flex-wrap">
                        <div>
                            <div class="fw-bold"><%= r.getItemName() %></div>
                            <div class="hl-meta"><%= r.getTypeLabel() %>
                                <% if (r.getBookingId() != null) { %>
                                · <a href="<%=request.getContextPath()%>/invoice?bookingId=<%= r.getBookingId() %>">Đơn #<%= r.getBookingId() %></a>
                                <% } %>
                            </div>
                        </div>
                        <div class="text-end">
                            <div class="hl-date"><%= r.getFormattedPerformedAt() %></div>
                            <% if (r.hasNextDue()) { %>
                            <div class="hl-meta">Làm lại: <%= r.getFormattedNextDueAt() %></div>
                            <% } %>
                        </div>
                    </div>
                </div>
            </div>
            <% } %>
        </div>
        <% } %>

        <%-- Khám sức khỏe --%>
        <% if (checkups != null && !checkups.isEmpty()) { %>
        <h2 class="hl-section">
            <i class="fa-solid fa-stethoscope me-2" style="color:var(--teal);"></i>Khám sức khỏe định kỳ
        </h2>
        <div class="hl-timeline mb-4">
            <% for (HealthRecord r : checkups) { %>
            <div class="hl-item">
                <div class="hl-dot bg-<%= r.getColorName() %>-tint text-<%= r.getColorName() %>">
                    <i class="fa-solid <%= r.getIconClass() %>"></i>
                </div>
                <div class="hl-body">
                    <div class="d-flex justify-content-between align-items-start gap-2 flex-wrap">
                        <div>
                            <div class="fw-bold"><%= r.getItemName() %></div>
                            <div class="hl-meta"><%= r.getTypeLabel() %>
                                <% if (r.getBookingId() != null) { %>
                                · <a href="<%=request.getContextPath()%>/invoice?bookingId=<%= r.getBookingId() %>">Đơn #<%= r.getBookingId() %></a>
                                <% } %>
                            </div>
                        </div>
                        <div class="text-end">
                            <div class="hl-date"><%= r.getFormattedPerformedAt() %></div>
                            <% if (r.hasNextDue()) { %>
                            <div class="hl-meta">Khám lại: <%= r.getFormattedNextDueAt() %></div>
                            <% } %>
                        </div>
                    </div>
                </div>
            </div>
            <% } %>
        </div>
        <% } %>

        <%-- Dịch vụ khác --%>
        <% if (others != null && !others.isEmpty()) { %>
        <h2 class="hl-section">
            <i class="fa-solid fa-notes-medical me-2" style="color:var(--blue);"></i>Dịch vụ y tế khác
        </h2>
        <div class="hl-timeline">
            <% for (HealthRecord r : others) { %>
            <div class="hl-item">
                <div class="hl-dot bg-blue-tint text-blue">
                    <i class="fa-solid fa-notes-medical"></i>
                </div>
                <div class="hl-body">
                    <div class="d-flex justify-content-between align-items-start gap-2 flex-wrap">
                        <div class="fw-bold"><%= r.getItemName() %></div>
                        <div class="hl-date"><%= r.getFormattedPerformedAt() %></div>
                    </div>
                </div>
            </div>
            <% } %>
        </div>
        <% } %>

        <% } %>
        <% } %>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.min.js"></script>
</body>

</html>
