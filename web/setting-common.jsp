<%-- Document : setting-common Created on : Sep 15, 2025, 11:24:07 PM Author : Admin --%>

    <%@page import="com.petweb.utils.ConnectionUtils" %>
        <%@page contentType="text/html" pageEncoding="UTF-8" %>
            <%@ page import="com.petweb.utils.DBUtils, com.petweb.utils.ConnectionUtils, com.petweb.model.UserAccount"
                %>
                <%@ page import="java.sql.Connection" %>
                    <% Integer userId=(Integer) session.getAttribute("userId"); if (userId==null) { return; } Connection
                        conn=ConnectionUtils.getConnection(); UserAccount user=DBUtils.findUser(conn, userId); String
                        userName=user.getUserName(); %>
                        <div>
                            <div class="profile-header">Cài đặt thông tin</div>
                            <div class="d-flex profile-menu flex-column align-items-start">
                                <a class="d-flex gap-2 align-items-center justify-content-center link-underline link-underline-opacity-0 text-body"
                                    href="${pageContext.request.contextPath}/editUser.jsp?userName=${userName}">
                                    <span class="circle"><i class="fa-solid fa-user mx-auto"></i></span>
                                    <span>Thông tin của bạn</span>
                                </a>
                                <a class="d-flex gap-2 align-items-center justify-content-center link-underline link-underline-opacity-0 text-body"
                                    href="${pageContext.request.contextPath}/petProfile.jsp">
                                    <span class="circle"><i class="fa-solid fa-paw mx-auto"></i></span>
                                    <span>Thông tin của thú cưng</span>
                                </a>
                                <a class="d-flex gap-2 align-items-center justify-content-center link-underline link-underline-opacity-0 text-body"
                                    href="${pageContext.request.contextPath}/notificationSetting.jsp">
                                    <span class="circle"><i class="fa-solid fa-bell mx-auto"></i></span>
                                    <span>Cài đặt thông báo</span>
                                </a>
                                <a class="d-flex gap-2 align-items-center justify-content-center link-underline link-underline-opacity-0 text-body"
                                    href="${pageContext.request.contextPath}/changePassword.jsp">
                                    <span class="circle"><i class="fa-solid fa-lock mx-auto"></i></span>
                                    <span>Cài đặt mật khẩu</span>
                                </a>
                            </div>
                        </div>