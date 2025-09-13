<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.petweb.model.MedicalServiceItem" %>
<%@ page import="java.time.LocalDateTime" %>

<html>
<head>
    <title>Medical booking</title>
</head>
<body>
<h2>Xin mời đăng ký</h2>

<%
    Integer medicalId = (Integer) request.getAttribute("medicalId");
    List<MedicalServiceItem> allItems = (List<MedicalServiceItem>) request.getAttribute("allItems");

    LocalDateTime now = LocalDateTime.now();
    String admissionDate = now.toString().substring(0,16);
%>

<form action="MedicalBookingServlet" method="post">
    <table border="1">
        <tr>
            <th>Chọn</th>
            <th>Tên dịch vụ</th>
            <th>Giá</th>
        </tr>
        <%
            if (allItems != null && !allItems.isEmpty()) {
                for (MedicalServiceItem item : allItems) {
        %>
        <tr>
            <td><input type="checkbox" name="itemIds" value="<%= item.getItemId() %>"></td>
            <td><%= item.getItemName() %></td>
            <td><%= item.getItemPrice() %> VND</td>
        </tr>
        <%
                }
            } else {        
            response.sendRedirect("chooseService.jsp");
            return;
            }
        %>
    </table>
    <label>Ngày nhập viện (Admission Date): </label>
    <input type="datetime-local" name="admissionDate" value="<%= admissionDate %>"><br><br>
    <button type="submit">Đăng ký</button>
    <a href="chooseService.jsp">Quay lại đặt dịch vụ</a>
</form>

</body>
</html>
