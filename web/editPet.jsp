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
<html>
<head>
    <meta charset="UTF-8">
    <title>Sửa Pet</title>
</head>
<body>
    <h2>Sửa thông tin Pet</h2>
    <form method="post" action="editPet" enctype="multipart/form-data">
        <input type="hidden" name="petId" value="<%= pet.getPetId() %>" />

        Tên: <input type="text" name="name" value="<%= pet.getName() %>" /><br/><br/>
        Loài: <input type="text" name="species" value="<%= pet.getSpecies() %>" /><br/><br/>
        Giống: <input type="text" name="breed" value="<%= pet.getBreed() %>" /><br/><br/>
        Giới tính (M/F): <input type="text" name="gender" value="<%= pet.getGender() %>" maxlength="1" /><br/><br/>
        Ngày sinh: <input type="date" name="dob" value="<%= (pet.getDob() != null ? pet.getDob() : "") %>" /><br/><br/>
        Màu lông: <input type="text" name="furColor" value="<%= pet.getFurColor() %>" /><br/><br/>
        Dấu hiệu nhận dạng: <input type="text" name="identifyingMarks" value="<%= pet.getIdentifyingMarks() %>" /><br/><br/>
        
       

        Ảnh hiện tại:<br/>
        <% if (pet.getPhoto() != null) { %>
            <img src="data:image/jpeg;base64,<%= Base64.getEncoder().encodeToString(pet.getPhoto()) %>" width="120" /><br/><br/>
        <% } else { %>
            Không có ảnh<br/><br/>
        <% } %>

        Tải ảnh mới: <input type="file" name="photo" accept="image/*" /><br/><br/>
        <input type="submit" value="Cập nhật Pet" />
    </form>

    <br/>
    <a href="index.jsp">Quay lại danh sách</a>
</body>
</html>
