<%@page import="java.util.Base64" %>
    <%@ page contentType="text/html; charset=UTF-8" %>
        <%@ page import="com.petweb.utils.DBUtils, com.petweb.utils.ConnectionUtils, com.petweb.model.UserAccount" %>
            <%@ page import="java.sql.Connection" %>

                <% String userName=request.getParameter("userName"); Connection conn=ConnectionUtils.getConnection();
                    UserAccount user=DBUtils.findUser(conn, userName);%>

                    <!DOCTYPE html>
                    <html>

                    <head>
                        <meta charset="UTF-8">
                        <title>Profile</title>
                        <jsp:include page="linkgroup.jsp"></jsp:include>
                        <link rel="stylesheet" href="css/common.css" />
                        <link rel="stylesheet" href="css/profile.css" />
                    </head>

                    <body>
                        <div class="container">
                            <div>
                                <span class="text-body">
                                    <i class="fa-solid fa-chevron-left"></i>
                                </span>
                                <a class="link-underline link-underline-opacity-0 text-body" href="#">Quay lại</a>
                            </div>
                            <div class="row">
                                <div class="col-12 col-sm-4">
                                    <jsp:include page="setting-common.jsp"></jsp:include>
                                </div>
                                <div class="col-12 col-sm-8">
                                    <form method="post" action="editUser" enctype="multipart/form-data">
                                        <input type="hidden" name="id" value="<%= user.getId()%>" />
                                        <input type="hidden" name="userName" value="<%= user.getUserName()%>" />
                                        <input type="hidden" name="password" value="<%= user.getPassword()%>" />

                                        <div class="d-flex align-items-end gap-3 mb-5">
                                            <div class="user-avatar">
                                                <i class="fa-regular fa-user mx-auto"></i>
                                            </div>
                                            <button type="button" class="w-fit h-fit btn-change-avatar">
                                                Thay đổi hình ảnh
                                            </button>
                                        </div>

                                        <div class="d-flex flex-column gap-4 mb-5">
                                            <div class="form-container">
                                                <label>Họ và tên</label>
                                                <input class="form-control" type="text" name="fullName"
                                                    value="<%= user.getFullName()%>" />
                                            </div>

                                            <div class="form-container">
                                                <label>Email</label>
                                                <input class="form-control" type="text" name="email"
                                                    value="<%= user.getEmail()%>" readonly />
                                            </div>
                                            <div class="form-container">
                                                <label>Giới tính</label>
                                                <select class="form-select" name="gender">
                                                    <option value="F" <%="F" .equals(user.getGender()) ? "selected" : ""
                                                        %>>Nữ</option>
                                                    <option value="M" <%="M" .equals(user.getGender()) ? "selected" : ""
                                                        %>>Nam</option>
                                                </select>
                                            </div>
                                            <div class="form-container">
                                                <label>Điện thoại</label>
                                                <input class="form-control" type="tel" name="phone"
                                                    value="<%= user.getPhone()%>" />
                                            </div>
                                            <div class="form-container">
                                                <label>Ngày gia nhập thành viên</label>
                                                <input class="form-control" type="tel" name="text" value="20/09/2025" />
                                            </div>
                                        </div>
                                        <button class="btn btn-submit text-primary-blue mx-auto d-block">Cập
                                            nhật</button>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </body>

                    </html>