<%@taglib prefix="c" uri="jakarta.tags.core" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<div class="container-fluid">
    <div style="padding-top: 34px">
        <div class="row">
            <div class="col-3">
                <div class="d-flex gap-1 align-items-center justify-content-center" style="width: fit-content">
                    <a href="${pageContext.request.contextPath}/home">
                        <img src="${pageContext.request.contextPath}/image/logo.svg" alt="logo" style="height: 48px" /> 
                    </a> 
                    <div class="header-brand-name">Pet-Care</div>
                </div> 
            </div>
            <div class="col-6 d-flex align-items-center">
                <div class="w-100 form-container position-relative" style="width: 80%">                            
                    <input class="form-control search-input" placeholder="Nhập thông tin tìm kiếm" />
                    <button class="btn btn-search bg-primary-blue position-absolute"><i class="fa fa-search" aria-hidden="true"></i></button>
                </div>
            </div>
            <div class="col-3 d-flex align-items-center">
                <a class="btn btn-primary-blue btn-member px-3" href="auth.jsp">Bạn là thành viên?</a>
            </div>
              
        </div>
    </div>

    <nav class="navbar navbar-expand-lg">
        <div class="container-fluid gap-4">
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="navbarNav">
                <ul class="navbar-nav w-100 justify-content-center gap-2">
                    <li class="nav-item">
                        <a class="nav-link active" aria-current="page" href="#">Trang chủ</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="#ve-chung-toi">Spa thú cưng</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="#dich-vu">Khách sạn thú cưng</a>
                    </li>
                    <li class="nav-item dropdown">
                        <a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                            Khám bệnh thú cưng
                        </a>
                        <ul class="dropdown-menu">
                            <li><a class="dropdown-item" href="#">Thông tin khám bệnh</a></li>
                            <li><a class="dropdown-item" href="#">Sổ tiêm chủng thú cưng</a></li>
                        </ul>
                    </li>
                </ul>
            </div>  
        </div>
    </nav>
</div>


