<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.User, model.CartItem, java.util.List" %>
<%
    User currentUser = (User) session.getAttribute("user");
    List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
    int cartCount = 0;
    if (cart != null) {
        for (CartItem item : cart) {
            cartCount += item.getQuantity();
        }
    }
%>

<nav class="navbar navbar-expand-lg navbar-custom sticky-top">
    <div class="container">
        <!-- Logo -->
        <a class="navbar-brand brand-logo" href="${pageContext.request.contextPath}/home">
            HIGH<span>FASHION</span>
        </a>

        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarMain">
            <span class="navbar-toggler-icon"></span>
        </button>

        <div class="collapse navbar-collapse" id="navbarMain">
            <!-- Navigation Links -->
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

            <!-- Search Bar -->
            <form action="${pageContext.request.contextPath}/products" method="GET" class="d-flex me-3 position-relative" style="width: 260px;">
                <input class="form-control rounded-pill pe-5 bg-light border-0 small" type="search" name="keyword" placeholder="Tìm quần áo..." aria-label="Search">
                <button class="btn position-absolute end-0 top-0 text-muted" type="submit" style="border: none;">
                    <i class="bi bi-search"></i>
                </button>
            </form>

            <!-- User Actions -->
            <div class="d-flex align-items-center gap-3">
                <!-- Cart Icon -->
                <a href="${pageContext.request.contextPath}/cart" class="position-relative text-dark fs-5 p-1">
                    <i class="bi bi-bag"></i>
                    <% if (cartCount > 0) { %>
                        <span class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger" style="font-size: 0.65rem;">
                            <%= cartCount %>
                        </span>
                    <% } %>
                </a>

                <% if (currentUser == null) { %>
                    <a href="${pageContext.request.contextPath}/login" class="btn btn-outline-dark rounded-pill px-3 fw-bold btn-sm">Đăng nhập</a>
                    <a href="${pageContext.request.contextPath}/register" class="btn btn-accent rounded-pill px-3 btn-sm">Đăng ký</a>
                <% } else { %>
                    <div class="dropdown">
                        <button class="btn btn-light rounded-pill dropdown-toggle fw-bold btn-sm border" type="button" data-bs-toggle="dropdown">
                            <i class="bi bi-person-circle me-1 text-danger"></i> <%= currentUser.getFullname() %>
                        </button>
                        <ul class="dropdown-menu dropdown-menu-end shadow border-0 mt-2">
                            <% if (currentUser.getRole() == 1) { %>
                                <li><a class="dropdown-menu-item text-danger fw-bold dropdown-item" href="${pageContext.request.contextPath}/admin/dashboard"><i class="bi bi-speedometer2 me-2"></i> Trang Admin</a></li>
                                <li><a class="dropdown-menu-item text-danger fw-bold dropdown-item" href="${pageContext.request.contextPath}/admin/stores"><i class="bi bi-geo-alt-fill me-2"></i> Quản Lý Bản Đồ</a></li>
                                <li><hr class="dropdown-divider"></li>
                            <% } %>
                            <li><a class="dropdown-item text-danger" href="${pageContext.request.contextPath}/logout"><i class="bi bi-box-arrow-right me-2"></i> Đăng xuất</a></li>
                        </ul>
                    </div>
                <% } %>
            </div>
        </div>
    </div>
</nav>
