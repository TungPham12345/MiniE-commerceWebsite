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

                <form action="${pageContext.request.contextPath}/register" method="POST">
                    <div class="row g-3">
                        <div class="col-md-6">
                            <label class="form-label fw-bold">Tên đăng nhập *</label>
                            <input type="text" name="username" class="form-control bg-light" placeholder="Username" required>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label fw-bold">Họ và tên *</label>
                            <input type="text" name="fullname" class="form-control bg-light" placeholder="Nguyễn Văn A" required>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label fw-bold">Mật khẩu *</label>
                            <input type="password" name="password" class="form-control bg-light" required>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label fw-bold">Xác nhận mật khẩu *</label>
                            <input type="password" name="confirmPassword" class="form-control bg-light" required>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label fw-bold">Email *</label>
                            <input type="email" name="email" class="form-control bg-light" placeholder="email@gmail.com" required>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label fw-bold">Số điện thoại *</label>
                            <input type="tel" name="phone" class="form-control bg-light" placeholder="0912345678" required>
                        </div>
                        <div class="col-12">
                            <label class="form-label fw-bold">Địa chỉ nhận hàng *</label>
                            <input type="text" name="address" class="form-control bg-light" placeholder="Số nhà, Tên đường, Quận/Huyện, TP" required>
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

<jsp:include page="includes/footer.jsp" />
