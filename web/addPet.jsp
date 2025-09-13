
<%@page import="com.petweb.model.UserAccount"%>

<%@ page import="com.petweb.utils.MyUtils" %>
<%@ page contentType="text/html;charset=UTF-8" %>

<%
    UserAccount user = MyUtils.getLoginedUser(session);
    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Thêm Pet</title>
</head>
<body>
    <h2>Thêm Pet mới</h2>
    <form method="post" action="addPet" enctype="multipart/form-data">
        

        Tên: <input type="text" name="name" required /><br/><br/>
        Loài: <input type="text" name="species" required /><br/><br/>
        Giống: <input type="text" name="breed" /><br/><br/>
        Giới tính (M/F): <input type="text" name="gender" maxlength="1" /><br/><br/>
        Ngày sinh: <input type="date" name="dob" /><br/><br/>
        Màu lông: <input type="text" name="furColor" /><br/><br/>
        Dấu hiệu nhận dạng: <input type="text" name="identifyingMarks" /><br/><br/>
        Ảnh: <input type="file" name="photo" accept="image/*" /><br/><br/>
        <input type="hidden" name="userId" value="<%= user.getId() %>" />
        <input type="submit" value="Thêm Pet" />
    </form>
</body>
</html>
