<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.User, model.Order, java.util.List, java.text.DecimalFormat" %>
<%
    User currentUser = (User) session.getAttribute("user");
    if (currentUser == null || (!currentUser.isShipper() && !currentUser.isAdmin())) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    List<Order> orders = (List<Order>) request.getAttribute("shipperOrders");
    Integer countAssigned = (Integer) request.getAttribute("countAssigned");
    Integer countDelivering = (Integer) request.getAttribute("countDelivering");
    Integer countDoorstep = (Integer) request.getAttribute("countDoorstep");
    Integer countPaid = (Integer) request.getAttribute("countPaid");
    Integer countCompleted = (Integer) request.getAttribute("countCompleted");

    if (countAssigned == null) countAssigned = 0;
    if (countDelivering == null) countDelivering = 0;
    if (countDoorstep == null) countDoorstep = 0;
    if (countPaid == null) countPaid = 0;
    if (countCompleted == null) countCompleted = 0;

    String toastSuccess = (String) session.getAttribute("toastSuccess");
    session.removeAttribute("toastSuccess");
    DecimalFormat df = new DecimalFormat("#,###");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cổng Giao Hàng Shipper - Last Mile</title>
    <!-- Dark Mode Initializer: Khởi tạo chủ đề Sáng / Tối ngay lập tức để chống chớp giật màn hình (No FOUT) -->
    <script>
        (function() {
            try {
                const savedTheme = localStorage.getItem('theme');
                const systemPrefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
                const activeTheme = savedTheme ? savedTheme : (systemPrefersDark ? 'dark' : 'light');
                document.documentElement.setAttribute('data-bs-theme', activeTheme);
            } catch (e) {
                document.documentElement.setAttribute('data-bs-theme', 'light');
            }
        })();
    </script>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
    <style>
        :root, [data-bs-theme="light"] {
            --portal-bg: #f8fafc;
            --portal-text: #0f172a;
            --portal-header-bg: #ffffff;
            --portal-header-border: #e2e8f0;
            --portal-card-bg: #ffffff;
            --portal-card-border: #e2e8f0;
            --portal-card-shadow: 0 2px 8px rgba(0,0,0,0.04);
            --portal-badge-bg: #f1f5f9;
            --portal-badge-border: #e2e8f0;
            --portal-subtext: #64748b;
        }
        [data-bs-theme="dark"] {
            --portal-bg: #0d1117;
            --portal-text: #f0f6fc;
            --portal-header-bg: #161b22;
            --portal-header-border: #30363d;
            --portal-card-bg: #161b22;
            --portal-card-border: #30363d;
            --portal-card-shadow: 0 4px 12px rgba(0,0,0,0.3);
            --portal-badge-bg: #21262d;
            --portal-badge-border: #30363d;
            --portal-subtext: #8b949e;
        }
        body { 
            background-color: var(--portal-bg); 
            color: var(--portal-text); 
            font-family: system-ui, -apple-system, sans-serif; 
            transition: background-color 0.3s ease, color 0.3s ease;
        }
        .portal-header { 
            background: var(--portal-header-bg); 
            border-bottom: 1px solid var(--portal-header-border); 
            transition: all 0.3s ease;
        }
        .order-card { 
            background: var(--portal-card-bg); 
            border: 1px solid var(--portal-card-border); 
            box-shadow: var(--portal-card-shadow); 
            border-radius: 12px; 
            margin-bottom: 1rem; 
            transition: all 0.3s ease;
        }
        .stat-badge { 
            background: var(--portal-badge-bg); 
            border: 1px solid var(--portal-badge-border); 
            border-radius: 8px; 
            padding: 0.5rem 1rem; 
            color: var(--portal-text);
            transition: all 0.3s ease;
        }
        .portal-title {
            color: var(--portal-text);
        }
        /* Nút Chuyển Đổi Chế Độ Sáng / Tối (Theme Toggle Button) */
        .theme-toggle-btn {
            width: 38px;
            height: 38px;
            border-radius: 50% !important;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            background: var(--portal-card-bg);
            border: 1px solid var(--portal-card-border);
            color: var(--portal-text);
            cursor: pointer;
            transition: all 0.3s ease;
            box-shadow: 0 2px 6px rgba(0,0,0,0.08);
            padding: 0;
        }
        .theme-toggle-btn:hover {
            transform: scale(1.08);
            border-color: #ef4444;
        }
        .theme-icon-moon { display: block; color: #6366f1; font-size: 1.1rem; }
        .theme-icon-sun { display: none; color: #f59e0b; font-size: 1.15rem; }
        [data-bs-theme="dark"] .theme-icon-moon { display: none; }
        [data-bs-theme="dark"] .theme-icon-sun { display: block; }
    </style>
</head>
<body>
    <header class="portal-header py-3 px-3 px-md-4 mb-3">
        <div class="d-flex align-items-center justify-content-between">
            <div class="d-flex align-items-center gap-2">
                <i class="bi bi-bicycle text-danger fs-3"></i>
                <div>
                    <div class="fw-bold fs-5 portal-title">SHIPPER APP</div>
                    <div class="small text-muted">Tuyến Giao Quận 5</div>
                </div>
            </div>
            <div class="d-flex align-items-center gap-3">
                <!-- Nút Bật/Tắt Chế Độ Sáng / Tối -->
                <button class="theme-toggle-btn" id="themeToggleBtn" type="button" 
                        title="Chuyển chế độ Tối / Sáng" aria-label="Chuyển đổi giao diện Sáng / Tối">
                    <i class="bi bi-moon-stars-fill theme-icon-moon"></i>
                    <i class="bi bi-sun-fill theme-icon-sun"></i>
                </button>
                <span class="d-none d-md-inline fw-semibold"><i class="bi bi-person-fill text-warning me-1"></i><%= currentUser.getFullname() %></span>
                <a href="${pageContext.request.contextPath}/logout" class="btn btn-sm btn-outline-danger rounded-pill" title="Đăng xuất"><i class="bi bi-box-arrow-right"></i></a>
            </div>
        </div>
    </header>

    <div class="container py-2" style="max-width: 800px;">
        <% if (toastSuccess != null) { %>
            <div class="alert alert-success alert-dismissible fade show" role="alert">
                <i class="bi bi-check-circle-fill me-2"></i><%= toastSuccess %>
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        <% } %>

        <!-- Thống kê nhanh của Shipper -->
        <div class="d-flex gap-2 overflow-auto pb-2 mb-3">
            <div class="stat-badge text-nowrap">
                <span class="text-muted small">Cần giao:</span> <strong class="text-warning"><%= countAssigned %></strong>
            </div>
            <div class="stat-badge text-nowrap">
                <span class="text-muted small">Đang giao:</span> <strong class="text-info"><%= countDelivering %></strong>
            </div>
            <div class="stat-badge text-nowrap">
                <span class="text-muted small">Đã đến nơi:</span> <strong class="text-primary"><%= countDoorstep %></strong>
            </div>
            <div class="stat-badge text-nowrap">
                <span class="text-muted small">Đã thu COD:</span> <strong class="text-success"><%= countPaid %></strong>
            </div>
            <div class="stat-badge text-nowrap">
                <span class="text-muted small">Hoàn thành:</span> <strong class="text-success"><%= countCompleted %></strong>
            </div>
        </div>

        <h5 class="fw-bold mb-3"><i class="bi bi-geo-alt me-2 text-danger"></i>Danh Sách Đơn Hàng Của Bạn</h5>

        <% if (orders == null || orders.isEmpty()) { %>
            <div class="text-center py-5 text-muted">
                <i class="bi bi-inbox fs-1 d-block mb-2"></i>
                Bạn hiện chưa có đơn hàng nào cần giao.
            </div>
        <% } else { %>
            <% for (Order o : orders) { 
                String st = o.getStatus() != null ? o.getStatus().toUpperCase() : "";
            %>
                <div class="card order-card p-3 shadow-sm">
                    <div class="d-flex justify-content-between align-items-start mb-2">
                        <div>
                            <span class="badge bg-secondary">Đơn #<%= o.getId() %></span>
                            <span class="badge bg-info text-dark ms-1"><%= o.getStatusVietnamese() %></span>
                        </div>
                        <div class="text-end">
                            <div class="text-muted small">Thu tiền (COD):</div>
                            <div class="fs-5 fw-bold text-danger"><%= df.format(o.getTotalMoney()) %> đ</div>
                        </div>
                    </div>

                    <div class="mb-2">
                        <div class="fw-bold fs-6"><i class="bi bi-person me-1 text-primary"></i><%= o.getCustomerName() %></div>
                        <div class="small text-muted"><i class="bi bi-telephone me-1"></i><%= o.getPhone() %></div>
                        <div class="small text-light mt-1"><i class="bi bi-geo-alt-fill me-1 text-danger"></i><%= o.getAddress() %></div>
                    </div>

                    <div class="border-top border-secondary pt-2 mt-2 d-flex gap-2 flex-wrap justify-content-end">
                        <a href="tel:<%= o.getPhone() %>" class="btn btn-sm btn-outline-light rounded-pill px-3">
                            <i class="bi bi-telephone-outbound me-1"></i>Gọi Khách
                        </a>

                        <% if ("SHIPPER_ASSIGNED".equals(st)) { %>
                            <a href="${pageContext.request.contextPath}/shipper/dashboard?action=updateStatus&orderId=<%= o.getId() %>&status=DELIVERING" 
                               class="btn btn-sm btn-warning rounded-pill fw-bold px-3">
                                <i class="bi bi-bicycle me-1"></i>Bắt Đầu Đi Giao
                            </a>
                        <% } else if ("DELIVERING".equals(st) || "SHIPPING".equals(st) || "VAN_CHUYEN".equals(st)) { %>
                            <a href="${pageContext.request.contextPath}/shipper/dashboard?action=updateStatus&orderId=<%= o.getId() %>&status=ARRIVED_DOORSTEP" 
                               class="btn btn-sm btn-info rounded-pill fw-bold px-3 text-dark">
                                <i class="bi bi-pin-map-fill me-1"></i>Đã Đến Nơi Giao
                            </a>
                        <% } else if ("ARRIVED_DOORSTEP".equals(st)) { %>
                            <a href="${pageContext.request.contextPath}/shipper/dashboard?action=updateStatus&orderId=<%= o.getId() %>&status=PAYMENT_DONE" 
                               class="btn btn-sm btn-primary rounded-pill fw-bold px-3">
                                <i class="bi bi-cash-coin me-1"></i>Hoàn Tất Thanh Toán COD
                            </a>
                        <% } else if ("PAYMENT_DONE".equals(st)) { %>
                            <a href="${pageContext.request.contextPath}/shipper/dashboard?action=updateStatus&orderId=<%= o.getId() %>&status=DELIVERED" 
                               class="btn btn-sm btn-success rounded-pill fw-bold px-3">
                                <i class="bi bi-check2-circle me-1"></i>Nhận Hàng Thành Công
                            </a>
                        <% } else if ("DELIVERED".equals(st) || "COMPLETED".equals(st) || "DA_GIAO".equals(st)) { %>
                            <span class="badge bg-success px-3 py-2"><i class="bi bi-check-all me-1"></i>Đã hoàn tất</span>
                        <% } %>

                        <% if (!"DELIVERED".equals(st) && !"COMPLETED".equals(st) && !"DA_GIAO".equals(st)) { %>
                            <a href="${pageContext.request.contextPath}/shipper/dashboard?action=updateStatus&orderId=<%= o.getId() %>&status=RETURNED" 
                               class="btn btn-sm btn-outline-danger rounded-pill px-2" title="Giao thất bại / Hoàn trả">
                                <i class="bi bi-x-circle"></i> Trả hàng
                            </a>
                        <% } %>
                    </div>
                </div>
            <% } %>
        <% } %>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <!-- Script xử lý Chuyển đổi Chế Độ Sáng / Tối (Dark Mode Toggle) đồng bộ toàn hệ thống -->
    <script>
        document.addEventListener('DOMContentLoaded', function() {
            const toggleBtn = document.getElementById('themeToggleBtn');
            if (!toggleBtn) return;
            function updateToggleState(theme) {
                if (theme === 'dark') {
                    toggleBtn.setAttribute('title', 'Chuyển sang chế độ Sáng (Light Mode)');
                    toggleBtn.setAttribute('aria-label', 'Chuyển sang chế độ Sáng');
                } else {
                    toggleBtn.setAttribute('title', 'Chuyển sang chế độ Tối (Dark Mode)');
                    toggleBtn.setAttribute('aria-label', 'Chuyển sang chế độ Tối');
                }
            }
            const currentTheme = document.documentElement.getAttribute('data-bs-theme') || 'light';
            updateToggleState(currentTheme);
            toggleBtn.addEventListener('click', function() {
                const activeTheme = document.documentElement.getAttribute('data-bs-theme');
                const newTheme = activeTheme === 'dark' ? 'light' : 'dark';
                document.documentElement.setAttribute('data-bs-theme', newTheme);
                try {
                    localStorage.setItem('theme', newTheme);
                } catch (e) {}
                updateToggleState(newTheme);
            });
        });
    </script>
</body>
</html>
