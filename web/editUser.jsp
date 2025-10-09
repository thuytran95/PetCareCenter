<%@page import="java.util.Base64" %>
    <%@page contentType="text/html; charset=UTF-8" %>
        <%@page import="com.petweb.utils.DBUtils, com.petweb.utils.ConnectionUtils, com.petweb.model.UserAccount" %>
            <%@page import="java.sql.Connection" %>

                <% UserAccount user=(UserAccount) request.getAttribute("user"); if (user==null) { String
                    userName=request.getParameter("userName"); if (userName !=null) { Connection
                    conn=ConnectionUtils.getConnection(); user=DBUtils.findUser(conn, userName); conn.close(); } } %>

                    <!DOCTYPE html>
                    <html>

                    <head>
                        <meta charset="UTF-8">
                        <title>Profile</title>
                        <jsp:include page="linkgroup.jsp" />
                        <link rel="stylesheet" href="css/common.css" />
                        <link rel="stylesheet" href="css/profile.css" />
                        <link rel="stylesheet" href="css/header.css">
                        <link rel="stylesheet" href="css/form.css" />

                    </head>

                    <body>
 
                        <div class="container py-5">
                            <div>
                                <button class="text-body btn" onclick="history.back()">Quay lại</button>
                            </div>
                            <div class="row">
                                <div class="col-12 col-sm-4">
                                    <jsp:include page="setting-common.jsp" />
                                </div>
                                <div class="col-12 col-sm-8">
                                    <form method="post" action="editUser" enctype="multipart/form-data">
                                        <input type="hidden" name="id" value="<%= user.getId()%>" />
                                        <input type="hidden" name="userName" value="<%= user.getUserName()%>" />
                                        <input type="hidden" name="password" value="<%= user.getPassword()%>" />

                                        <div class="d-flex align-items-end gap-3 mb-5">
                                            <div class="user-avatar">
                                                <% if (user.getAvatar() !=null) { %>
                                                    <img src="data:image/png;base64,<%= Base64.getEncoder().encodeToString(user.getAvatar()) %>"
                                                        class="rounded-circle" width="100" height="100" />
                                                    <% } else { %>
                                                        <i class="fa-regular fa-user mx-auto"></i>
                                                        <% } %>
                                            </div>
                                            <input type="file" name="avatar" class="form-control w-auto" />
                                        </div>

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
                                                <option value="F" <%="F" .equals(user.getGender()) ? "selected" : "" %>
                                                    >Nữ</option>
                                                <option value="M" <%="M" .equals(user.getGender()) ? "selected" : "" %>
                                                    >Nam</option>
                                            </select>
                                        </div>

                                        <div class="form-container mb-4">
                                            <label>Điện thoại</label>
                                            <input class="form-control" type="tel" name="phone"
                                                value="<%= user.getPhone()%>" />
                                        </div>

                                        <button class="btn btn-submit text-primary-blue mx-auto d-block">Cập
                                            nhật</button>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </body>

                    </html>