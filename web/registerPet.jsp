<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Thêm Pet</title>
</head>
<body>
    <h2>Đăng ký thú cưng</h2>

    <p style="color:red;"><%= request.getAttribute("error") != null ? request.getAttribute("error") : "" %></p>
    <p style="color:green;"><%= request.getAttribute("message") != null ? request.getAttribute("message") : "" %></p>

    <form action="<%=request.getContextPath()%>/addPet" method="post" enctype="multipart/form-data">
        <table>
            <tr>
                <td>Tên thú cưng*</td>
                <td><input type="text" name="name" required></td>
            </tr>
            <tr>
                <td>Loài (species)</td>
                <td><input type="text" name="species"></td>
            </tr>
            <tr>
                <td>Giống (breed)</td>
                <td><input type="text" name="breed"></td>
            </tr>
            <tr>
                <td>Giới tính</td>
                <td>
                    <select name="gender">
                        <option value="M">Đực</option>
                        <option value="F">Cái</option>
                    </select>
                </td>
            </tr>
            <tr>
                <td>Ngày sinh (DOB)</td>
                <td><input type="date" name="dob"></td>
            </tr>
            <tr>
                <td>Màu lông</td>
                <td><input type="color" name="furColor"></td>
            </tr>
            <tr>
                <td>Đặc điểm nhận dạng</td>
                <td><input type="text" name="identifyingMarks"></td>
            </tr>
            <tr>
                <td>Ảnh</td>
                <td><input type="file" name="photo" accept="image/*"></td>
            </tr>
            <tr>
                <td colspan="2"><input type="submit" value="Lưu Pet"></td>
            </tr>
        </table>
    </form>
</body>
</html>
