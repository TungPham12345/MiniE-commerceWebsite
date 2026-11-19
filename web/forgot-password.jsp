<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%!
    private String escapeHtml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                    .replace("\"", "&quot;").replace("'", "&#39;");
    }
%>
<%
    String step = (String) request.getAttribute("forgotStep");
    if (step == null) step = "email";
    String email = (String) request.getAttribute("forgotEmail");
    String maskedEmail = (String) request.getAttribute("maskedEmail");
    String errorMessage = (String) request.getAttribute("errorMessage");
    String successMessage = (String) request.getAttribute("successMessage");
%>

<jsp:include page="includes/header.jsp" />
<jsp:include page="includes/navbar.jsp" />

<main class="container my-5 forgot-password-page">
    <div class="row justify-content-center">
        <div class="col-md-6 col-lg-5">
            <section class="bg-white p-4 p-md-5 rounded-4 border shadow-lg">
                <div class="text-center mb-4">
                    <div class="account-section-icon bg-primary-subtle text-primary mx-auto mb-3">
                        <i class="bi bi-shield-lock"></i>
                    </div>
                    <h3 class="fw-bold mb-1">Quên mật khẩu</h3>
                    <p class="text-muted small mb-0">Xác nhận email để tạo mật khẩu mới.</p>
                </div>

                <% if (errorMessage != null) { %>
                    <div class="alert alert-danger rounded-3 py-2 small" role="alert"><%= escapeHtml(errorMessage) %></div>
                <% } %>
                <% if (successMessage != null) { %>
                    <div class="alert alert-success rounded-3 py-2 small" role="status"><%= escapeHtml(successMessage) %></div>
                <% } %>

                <% if ("verify".equals(step)) { %>
                    <p class="text-secondary small mb-3">Nhập mã 6 chữ số đã gửi đến <strong><%= escapeHtml(maskedEmail) %></strong>.</p>
                    <form action="${pageContext.request.contextPath}/forgot-password" method="post">
                        <input type="hidden" name="action" value="verifyCode">
                        <div class="mb-3">
                            <label class="form-label fw-semibold" for="resetCode">Mã xác nhận</label>
                            <input class="form-control text-center fw-bold fs-5" id="resetCode" name="code"
                                   inputmode="numeric" autocomplete="one-time-code" pattern="[0-9]{6}"
                                   maxlength="6" required autofocus>
                        </div>
                        <button class="btn btn-accent w-100 rounded-pill py-2 fw-bold" type="submit">Xác nhận mã</button>
                    </form>
                    <form action="${pageContext.request.contextPath}/forgot-password" method="post" class="mt-3">
                        <input type="hidden" name="action" value="sendCode">
                        <button class="btn btn-link w-100 text-decoration-none" type="submit">Gửi lại mã</button>
                    </form>
                <% } else if ("reset".equals(step)) { %>
                    <p class="text-secondary small mb-3">Đặt mật khẩu mới cho tài khoản <strong><%= escapeHtml(maskedEmail) %></strong>.</p>
                    <form action="${pageContext.request.contextPath}/forgot-password" method="post">
                        <input type="hidden" name="action" value="resetPassword">
                        <div class="mb-3">
                            <label class="form-label fw-semibold" for="resetNewPassword">Mật khẩu mới</label>
                            <div class="input-group">
                                <input class="form-control" id="resetNewPassword" name="newPassword" type="password"
                                       minlength="6" maxlength="255" autocomplete="new-password" required>
                                <button class="btn btn-outline-secondary" type="button" data-password-toggle="resetNewPassword"
                                        aria-label="Hiện mật khẩu" title="Hiện mật khẩu"><i class="bi bi-eye" aria-hidden="true"></i></button>
                            </div>
                        </div>
                        <div class="mb-4">
                            <label class="form-label fw-semibold" for="resetConfirmPassword">Xác nhận mật khẩu mới</label>
                            <div class="input-group">
                                <input class="form-control" id="resetConfirmPassword" name="confirmPassword" type="password"
                                       minlength="6" maxlength="255" autocomplete="new-password" required>
                                <button class="btn btn-outline-secondary" type="button" data-password-toggle="resetConfirmPassword"
                                        aria-label="Hiện mật khẩu" title="Hiện mật khẩu"><i class="bi bi-eye" aria-hidden="true"></i></button>
                            </div>
                            <div class="form-text">Mật khẩu cần có ít nhất 6 ký tự.</div>
                        </div>
                        <button class="btn btn-accent w-100 rounded-pill py-2 fw-bold" type="submit">Lưu mật khẩu mới</button>
                    </form>
                <% } else { %>
                    <form action="${pageContext.request.contextPath}/forgot-password" method="post">
                        <input type="hidden" name="action" value="sendCode">
                        <div class="mb-4">
                            <label class="form-label fw-semibold" for="resetEmail">Email đã đăng ký</label>
                            <input class="form-control" id="resetEmail" name="email" type="email"
                                   maxlength="100" autocomplete="email" value="<%= escapeHtml(email) %>" required>
                        </div>
                        <button class="btn btn-accent w-100 rounded-pill py-2 fw-bold" type="submit">Gửi mã xác nhận</button>
                    </form>
                <% } %>

                <div class="text-center mt-4">
                    <a href="${pageContext.request.contextPath}/login" class="small text-danger fw-semibold text-decoration-none">
                        <i class="bi bi-arrow-left me-1"></i>Quay lại đăng nhập
                    </a>
                </div>
            </section>
        </div>
    </div>
</main>

<script>
document.querySelectorAll("[data-password-toggle]").forEach(function (button) {
    button.addEventListener("click", function () {
        var input = document.getElementById(button.dataset.passwordToggle);
        var showing = input.type === "text";
        input.type = showing ? "password" : "text";
        button.setAttribute("aria-label", showing ? "Hiện mật khẩu" : "Ẩn mật khẩu");
        button.setAttribute("title", showing ? "Hiện mật khẩu" : "Ẩn mật khẩu");
        button.querySelector("i").className = showing ? "bi bi-eye" : "bi bi-eye-slash";
    });
});
</script>

<jsp:include page="includes/footer.jsp" />
