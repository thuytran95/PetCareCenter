<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <!DOCTYPE html>
    <html>

    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Pet profile</title>
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
                    <form method="post" action="editUser" enctype="multipart/form-data" class="mb-5">
                        <div class="d-flex align-items-end gap-3 mb-5">
                            <div class="user-avatar">
                                <i class="fa-solid fa-paw mx-auto"></i>
                            </div>
                            <button type="button" class="w-fit h-fit btn-change-avatar">
                                Thay đổi hình ảnh
                            </button>
                        </div>

                        <div class="d-flex flex-column gap-4 mb-5">
                            <div class="form-container">
                                <label>Tên thú cưng</label>
                                <input class="form-control" type="text" name="fullName" />
                            </div>
                            <div class="form-container">
                                <label>Giới tính</label>
                                <select class="form-select" name="gender">
                                    <option value="F">Cái</option>
                                    <option value="M">Đực</option>
                                </select>
                            </div>
                            <div class="form-container">
                                <label>Ngày sinh</label>
                                <input class="form-control" type="date" name="phone" />
                            </div>
                            <div class="form-container">
                                <label>Loài</label>
                                <input class="form-control" type="text" />
                            </div>
                            <div class="form-container">
                                <label>Giống</label>
                                <input class="form-control" type="text" />
                            </div>
                            <div class="form-container">
                                <label>Màu lông</label>
                                <input class="form-control" type="text" />
                            </div>
                            <div class="form-container">
                                <label>Đặt điểm nhận dạng</label>
                                <input class="form-control" type="text" />
                            </div>
                            <div class="form-container">
                                <label>Đặt điểm nhận dạng</label>
                                <input class="form-control" type="text" />
                            </div>
                            <div class="form-container">
                                <label>Ngày gia nhập thành viên</label>
                                <input class="form-control" type="tel" name="text" value="20/09/2025" />
                            </div>
                        </div>
                        <button class="btn btn-submit text-primary-blue mx-auto d-block">Cập nhật</button>
                    </form>
                </div>
            </div>
        </div>
    </body>

    </html>