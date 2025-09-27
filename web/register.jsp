<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Đăng ký</title>
        <jsp:include page="linkgroup.jsp"></jsp:include>
            <link rel="stylesheet" href="css/common.css"/>
            <link rel="stylesheet" href="css/form.css"/>
            <link rel="stylesheet" href="css/register.css"/>
        </head>
        <body>
        <c:set var="user" value="${requestScope.user}"/>
        <div class="container-fluid">
            <div class="toast-container position-fixed top-0 left-0 p-3">
                <div id="liveToast" class="toast" role="alert" aria-live="assertive" aria-atomic="true">
                    <div class="toast-header d-flex align-items-center justify-content-end">
                        <button type="button" class="btn-close" data-bs-dismiss="toast" aria-label="Close"></button>
                    </div>
                    <div class="toast-body">
                        <c:if test="${requestScope.error != null}">
                            <p class="text-danger">${requestScope.error}</p>
                        </c:if>
                        <c:if test="${requestScope.message != null}">
                            <p class="text-danger">${requestScope.message}</p>
                        </c:if>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-12 col-sm-6">
                    <div class="register-container-form">
                        <div class="logo d-flex justify-content-center align-items-center gap-2">
                            <span><i class="fa-solid fa-paw"></i></span>
                            <span>Pet-Care</span>
                        </div> 
                        <form autocomplete="off" action="<%=request.getContextPath()%>/register" method="post" enctype="multipart/form-data" style="margin-bottom: 48px">
                            <div class="form-container form-container-icon d-flex align-items-center mb-4">
                                <span class="icon"><i class="fa-solid fa-id-badge"></i></span>
                                <input class="form-control ps-6" placeholder="Họ và tên" name="fullName" type="text" value="${user != null ? user.fullName: ''}" />
                            </div>  
                            <div class="form-container form-container-icon d-flex align-items-center mb-4">
                                <span class="icon"><i class="fa-solid fa-user"></i></span>
                                <input class="form-control ps-6" placeholder="Nhập tên đăng nhập" name="userName" type="text" required value="${user != null ? user.userName : ''}"/>
                            </div>  
                            <div class="form-container form-container-icon d-flex align-items-center mb-4">
                                <span class="icon"><i class="fa-solid fa-envelope"></i> </span>
                                <input class="form-control ps-6" placeholder="Email: username@gmail.com" name="email" type="email" required value="${user != null ? user.email : ''}"/>
                            </div>  
                            <div class="form-container form-container-icon d-flex align-items-center mb-4">
                                <span class="icon"><i class="fa-solid fa-phone"></i></span>
                                <input class="form-control ps-6" placeholder="Phone Number: 0xx or +84xx" name="phone" type="tel" value="${user != null ? user.phone : ''}"/>
                            </div>  
                            <div class="form-container form-container-icon d-flex align-items-center" style="margin-bottom: 48px">
                                <span class="icon"><i class="fa-solid fa-lock"></i></span>  
                                <input class="form-control ps-6" placeholder="Mật khẩu: tối đa 8 ký tự" name="password" type="password" required value="${user != null ? user.password : ''}" />
                            </div>
                            <button class="btn btn-primary-blue w-100" type="submit">Đăng ký</button>
                        </form>
                        <div>
                            Bạn đã là thành viên của trung tâm?
                            <a href="<%=request.getContextPath()%>/login" class="text-primary-blue fw-bold link-underline link-underline-opacity-0" >Đăng nhập</a>
                        </div>
                    </div>
                </div>
                <div class="col-12 col-sm-6">
                    <div class="overflow-hidden" style="margin: 0 calc(var(--bs-gutter-x) * -0.5)" >
                        <img class="w-100 h-100" src="image/bg-register.jpg" alt="alt"/>
                    </div>
                </div>
            </div>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/@popperjs/core@2.11.8/dist/umd/popper.min.js" integrity="sha384-I7E8VVD/ismYTF4hNIPjVp/Zjvgyol6VFvRkX/vR+Vc4jQkC+hVqc2pM8ODewa9r" crossorigin="anonymous"></script>
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.min.js"></script>
        <script>
            const toastEl = document.querySelector('.toast');
            const toast = new bootstrap.Toast(toastEl);

            if (requestScope.error || requestScope.message) {
                toast.show();
            }

        </script>
    </body>
</html>
