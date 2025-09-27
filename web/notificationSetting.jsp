<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <!DOCTYPE html>
    <html>

    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Cài đặt thông báo</title>
        <jsp:include page="linkgroup.jsp"></jsp:include>
        <link rel="stylesheet" href="css/common.css" />
        <link rel="stylesheet" href="css/profile.css" />
    </head>

    <body>
        <div class="container pb-5">
            <div>
                <span class="text-body">
                    <i class="fa-solid fa-chevron-left"></i>
                </span>
                <a class="link-underline link-underline-opacity-0 text-body" href="#">Quay lại</a>
            </div>
            <div class="row">
                <div class="col-12 col-sm-4">
                    <jsp:include page="setting-common.jsp"></jsp:include>
                </div>
                <div class="col-12 col-sm-8">
                    <div class="d-flex flex-column gap-4">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <div class="profile-sub-title">Thông báo đặt lịch dịch vụ</div>
                                <div>Các thông tin đặt lịch sử dụng dịch vụ tại trung tâm</div>
                            </div>
                            <div class="form-check form-switch">
                                <input class="form-check-input" type="checkbox" checked switch />
                            </div>
                        </div>
                        <hr />
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <div class="profile-sub-title">Thông báo cập nhật sổ tiêm</div>
                                <div>Các thông tin sổ tiêm định kỳ cho thú cưng</div>
                            </div>
                            <div class="form-check form-switch">
                                <input class="form-check-input" type="checkbox" checked switch />
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </body>

    </html>