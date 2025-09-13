<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.petweb.model.MedicalServiceItem" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="com.petweb.model.MedicalDetail" %>

<html>
<head>
    <title>Kết quả đặt dịch vụ Y tế</title>
</head>
<body>
    <h2>Kết quả đặt dịch vụ Y tế</h2>

    <%
        List<MedicalServiceItem> selectedItems = (List<MedicalServiceItem>) request.getAttribute("selectedItems");
        BigDecimal totalPrice = (BigDecimal) request.getAttribute("totalPrice");
        MedicalDetail medical = (MedicalDetail) request.getAttribute("medical");
    %>

    <table border="1">
        <tr>
            <th>STT</th>
            <th>Tên dịch vụ</th>
            <th>Giá</th>
        </tr>
        <%
            int index = 1;
            for(MedicalServiceItem item : selectedItems) {
        %>
        <tr>
            <td><%= index++ %></td>
            <td><%= item.getItemName() %></td>
            <td><%= item.getItemPrice() %> VNĐ</td>
        </tr>
        <% } %>
        <tr>
            <td colspan="2"><b>Tổng cộng</b></td>
            <td><b><%= totalPrice %> VNĐ</b></td>
        </tr>
    </table>

    <p>Ngày nhập viện: <%= medical.getFormattedAdmissionDate() %></p>
    <a href="chooseService.jsp">Quay lại đặt dịch vụ</a>
</body>
</html>
