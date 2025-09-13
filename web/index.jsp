<%@page import="java.util.Base64"%>
<%@page import="com.petweb.model.Pet"%>
<%@page import="com.petweb.utils.ConnectionUtils"%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="com.petweb.model.UserAccount, com.petweb.utils.MyUtils, com.petweb.utils.DBUtils" %>
<%@ page import="java.sql.Connection, java.util.List" %>
<%
    UserAccount u = MyUtils.getLoginedUser(session);
    if (u == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Home</title>
    <style>
        table { border-collapse: collapse; width: 80%; margin-top: 20px; }
        th, td { border: 1px solid #ccc; padding: 8px; text-align: left; }
        th { background-color: #f2f2f2; }
        a { text-decoration: none; color: blue; }
    </style>
</head>
<body>
<h2>PetWeb</h2>

Xin chào, <b><%= u.getFullName() != null ? u.getFullName() : u.getUserName() %></b>!
<br><br>

<% if ("admin".equals(u.getRole())) {
    System.out.println(u.getRole()); 
    Connection conn = ConnectionUtils.getConnection();
    List<UserAccount> listUser = DBUtils.queryUsers(conn); 
%>
    <h3>Danh sách tài khoản</h3>
    <table>
        <tr>
            <th>ID</th>
            <th>Avatar</th>
            <th>Username</th>
            <th>Email</th>
            <th>Phone</th>
            <th>Address</th>
            <th>Role</th>
            <th>Actions</th>
        </tr>
        <% for (UserAccount user : listUser) { %>
            <tr>
                <td><%= user.getId() %></td>
                <td>
                    <img src="data:image/jpeg;base64,<%= java.util.Base64.getEncoder().encodeToString(user.getAvatar()) %>" 
                     width="80" height="80" style="border-radius:4px;"/>

                </td>
                <td><%= user.getUserName() %></td>
                <td><%= user.getEmail() %></td>
                <td><%= user.getPhone() %></td>
                <td><%= user.getAddress() %></td>
                <td><%= user.getRole() %></td>
                <td>
                    <a href="editUser.jsp?userName=<%= user.getUserName() %>">Sửa</a>|
                    <a href="deleteUser?userName=<%= user.getUserName() %>">Xóa</a>
                </td>
            </tr>
        <% }%>
    </table>
<% } else { %>
    <h3>Thông tin cá nhân</h3>
      <td>
         <img src="<%= u.getAvatar() %>" alt="avatar" width="60" height="60" style="object-fit:cover; border-radius:50%;" />
       </td>
    <p><b>Username:</b> <%= u.getUserName() %></p>
    <p><b>Email:</b> <%= u.getEmail() %></p>
    <p><b>Phone:</b> <%= u.getPhone() %></p>
    <p><b>Address:</b> <%= u.getAddress() %></p>
<% } %>
<%
    List<Pet> listPet = null; 
    try {
        Connection conn = ConnectionUtils.getConnection();
        listPet = DBUtils.queryPets(conn, u.getId());
    } catch (Exception e) {
        out.println("<pre style='color:red;'>");
        e.printStackTrace(new java.io.PrintWriter(out));
        out.println("</pre>");
    }
%>

<h3>Danh sách Pet</h3>
<div style="display:flex; flex-wrap:wrap; gap:15px;">
<% if (listPet != null) {
    for (Pet p : listPet) { %>
    <div style="border:1px solid #000; padding:10px; width:200px; border-radius:8px; box-shadow:2px 2px 5px rgba(0,0,0,0.2);">
        <div style="text-align:center; margin-bottom:8px;">
            <% if (p.getPhoto() != null) { %>
                <img src="data:image/jpeg;base64,<%= java.util.Base64.getEncoder().encodeToString(p.getPhoto()) %>" 
                     width="80" height="80" style="border-radius:4px;"/>
            <% } else { %>
                Không có ảnh
            <% } %>
        </div>
        <div><strong>Tên:</strong> <%= p.getName() %></div>
        <div><strong>Loài:</strong> <%= p.getSpecies() %></div>
        <div><strong>Giống:</strong> <%= p.getBreed() %></div>
        <div><strong>Giới tính:</strong> <%= p.getGender() %></div>
        <div><strong>Ngày sinh:</strong> <%= p.getDob() %></div>
        <div><strong>Màu lông:</strong> <%= p.getFurColor() %></div>
        
        <div><strong>Đặc điểm:</strong> <%= p.getIdentifyingMarks() %></div>
        <div style="margin-top:8px; text-align:center;">
            <a href="<%=request.getContextPath()%>/editPet?petId=<%= p.getPetId() %>">Sửa</a>
            | 
            <a href="<%=request.getContextPath()%>/deletePet?petid=<%= p.getPetId() %>">Xóa</a>
            |
            <a href="BookingServlet?petId=<%= p.getPetId() %>">Booking</a>
        </div>
    </div>
<% } } %>

</div>

        

<br>
<a href="editUser.jsp?userName=<%= u.getUserName() %>">Sửa</a> |
<a href="<%=request.getContextPath()%>/login">Đăng xuất</a> |
<a href="changePassword.jsp">Thay đổi mật khẩu</a> |
<a href="addPet">Thêm Pet mới</a> |

</body>
</html>
