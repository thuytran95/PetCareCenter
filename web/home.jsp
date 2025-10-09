<%-- Document : home Created on : Sep 6, 2025, 10:03:17 AM Author : Admin --%>

    <%@page contentType="text/html" pageEncoding="UTF-8" %>
        <%@taglib prefix="c" uri="jakarta.tags.core" %>
            <!DOCTYPE html>
            <html>

            <head>
                <meta charset="UTF-8">
                <title>Pet Care Center - Trung tâm chăm sóc thú cưng</title>
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <jsp:include page="linkgroup.jsp"></jsp:include>
                <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css"
                    rel="stylesheet">
                <link rel="stylesheet" href="css/header.css">
                <link rel="stylesheet" href="css/footer.css" />
                <link rel="stylesheet" href="css/home.css">
            </head>

            <body>
                <jsp:include page="Header.jsp"></jsp:include>
                <section class="banner">
                    <div class="container">
                        <div class="row">
                            <div class="d-flex flex-lg-row flex-column">
                                <div class="w-20">
                                    <div class="heading mb-4">Nơi thú cưng được yêu thương trọn vẹn</div>
                                    <div class="banner-content text-justify">
                                        <p>Trung tâm Chăm sóc thú cưng là nơi gửi gắm yêu thương để thú cưng luôn khỏe
                                            mạnh, hạnh phúc và được nâng niu.</p>
                                        <p>Chúng tôi chăm sóc thú cưng của bạn bằng tình yêu và sự tận tâm, luôn coi các
                                            bé như những người bạn nhỏ thân yêu trong gia đình.</p>
                                    </div>
                                    <a class="btn btn-primary-blue" href="booking.jsp">Đặt lịch ngay</a>
                                </div>
                                <div class="banner-image flex-grow">
                                    <img class="object-cover" src="image/banner.jpg" alt="banner" />
                                </div>
                            </div>
                        </div>
                    </div>
                </section>
                <section class="service">
                    <div class="container">
                        <div class="row">
                            <div class="col-12 col-lg-6">
                                <div class="d-flex flex-column  ">
                                    <div class="heading">
                                        Ngôi nhà thứ hai cho thú cưng của bạn
                                    </div>
                                    <p>Chúng tôi cung cấp các dịch vụ chăm sóc thú cưng toàn diện – từ sức khỏe đến nghỉ
                                        dưỡng</p>
                                    <img class="ms-auto" src="image/cat.png" alt="cat" />
                                </div>
                            </div>
                            <div class="col-12 col-lg-6">
                                <div class="d-flex flex-column gap-4 service-list">
                                    <div class="row">
                                        <div class="col-12 col-sm-6">
                                            <div class="service-item">
                                                <div class="service-item-icon">
                                                    <i class="fa-solid fa-paw"></i>
                                                </div>
                                                <div class="service-item-title mb-1 text-primary-blue fw-bold">
                                                    Spa thú cưng
                                                </div>
                                                <p class="text-justify">Thư giãn, nâng niu từng phút giây cho các bé thú
                                                    cưng</p>
                                                <a class="service-item-link" href="#">Xem thêm</a>
                                            </div>
                                        </div>
                                        <div class="col-12 col-sm-6">
                                            <div class="service-item">
                                                <div class="service-item-icon">
                                                    <i class="fa-solid fa-house"></i>
                                                </div>
                                                <div class="service-item-title mb-1 text-primary-blue fw-bold">
                                                    Khách sạn thú cưng
                                                </div>
                                                <p class="text-justify">Ngôi nhà thứ hai ấm áp, an toàn và đầy yêu
                                                    thương</p>
                                                <a class="service-item-link" href="#">Xem thêm</a>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="row">
                                        <div class="col-12 col-sm-6">
                                            <div class="service-item">
                                                <div class="service-item-icon">
                                                    <i class="fa-solid fa-syringe"></i>
                                                </div>
                                                <div class="service-item-title mb-1 text-primary-blue fw-bold">
                                                    Tiêm vaccine
                                                </div>
                                                <p class="text-justify">Bảo vệ thú cưng từ những phương pháp phòng chống
                                                    bệnh tốt nhất</p>
                                                <a class="service-item-link" href="#">Xem thêm</a>
                                            </div>
                                        </div>
                                        <div class="col-12 col-sm-6">
                                            <div class="service-item">
                                                <div class="service-item-icon">
                                                    <i class="fa-solid fa-suitcase-medical"></i>
                                                </div>
                                                <div class="service-item-title mb-1 text-primary-blue fw-bold">
                                                    Khám bệnh
                                                </div>
                                                <p class="text-justify">Mang lại sức khỏe, bình an cho thú cưng</p>
                                                <a class="service-item-link" href="#">Xem thêm</a>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                            </div>
                        </div>
                    </div>
                </section>

                <section id="team">
                    <div class="container">
                        <div class="heading text-main mb-4">Đội ngũ phát triển</div>
                        <div class="row gy-4">
                            <div class="col-6 col-lg-3">
                                <div class="card member-card text-center p-3">
                                    <div class="d-flex flex-column align-items-center">
                                        <div class="avatar mb-3">
                                            <a href="#" data-bs-toggle="modal" data-bs-target="#myModal">
                                                <img src="image/hoangvanbinh.jpg" alt="Binh" class="rounded-circle"
                                                    style="width: 84px; height: 84px; object-fit: cover;">
                                            </a>
                                        </div>
                                        <h5 class="mb-1">Hoàng Văn Bình</h5>
                                        <div class="small">Trưởng nhóm</div>
                                        <div class="small muted mb-3">
                                            <a class="link-underline link-underline-opacity-0 muted text-truncate d-inline-block"
                                                href="https://caodangcodientaybac.edu.vn/" target="_blank">
                                                Trường Cao Đẳng Cơ Điện Tây Bắc
                                            </a>
                                        </div>
                                        <div class="w-100 text-start small">
                                            <div class="d-flex align-items-center gap-2 icon-line mb-2">
                                                <i class="bi bi-person-badge"></i>
                                                <span>MSV: K23DTCN528</span>
                                            </div>
                                            <div class="d-flex align-items-center gap-2 icon-line mb-2">
                                                <i class="bi bi-calendar-event"></i>
                                                <span>10/04/1985</span>
                                            </div>
                                            <div class="d-flex align-items-center gap-2 icon-line mb-2">
                                                <i class="bi bi-mortarboard"></i>
                                                <span>Lớp: D23TXCN09-K</span>
                                            </div>
                                            <div class="d-flex align-items-center gap-2 icon-line mb-2">
                                                <i class="bi bi-envelope"></i>
                                                <a class="text-decoration-none text-black text-truncate d-inline-block"
                                                    href="mailto:nhansu104@gmail.com">nhansu104@gmail.com</a>
                                            </div>
                                            <div class="d-flex align-items-center gap-2 icon-line mb-2">
                                                <i class="bi bi-telephone"></i>
                                                <span>0383385868</span>
                                            </div>
                                        </div>
                                    </div>

                                </div>
                            </div>
                            <div class="col-6 col-lg-3">
                                <div class="card member-card text-center p-3">
                                    <div class="d-flex flex-column align-items-center">
                                        <div class="avatar mb-3">
                                            <a href="#" data-bs-toggle="modal" data-bs-target="#myModal">
                                                <img src="image/tranthuthuy.png" alt="Trần Thu Thuỷ"
                                                    class="rounded-circle"
                                                    style="width: 84px; height: 84px; object-fit: cover;">
                                            </a>

                                        </div>
                                        <h5 class="mb-1">Trần Thu Thủy</h5>
                                        <div class="small">Frontend</div>
                                        <div class="small muted mb-3">
                                            <a class="link-underline link-underline-opacity-0 muted text-truncate d-inline-block"
                                                href="https://kaopiz.com/en/" target="_blank">
                                                Công ty cổ phần Kaopiz software
                                            </a>
                                        </div>
                                        <div class="w-100 text-start small">
                                            <div class="d-flex align-items-center gap-2 icon-line mb-2">
                                                <i class="bi bi-person-badge"></i>
                                                <span>MSV: B23DTCN220</span>
                                            </div>
                                            <div class="d-flex align-items-center gap-2 icon-line mb-2">
                                                <i class="bi bi-calendar-event"></i>
                                                <span>19/02/1995</span>
                                            </div>
                                            <div class="d-flex align-items-center gap-2 icon-line mb-2">
                                                <i class="bi bi-mortarboard"></i>
                                                <span>Lớp: D23TXCN06-B</span>
                                            </div>
                                            <div class="d-flex align-items-center gap-2 icon-line mb-2">
                                                <i class="bi bi-envelope"></i>
                                                <a class="text-decoration-none text-black text-truncate d-inline-block"
                                                    href="mailto:thuytt0295@gmail.com">thuytt0295@gmail.com</a>
                                            </div>
                                            <div class="d-flex align-items-center gap-2 icon-line mb-2">
                                                <i class="bi bi-telephone"></i>
                                                <span>0987341195</span>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="col-6 col-lg-3">
                                <div class="card member-card text-center p-3">
                                    <div class="d-flex flex-column align-items-center">
                                        <div class="avatar mb-3">
                                            <a href="#" data-bs-toggle="modal" data-bs-target="#myModal">
                                                <img src="image/tranthutrang.jpg" alt="Trần Thu Trang"
                                                    class="rounded-circle"
                                                    style="width: 84px; height: 84px; object-fit: cover;">
                                            </a>

                                        </div>
                                        <h5 class="mb-1">Trần Thu Trang</h5>
                                        <div class="small">BA</div>
                                        <div class="small muted mb-3">
                                            <a class="link-underline link-underline-opacity-0 muted text-truncate d-inline-block"
                                                href="https://fptsoftware.com" target="_blank">
                                                FPT Software
                                            </a>
                                        </div>
                                        <div class="w-100 text-start small">
                                            <div class="d-flex align-items-center gap-2 icon-line mb-2">
                                                <i class="bi bi-person-badge"></i>
                                                <span>MSV: K23DTCN552</span>
                                            </div>
                                            <div class="d-flex align-items-center gap-2 icon-line mb-2">
                                                <i class="bi bi-calendar-event"></i>
                                                <span> 20/03/1999</span>
                                            </div>
                                            <div class="d-flex align-items-center gap-2 icon-line mb-2">
                                                <i class="bi bi-mortarboard"></i>
                                                <span>Lớp: D23TXCN09-K</span>
                                            </div>
                                            <div class="d-flex align-items-center gap-2 icon-line mb-2">
                                                <i class="bi bi-envelope"></i>
                                                <a class="text-decoration-none text-black text-truncate d-inline-block"
                                                    href="mailto:trangthutran203@gmail.com">trangthutran203@gmail.com</a>
                                            </div>
                                            <div class="d-flex align-items-center gap-2 icon-line mb-2">
                                                <i class="bi bi-telephone"></i>
                                                <span>0944735211</span>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="col-6 col-lg-3">
                                <div class="card member-card text-center p-3">
                                    <div class="d-flex flex-column align-items-center">
                                        <div class="avatar mb-3">
                                            <a href="#" data-bs-toggle="modal" data-bs-target="#myModal">
                                                <img src="image/havanduyet.jpg" alt="Hà Văn Duyệt"
                                                    class="rounded-circle"
                                                    style="width: 84px; height: 84px; object-fit: cover;">
                                            </a>
                                        </div>
                                        <h5 class="mb-1">Hà Văn Duyệt</h5>
                                        <div class="small">Backend</div>
                                        <div class="small muted mb-3">
                                            <a class="link-underline link-underline-opacity-0 muted text-truncate d-inline-block"
                                                href="https://gcool.com.vn/" target="_blank">
                                                Gcool
                                            </a>
                                        </div>
                                        <div class="w-100 text-start small">
                                            <div class="d-flex align-items-center gap-2 icon-line mb-2">
                                                <i class="bi bi-person-badge"></i>
                                                <span>MSV: K23DTCN532</span>
                                            </div>
                                            <div class="d-flex align-items-center gap-2 icon-line mb-2">
                                                <i class="bi bi-calendar-event"></i>
                                                <span>11/01/1998</span>
                                            </div>
                                            <div class="d-flex align-items-center gap-2 icon-line mb-2">
                                                <i class="bi bi-mortarboard"></i>
                                                <span>Lớp: D23TXCN09-K</span>
                                            </div>
                                            <div class="d-flex align-items-center gap-2 icon-line mb-2">
                                                <i class="bi bi-envelope"></i>
                                                <a class="text-decoration-none text-black text-truncate d-inline-block"
                                                    href="mailto:havanduyet1998st@gmail.com">havanduyet1998st@gmail.com</a>
                                            </div>
                                            <div class="d-flex align-items-center gap-2 icon-line mb-2">
                                                <i class="bi bi-telephone"></i>
                                                <span>0388335981</span>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="modal fade" id="myModal" tabindex="-1" aria-hidden="true">
                        <div class="modal-dialog modal-fullscreen">
                            <div class="modal-content">
                                <div class="modal-header">
                                    <h1 class="modal-title fs-5" id="staticBackdropLabel">
                                        Hoàng Văn Bình
                                    </h1>
                                    <button type="button" class="btn-close" data-bs-dismiss="modal"
                                        aria-label="Close"></button>
                                </div>
                                <div class="modal-body">
                                    <img src="image/hoangvanbinh.jpg" alt="Binh"
                                        style="width: 100%; height: 100%; object-fit: contain;">
                                </div>

                            </div>
                        </div>
                    </div>
                </section>

                <jsp:include page="Footer.jsp"></jsp:include>
                <script src="https://cdn.jsdelivr.net/npm/@popperjs/core@2.11.8/dist/umd/popper.min.js"
                    integrity="sha384-I7E8VVD/ismYTF4hNIPjVp/Zjvgyol6VFvRkX/vR+Vc4jQkC+hVqc2pM8ODewa9r"
                    crossorigin="anonymous"></script>
                <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.min.js"></script>
                <script src="js/script.js"></script>
            </body>

            </html>