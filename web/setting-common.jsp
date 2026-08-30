<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="com.petweb.utils.DBUtils, com.petweb.utils.ConnectionUtils, com.petweb.model.UserAccount"%>
<%@page import="java.sql.Connection"%>
<%
    String userName = null;
    Object sessionUserName = session.getAttribute("userName");
    if (sessionUserName != null) {
        userName = String.valueOf(sessionUserName);
    } else {
        UserAccount loginedUser = (UserAccount) session.getAttribute("loginedUser");
        if (loginedUser != null) {
            userName = loginedUser.getUserName();
        }
    }
    if (userName == null || userName.isBlank()) {
        String paramUserName = request.getParameter("userName");
        if (paramUserName != null && !paramUserName.isBlank()) {
            userName = paramUserName;
        }
    }
    if (userName == null || userName.isBlank()) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId != null) {
            Connection conn = ConnectionUtils.getConnection();
            try {
                UserAccount user = DBUtils.findUser(conn, userId);
                if (user != null) {
                    userName = user.getUserName();
                }
            } finally {
                conn.close();
            }
        }
    }
    if (userName == null || userName.isBlank()) {
        return;
    }

    String uri = request.getRequestURI();
    boolean profileActive = uri.contains("editUser");
    boolean passwordActive = uri.contains("changePassword");
    String profileMenuClass = "d-flex gap-2 align-items-center justify-content-center link-underline link-underline-opacity-0 text-body"
            + (profileActive ? " profile-menu__item--active" : "");
    String passwordMenuClass = "d-flex gap-2 align-items-center justify-content-center link-underline link-underline-opacity-0 text-body"
            + (passwordActive ? " profile-menu__item--active" : "");
%>
<div>
    <a href="${pageContext.request.contextPath}/" class="btn btn-back mb-3">
        <i class="fa-solid fa-arrow-left"></i> Quay lại
    </a>
    <div class="profile-header">Cài đặt thông tin</div>
    <div class="d-flex profile-menu flex-column align-items-start">
        <a class="<%= profileMenuClass %>"
           href="${pageContext.request.contextPath}/editUser.jsp?userName=<%= userName %>">
            <span class="circle"><i class="fa-solid fa-user mx-auto"></i></span>
            <span>Thông tin của bạn</span>
        </a>
        <a class="<%= passwordMenuClass %>"
           href="${pageContext.request.contextPath}/changePassword.jsp">
            <span class="circle"><i class="fa-solid fa-lock mx-auto"></i></span>
            <span>Cài đặt mật khẩu</span>
        </a>
    </div>
</div>
