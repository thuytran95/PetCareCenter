<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>

<%
    Integer petId = (Integer) session.getAttribute("petId");
    Integer serviceId = (Integer) session.getAttribute("serviceId");
    List<String> selectedServices = (List<String>) session.getAttribute("selectedServices");
%>

<html>
<head>
    <title>Xem trước giao dịch</title>
</head>
<body>
    <h2>Xem trước giao dịch</h2>

    <p><b>Pet ID:</b> <%= petId %></p>
    <p><b>Service ID:</b> <%= serviceId %></p>

    <h3>Dịch vụ đã chọn:</h3>
    <ul>
        <% if (selectedServices != null) {
               for (String s : selectedServices) { %>
                   <li><%= s %></li>
        <%     }
           } else { %>
               <li>Chưa chọn dịch vụ nào</li>
        <% } %>
    </ul>

    <hr>
    <form action="BookingServlet" method="post">
        <button type="submit" name="action" value="confirm">Xác nhận giao dịch</button>
    </form>

    <br>
    <a href="chooseService.jsp?petId=<%= petId %>">Chọn thêm dịch vụ</a>
</body>
</html>
