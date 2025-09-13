<%@page import="com.petweb.model.SpaDetail"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.petweb.model.SpaServiceItem" %>
<%@ page import="java.math.BigDecimal" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Kết quả đặt Spa</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        h2 { color: #2c3e50; }
        table { width: 50%; border-collapse: collapse; margin-top: 20px; }
        th, td { border: 1px solid #ccc; padding: 8px; text-align: left; }
        th { background-color: #f2f2f2; }
        .total { font-weight: bold; }
    </style>
</head>
<body>
    <h2>Kết quả đặt dịch vụ Spa</h2>

    <%
        List<SpaServiceItem> selectedItems = (List<SpaServiceItem>) request.getAttribute("selectedItems");
        BigDecimal totalPrice = (BigDecimal) request.getAttribute("totalPrice");
        SpaDetail spa = (SpaDetail) request.getAttribute("spa");
        if(selectedItems == null || selectedItems.isEmpty()) {
    %>
        <p>Bạn chưa chọn dịch vụ nào.</p>
    <%
        } else {
    %>
        <table>
            <tr>
                <th>STT</th>
                <th>Tên dịch vụ</th>
                <th>Giá</th>
                
            </tr>
            <%
                int index = 1;
                for(SpaServiceItem item : selectedItems) {
            %>
            <tr>
                <td><%= index++ %></td>
                <td><%= item.getItemName() %></td>
                <td><%= item.getItemPrice() %> VNĐ</td>
            </tr>
            <%
                }
            %>
            <tr>
                <td colspan="2" class="total">Tổng cộng</td>
                <td class="total"><%= totalPrice %> VNĐ</td>
            </tr>
        </table>
    <%
        }
    %>
        <p>Ngày Hẹn: <%= spa.getFormattedbookingDate()%></p>
    <br>
     <a href="chooseService.jsp">Quay lại đặt dịch vụ</a>
</body>
</html>
