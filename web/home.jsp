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
                                    <button class="btn btn-primary-blue">Đặt lịch ngay</button>
                                </div>
                                <div class="banner-image flex-grow">
                                    <img class="object-cover" src="image/banner.png" alt="banner" />
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

                <!--            <section id="team" class="section" >
                            <div class="container">
                                <div class="title text-main">Đội ngũ phát triển</div>
                                <div id="team-info" class="team-container">
                                    <div class="row mb-4">
                                        <div class="col">
                                            <div class="card member d-flex flex-row">
                                                <img src="image/hoangvanbinh.jpg" alt="Hoàng Văn Bình" onclick="openImage(this.src)">
                                                <div class="card-body">
                                                    <div class="card-text info">
                                                        <h3>Hoàng Văn Bình</h3>
                                                        <p><strong>Ngày sinh:</strong> 10/04/1985</p>
                                                        <p><strong>Mã SV:</strong> K23DTCN528</p>
                                                        <p><strong>Lớp:</strong> D23TXCN09-K</p>
                                                        <p><strong>SĐT:</strong> 0383385868</p>
                                                        <p><strong>Email:</strong> nhansu104@gmail.com</p>
                                                        <p><strong>Đơn vị công tác:</strong> <a href="https://caodangcodientaybac.edu.vn/" target="_blank">Trường Cao Đẳng Cơ Điện Tây Bắc</a></p>
                                                        <p><strong>Nhiệm vụ:</strong> Trưởng nhóm</p>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="col">
                                            <div class="card member d-flex flex-row">
                                                <img src="image/tranthuthuy.png" alt="Trần Thu Thủy" onclick="openImage(this.src)">
                                                <div class="card-body">
                                                    <div class="card-text info">
                                                        <h3>Trần Thu Thủy</h3>
                                                        <p><strong>Ngày sinh:</strong> 19/02/1995</p>
                                                        <p><strong>Mã SV:</strong> B23DTCN220</p>
                                                        <p><strong>Lớp:</strong> D23TXCN06-B</p>
                                                        <p><strong>SĐT:</strong> 0987341195</p>
                                                        <p><strong>Email:</strong> thuytt0295@gmail.com</p>
                                                        <p><strong>Đơn vị công tác:</strong> <a href="https://kaopiz.com/en/" target="_blank">Công ty cổ phần Kaopiz software</a></p>
                                                        <p><strong>Nhiệm vụ:</strong> Fullstack (FE + BE)</p>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
            
                                    <div class="row">
                                        <div class="col">
                                            <div class="card member d-flex flex-row">
                                                <img src="image/tranthutrang.jpg" alt="Trần Thu Trang" onclick="openImage(this.src)">
                                                <div class="card-body">
                                                    <div class="card-text info">
                                                        <h3>Trần Thu Trang</h3>
                                                        <p><strong>Ngày sinh:</strong> 20/03/1999</p>
                                                        <p><strong>Mã SV:</strong> K23DTCN552</p>
                                                        <p><strong>Lớp:</strong> D23TXCN09-K</p>
                                                        <p><strong>SĐT:</strong> 0944735211</p>
                                                        <p><strong>Email:</strong> trangthutran203@gmail.com</p>
                                                        <p><strong>Đơn vị công tác:</strong> <a href="https://fptsoftware.com" target="_blank">FPT Software</a></p>
                                                        <p><strong>Nhiệm vụ:</strong> BA + thiết kế DB</p>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="col">
                                            <div class="card member d-flex flex-row">
                                                <img src="image/havanduyet.jpg" alt="Hà Văn Duyệt" onclick="openImage(this.src)">
                                                <div class="card-body">
                                                    <div class="card-text info">
                                                        <h3>Hà Văn Duyệt</h3>
                                                        <p><strong>Ngày sinh:</strong> 11/01/1998</p>
                                                        <p><strong>Mã SV:</strong> K23DTCN532</p>
                                                        <p><strong>Lớp:</strong> D23TXCN09-K</p>
                                                        <p><strong>SĐT:</strong> 0388335981</p>
                                                        <p><strong>Email:</strong> havanduyet1998st@gmail.com</p>
                                                        <p><strong>Đơn vị công tác:</strong> <a href="#" target="_blank"> Làm việc tự do</a></p>
                                                        <p><strong>Nhiệm vụ:</strong> Thiết kế DB + kiểm thử</p>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
            
            
                                     Modal phóng to ảnh 
                                    <div id="imageModal" class="modal" onclick="closeImage()">
                                        <span class="close">&times;</span>
                                        <img class="modal-content" id="modalImg">
                                    </div>
            
                                </div>
                            </div>
                        </section>-->



                <jsp:include page="Footer.jsp"></jsp:include>

                <script src="https://cdn.jsdelivr.net/npm/@popperjs/core@2.11.8/dist/umd/popper.min.js"
                    integrity="sha384-I7E8VVD/ismYTF4hNIPjVp/Zjvgyol6VFvRkX/vR+Vc4jQkC+hVqc2pM8ODewa9r"
                    crossorigin="anonymous"></script>
                <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.min.js"></script>
                <script src="js/script.js"></script>
            </body>

            </html>