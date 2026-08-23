<%@page contentType="text/html; charset=UTF-8" %>
<%@page import="java.util.List" %>
<%@page import="com.petweb.model.Pet" %>
<%@page import="java.util.Base64" %>
<%
    // Dữ liệu do PetProfileServlet nạp sẵn (JSP không tự truy vấn DB nữa:
    // JDBCFilter chỉ cấp Connection cho URL trỏ tới servlet).
    List<Pet> pets = (List<Pet>) request.getAttribute("pets");
    String loadError = (String) request.getAttribute("loadError");

    String flashMessage = (String) session.getAttribute("message");
    String flashError = (String) session.getAttribute("error");
    session.removeAttribute("message");
    session.removeAttribute("error");

    String[] petColors = {"blue", "pink", "amber", "teal"};

    // Khi khách bấm "Đặt lịch" cho một dịch vụ ở trang chủ, serviceType được mang tới đây
    // để sau khi chọn thú cưng là vào thẳng dịch vụ đó.
    String wantService = request.getParameter("serviceType");
    String serviceQuery = "";
    String serviceLabel = null;
    if ("spa".equals(wantService))          { serviceQuery = "&serviceType=spa";     serviceLabel = "Spa"; }
    else if ("hotel".equals(wantService))   { serviceQuery = "&serviceType=hotel";   serviceLabel = "Khách sạn"; }
    else if ("medical".equals(wantService)) { serviceQuery = "&serviceType=medical"; serviceLabel = "Y tế"; }
%>
<!DOCTYPE html>
<html>

    <head>
        <meta charset="UTF-8">
        <title>Hồ sơ thú cưng</title>
        <jsp:include page="linkgroup.jsp" />
        <link rel="stylesheet" href="css/common.css" />
        <link rel="stylesheet" href="css/form.css" />
        <link rel="stylesheet" href="css/header.css">
        <link rel="stylesheet" href="css/pet.css">
    </head>

    <body>
        <jsp:include page="Header.jsp" />
        <div class="container pb-5">
            <div class="d-flex align-items-center justify-content-between flex-wrap gap-3 mb-4 pt-4">
                <div>
                    <h1 class="h3 fw-bold mb-1">Danh sách thú cưng</h1>
                    <p class="mb-0" style="color: var(--text-body); font-size: 14.5px;">Quản lý hồ sơ và đặt lịch chăm sóc cho từng bé.</p>
                </div>
                <a class="btn btn-primary-blue fw-semibold px-4 py-2" href="<%=request.getContextPath()%>/addPet">
                    <i class="fa-solid fa-plus me-1"></i> Thêm thú cưng
                </a>
            </div>

            <% if (serviceLabel != null) { %>
            <div class="pet-flash" style="background:var(--light-blue);color:var(--primary-blue);">
                <i class="fa-solid fa-circle-info"></i>
                Chọn thú cưng bạn muốn đặt dịch vụ <strong><%= serviceLabel %></strong>
            </div>
            <% } %>

            <% if (flashMessage != null) { %>
            <div class="pet-flash pet-flash--ok">
                <i class="fa-solid fa-circle-check"></i> <%= flashMessage %>
            </div>
            <% } %>
            <% if (flashError != null || loadError != null) { %>
            <div class="pet-flash pet-flash--err">
                <i class="fa-solid fa-circle-exclamation"></i> <%= flashError != null ? flashError : loadError %>
            </div>
            <% } %>

            <% if (pets != null && !pets.isEmpty()) { %>
            <div class="row g-3">
                <% int idx = 0;
                    for (Pet p : pets) {
                        String color = petColors[idx % petColors.length];
                        idx++;
                %>
                <div class="col-12 col-md-6 col-lg-4">
                    <div class="pet-card d-flex flex-column gap-3">
                        <div class="d-flex align-items-center gap-3">
                            <div class="pet-avatar bg-<%= color%>-tint text-<%= color%>">
                                <% if (p.getPhoto() != null) {%>
                                <img src="data:image/png;base64,<%= Base64.getEncoder().encodeToString(p.getPhoto())%>" alt="<%= p.getName()%>" />
                                <% } else { %>
                                <i class="fa-solid fa-paw fa-lg"></i>
                                <% }%>
                            </div>
                            <div>
                                <h5 class="mb-0 fw-bold"><%= p.getName()%></h5>
                                <div style="color: var(--text-body); font-size: 13px;">
                                    <%= p.getSpecies() == null ? "" : p.getSpecies() %><%= p.getBreed() == null ? "" : " · " + p.getBreed() %>
                                </div>
                            </div>
                        </div>
                        <div class="pet-menu mt-auto">
                            <a class="pet-menu-item"
                               href="<%=request.getContextPath()%>/BookingServlet?petId=<%= p.getPetId()%><%= serviceQuery %>">
                                <span class="pet-menu-icon bg-<%= color%>-tint text-<%= color%>">
                                    <i class="fa-regular fa-calendar-plus"></i>
                                </span>
                                <span class="flex-grow-1">
                                    <span class="pet-menu-title">Đặt lịch dịch vụ</span>
                                    <span class="pet-menu-sub">Khách sạn, spa hoặc y tế</span>
                                </span>
                                <i class="fa-solid fa-chevron-right pet-menu-go"></i>
                            </a>
                            <a class="pet-menu-item"
                               href="<%=request.getContextPath()%>/petHealth?petId=<%= p.getPetId()%>">
                                <span class="pet-menu-icon bg-amber-tint text-amber">
                                    <i class="fa-solid fa-syringe"></i>
                                </span>
                                <span class="flex-grow-1">
                                    <span class="pet-menu-title">Khám định kỳ &amp; sổ tiêm</span>
                                    <span class="pet-menu-sub">Xem lịch và lịch sử khám</span>
                                </span>
                                <i class="fa-solid fa-chevron-right pet-menu-go"></i>
                            </a>
                        </div>
                        <div class="pet-actions d-flex gap-2">
                            <a class="btn btn-sm btn-outline-secondary flex-fill" title="Sửa hồ sơ"
                               href="<%=request.getContextPath()%>/editPet?petId=<%= p.getPetId()%>"><i class="fa-solid fa-pen me-1"></i> Sửa</a>
                            <a class="btn btn-sm btn-outline-secondary btn-delete-pet flex-fill" title="Xóa hồ sơ"
                               href="<%=request.getContextPath()%>/deletePet?petid=<%= p.getPetId()%>"><i class="fa-solid fa-trash me-1"></i> Xóa</a>
                        </div>
                    </div>
                </div>
                <% } %>

                <div class="col-12 col-md-6 col-lg-4">
                    <a class="pet-add-card" href="<%=request.getContextPath()%>/addPet">
                        <i class="fa-solid fa-plus fa-lg"></i>
                        <span class="fw-semibold" style="font-size:13.5px;">Thêm thú cưng mới</span>
                    </a>
                </div>
            </div>
            <% } else if (loadError == null) { %>
            <a class="pet-add-card mx-auto" style="max-width: 360px;" href="<%=request.getContextPath()%>/addPet">
                <i class="fa-solid fa-paw fa-2x"></i>
                <span class="fw-semibold">Chưa có thú cưng nào, hãy thêm bé đầu tiên!</span>
            </a>
            <% }%>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
        <script>
            document.querySelectorAll(".btn-delete-pet").forEach(function (el) {
                el.addEventListener("click", function (e) {
                    if (!confirm("Bạn chắc chắn muốn xóa thú cưng này?")) e.preventDefault();
                });
            });
        </script>
    </body>

</html>
