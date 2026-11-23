<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.User, model.CartItem, java.util.List" %>
<%
    // Lấy thông tin người dùng đang đăng nhập từ Session
    User currentUser = (User) session.getAttribute("user");
    // Lấy giỏ hàng từ Session
    List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
    // Hiển thị số dòng sản phẩm trong giỏ, không cộng số lượng từng dòng
    int cartCount = cart != null ? cart.size() : 0;
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

                <% if (currentUser == null || (!currentUser.isAdmin() && currentUser.getRole() != 1)) { %>
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
                            <% if (currentUser.isAdmin() || currentUser.getRole() == 1) { %>
                                <li class="px-3 py-2 border-bottom mb-1 bg-light">
                                    <div class="d-flex align-items-center gap-1 text-danger fw-bold small">
                                        <i class="bi bi-shield-lock-fill"></i> QUẢN TRỊ VIÊN
                                    </div>
                                    <div class="text-muted small text-truncate"><%= currentUser.getEmail() != null ? currentUser.getEmail() : currentUser.getUsername() %></div>
                                </li>
                                <li><a class="dropdown-item fw-semibold py-2" href="${pageContext.request.contextPath}/admin/dashboard"><i class="bi bi-speedometer2 me-2 text-danger"></i> Bảng điều khiển Admin</a></li>
                            <% } else if (currentUser.isWarehouse()) { %>
                                <li class="px-3 py-2 border-bottom mb-1 bg-light">
                                    <div class="d-flex align-items-center gap-1 text-warning fw-bold small">
                                        <i class="bi bi-box-seam"></i> KHO SHOP
                                    </div>
                                    <div class="text-muted small text-truncate"><%= currentUser.getEmail() != null ? currentUser.getEmail() : currentUser.getUsername() %></div>
                                </li>
                                <li><a class="dropdown-item fw-semibold py-2" href="${pageContext.request.contextPath}/warehouse/dashboard"><i class="bi bi-boxes me-2 text-warning"></i> Cổng Kho Shop</a></li>
                            <% } else if (currentUser.isLogistics()) { %>
                                <li class="px-3 py-2 border-bottom mb-1 bg-light">
                                    <div class="d-flex align-items-center gap-1 text-primary fw-bold small">
                                        <i class="bi bi-truck"></i> ĐVVC EXPRESS
                                    </div>
                                    <div class="text-muted small text-truncate"><%= currentUser.getEmail() != null ? currentUser.getEmail() : currentUser.getUsername() %></div>
                                </li>
                                <li><a class="dropdown-item fw-semibold py-2" href="${pageContext.request.contextPath}/logistics/dashboard"><i class="bi bi-truck me-2 text-primary"></i> Cổng ĐVVC Express</a></li>
                            <% } else if (currentUser.isHub()) { %>
                                <li class="px-3 py-2 border-bottom mb-1 bg-light">
                                    <div class="d-flex align-items-center gap-1 text-success fw-bold small">
                                        <i class="bi bi-buildings"></i> BƯU CỤC PHÁT
                                    </div>
                                    <div class="text-muted small text-truncate"><%= currentUser.getEmail() != null ? currentUser.getEmail() : currentUser.getUsername() %></div>
                                </li>
                                <li><a class="dropdown-item fw-semibold py-2" href="${pageContext.request.contextPath}/hub/dashboard"><i class="bi bi-building-check me-2 text-success"></i> Cổng Bưu Cục Phát</a></li>
                            <% } else if (currentUser.isShipper()) { %>
                                <li class="px-3 py-2 border-bottom mb-1 bg-light">
                                    <div class="d-flex align-items-center gap-1 text-danger fw-bold small">
                                        <i class="bi bi-bicycle"></i> SHIPPER
                                    </div>
                                    <div class="text-muted small text-truncate"><%= currentUser.getEmail() != null ? currentUser.getEmail() : currentUser.getUsername() %></div>
                                </li>
                                <li><a class="dropdown-item fw-semibold py-2" href="${pageContext.request.contextPath}/shipper/dashboard"><i class="bi bi-geo-alt me-2 text-danger"></i> Cổng Giao Hàng Shipper</a></li>
                            <% } else { %>
                                <li class="px-3 py-2 border-bottom mb-1 bg-light">
                                    <div class="d-flex align-items-center gap-1 text-primary fw-bold small">
                                        <i class="bi bi-person-fill"></i> KHÁCH HÀNG
                                    </div>
                                    <div class="text-muted small text-truncate"><%= currentUser.getEmail() != null ? currentUser.getEmail() : currentUser.getUsername() %></div>
                                </li>
                                <li><a class="dropdown-item fw-semibold py-2" href="${pageContext.request.contextPath}/my-orders"><i class="bi bi-bag-check-fill me-2 text-danger"></i> Đơn mua của tôi</a></li>
                            <% } %>
                            <li><a class="dropdown-item fw-semibold py-2" href="${pageContext.request.contextPath}/account"><i class="bi bi-person-vcard me-2 text-primary"></i> Thông tin cá nhân</a></li>
                            <li><hr class="dropdown-divider my-1"></li>
                            <li><a class="dropdown-item text-danger py-2" href="${pageContext.request.contextPath}/logout"><i class="bi bi-box-arrow-right me-2"></i> Đăng xuất</a></li>
                        </ul>
                    </div>
                <% } %>
            </div>
        </div>
    </div>
</nav>
<% if (currentUser != null && (currentUser.isAdmin() || currentUser.getRole() == 1)) { %>
    <jsp:include page="admin-sidebar.jsp" />
<% } %>
