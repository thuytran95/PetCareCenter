<%@page contentType="text/html; charset=UTF-8" %>
<%@page import="java.util.List"%>
<%@page import="com.petweb.utils.DBUtils, com.petweb.utils.ConnectionUtils, com.petweb.model.Pet" %>
<%@page import="java.sql.Connection" %>
<%@page import="java.util.Base64"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%
    List<Pet> pets = null;
    try (Connection conn = ConnectionUtils.getConnection()) {
        // Nếu muốn theo userId thì viết queryPets(conn, userId)
        pets = DBUtils.queryAllPets(conn);
    } catch (Exception e) {
        e.printStackTrace();
    }
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Danh sách Pet</title>
    <jsp:include page="linkgroup.jsp"/>
    <link rel="stylesheet" href="css/common.css"/>
            <link rel="stylesheet" href="css/form.css"/>
            <link rel="stylesheet" href="css/login.css"/>
</head>
<body>
<jsp:include page="Header.jsp"/>
<div class="container">
    <h3 class="mb-4">Danh sách thú cưng</h3>
    <div class="text-end m-3">
    <a class="btn btn-success" href="<%=request.getContextPath()%>/addPet">+ Thêm thú cưng</a>
    </div>
    <% if (pets != null && !pets.isEmpty()) { %>
        <div class="row">
            <% for (Pet p : pets) { %>
                <div class="col-12 col-md-6 mb-3">
                    <div class="card p-3 shadow-sm">
                        <div class="d-flex align-items-center gap-3">
                            <div>
                                <% if (p.getPhoto() != null) { %>
                                    <img src="data:image/png;base64,<%= Base64.getEncoder().encodeToString(p.getPhoto()) %>"
                                         class="rounded-circle" width="70" height="70"/>
                                <% } else { %>
                                    <i class="fa-solid fa-paw fa-2x"></i>
                                <% } %>
                            </div>
                            <div>
                                <h5><%= p.getName() %></h5>
                                <p class="mb-1">Loài: <%= p.getSpecies() %></p>
                                <p class="mb-1">Giống: <%= p.getBreed() %></p>
                                <a class="btn btn-sm btn-primary" href="<%=request.getContextPath()%>/editPet?petId=<%= p.getPetId() %>">Sửa</a>
                                <a class="btn btn-sm btn-primary" href="<%=request.getContextPath()%>/deletePet?petid=<%= p.getPetId() %>">Xóa</a>
                                <a class="btn btn-sm btn-primary" href="BookingServlet?petId=<%= p.getPetId() %>">Booking</a>
                            </div>
                        </div>
                    </div>
                </div>
                            
            <% } %>
        </div>
    <% } else { %>
        <p>Chưa có thú cưng nào.</p>
    <% } %>
</div>
</body>
</html>
