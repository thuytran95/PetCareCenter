<%-- 
    Document   : result
    Created on : Aug 9, 2025, 9:34:36 PM
    Author     : Duyet
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Kết quả đọc file</title>
        <style>
            body {
                font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                background-color: #fafafa;
                color: #333;
                margin: 30px auto;
                max-width: 700px;
                padding: 0 20px;
            }
            h2 {
                color: #1a73e8;
                text-align: center;
                margin-bottom: 25px;
            }
            div.content-box {
                background: #f9f9f9;
                padding: 20px;
                border: 1px solid #ccc;
                border-radius: 8px;
                white-space: pre-wrap; /* Giữ format xuống dòng trong nội dung */
                font-size: 16px;
                line-height: 1.5;
                box-shadow: 0 2px 6px rgba(0,0,0,0.1);
                min-height: 150px;
            }
            a {
                display: inline-block;
                margin-top: 30px;
                text-decoration: none;
                color: #1a73e8;
                font-weight: 600;
                border: 2px solid #1a73e8;
                padding: 8px 16px;
                border-radius: 6px;
                transition: background-color 0.3s, color 0.3s;
            }
            a:hover {
                background-color: #1a73e8;
                color: white;
            }
        </style>
    </head>
    <body>
        <h2>Nội dung của file:</h2>
        <div class="content-box">
            <%= request.getAttribute("content") %>
        </div>

        <a href="index.html">Trở về</a>
    </body>
</html>
