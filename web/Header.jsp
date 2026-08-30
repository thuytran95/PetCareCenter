<%@page import="com.petweb.model.UserAccount" %>
    <%@page import="com.petweb.utils.DBUtils" %>
        <%@page import="com.petweb.utils.ConnectionUtils" %>
            <%@ page import="java.sql.Connection" %>
                <%@taglib prefix="c" uri="jakarta.tags.core" %>
                    <%@page contentType="text/html" pageEncoding="UTF-8" %>


                        <div class="container-fluid">
                            <div style="padding-top: 34px">
                                <div class="row">
                                    <div class="col-3">
                                        <div class="d-flex gap-1 align-items-center justify-content-center"
                                            style="width: fit-content">
                                            <a href="${pageContext.request.contextPath}/home" class="text-primary-blue" style="font-size: 30px;">
                                                <i class="fa-solid fa-paw"></i>
                                            </a>
                                            <div class="header-brand-name">Pet-Care</div>
                                        </div>
                                    </div>
                                    <div class="col-6 d-flex align-items-center">
                                        <form class="w-100 form-container position-relative" style="width: 80%"
                                            action="${pageContext.request.contextPath}/search" method="get" role="search">
                                            <input class="form-control search-input" name="q"
                                                value="${param.q}"
                                                placeholder="Tìm dịch vụ, thú cưng của bạn..." />
                                            <button class="btn btn-search bg-primary-blue position-absolute"
                                                type="submit" aria-label="Tìm kiếm"><i
                                                    class="fa fa-search" aria-hidden="true"></i></button>
                                        </form>
                                    </div>
                                    <div class="col-3 d-flex align-items-center justify-content-center gap-0">
                                        <c:set var="user" value="${sessionScope.loginedUser}"></c:set>
                                        <c:if test="${user == null}">
                                            <a class="btn btn-primary-blue btn-member px-3"
                                                href="${pageContext.request.contextPath}/auth.jsp">Bạn là thành viên?</a>
                                        </c:if>
                                        <c:if test="${user!= null}">
                                            <div class="d-flex align-items-stretch gap-2 header-account">
                                                <span
                                                    class="circle circle-primary-blue d-flex justify-content-center align-items-center"
                                                    title="${user.fullName}">
                                                    <i class="fa-regular fa-user"></i>
                                                </span>
                                                <span>${user.userName}</span>
                                                <a class="link-underline link-underline-opacity-0 text-primary-blue"
                                                    href="editUser.jsp?userName=${user.userName}">
                                                    <i class="fa-solid fa-pen"></i>
                                                </a>
                                            </div>
                                            <div class="header-icon">
                                                <a class="circle border border-0 text-white p-0 bg-primary-blue d-flex justify-content-center align-items-center text-decoration-none"
                                                    href="${pageContext.request.contextPath}/notifications" title="Thông báo của tôi">
                                                    <i class="fa-regular fa-bell"></i>
                                                </a>
                                            </div>
                                            <div class="header-icon">
                                                <form action="${pageContext.request.contextPath}/logout" method="post"
                                                    style="display:inline;">
                                                    <button type="submit"
                                                        class="circle border border-0 text-white p-0 bg-primary-blue d-flex justify-content-center align-items-center">
                                                        <i class="fa-solid fa-power-off"></i>
                                                    </button>
                                                </form>

                                            </div>

                                        </c:if>
                                    </div>

                                </div>
                            </div>

                            <nav class="navbar navbar-expand-lg">
                                <div class="container-fluid gap-4">
                                    <button class="navbar-toggler" type="button" data-bs-toggle="collapse"
                                        data-bs-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false"
                                        aria-label="Toggle navigation">
                                        <span class="navbar-toggler-icon"></span>
                                    </button>
                                    <div class="collapse navbar-collapse" id="navbarNav">
                                        <ul class="navbar-nav w-100 justify-content-center gap-2">
                                            <li class="nav-item">
                                                <a class="nav-link active" aria-current="page" href="${pageContext.request.contextPath}/home">Trang
                                                    chủ</a>
                                            </li>
                                            <c:if test="${user != null}">
                                                <li class="nav-item">
                                                    <a class="nav-link" href="${pageContext.request.contextPath}/petProfile">
                                                        <i class="fa-solid fa-paw me-1"></i>Thú cưng của tôi</a>
                                                </li>
                                                <li class="nav-item">
                                                    <a class="nav-link" href="${pageContext.request.contextPath}/myBookings">
                                                        <i class="fa-regular fa-rectangle-list me-1"></i>Đơn của tôi</a>
                                                </li>
                                            </c:if>
                                            <li class="nav-item">
                                                <a class="nav-link" href="${pageContext.request.contextPath}/service?type=spa">Spa thú cưng</a>
                                            </li>
                                            <li class="nav-item">
                                                <a class="nav-link" href="${pageContext.request.contextPath}/service?type=hotel">Khách sạn thú cưng</a>
                                            </li>
                                            <li class="nav-item dropdown">
                                                <a class="nav-link dropdown-toggle" href="#" role="button"
                                                    data-bs-toggle="dropdown" aria-expanded="false">
                                                    Khám bệnh thú cưng
                                                </a>
                                                <ul class="dropdown-menu">
                                                    <li><a class="dropdown-item" href="${pageContext.request.contextPath}/service?type=medical">Thông tin khám bệnh</a>
                                                    </li>
                                                    <li><a class="dropdown-item" href="${pageContext.request.contextPath}/service?type=vaccine">Tiêm vaccine</a>
                                                    </li>
                                                </ul>
                                            </li>
                                        </ul>
                                    </div>
                                </div>
                            </nav>
                        </div>

                        <script>
                        </script>