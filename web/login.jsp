<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String errorMessage = (String) request.getAttribute("errorMessage");
    String successMessage = (String) request.getAttribute("successMessage");
    if (errorMessage == null) {
        errorMessage = (String) session.getAttribute("errorMessage");
        if (errorMessage != null) {
            session.removeAttribute("errorMessage");
        }
    }
    String redirectParam = request.getParameter("redirect");
    if (errorMessage == null && redirectParam != null && !redirectParam.trim().isEmpty()) {
        errorMessage = "⚠️ Bạn chưa đăng nhập! Vui lòng đăng nhập tài khoản trước khi mua hàng hoặc thêm vào giỏ hàng.";
    }
%>

<jsp:include page="includes/header.jsp" />
<jsp:include page="includes/navbar.jsp" />

<div class="container my-5">
    <div class="row justify-content-center">
        <div class="col-md-5">
            <div class="bg-white p-4 p-md-5 rounded-4 border shadow-lg">
                <h3 class="fw-bold text-center mb-1">Đăng Nhập Tài Khoản</h3>
                <p class="text-muted text-center small mb-4">Chào mừng bạn quay trở lại với HighFashion</p>

                <% if (errorMessage != null) { %>
                    <div class="alert alert-danger rounded-3 py-2 small" role="alert">
                        <%= errorMessage %>
                    </div>
                <% } %>
                <% if (successMessage != null) { %>
                    <div class="alert alert-success rounded-3 py-2 small" role="alert">
                        <%= successMessage %>
                    </div>
                <% } %>

                <form action="${pageContext.request.contextPath}/login" method="POST" id="loginForm" novalidate>
                    <div class="mb-3">
                        <label class="form-label fw-bold">Tên đăng nhập <span class="text-danger fw-bold">*</span></label>
                        <div class="input-group">
                            <span class="input-group-text bg-light border-end-0"><i class="bi bi-person text-muted"></i></span>
                            <input type="text" name="username" class="form-control border-start-0 bg-light required-input" placeholder="Nhập tên đăng nhập" required>
                        </div>
                        <div class="invalid-feedback"><i class="bi bi-exclamation-circle me-1"></i>Đây là trường bắt buộc</div>
                    </div>

                    <div class="mb-4">
                        <label class="form-label fw-bold">Mật khẩu <span class="text-danger fw-bold">*</span></label>
                        <div class="input-group">
                            <span class="input-group-text bg-light border-end-0"><i class="bi bi-lock text-muted"></i></span>
                            <input type="password" name="password" class="form-control border-start-0 bg-light required-input" placeholder="Nhập mật khẩu" required>
                        </div>
                        <div class="invalid-feedback"><i class="bi bi-exclamation-circle me-1"></i>Đây là trường bắt buộc</div>
                    </div>

                    <button type="submit" class="btn btn-accent w-100 rounded-pill py-2.5 fw-bold mb-3">Đăng Nhập</button>
                    
                    <div class="text-center text-muted small">
                        Chưa có tài khoản? <a href="${pageContext.request.contextPath}/register" class="text-danger fw-bold text-decoration-none">Đăng ký ngay</a>
                    </div>
                </form>

                <script>
                document.addEventListener("DOMContentLoaded", function() {
                    const form = document.getElementById("loginForm");
                    if (!form) return;
                    const inputs = form.querySelectorAll(".required-input");
                    function validate(input) {
                        const val = input.value.trim();
                        const feedback = input.closest(".mb-3, .mb-4").querySelector(".invalid-feedback");
                        if (!val) {
                            input.classList.add("is-invalid");
                            if (feedback) feedback.style.display = "block";
                            return false;
                        } else {
                            input.classList.remove("is-invalid");
                            if (feedback) feedback.style.display = "none";
                            return true;
                        }
                    }
                    inputs.forEach(input => {
                        input.addEventListener("blur", function() { validate(this); });
                        input.addEventListener("input", function() { if (this.classList.contains("is-invalid")) validate(this); });
                    });
                    form.addEventListener("submit", function(e) {
                        let valid = true;
                        inputs.forEach(input => { if (!validate(input)) valid = false; });
                        if (!valid) e.preventDefault();
                    });
                });
                </script>
            </div>
        </div>
    </div>
</div>

<jsp:include page="includes/footer.jsp" />
