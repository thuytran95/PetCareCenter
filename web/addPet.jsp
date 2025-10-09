<%@page import="com.petweb.model.UserAccount"%>
<%@page import="com.petweb.utils.MyUtils" %>
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
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">

<div class="container py-5">
    <div class="row justify-content-center">
        <div class="col-md-8 col-lg-6">
            <div class="card shadow-sm">
                <div class="card-header bg-primary text-white text-center">
                    <h4 class="mb-0">Thêm Pet mới</h4>
                </div>
                <div class="card-body">
                    <form method="post" action="addPet" enctype="multipart/form-data">
                        
                        <div class="mb-3">
                            <label class="form-label">Tên</label>
                            <input type="text" class="form-control" name="name" required>
                        </div>

                        <div class="mb-3">
                            <label class="form-label">Loài</label>
                            <input type="text" class="form-control" name="species" required>
                        </div>

                        <div class="mb-3">
                            <label class="form-label">Giống</label>
                            <input type="text" class="form-control" name="breed">
                        </div>

                        <div class="mb-3">
                            <label class="form-label">Giới tính</label>
                            <select class="form-select" name="gender">
                                <option value="">-- Chọn --</option>
                                <option value="M">Đực (M)</option>
                                <option value="F">Cái (F)</option>
                            </select>
                        </div>

                        <div class="mb-3">
                            <label class="form-label">Ngày sinh</label>
                            <input type="date" class="form-control" name="dob">
                        </div>

                        <div class="mb-3">
                            <label class="form-label">Màu lông</label>
                            <input type="text" class="form-control" name="furColor">
                        </div>

                        <div class="mb-3">
                            <label class="form-label">Dấu hiệu nhận dạng</label>
                            <input type="text" class="form-control" name="identifyingMarks">
                        </div>

                        <div class="mb-3">
                            <label class="form-label">Ảnh</label>
                            <input type="file" class="form-control" name="photo" accept="image/*">
                        </div>

                        <input type="hidden" name="userId" value="<%= user.getId() %>">

                        <div class="d-grid">
                            <button type="submit" class="btn btn-success">+ Thêm Pet</button>
                        </div>
                    </form>
                </div>
                <div class="card-footer text-center">
                    <a href="<%=request.getContextPath()%>/petProfile.jsp" class="btn btn-link">← Quay lại danh sách</a>
                </div>
            </div>
        </div>
    </div>
</div>

</body>
</html>
