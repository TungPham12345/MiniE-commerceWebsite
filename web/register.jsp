<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String errorMessage = (String) request.getAttribute("errorMessage");
%>

<jsp:include page="includes/header.jsp" />
<jsp:include page="includes/navbar.jsp" />

<div class="container my-5">
    <div class="row justify-content-center">
        <div class="col-md-6">
            <div class="bg-white p-4 p-md-5 rounded-4 border shadow-lg">
                <h3 class="fw-bold text-center mb-1">Đăng Ký Tài Khoản</h3>
                <p class="text-muted text-center small mb-4">Tạo tài khoản mua sắm thời trang tiện lợi</p>

                <% if (errorMessage != null) { %>
                    <div class="alert alert-danger rounded-3 py-2 small" role="alert">
                        <%= errorMessage %>
                    </div>
                <% } %>

                <form action="${pageContext.request.contextPath}/register" method="POST" id="registerForm" novalidate>
                    <div class="row g-3">
                        <div class="col-md-6">
                            <label class="form-label fw-bold">Tên đăng nhập <span class="text-danger fw-bold">*</span></label>
                            <input type="text" name="username" class="form-control bg-light required-input" placeholder="Username" required>
                            <div class="invalid-feedback"><i class="bi bi-exclamation-circle me-1"></i>Đây là trường bắt buộc</div>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label fw-bold">Họ và tên <span class="text-danger fw-bold">*</span></label>
                            <input type="text" name="fullname" class="form-control bg-light required-input" placeholder="Nguyễn Văn A" required>
                            <div class="invalid-feedback"><i class="bi bi-exclamation-circle me-1"></i>Đây là trường bắt buộc</div>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label fw-bold">Mật khẩu <span class="text-danger fw-bold">*</span></label>
                            <input type="password" name="password" id="password" class="form-control bg-light required-input" placeholder="Nhập mật khẩu" required>
                            <div class="invalid-feedback"><i class="bi bi-exclamation-circle me-1"></i>Đây là trường bắt buộc</div>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label fw-bold">Xác nhận mật khẩu <span class="text-danger fw-bold">*</span></label>
                            <input type="password" name="confirmPassword" id="confirmPassword" class="form-control bg-light required-input" placeholder="Nhập lại mật khẩu" required>
                            <div class="invalid-feedback"><i class="bi bi-exclamation-circle me-1"></i>Đây là trường bắt buộc</div>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label fw-bold">Email <span class="text-danger fw-bold">*</span></label>
                            <input type="email" name="email" class="form-control bg-light required-input" placeholder="email@gmail.com" required>
                            <div class="invalid-feedback"><i class="bi bi-exclamation-circle me-1"></i>Đây là trường bắt buộc</div>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label fw-bold">Số điện thoại <span class="text-danger fw-bold">*</span></label>
                            <input type="tel" name="phone" class="form-control bg-light required-input" placeholder="0912345678" required>
                            <div class="invalid-feedback"><i class="bi bi-exclamation-circle me-1"></i>Đây là trường bắt buộc</div>
                        </div>
                        <div class="col-12">
                            <label class="form-label fw-bold">Địa chỉ nhận hàng <span class="text-danger fw-bold">*</span></label>
                            <input type="text" name="address" class="form-control bg-light required-input" placeholder="Số nhà, Tên đường, Quận/Huyện, TP" required>
                            <div class="invalid-feedback"><i class="bi bi-exclamation-circle me-1"></i>Đây là trường bắt buộc</div>
                        </div>
                    </div>

                    <button type="submit" class="btn btn-accent w-100 rounded-pill py-2.5 fw-bold mt-4 mb-3">Tạo Tài Khoản</button>
                    
                    <div class="text-center text-muted small">
                        Đã có tài khoản? <a href="${pageContext.request.contextPath}/login" class="text-danger fw-bold text-decoration-none">Đăng nhập ngay</a>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<script>
document.addEventListener("DOMContentLoaded", function() {
    const form = document.getElementById("registerForm");
    if (!form) return;

    const requiredInputs = form.querySelectorAll(".required-input");

    function validateField(input) {
        const val = input.value.trim();
        const parent = input.parentElement;
        const feedback = parent.querySelector(".invalid-feedback");

        // Kiểm tra trống khi click chuột ra ngoài
        if (!val) {
            input.classList.add("is-invalid");
            if (feedback) {
                feedback.innerHTML = '<i class="bi bi-exclamation-circle me-1"></i>Đây là trường bắt buộc';
                feedback.style.display = "block";
            }
            return false;
        }

        // Kiểm tra khớp mật khẩu
        if (input.name === "confirmPassword") {
            const pwdInput = form.querySelector('input[name="password"]');
            if (pwdInput && val !== pwdInput.value.trim()) {
                input.classList.add("is-invalid");
                if (feedback) {
                    feedback.innerHTML = '<i class="bi bi-exclamation-circle me-1"></i>Mật khẩu xác nhận không khớp';
                    feedback.style.display = "block";
                }
                return false;
            }
        }

        // Nếu sửa mật khẩu chính thì kiểm tra lại ô xác nhận nếu đã nhập
        if (input.name === "password") {
            const confirmInput = form.querySelector('input[name="confirmPassword"]');
            if (confirmInput && confirmInput.value.trim()) {
                validateField(confirmInput);
            }
        }

        // Kiểm tra định dạng Email
        if (input.type === "email" && val) {
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!emailRegex.test(val)) {
                input.classList.add("is-invalid");
                if (feedback) {
                    feedback.innerHTML = '<i class="bi bi-exclamation-circle me-1"></i>Email không đúng định dạng (ví dụ: email@gmail.com)';
                    feedback.style.display = "block";
                }
                return false;
            }
        }

        // Kiểm tra định dạng số điện thoại (10 chữ số)
        if (input.name === "phone" && val) {
            const phoneRegex = /^0\d{9}$/;
            if (!phoneRegex.test(val)) {
                input.classList.add("is-invalid");
                if (feedback) {
                    feedback.innerHTML = '<i class="bi bi-exclamation-circle me-1"></i>Số điện thoại phải gồm 10 chữ số và bắt đầu bằng số 0';
                    feedback.style.display = "block";
                }
                return false;
            }
        }

        // Hợp lệ -> Xóa lỗi
        input.classList.remove("is-invalid");
        if (feedback) {
            feedback.style.display = "none";
        }
        return true;
    }

    requiredInputs.forEach(input => {
        // Sự kiện khi click chuột ra ngoài (blur)
        input.addEventListener("blur", function() {
            validateField(this);
        });

        // Sự kiện khi đang gõ phím (input) -> tự động xóa thông báo lỗi nếu đã nhập hợp lệ
        input.addEventListener("input", function() {
            if (this.classList.contains("is-invalid")) {
                validateField(this);
            }
        });
    });

    // Sự kiện khi bấm submit "Tạo Tài Khoản"
    form.addEventListener("submit", function(e) {
        let isFormValid = true;
        let firstInvalidField = null;

        requiredInputs.forEach(input => {
            const ok = validateField(input);
            if (!ok) {
                isFormValid = false;
                if (!firstInvalidField) {
                    firstInvalidField = input;
                }
            }
        });

        if (!isFormValid) {
            e.preventDefault();
            if (firstInvalidField) {
                firstInvalidField.focus();
            }
        }
    });
});
</script>

<jsp:include page="includes/footer.jsp" />
