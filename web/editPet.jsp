<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="com.petweb.model.Pet, java.util.Base64" %>

<%
    Pet pet = (Pet) request.getAttribute("pet");
    if (pet == null) {
        response.sendRedirect("index.jsp");
        return;
    }
%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Sửa thông tin Pet</title>
    <jsp:include page="linkgroup.jsp"/>
    <link rel="stylesheet" href="css/common.css" />
</head>
<body>
    <jsp:include page="Header.jsp"/>
<div class="container mt-5">
    <div class="card shadow-sm p-4 mx-auto" style="max-width: 500px;">
        
        <!-- Ảnh đại diện -->
        <div class="text-center mb-4">
            <% if (pet.getPhoto() != null) { %>
                <img src="data:image/jpeg;base64,<%= Base64.getEncoder().encodeToString(pet.getPhoto()) %>" 
                     class="rounded-circle border" width="120" height="120"/>
            <% } else { %>
                <div class="rounded-circle bg-light d-flex align-items-center justify-content-center border" 
                     style="width:120px; height:120px; margin: 0 auto;">
                    <i class="fa-solid fa-paw fs-1 text-muted"></i>
                </div>
            <% } %>
        </div>

        <h3 class="text-center text-primary mb-4">Sửa thông tin Pet</h3>

        <form method="post" action="editPet" enctype="multipart/form-data">
            <input type="hidden" name="petId" value="<%= pet.getPetId() %>" />

            <div class="mb-3">
                <label class="form-label">Tên</label>
                <input type="text" class="form-control" name="name" value="<%= pet.getName() %>" />
            </div>

            <div class="mb-3">
                <label class="form-label">Loài</label>
                <input type="text" class="form-control" name="species" value="<%= pet.getSpecies() %>" />
            </div>

            <div class="mb-3">
                <label class="form-label">Giống</label>
                <input type="text" class="form-control" name="breed" value="<%= pet.getBreed() %>" />
            </div>

            <div class="mb-3">
                <label class="form-label">Giới tính</label>
                <select name="gender" class="form-select">
                    <option value="M" <%= "M".equals(pet.getGender()) ? "selected" : "" %>>Đực</option>
                    <option value="F" <%= "F".equals(pet.getGender()) ? "selected" : "" %>>Cái</option>
                </select>
            </div>

            <div class="mb-3">
                <label class="form-label">Ngày sinh</label>
                <input type="date" class="form-control" name="dob" 
                       value="<%= (pet.getDob() != null ? pet.getDob() : "") %>" />
            </div>

            <div class="mb-3">
                <label class="form-label">Màu lông</label>
                <input type="text" class="form-control" name="furColor" value="<%= pet.getFurColor() %>" />
            </div>

            <div class="mb-3">
                <label class="form-label">Đặc điểm nhận dạng</label>
                <input type="text" class="form-control" name="identifyingMarks" value="<%= pet.getIdentifyingMarks() %>" />
            </div>

            <div class="mb-4">
                <label class="form-label">Tải ảnh mới</label>
                <input type="file" class="form-control" name="photo" accept="image/*" />
            </div>

            <div class="d-flex justify-content-between">
                <a href="petProfile.jsp" class="btn btn-outline-secondary">Quay lại</a>
                <button type="submit" class="btn btn-primary">Cập nhật Pet</button>
            </div>
        </form>
    </div>
</div>
</body>
</html>
