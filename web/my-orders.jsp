<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.Order, model.OrderDetail, model.User, java.util.List" %>
<%
    // Kiểm tra quyền hạn: Tài khoản Admin không sử dụng trang Đơn Mua cá nhân
    User sessionUser = (User) session.getAttribute("user");
    if (sessionUser != null && (sessionUser.isAdmin() || sessionUser.getRole() == 1)) {
        session.setAttribute("toastInfo", "Tài khoản Quản Trị Viên quản lý và kiểm duyệt toàn bộ đơn hàng tại Bảng Quản Trị Hệ Thống.");
        response.sendRedirect(request.getContextPath() + "/admin/dashboard");
        return;
    }

    // Lấy danh sách đơn hàng đã được lọc từ Controller
    List<Order> orders = (List<Order>) request.getAttribute("orders");
    String activeTab = (String) request.getAttribute("activeTab");
    if (activeTab == null) activeTab = "all";
    String keyword = (String) request.getAttribute("keyword");
    if (keyword == null) keyword = "";

    // Đếm số lượng đơn hàng theo từng tab
    Integer countAll = (Integer) request.getAttribute("countAll");
    Integer countUnpaid = (Integer) request.getAttribute("countUnpaid");
    Integer countShipping = (Integer) request.getAttribute("countShipping");
    Integer countPending = (Integer) request.getAttribute("countPending");
    Integer countCompleted = (Integer) request.getAttribute("countCompleted");
    Integer countCancelled = (Integer) request.getAttribute("countCancelled");
    Integer countRefunded = (Integer) request.getAttribute("countRefunded");

    if (countAll == null) countAll = 0;
    if (countUnpaid == null) countUnpaid = 0;
    if (countShipping == null) countShipping = 0;
    if (countPending == null) countPending = 0;
    if (countCompleted == null) countCompleted = 0;
    if (countCancelled == null) countCancelled = 0;
    if (countRefunded == null) countRefunded = 0;

    String toastSuccess = (String) request.getAttribute("toastSuccess");
    String toastError = (String) request.getAttribute("toastError");
%>

<jsp:include page="includes/header.jsp" />
<jsp:include page="includes/navbar.jsp" />

<style>
    /* Shopee Style Order Tabs Navigation */
    .shopee-tabs-wrapper {
        background: var(--card-bg);
        border: 1px solid var(--border-color);
        border-radius: 12px;
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
        overflow-x: auto;
        white-space: nowrap;
        scrollbar-width: none;
    }
    .shopee-tabs-wrapper::-webkit-scrollbar {
        display: none;
    }
    .shopee-nav-tabs {
        display: flex;
        margin: 0;
        padding: 0;
        list-style: none;
        border-bottom: 2px solid var(--border-color);
    }
    .shopee-nav-item {
        flex: 1;
        min-width: 130px;
        text-align: center;
    }
    .shopee-nav-link {
        display: block;
        padding: 16px 14px;
        color: var(--text-dark);
        font-weight: 600;
        font-size: 0.95rem;
        text-decoration: none !important;
        position: relative;
        transition: all 0.25s ease;
        border-bottom: 3px solid transparent;
        margin-bottom: -2px;
    }
    .shopee-nav-link:hover {
        color: var(--accent-color);
    }
    .shopee-nav-link.active {
        color: var(--accent-color);
        font-weight: 700;
        border-bottom: 3px solid var(--accent-color);
    }
    .shopee-tab-badge {
        font-size: 0.75rem;
        padding: 2px 6px;
        border-radius: 10px;
        margin-left: 4px;
        vertical-align: middle;
    }

    /* Shopee Search Box */
    .shopee-search-bar {
        background: var(--card-bg);
        border: 1px solid var(--border-color);
        border-radius: 8px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);
        padding: 6px 14px;
        margin-top: 14px;
        margin-bottom: 20px;
        display: flex;
        align-items: center;
        gap: 8px;
    }
    .shopee-search-input {
        border: none;
        outline: none;
        background: transparent;
        color: var(--text-dark);
        flex: 1 1 auto;
        min-width: 0;
        width: auto;
        padding: 6px 10px;
        font-size: 0.92rem;
    }
    .shopee-search-input::placeholder {
        color: var(--text-muted);
    }
    .shopee-search-btn {
        white-space: nowrap !important;
        flex-shrink: 0 !important;
        min-width: 110px;
        height: 38px;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        padding: 0 20px;
        line-height: 1;
    }

    /* Order Card Style */
    .order-item-card {
        background: var(--card-bg);
        border: 1px solid var(--border-color);
        border-radius: 12px;
        box-shadow: 0 2px 10px rgba(0, 0, 0, 0.03);
        transition: transform 0.2s ease, box-shadow 0.2s ease;
        margin-bottom: 18px;
        overflow: hidden;
    }
    .order-item-card:hover {
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.07);
    }
    .order-card-header {
        padding: 14px 20px;
        background: rgba(0, 0, 0, 0.015);
        border-bottom: 1px solid var(--border-color);
        display: flex;
        justify-content: space-between;
        align-items: center;
        flex-wrap: wrap;
        gap: 10px;
    }
    .order-product-row {
        padding: 16px 20px;
        border-bottom: 1px solid var(--border-color);
        display: flex;
        align-items: center;
        gap: 16px;
        transition: background-color 0.15s ease;
    }
    .order-product-row:hover {
        background-color: rgba(0, 0, 0, 0.01);
    }
    .order-product-img {
        width: 80px;
        height: 80px;
        object-fit: cover;
        border-radius: 8px;
        border: 1px solid var(--border-color);
        flex-shrink: 0;
    }
    .order-card-footer {
        padding: 16px 20px;
        background: rgba(0, 0, 0, 0.01);
        display: flex;
        justify-content: space-between;
        align-items: center;
        flex-wrap: wrap;
        gap: 12px;
    }

    /* Order Stepper Timeline Tracking */
    .order-timeline {
        display: flex;
        justify-content: space-between;
        position: relative;
        margin: 20px 0 30px 0;
    }
    .order-timeline::before {
        content: '';
        position: absolute;
        top: 20px;
        left: 5%;
        right: 5%;
        height: 3px;
        background: #e2e8f0;
        z-index: 1;
    }
    .timeline-step {
        position: relative;
        z-index: 2;
        text-align: center;
        flex: 1;
    }
    .timeline-icon {
        width: 40px;
        height: 40px;
        border-radius: 50%;
        background: #e2e8f0;
        color: #64748b;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        font-size: 1.1rem;
        margin-bottom: 8px;
        border: 3px solid #ffffff;
        transition: all 0.3s ease;
    }
    .timeline-step.completed .timeline-icon {
        background: #10b981;
        color: #ffffff;
    }
    .timeline-step.active .timeline-icon {
        background: var(--accent-color);
        color: #ffffff;
        box-shadow: 0 0 0 4px rgba(225, 29, 72, 0.2);
    }
    .timeline-title {
        font-size: 0.82rem;
        font-weight: 600;
        color: var(--text-dark);
    }
</style>

<div class="container my-4" style="min-height: 70vh;">
    <!-- Breadcrumb Điều Hướng -->
    <nav aria-label="breadcrumb" class="mb-3">
        <ol class="breadcrumb small">
            <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/home" class="text-decoration-none">Trang chủ</a></li>
            <li class="breadcrumb-item active" aria-current="page">Đơn mua của tôi</li>
        </ol>
    </nav>

    <!-- Tiêu Đề Trang -->
    <div class="d-flex justify-content-between align-items-center mb-3">
        <div>
            <h3 class="fw-bold mb-1"><i class="bi bi-bag-check-fill text-danger me-2"></i>Đơn Mua Của Bạn</h3>
            <p class="text-muted small m-0">Quản lý và theo dõi tiến trình các đơn hàng bạn đã mua tại HighFashion</p>
        </div>
        <a href="${pageContext.request.contextPath}/products" class="btn btn-outline-dark rounded-pill btn-sm px-3 fw-semibold">
            <i class="bi bi-cart-plus me-1"></i> Tiếp tục mua sắm
        </a>
    </div>

    <!-- Thông Báo Toast Phản Hồi (Nếu có) -->
    <% if (toastSuccess != null) { %>
        <div class="alert alert-success alert-dismissible fade show border-0 shadow-sm rounded-3 py-3 d-flex align-items-center" role="alert">
            <i class="bi bi-check-circle-fill fs-5 me-2 text-success"></i>
            <div><%= toastSuccess %></div>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    <% } %>
    <% if (toastError != null) { %>
        <div class="alert alert-danger alert-dismissible fade show border-0 shadow-sm rounded-3 py-3 d-flex align-items-center" role="alert">
            <i class="bi bi-exclamation-triangle-fill fs-5 me-2 text-danger"></i>
            <div><%= toastError %></div>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    <% } %>

    <!-- 1. Thanh Tabs Trạng Thái Đơn Hàng (Shopee Style Navigation Tabs) -->
    <div class="shopee-tabs-wrapper">
        <ul class="shopee-nav-tabs">
            <!-- Tab: Tất cả -->
            <li class="shopee-nav-item">
                <a class="shopee-nav-link <%= "all".equalsIgnoreCase(activeTab) ? "active" : "" %>" 
                   href="${pageContext.request.contextPath}/my-orders?tab=all">
                    Tất cả
                    <% if (countAll > 0) { %>
                        <span class="badge <%= "all".equalsIgnoreCase(activeTab) ? "bg-danger" : "bg-secondary-subtle text-secondary" %> shopee-tab-badge"><%= countAll %></span>
                    <% } %>
                </a>
            </li>
            <!-- Tab: Chờ thanh toán -->
            <li class="shopee-nav-item">
                <a class="shopee-nav-link <%= "unpaid".equalsIgnoreCase(activeTab) ? "active" : "" %>" 
                   href="${pageContext.request.contextPath}/my-orders?tab=unpaid">
                    Chờ thanh toán
                    <% if (countUnpaid > 0) { %>
                        <span class="badge <%= "unpaid".equalsIgnoreCase(activeTab) ? "bg-danger" : "bg-secondary-subtle text-secondary" %> shopee-tab-badge"><%= countUnpaid %></span>
                    <% } %>
                </a>
            </li>
            <!-- Tab: Vận chuyển -->
            <li class="shopee-nav-item">
                <a class="shopee-nav-link <%= "shipping".equalsIgnoreCase(activeTab) ? "active" : "" %>" 
                   href="${pageContext.request.contextPath}/my-orders?tab=shipping">
                    Vận chuyển
                    <% if (countShipping > 0) { %>
                        <span class="badge <%= "shipping".equalsIgnoreCase(activeTab) ? "bg-danger" : "bg-secondary-subtle text-secondary" %> shopee-tab-badge"><%= countShipping %></span>
                    <% } %>
                </a>
            </li>
            <!-- Tab: Chờ giao hàng -->
            <li class="shopee-nav-item">
                <a class="shopee-nav-link <%= "pending".equalsIgnoreCase(activeTab) ? "active" : "" %>" 
                   href="${pageContext.request.contextPath}/my-orders?tab=pending">
                    Chờ giao hàng
                    <% if (countPending > 0) { %>
                        <span class="badge <%= "pending".equalsIgnoreCase(activeTab) ? "bg-danger" : "bg-secondary-subtle text-secondary" %> shopee-tab-badge"><%= countPending %></span>
                    <% } %>
                </a>
            </li>
            <!-- Tab: Hoàn thành -->
            <li class="shopee-nav-item">
                <a class="shopee-nav-link <%= "completed".equalsIgnoreCase(activeTab) ? "active" : "" %>" 
                   href="${pageContext.request.contextPath}/my-orders?tab=completed">
                    Hoàn thành
                    <% if (countCompleted > 0) { %>
                        <span class="badge <%= "completed".equalsIgnoreCase(activeTab) ? "bg-danger" : "bg-secondary-subtle text-secondary" %> shopee-tab-badge"><%= countCompleted %></span>
                    <% } %>
                </a>
            </li>
            <!-- Tab: Đã hủy -->
            <li class="shopee-nav-item">
                <a class="shopee-nav-link <%= "cancelled".equalsIgnoreCase(activeTab) ? "active" : "" %>" 
                   href="${pageContext.request.contextPath}/my-orders?tab=cancelled">
                    Đã hủy
                    <% if (countCancelled > 0) { %>
                        <span class="badge <%= "cancelled".equalsIgnoreCase(activeTab) ? "bg-danger" : "bg-secondary-subtle text-secondary" %> shopee-tab-badge"><%= countCancelled %></span>
                    <% } %>
                </a>
            </li>
            <!-- Tab: Trả hàng/Hoàn tiền -->
            <li class="shopee-nav-item">
                <a class="shopee-nav-link <%= "refunded".equalsIgnoreCase(activeTab) ? "active" : "" %>" 
                   href="${pageContext.request.contextPath}/my-orders?tab=refunded">
                    Trả hàng/Hoàn tiền
                    <% if (countRefunded > 0) { %>
                        <span class="badge <%= "refunded".equalsIgnoreCase(activeTab) ? "bg-danger" : "bg-secondary-subtle text-secondary" %> shopee-tab-badge"><%= countRefunded %></span>
                    <% } %>
                </a>
            </li>
        </ul>
    </div>

    <!-- 2. Thanh Tìm Kiếm Đơn Hàng (Shopee Style Search Bar) -->
    <form action="${pageContext.request.contextPath}/my-orders" method="GET" class="shopee-search-bar" id="orderSearchForm">
        <input type="hidden" name="tab" id="currentActiveTab" value="<%= activeTab %>">
        <i class="bi bi-search text-muted fs-5 flex-shrink-0"></i>
        <input type="text" 
               class="shopee-search-input" 
               id="orderSearchInput"
               name="keyword" 
               value="<%= keyword %>"
               placeholder="Bạn có thể tìm kiếm theo tên Shop, ID đơn hàng hoặc Tên Sản phẩm" 
               aria-label="Tìm kiếm đơn mua"
               autocomplete="off">
        <button type="button" 
                id="clearSearchBtn" 
                class="btn btn-sm btn-link text-muted text-decoration-none p-1 flex-shrink-0 <%= keyword.isEmpty() ? "d-none" : "" %>" 
                title="Xóa tìm kiếm">
            <i class="bi bi-x-circle-fill fs-5"></i>
        </button>
        <button type="submit" 
                id="searchSubmitBtn" 
                class="btn btn-danger btn-sm rounded-pill fw-semibold text-nowrap flex-shrink-0 shopee-search-btn">
            Tìm kiếm
        </button>
    </form>

    <!-- Thông báo kết quả Live Search khi không có đơn nào khớp -->
    <div id="liveSearchEmptyState" class="card border-0 shadow-sm rounded-4 text-center py-5 my-4 d-none" style="background: var(--card-bg);">
        <div class="py-5">
            <div class="mb-3">
                <i class="bi bi-search text-muted" style="font-size: 3.5rem; opacity: 0.4;"></i>
            </div>
            <h5 class="fw-bold mb-2">Không tìm thấy đơn hàng phù hợp</h5>
            <p class="text-muted small mb-3">
                Không có sản phẩm hoặc đơn hàng nào khớp với từ khóa "<strong id="liveSearchQueryDisplay" class="text-danger"></strong>".
            </p>
            <button type="button" class="btn btn-outline-danger rounded-pill px-4 btn-sm fw-semibold" onclick="resetSearch()">
                <i class="bi bi-arrow-counterclockwise me-1"></i> Xóa tìm kiếm & quay lại ban đầu
            </button>
        </div>
    </div>

    <!-- 3. Danh Sách Đơn Hàng -->
    <div id="orderListContainer">
        <% if (orders == null || orders.isEmpty()) { %>
            <!-- Trạng thái trống (Empty State) -->
            <div class="card border-0 shadow-sm rounded-4 text-center py-5 my-4" style="background: var(--card-bg);">
                <div class="py-5">
                    <div class="mb-3">
                        <i class="bi bi-receipt-cutoff text-muted" style="font-size: 4.5rem; opacity: 0.5;"></i>
                    </div>
                    <h5 class="fw-bold mb-2">Chưa có đơn hàng nào</h5>
                    <p class="text-muted small mb-4">
                        <% if (!keyword.isEmpty()) { %>
                            Không tìm thấy đơn hàng nào khớp với từ khóa "<strong><%= keyword %></strong>".
                        <% } else { %>
                            Bạn chưa có đơn hàng nào trong mục này. Hãy khám phá và mua sắm ngay!
                        <% } %>
                    </p>
                    <a href="${pageContext.request.contextPath}/products" class="btn btn-accent rounded-pill px-4 py-2 fw-semibold">
                        <i class="bi bi-bag-plus me-1"></i> Mua sắm ngay
                    </a>
                </div>
            </div>
        <% } else { %>
            <!-- Duyệt từng đơn hàng -->
            <% for (Order o : orders) { %>
                <div class="order-item-card order-card-item" 
                     data-order-id="<%= o.getId() %>" 
                     data-customer="<%= o.getCustomerName() != null ? o.getCustomerName().toLowerCase() : "" %>"
                     data-status="<%= o.getStatus() %>">
                    
                    <!-- Header Đơn Hàng: Tên Shop + Trạng Thái -->
                    <div class="order-card-header">
                        <div class="d-flex align-items-center flex-wrap gap-2">
                            <span class="badge bg-danger rounded-1 px-2 py-1 fw-bold" style="font-size: 0.72rem;">Mall</span>
                            <span class="fw-bold fs-6"><i class="bi bi-shop text-danger me-1"></i> HighFashion Official Store</span>
                            <span class="text-muted small">|</span>
                            <span class="badge bg-light text-secondary border fw-semibold">Đơn #<%= o.getId() %></span>
                            <span class="text-muted small"><i class="bi bi-clock me-1"></i><%= o.getCreatedAt() %></span>
                        </div>
                        <div>
                            <!-- Badge Trạng thái Shopee Style -->
                            <span class="badge <%= o.getStatusBadgeClass() %> px-3 py-2 fw-bold" style="font-size: 0.85rem;">
                                <i class="bi <%= o.getStatusIcon() %> me-1"></i>
                                <%= o.getStatusVietnamese().toUpperCase() %>
                            </span>
                        </div>
                    </div>

                    <!-- Danh Sách Sản Phẩm trong Đơn -->
                    <div class="order-card-body">
                        <% if (o.getOrderDetails() != null && !o.getOrderDetails().isEmpty()) { %>
                            <% for (OrderDetail d : o.getOrderDetails()) { %>
                                <div class="order-product-row" data-product-name="<%= d.getProductName() != null ? d.getProductName().toLowerCase() : "" %>">
                                    <a href="${pageContext.request.contextPath}/product-detail?id=<%= d.getProductId() %>" class="d-inline-block text-decoration-none">
                                        <img src="<%= d.getProductImage() != null ? d.getProductImage() : "https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=600" %>" 
                                             alt="<%= d.getProductName() %>" 
                                             class="order-product-img">
                                    </a>
                                    
                                    <div class="flex-grow-1">
                                        <a href="${pageContext.request.contextPath}/product-detail?id=<%= d.getProductId() %>" 
                                           class="fw-semibold text-dark text-decoration-none d-block mb-1 fs-6 product-name-link">
                                            <%= d.getProductName() != null ? d.getProductName() : "Sản phẩm thời trang HighFashion" %>
                                        </a>
                                        <div class="text-muted small mb-1">
                                            Phân loại hàng: 
                                            <span class="badge bg-light text-dark border me-1"><%= d.getColor() != null ? d.getColor() : "Mặc định" %></span>
                                            <span class="badge bg-light text-dark border">Size <%= d.getSize() != null ? d.getSize() : "Freesize" %></span>
                                        </div>
                                        <div class="text-muted small">
                                            Số lượng: <strong class="text-dark">x<%= d.getQuantity() %></strong>
                                        </div>
                                    </div>

                                    <div class="text-end">
                                        <div class="fw-bold text-danger fs-6">
                                            <%= String.format("%,.0f đ", d.getPrice()).replace(',', '.') %>
                                        </div>
                                    </div>
                                </div>
                            <% } %>
                        <% } else { %>
                            <div class="p-4 text-center text-muted small">
                                <em>Không có dữ liệu chi tiết mặt hàng</em>
                            </div>
                        <% } %>
                    </div>

                    <!-- Footer Đơn Hàng: Tổng tiền + Các nút thao tác -->
                    <div class="order-card-footer">
                        <div class="d-flex align-items-center flex-wrap gap-2">
                            <span class="small text-muted">Phương thức:</span>
                            <span class="badge bg-secondary-subtle text-secondary border"><%= o.getPaymentMethod() %></span>
                            <span class="small text-muted ms-2"><i class="bi bi-geo-alt-fill text-danger me-1"></i><%= o.getAddress() %></span>
                        </div>

                        <div class="d-flex align-items-center flex-wrap gap-3">
                            <div>
                                <span class="small text-muted">Thành tiền (<%= o.getTotalItemCount() %> sản phẩm):</span>
                                <span class="fs-5 fw-extrabold text-danger ms-1"><%= o.getFormattedTotalMoney() %></span>
                            </div>

                            <div class="d-flex gap-2">
                                <!-- Nút Xem Chi Tiết Đơn -->
                                <button type="button" 
                                        class="btn btn-outline-dark rounded-pill btn-sm px-3 fw-semibold btn-view-detail" 
                                        data-bs-toggle="modal" 
                                        data-bs-target="#orderDetailModal<%= o.getId() %>"
                                        onclick="openOrderDetail(<%= o.getId() %>);">
                                    <i class="bi bi-eye me-1"></i> Chi tiết đơn
                                </button>

                                <!-- Nút Hủy Đơn (Nếu đang PENDING hoặc UNPAID) -->
                                <% 
                                    String st = o.getStatus() != null ? o.getStatus().toUpperCase() : "";
                                    if ("PENDING".equals(st) || "UNPAID".equals(st) || "CONFIRMED".equals(st)) { 
                                %>
                                    <button type="button" 
                                            class="btn btn-outline-danger rounded-pill btn-sm px-3 fw-semibold btn-cancel-order"
                                            data-bs-toggle="modal" 
                                            data-bs-target="#cancelOrderModal<%= o.getId() %>"
                                            onclick="openCancelOrder(<%= o.getId() %>);">
                                        <i class="bi bi-x-circle me-1"></i> Hủy đơn
                                    </button>
                                <% } %>

                                <!-- Nút Đã Nhận Được Hàng (Nếu đang SHIPPING) -->
                                <% if ("SHIPPING".equals(st)) { %>
                                    <form method="POST" action="${pageContext.request.contextPath}/my-orders" class="d-inline">
                                        <input type="hidden" name="action" value="confirmReceived">
                                        <input type="hidden" name="orderId" value="<%= o.getId() %>">
                                        <button type="submit" class="btn btn-success rounded-pill btn-sm px-3 fw-semibold shadow-sm">
                                            <i class="bi bi-check2-circle me-1"></i> Đã nhận được hàng
                                        </button>
                                    </form>
                                <% } %>

                                <!-- Nút Mua Lại (Nếu đã DELIVERED hoặc CANCELLED) -->
                                <% if ("DELIVERED".equals(st) || "COMPLETED".equals(st) || "CANCELLED".equals(st)) { %>
                                    <form method="POST" action="${pageContext.request.contextPath}/my-orders" class="d-inline">
                                        <input type="hidden" name="action" value="repurchase">
                                        <input type="hidden" name="orderId" value="<%= o.getId() %>">
                                        <button type="submit" class="btn btn-danger rounded-pill btn-sm px-3 fw-semibold shadow-sm">
                                            <i class="bi bi-arrow-repeat me-1"></i> Mua lại
                                        </button>
                                    </form>
                                <% } %>
                            </div>
                        </div>
                    </div>
                </div>
            <% } %>
        <% } %>
    </div>
</div>

<!-- =================================================================== -->
<!-- KHU VỰC CÁC MODAL (Tách biệt ngoài danh sách để không lỗi giao diện) -->
<!-- =================================================================== -->
<% if (orders != null) { %>
    <% for (Order o : orders) { %>
        <% 
            String st = o.getStatus() != null ? o.getStatus().toUpperCase() : "";
            boolean isPending = "PENDING".equals(st) || "UNPAID".equals(st);
            boolean isConfirmed = "CONFIRMED".equals(st) || "PROCESSING".equals(st) || "SHIPPING".equals(st) || "DELIVERED".equals(st);
            boolean isShipping = "SHIPPING".equals(st) || "DELIVERED".equals(st);
            boolean isDelivered = "DELIVERED".equals(st) || "COMPLETED".equals(st);
            boolean isCancelled = "CANCELLED".equals(st);
        %>

        <!-- 1. Modal Chi Tiết Đơn Hàng (Kèm Tiến Trình Đơn Hàng Timeline) -->
        <div class="modal fade" id="orderDetailModal<%= o.getId() %>" tabindex="-1" aria-labelledby="modalLabel<%= o.getId() %>" aria-hidden="true">
            <div class="modal-dialog modal-lg modal-dialog-centered modal-dialog-scrollable">
                <div class="modal-content border-0 shadow-lg rounded-4">
                    <div class="modal-header border-bottom py-3">
                        <div class="d-flex align-items-center gap-2">
                            <span class="badge bg-danger rounded-1 px-2 py-1">HighFashion</span>
                            <h5 class="modal-title fw-bold m-0" id="modalLabel<%= o.getId() %>">Chi Tiết Đơn Hàng #<%= o.getId() %></h5>
                        </div>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>

                    <div class="modal-body p-4">
                        <!-- Timeline Tiến Trình Giao Hàng -->
                        <% if (!isCancelled) { %>
                            <div class="order-timeline">
                                <div class="timeline-step <%= isPending || isConfirmed || isShipping || isDelivered ? "completed" : "" %>">
                                    <div class="timeline-icon"><i class="bi bi-file-earmark-check"></i></div>
                                    <div class="timeline-title">Đơn Đã Đặt</div>
                                    <div class="text-muted" style="font-size: 0.7rem;"><%= o.getCreatedAt() %></div>
                                </div>
                                <div class="timeline-step <%= isConfirmed ? "completed" : (isPending ? "active" : "") %>">
                                    <div class="timeline-icon"><i class="bi bi-box-seam"></i></div>
                                    <div class="timeline-title">Đã Xác Nhận</div>
                                </div>
                                <div class="timeline-step <%= isShipping ? "completed" : (isConfirmed && !isShipping ? "active" : "") %>">
                                    <div class="timeline-icon"><i class="bi bi-truck"></i></div>
                                    <div class="timeline-title">Đang Vận Chuyển</div>
                                </div>
                                <div class="timeline-step <%= isDelivered ? "completed" : "" %>">
                                    <div class="timeline-icon"><i class="bi bi-house-check"></i></div>
                                    <div class="timeline-title">Đã Nhận Hàng</div>
                                </div>
                            </div>
                        <% } else { %>
                            <div class="alert alert-danger rounded-3 d-flex align-items-center mb-4">
                                <i class="bi bi-x-octagon-fill fs-3 me-3 text-danger"></i>
                                <div>
                                    <strong class="d-block">Đơn hàng này đã bị hủy</strong>
                                    <span class="small">Lý do: Khách hàng yêu cầu hủy đơn hoặc thay đổi nhu cầu mua sắm.</span>
                                </div>
                            </div>
                        <% } %>

                        <!-- Khối Thông Tin Người Nhận & Địa Chỉ -->
                        <div class="bg-light p-3 rounded-3 mb-4">
                            <h6 class="fw-bold mb-2 text-dark"><i class="bi bi-geo-alt-fill text-danger me-2"></i>Địa Chỉ Nhận Hàng</h6>
                            <div class="row g-2 small text-secondary">
                                <div class="col-sm-6">
                                    <div><strong>Người nhận:</strong> <%= o.getCustomerName() %></div>
                                    <div><strong>Điện thoại:</strong> <%= o.getPhone() %></div>
                                    <div><strong>Email:</strong> <%= o.getEmail() %></div>
                                </div>
                                <div class="col-sm-6">
                                    <div><strong>Địa chỉ:</strong> <%= o.getAddress() %></div>
                                    <div><strong>Phương thức:</strong> <%= o.getPaymentMethod() %></div>
                                    <div><strong>Ghi chú:</strong> <%= o.getNote() != null && !o.getNote().isEmpty() ? o.getNote() : "Không có" %></div>
                                </div>
                            </div>
                        </div>

                        <!-- Bảng Kê Chi Tiết Mặt Hàng -->
                        <h6 class="fw-bold mb-2 text-dark"><i class="bi bi-bag-fill text-danger me-2"></i>Sản Phẩm Đã Mua</h6>
                        <div class="table-responsive mb-4">
                            <table class="table table-sm align-middle border-bottom">
                                <thead class="table-light small">
                                    <tr>
                                        <th>Sản phẩm</th>
                                        <th class="text-center">Phân loại</th>
                                        <th class="text-center">Số lượng</th>
                                        <th class="text-end">Đơn giá</th>
                                        <th class="text-end">Tạm tính</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <% if (o.getOrderDetails() != null) { %>
                                        <% for (OrderDetail d : o.getOrderDetails()) { %>
                                            <tr>
                                                <td>
                                                    <div class="d-flex align-items-center gap-2">
                                                        <a href="${pageContext.request.contextPath}/product-detail?id=<%= d.getProductId() %>" class="d-inline-block text-decoration-none">
                                                            <img src="<%= d.getProductImage() != null ? d.getProductImage() : "https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=600" %>" 
                                                                 style="width: 48px; height: 48px; object-fit: cover; border-radius: 6px;" alt="">
                                                        </a>
                                                        <div>
                                                            <a href="${pageContext.request.contextPath}/product-detail?id=<%= d.getProductId() %>" class="fw-semibold small text-dark text-decoration-none d-block">
                                                                <%= d.getProductName() %>
                                                            </a>
                                                        </div>
                                                    </div>
                                                </td>
                                                <td class="text-center small"><%= d.getColor() %> / <%= d.getSize() %></td>
                                                <td class="text-center small fw-bold"><%= d.getQuantity() %></td>
                                                <td class="text-end small"><%= String.format("%,.0f đ", d.getPrice()).replace(',', '.') %></td>
                                                <td class="text-end small fw-bold text-danger"><%= String.format("%,.0f đ", d.getTotalMoney()).replace(',', '.') %></td>
                                            </tr>
                                        <% } %>
                                    <% } %>
                                </tbody>
                            </table>
                        </div>

                        <!-- Bảng Tổng Kết Chi Phí -->
                        <div class="d-flex justify-content-end">
                            <div style="min-width: 280px;" class="small">
                                <div class="d-flex justify-content-between mb-1">
                                    <span class="text-muted">Tổng tiền hàng:</span>
                                    <span><%= o.getFormattedTotalMoney() %></span>
                                </div>
                                <div class="d-flex justify-content-between mb-1">
                                    <span class="text-muted">Phí vận chuyển:</span>
                                    <span class="text-success fw-semibold">Miễn phí (0 đ)</span>
                                </div>
                                <div class="d-flex justify-content-between mb-2">
                                    <span class="text-muted">Giảm giá voucher:</span>
                                    <span>0 đ</span>
                                </div>
                                <hr class="my-2">
                                <div class="d-flex justify-content-between fs-6 fw-bold">
                                    <span>Tổng thanh toán:</span>
                                    <span class="text-danger fs-5"><%= o.getFormattedTotalMoney() %></span>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="modal-footer border-top py-2">
                        <button type="button" class="btn btn-secondary rounded-pill btn-sm px-4" data-bs-dismiss="modal">Đóng</button>
                    </div>
                </div>
            </div>
        </div>

        <!-- 2. Modal Xác Nhận Hủy Đơn Hàng -->
        <% if (isPending || "CONFIRMED".equals(st)) { %>
            <div class="modal fade" id="cancelOrderModal<%= o.getId() %>" tabindex="-1" aria-hidden="true">
                <div class="modal-dialog modal-dialog-centered">
                    <div class="modal-content border-0 shadow-lg rounded-4">
                        <div class="modal-header border-bottom py-3">
                            <h5 class="modal-title fw-bold text-danger"><i class="bi bi-exclamation-triangle-fill me-2"></i>Xác Nhận Hủy Đơn</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                        </div>
                        <div class="modal-body p-4 text-center">
                            <div class="mb-3">
                                <i class="bi bi-x-circle text-danger" style="font-size: 3.5rem;"></i>
                            </div>
                            <h6 class="fw-bold mb-2">Bạn có chắc chắn muốn hủy đơn hàng #<%= o.getId() %>?</h6>
                            <p class="text-muted small mb-0">Sau khi hủy, đơn hàng sẽ chuyển sang trạng thái <strong>Đã hủy</strong>. Bạn vẫn có thể bấm "Mua lại" bất cứ lúc nào.</p>
                        </div>
                        <div class="modal-footer border-top py-2">
                            <form method="POST" action="${pageContext.request.contextPath}/my-orders">
                                <input type="hidden" name="action" value="cancel">
                                <input type="hidden" name="orderId" value="<%= o.getId() %>">
                                <button type="button" class="btn btn-light rounded-pill btn-sm px-3" data-bs-dismiss="modal">Không, giữ đơn</button>
                                <button type="submit" class="btn btn-danger rounded-pill btn-sm px-4 fw-semibold">Đồng ý hủy đơn</button>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        <% } %>
    <% } %>
<% } %>

<!-- JavaScript: Realtime Client-side Filter -->
<script>
    function openOrderDetail(orderId) {
        try {
            const el = document.getElementById('orderDetailModal' + orderId);
            if (el && window.bootstrap) {
                const modal = bootstrap.Modal.getOrCreateInstance(el);
                modal.show();
            }
        } catch (e) {
            console.error('Error opening order detail modal:', e);
        }
    }

    function openCancelOrder(orderId) {
        try {
            const el = document.getElementById('cancelOrderModal' + orderId);
            if (el && window.bootstrap) {
                const modal = bootstrap.Modal.getOrCreateInstance(el);
                modal.show();
            }
        } catch (e) {
            console.error('Error opening cancel order modal:', e);
        }
    }

    // Hàm loại bỏ dấu tiếng Việt để tìm kiếm không dấu chuẩn xác (Ví dụ: "ao b" khớp "Áo Blazer")
    function removeVietnameseDiacritics(str) {
        if (!str) return '';
        return str.normalize('NFD')
            .replace(/[\u0300-\u036f]/g, '')
            .replace(/đ/g, 'd')
            .replace(/Đ/g, 'D')
            .toLowerCase();
    }

    // Hàm thực thi lọc đơn hàng trực tiếp tức thì (Live Search)
    function performLiveFilter(rawQuery) {
        const query = (rawQuery || '').trim();
        const normQuery = removeVietnameseDiacritics(query);
        const lowerQuery = query.toLowerCase();

        const searchInput = document.getElementById('orderSearchInput');
        const clearBtn = document.getElementById('clearSearchBtn');
        const emptyState = document.getElementById('liveSearchEmptyState');
        const queryDisplay = document.getElementById('liveSearchQueryDisplay');
        const orderCards = document.querySelectorAll('.order-card-item');

        // Bật/tắt nút xóa (Clear Button)
        if (clearBtn) {
            if (query.length > 0) {
                clearBtn.classList.remove('d-none');
            } else {
                clearBtn.classList.add('d-none');
            }
        }

        // KHI XÓA HẾT KÝ TỰ: TRỞ VỀ BAN ĐẦU NGAY LẬP TỨC
        if (query === '') {
            orderCards.forEach(card => {
                card.style.display = '';
                const rows = card.querySelectorAll('.order-product-row');
                rows.forEach(r => r.style.display = '');
            });
            if (emptyState) emptyState.classList.add('d-none');
            try {
                const activeTab = document.getElementById('currentActiveTab') ? document.getElementById('currentActiveTab').value : 'all';
                const cleanUrl = window.location.pathname + '?tab=' + encodeURIComponent(activeTab);
                window.history.replaceState({}, document.title, cleanUrl);
            } catch(e) {}
            return;
        }

        // Lọc từng đơn hàng VÀ từng sản phẩm bên trong đơn hàng:
        let visibleCount = 0;
        orderCards.forEach(card => {
            const orderId = (card.getAttribute('data-order-id') || '').trim().toLowerCase();
            const shopName = "highfashion official store";
            const isOrderLevelMatch = orderId === lowerQuery || 
                                     ('#' + orderId) === lowerQuery || 
                                     ('đơn #' + orderId) === lowerQuery ||
                                     shopName.includes(lowerQuery) ||
                                     normQuery === 'mall';

            const productRows = card.querySelectorAll('.order-product-row');
            let matchedProductsInThisOrder = 0;

            if (isOrderLevelMatch) {
                // Khớp mã đơn hoặc tên shop -> hiển thị tất cả sản phẩm trong đơn
                productRows.forEach(r => r.style.display = '');
                matchedProductsInThisOrder = productRows.length;
            } else {
                // Lọc TỪNG DÒNG SẢN PHẨM: Sản phẩm nào khớp từ khóa thì hiện, sản phẩm khác ẩn đi!
                productRows.forEach(row => {
                    const rowText = row.textContent || '';
                    const rowNorm = removeVietnameseDiacritics(rowText);
                    const prodName = (row.getAttribute('data-product-name') || '').toLowerCase();
                    const prodNorm = removeVietnameseDiacritics(prodName);

                    const matchProduct = rowNorm.includes(normQuery) || 
                                         rowText.toLowerCase().includes(lowerQuery) ||
                                         prodNorm.includes(normQuery) ||
                                         prodName.includes(lowerQuery);

                    if (matchProduct) {
                        row.style.display = '';
                        matchedProductsInThisOrder++;
                    } else {
                        row.style.display = 'none';
                    }
                });
            }

            // Nếu đơn hàng có ít nhất 1 sản phẩm khớp -> hiển thị đơn hàng
            // Nếu không có sản phẩm nào trong đơn khớp -> ẩn cả đơn hàng
            if (matchedProductsInThisOrder > 0) {
                card.style.display = '';
                visibleCount++;
            } else {
                card.style.display = 'none';
            }
        });

        // Hiển thị giao diện thông báo nếu không có đơn nào khớp
        if (emptyState && queryDisplay) {
            if (visibleCount === 0 && orderCards.length > 0) {
                queryDisplay.textContent = query;
                emptyState.classList.remove('d-none');
            } else {
                emptyState.classList.add('d-none');
            }
        }

        // Cập nhật URL trình duyệt (không reload)
        try {
            const activeTab = document.getElementById('currentActiveTab') ? document.getElementById('currentActiveTab').value : 'all';
            const newUrl = window.location.pathname + '?tab=' + encodeURIComponent(activeTab) + '&keyword=' + encodeURIComponent(query);
            window.history.replaceState({}, document.title, newUrl);
        } catch(e) {}
    }

    // Hàm xóa tìm kiếm và quay lại ban đầu
    function resetSearch() {
        const searchInput = document.getElementById('orderSearchInput');
        if (searchInput) {
            searchInput.value = '';
            searchInput.focus();
        }
        performLiveFilter('');
    }

    document.addEventListener('DOMContentLoaded', function() {
        const searchInput = document.getElementById('orderSearchInput');
        const clearBtn = document.getElementById('clearSearchBtn');
        const searchForm = document.getElementById('orderSearchForm');

        if (searchInput) {
            // Sự kiện gõ phím -> lọc ngay tức thì
            searchInput.addEventListener('input', function() {
                performLiveFilter(this.value);
            });
            searchInput.addEventListener('keyup', function(e) {
                if (e.key === 'Escape') {
                    resetSearch();
                } else {
                    performLiveFilter(this.value);
                }
            });
            searchInput.addEventListener('search', function() {
                performLiveFilter(this.value);
            });
        }

        if (clearBtn) {
            clearBtn.addEventListener('click', function(e) {
                e.preventDefault();
                resetSearch();
            });
        }

        if (searchForm) {
            // Ngăn reload trang khi bấm Enter hoặc nút Tìm kiếm, thực thi lọc tức thì
            searchForm.addEventListener('submit', function(e) {
                e.preventDefault();
                if (searchInput) {
                    performLiveFilter(searchInput.value);
                }
            });
        }

        // Nếu lúc mới mở trang đã có sẵn từ khóa tìm kiếm -> chạy lọc luôn
        if (searchInput && searchInput.value.trim().length > 0) {
            performLiveFilter(searchInput.value);
        }
    });
</script>

<jsp:include page="includes/footer.jsp" />
