<%@page import="java.util.Base64" %>
    <%@page contentType="text/html; charset=UTF-8" %>
        <%@page import="com.petweb.utils.DBUtils, com.petweb.utils.ConnectionUtils, com.petweb.model.UserAccount" %>
            <%@page import="java.sql.Connection" %>
<%!
    /**
     * In giá trị an toàn. Thẻ biểu thức JSP mặc định in ra chữ "null" khi giá trị
     * là null, và chuỗi đó bị lưu thẳng vào CSDL khi người dùng bấm Lưu.
     * Đây chính là lý do cột phone của một tài khoản đang chứa chữ "null".
     */
    private String nz(Object v) { return v == null ? "" : String.valueOf(v); }
%>

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
                            <div class="row">
                                <div class="col-12 col-sm-4">
                                    <jsp:include page="setting-common.jsp" />
                                </div>
                                <div class="col-12 col-sm-8">
                                    <form method="post" action="editUser" enctype="multipart/form-data">
                                        <input type="hidden" name="id" value="<%= nz(user.getId())%>" />
                                        <input type="hidden" name="userName" value="<%= nz(user.getUserName())%>" />
                                        <input type="hidden" name="password" value="<%= nz(user.getPassword())%>" />

                                        <% String formError = (String) request.getAttribute("error"); %>
                                        <% if (formError != null) { %>
                                        <div class="d-flex align-items-start gap-2 rounded-4 px-3 py-2 mb-4"
                                             style="background:var(--pink-tint);color:var(--pink);font-size:13.5px;font-weight:600;">
                                            <i class="fa-solid fa-circle-exclamation mt-1"></i>
                                            <span><%= formError %></span>
                                        </div>
                                        <% } %>

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
                                                value="<%= nz(user.getFullName())%>" />
                                        </div>

                                        <div class="form-container">
                                            <label>Email</label>
                                            <input class="form-control" type="text" name="email"
                                                value="<%= nz(user.getEmail())%>" readonly />
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
                                            <input class="form-control" type="tel" name="phone" placeholder="VD: 0912345678"
                                                value="<%= nz(user.getPhone())%>" />
                                            <small style="color:var(--text-body);font-size:12px;">
                                                Cần ít nhất 8 chữ số để nhận thông báo đặt lịch.
                                                Để trống nếu bạn chưa muốn nhận tin.
                                            </small>
                                        </div>

                                        <button class="btn btn-submit text-primary-blue mx-auto d-block">Cập
                                            nhật</button>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </body>

                    </html>