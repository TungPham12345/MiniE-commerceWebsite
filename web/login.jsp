<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String errorMessage = (String) request.getAttribute("errorMessage");
    String successMessage = (String) request.getAttribute("successMessage");
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

                <form action="${pageContext.request.contextPath}/login" method="POST">
                    <div class="mb-3">
                        <label class="form-label fw-bold">Tên đăng nhập</label>
                        <div class="input-group">
                            <span class="input-group-text bg-light border-end-0"><i class="bi bi-person text-muted"></i></span>
                            <input type="text" name="username" class="form-control border-start-0 bg-light" placeholder="Nhập username" required value="customer1">
                        </div>
                    </div>

                    <div class="mb-4">
                        <label class="form-label fw-bold">Mật khẩu</label>
                        <div class="input-group">
                            <span class="input-group-text bg-light border-end-0"><i class="bi bi-lock text-muted"></i></span>
                            <input type="password" name="password" class="form-control border-start-0 bg-light" placeholder="Nhập mật khẩu" required value="123456">
                        </div>
                    </div>

                    <button type="submit" class="btn btn-accent w-100 rounded-pill py-2.5 fw-bold mb-3">Đăng Nhập</button>
                    
                    <div class="text-center text-muted small">
                        Chưa có tài khoản? <a href="${pageContext.request.contextPath}/register" class="text-danger fw-bold text-decoration-none">Đăng ký ngay</a>
                    </div>
                </form>

                <div class="mt-4 pt-3 border-top bg-light p-3 rounded-3 small">
                    <strong class="d-block mb-1 text-dark">🔑 Tài khoản Demo sẵn sàng test:</strong>
                    <div>• Customer: <code>customer1</code> / <code>123456</code></div>
                    <div>• Admin: <code>admin</code> / <code>123456</code></div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="includes/footer.jsp" />
