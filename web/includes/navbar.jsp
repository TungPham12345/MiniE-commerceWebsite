<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.User, model.CartItem, java.util.List" %>
<%
    // Lấy thông tin người dùng đang đăng nhập từ Session
    User currentUser = (User) session.getAttribute("user");
    // Lấy giỏ hàng từ Session
    List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
    // Đếm tổng số lượng món hàng trong giỏ
    int cartCount = 0;
    if (cart != null) {
        for (CartItem item : cart) {
            cartCount += item.getQuantity();
        }
    }
%>

<!-- Thanh Điều Hướng Trên Cùng (Navbar) -->
<nav class="navbar navbar-expand-lg navbar-custom sticky-top">
    <div class="container">
        <!-- Logo Thương Hiệu -->
        <a class="navbar-brand brand-logo" href="${pageContext.request.contextPath}/home">
            HIGH<span>FASHION</span>
        </a>

        <!-- Nút Toggler khi thu nhỏ màn hình Mobile -->
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarMain">
            <span class="navbar-toggler-icon"></span>
        </button>

        <div class="collapse navbar-collapse" id="navbarMain">
            <!-- Danh Sách Các Menu Điều Hướng Chính -->
            <ul class="navbar-nav me-auto mb-2 mb-lg-0 fw-semibold text-uppercase fs-6">
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/home">Trang chủ</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/products">Cửa hàng</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/stores">
                        <i class="bi bi-geo-alt-fill text-danger me-1"></i> Cửa Hàng & Bản Đồ
                    </a>
                </li>
            </ul>

            <!-- Khung Tìm Kiếm Sản Phẩm Nhanh -->
            <form action="${pageContext.request.contextPath}/products" method="GET" class="d-flex me-3 position-relative" style="width: 260px;">
                <input class="form-control rounded-pill pe-5 bg-light border-0 small" type="search" name="keyword" placeholder="Tìm quần áo..." aria-label="Search">
                <button class="btn position-absolute end-0 top-0 text-muted" type="submit" style="border: none;">
                    <i class="bi bi-search"></i>
                </button>
            </form>

            <!-- Nhóm Tác Vụ Người Dùng (Chuyển chế độ tối, Giỏ hàng, Đăng nhập/Đăng ký/Tài khoản) -->
            <div class="d-flex align-items-center gap-3">
                <!-- Nút Bật/Tắt Chế Độ Tối (Dark Mode Toggle Button) -->
                <button class="theme-toggle-btn" id="themeToggleBtn" type="button" 
                        title="Chuyển chế độ Tối / Sáng" aria-label="Chuyển đổi giao diện Sáng / Tối">
                    <i class="bi bi-moon-stars-fill theme-icon-moon"></i>
                    <i class="bi bi-sun-fill theme-icon-sun"></i>
                </button>

                <% if (currentUser != null && (currentUser.isAdmin() || currentUser.getRole() == 1)) { %>
                    <!-- Biểu Tượng Bảng Quản Trị Dành Riêng Cho Admin (Admin không sử dụng giỏ hàng) -->
                    <a href="${pageContext.request.contextPath}/admin/dashboard" class="position-relative nav-action-link fs-5 p-1 text-danger" title="Bảng Quản Trị Hệ Thống" id="navbarAdminIcon">
                        <i class="bi bi-speedometer2"></i>
                    </a>
                <% } else { %>
                    <!-- Biểu Tượng Giỏ Hàng Có Huy Hiệu Số Lượng (Dành Cho Khách Hàng Mua Sắm) -->
                    <a href="${pageContext.request.contextPath}/cart" class="position-relative nav-action-link fs-5 p-1" title="Xem giỏ hàng" id="navbarCartIcon">
                        <i class="bi bi-bag"></i>
                        <span id="navbarCartBadge" class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger <%= cartCount > 0 ? "" : "d-none" %>" style="font-size: 0.65rem;">
                            <%= cartCount %>
                        </span>
                    </a>
                <% } %>

                <% if (currentUser == null) { %>
                    <!-- Khách chưa đăng nhập: Nút Đăng nhập và Đăng ký -->
                    <a href="${pageContext.request.contextPath}/login" class="btn btn-outline-dark rounded-pill px-3 fw-bold btn-sm">Đăng nhập</a>
                    <a href="${pageContext.request.contextPath}/register" class="btn btn-accent rounded-pill px-3 btn-sm">Đăng ký</a>
                <% } else { %>
                    <!-- Người dùng đã đăng nhập: Menu tài khoản xổ xuống (Dropdown) -->
                    <div class="dropdown">
                        <button class="btn btn-light rounded-pill dropdown-toggle fw-bold btn-sm border" type="button" data-bs-toggle="dropdown">
                            <i class="bi bi-person-circle me-1 text-danger"></i> <%= currentUser.getFullname() %>
                        </button>
                        <ul class="dropdown-menu dropdown-menu-end shadow border-0 mt-2 py-2" style="min-width: 230px;">
                            <% if (currentUser.getRole() == 1 || currentUser.isAdmin()) { %>
                                <!-- Header Vai trò Quản trị viên -->
                                <li class="px-3 py-2 border-bottom mb-1 bg-light">
                                    <div class="d-flex align-items-center gap-1 text-danger fw-bold small">
                                        <i class="bi bi-shield-lock-fill"></i> QUẢN TRỊ VIÊN
                                    </div>
                                    <div class="text-muted small text-truncate"><%= currentUser.getEmail() != null ? currentUser.getEmail() : currentUser.getUsername() %></div>
                                </li>
                                <!-- Các mục Quản trị Admin CẦN CÓ (Admin KHÔNG CÓ Đơn mua) -->
                                <li><a class="dropdown-item fw-bold text-danger py-2" href="${pageContext.request.contextPath}/admin/dashboard"><i class="bi bi-speedometer2 me-2"></i> Bảng Quản Trị Hệ Thống</a></li>
                                <li><a class="dropdown-item fw-semibold text-dark py-2" href="${pageContext.request.contextPath}/admin/products"><i class="bi bi-box-seam me-2 text-danger"></i> Quản Lý Sản Phẩm</a></li>
                                <li><a class="dropdown-item fw-semibold text-dark py-2" href="${pageContext.request.contextPath}/admin/stores"><i class="bi bi-geo-alt-fill me-2 text-danger"></i> Quản Lý Bản Đồ</a></li>
                                <li><hr class="dropdown-divider my-1"></li>
                            <% } else { %>
                                <!-- Header Vai trò Khách hàng -->
                                <li class="px-3 py-2 border-bottom mb-1 bg-light">
                                    <div class="d-flex align-items-center gap-1 text-primary fw-bold small">
                                        <i class="bi bi-person-fill"></i> KHÁCH HÀNG
                                    </div>
                                    <div class="text-muted small text-truncate"><%= currentUser.getEmail() != null ? currentUser.getEmail() : currentUser.getUsername() %></div>
                                </li>
                                <!-- Menu Đơn Mua chỉ dành riêng cho Khách hàng -->
                                <li><a class="dropdown-item fw-semibold py-2" href="${pageContext.request.contextPath}/my-orders"><i class="bi bi-bag-check-fill me-2 text-danger"></i> Đơn mua của tôi</a></li>
                                <li><hr class="dropdown-divider my-1"></li>
                            <% } %>
                            <!-- Đăng xuất tài khoản -->
                            <li><a class="dropdown-item text-danger py-2" href="${pageContext.request.contextPath}/logout"><i class="bi bi-box-arrow-right me-2"></i> Đăng xuất</a></li>
                        </ul>
                    </div>
                <% } %>
            </div>
        </div>
    </div>
</nav>
