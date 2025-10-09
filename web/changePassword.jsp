<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <html>

    <head>
        <title>Đổi mật khẩu</title>
        <jsp:include page="linkgroup.jsp"></jsp:include>
        <link rel="stylesheet" href="css/common.css" />
        <link rel="stylesheet" href="css/profile.css" />
    </head>

    <body>
        <div class="container py-5">
            <div>
                <span class="text-body">
                    <i class="fa-solid fa-chevron-left"></i>
                </span>
                <button class="text-body btn" onclick="history.back()">Quay lại</button>
            </div>
            <div class="row">
                <div class="col-12 col-sm-4">
                    <jsp:include page="setting-common.jsp"></jsp:include>
                </div>
                <div class="col-12 col-sm-8">
                    <form id="changePassword" method="post" action="changePassword" class="mb-5 needs-validation"
                        novalidate>
                        <div class="d-flex align-items-end gap-3 mb-5">
                            <div class="user-avatar">
                                <i class="fa-solid fa-paw mx-auto"></i>
                            </div>
                            <button type="button" class="w-fit h-fit btn-change-avatar">
                                Thay đổi hình ảnh
                            </button>
                        </div>

                        <div class="d-flex flex-column gap-4 mb-5">
                            <div class="form-container has-validation">
                                <label>Mật khẩu hiện tại</label>
                                <input class="form-control" type="text" name="oldPassword" required />
                                <div class="invalid-feedback">Vui lòng nhập mật khẩu hiện tại</div>
                            </div>
                            <div class="form-container">
                                <label>Mật khẩu mới</label>
                                <input class="form-control" type="text" name="newPassword" required />
                                <div class="invalid-feedback">Vui lòng nhập mật khẩu mới</div>
                            </div>

                            <div class="form-container">
                                <label>Xác thực mật khẩu mới</label>
                                <input class="form-control" type="text" name="confirmPassword" required />
                                <div class="invalid-feedback">Vui lòng nhập mật khẩu mới</div>
                            </div>
                            <button class="btn btn-submit text-primary-blue mx-auto d-block">Lưu thông tin</button>
                    </form>
                </div>
            </div>
        </div>

        <div class="toast-container position-fixed top-0 left-0 p-3">
            <div class="toast bg-white" role="alert" aria-live="assertive" aria-atomic="true">
                <div class="toast-body">
                    <div class="d-flex align-items-center justify-content-between">
                        <c:if test="${requestScope.error != null}">
                            <div class="text-danger">${requestScope.error}</div>
                        </c:if>
                        <c:if test="${requestScope.success != null}">
                            <div class="text-success">${requestScope.success}</div>
                        </c:if>
                        <button type="button" class="btn-close" data-bs-dismiss="toast" aria-label="Close"></button>
                    </div>
                </div>
            </div>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/@popperjs/core@2.11.8/dist/umd/popper.min.js"
            integrity="sha384-I7E8VVD/ismYTF4hNIPjVp/Zjvgyol6VFvRkX/vR+Vc4jQkC+hVqc2pM8ODewa9r"
            crossorigin="anonymous"></script>
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.min.js"></script>
        <script>

            const form = document.querySelector("#changePassword");
            const oldPasswordInput = document.getElementsByName("oldPassword")[0];
            const newPasswordInput = document.getElementsByName("newPassword")[0];
            const confirmPasswordInput = document.getElementsByName("confirmPassword")[0];
            const errorOldPassword = oldPasswordInput.nextElementSibling;
            const errorNewPassword = newPasswordInput.nextElementSibling;
            const errorConfirmation = confirmPasswordInput.nextElementSibling;

            function validateOldPassword() {
                const value = oldPasswordInput.value.trim();
                let ok = value.length > 0;

                if (!ok) {
                    oldPasswordInput.classList.toggle('is-invalid', !ok);
                    oldPasswordInput.classList.toggle('is-valid', ok);
                    errorOldPassword.innerHTML = ok ? "" : "Vui lòng nhập mật khẩu hiện tại";
                    return;
                }

                const newPassword = newPasswordInput.value.trim();
                if (newPassword.length === 0)
                    return;

                ok = (newPassword.length > 0 && newPassword !== value);
                newPasswordInput.classList.toggle('is-invalid', !ok);
                newPasswordInput.classList.toggle('is-valid', ok);
                errorNewPassword.innerHTML = 'Mật khẩu trùng với mật khẩu hiện tại';
            }

            function validateNewPassword() {
                const value = newPasswordInput.value.trim();
                const ok = oldPasswordInput.value.trim() !== value && value.length > 0;
                let message = '';
                if (!value.length) {
                    message = "Vui lòng nhập mật khẩu";
                } else {
                    if (oldPasswordInput.value === value) {
                        message = 'Mật khẩu trùng với mật khẩu hiện tại';
                    }
                }
                newPasswordInput.classList.toggle('is-invalid', !ok);
                newPasswordInput.classList.toggle('is-valid', ok);
                errorNewPassword.innerHTML = message;
            }



            function validateConfirm() {
                const ok = newPasswordInput.value === confirmPasswordInput.value && confirmPasswordInput.value.length > 0;
                confirmPasswordInput.classList.toggle('is-invalid', !ok);
                confirmPasswordInput.classList.toggle('is-valid', ok);
                errorConfirmation.innerHTML = ok ? "" : "Mật khẩu nhập lại chưa khớp";
            }

            // Lắng nghe khi người dùng gõ
            oldPasswordInput.addEventListener('input', validateOldPassword);
            newPasswordInput.addEventListener('input', validateNewPassword);
            confirmPasswordInput.addEventListener('input', validateConfirm);

            // Chặn submit nếu có lỗi, và thêm .was-validated để Bootstrap hiển thị feedback
            form.addEventListener('submit', (e) => {
                validateOldPassword();
                validateConfirm();

                if (!form.checkValidity()) {
                    e.preventDefault();
                    e.stopPropagation();
                }
                form.classList.add('was-validated');

            });

            const toastEl = document.querySelector('.toast');
            const toast = new bootstrap.Toast(toastEl);
            const hasError = ${ requestScope.error != null};
            const hasSuccess = ${ requestScope.success != null};
            if (hasError || hasSuccess) {
                toast.show();
            }

        </script>
    </body>

    </html>