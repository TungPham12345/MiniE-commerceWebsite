<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.User" %>
<%!
    private String escapeHtml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                    .replace("\"", "&quot;").replace("'", "&#39;");
    }
%>
<%
    User account = (User) request.getAttribute("account");
    if (account == null) {
        response.sendRedirect(request.getContextPath() + "/account");
        return;
    }
    String toastSuccess = (String) session.getAttribute("toastSuccess");
    String toastError = (String) session.getAttribute("toastError");
    if (toastSuccess != null) session.removeAttribute("toastSuccess");
    if (toastError != null) session.removeAttribute("toastError");
%>

<jsp:include page="includes/header.jsp" />
<jsp:include page="includes/navbar.jsp" />

<main class="container account-page py-4 py-lg-5">
    <% if (toastSuccess != null) { %>
        <div class="alert alert-success alert-dismissible fade show rounded-4 shadow-sm" role="alert">
            <i class="bi bi-check-circle-fill me-2"></i><%= escapeHtml(toastSuccess) %>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Đóng"></button>
        </div>
    <% } %>
    <% if (toastError != null) { %>
        <div class="alert alert-danger alert-dismissible fade show rounded-4 shadow-sm" role="alert">
            <i class="bi bi-exclamation-triangle-fill me-2"></i><%= escapeHtml(toastError) %>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Đóng"></button>
        </div>
    <% } %>

    <div class="mb-4">
        <span class="badge bg-danger mb-2">TÀI KHOẢN CỦA TÔI</span>
        <h2 class="fw-bold mb-1">Thông tin cá nhân</h2>
        <p class="text-secondary mb-0">Cập nhật thông tin liên hệ và bảo vệ tài khoản của bạn.</p>
    </div>

    <div class="row g-4">
        <div class="col-lg-7">
            <section class="card border-0 shadow-sm rounded-4 h-100">
                <div class="card-body p-4 p-xl-5">
                    <div class="d-flex align-items-center gap-3 mb-4">
                        <div class="account-section-icon bg-primary-subtle text-primary"><i class="bi bi-person-lines-fill"></i></div>
                        <div>
                            <h4 class="fw-bold mb-1">Thông tin cá nhân</h4>
                            <div class="text-secondary small">Tên đăng nhập: <strong><%= escapeHtml(account.getUsername()) %></strong></div>
                        </div>
                    </div>
                    <form action="${pageContext.request.contextPath}/account" method="post">
                        <input type="hidden" name="action" value="profile">
                        <div class="mb-3">
                            <label class="form-label fw-semibold" for="profileFullname">Họ và tên</label>
                            <input class="form-control" id="profileFullname" name="fullname" maxlength="100" required
                                   value="<%= escapeHtml(account.getFullname()) %>">
                        </div>
                        <div class="mb-3">
                            <label class="form-label fw-semibold" for="profileEmail">Email</label>
                            <input class="form-control" id="profileEmail" type="email" name="email" maxlength="100" required
                                   value="<%= escapeHtml(account.getEmail()) %>">
                        </div>
                        <div class="mb-3">
                            <label class="form-label fw-semibold" for="profilePhone">Số điện thoại</label>
                            <input class="form-control" id="profilePhone" name="phone" maxlength="20"
                                   value="<%= escapeHtml(account.getPhone()) %>">
                        </div>
                        <div class="mb-4">
                            <label class="form-label fw-semibold" for="profileAddress">Địa chỉ</label>
                            <textarea class="form-control" id="profileAddress" name="address" maxlength="255" rows="3"><%= escapeHtml(account.getAddress()) %></textarea>
                        </div>
                        <button class="btn btn-danger rounded-pill px-4 fw-bold" type="submit">
                            <i class="bi bi-save me-1"></i>Lưu thông tin
                        </button>
                    </form>
                </div>
            </section>
        </div>

        <div class="col-lg-5">
            <section class="card border-0 shadow-sm rounded-4 h-100">
                <div class="card-body p-4 p-xl-5">
                    <div class="d-flex align-items-center gap-3 mb-4">
                        <div class="account-section-icon bg-warning-subtle text-warning-emphasis"><i class="bi bi-shield-lock"></i></div>
                        <div>
                            <h4 class="fw-bold mb-1">Đổi mật khẩu</h4>
                            <div class="text-secondary small">Nhập mật khẩu hiện tại để xác nhận.</div>
                        </div>
                    </div>
                    <form action="${pageContext.request.contextPath}/account" method="post">
                        <input type="hidden" name="action" value="password">
                        <div class="mb-3">
                            <label class="form-label fw-semibold" for="currentPassword">Mật khẩu hiện tại</label>
                            <input class="form-control" id="currentPassword" name="currentPassword" type="password" autocomplete="current-password" required>
                        </div>
                        <div class="mb-3">
                            <label class="form-label fw-semibold" for="newPassword">Mật khẩu mới</label>
                            <input class="form-control" id="newPassword" name="newPassword" type="password" minlength="6" maxlength="255" autocomplete="new-password" required>
                            <div class="form-text">Mật khẩu mới cần có ít nhất 6 ký tự.</div>
                        </div>
                        <div class="mb-4">
                            <label class="form-label fw-semibold" for="confirmPassword">Xác nhận mật khẩu mới</label>
                            <input class="form-control" id="confirmPassword" name="confirmPassword" type="password" minlength="6" maxlength="255" autocomplete="new-password" required>
                        </div>
                        <button class="btn btn-outline-danger rounded-pill px-4 fw-bold" type="submit">
                            <i class="bi bi-key me-1"></i>Cập nhật mật khẩu
                        </button>
                    </form>
                </div>
            </section>
        </div>
    </div>
</main>

<jsp:include page="includes/footer.jsp" />
