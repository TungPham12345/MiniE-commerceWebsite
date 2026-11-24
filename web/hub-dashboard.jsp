<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.User, model.Order, java.util.List, java.text.DecimalFormat" %>
<%
    User currentUser = (User) session.getAttribute("user");
    if (currentUser == null || (!currentUser.isHub() && !currentUser.isAdmin())) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    List<Order> orders = (List<Order>) request.getAttribute("hubOrders");
    Integer countArrived = (Integer) request.getAttribute("countArrived");
    Integer countChecked = (Integer) request.getAttribute("countChecked");
    Integer countAssigned = (Integer) request.getAttribute("countAssigned");

    if (countArrived == null) countArrived = 0;
    if (countChecked == null) countChecked = 0;
    if (countAssigned == null) countAssigned = 0;

    String toastSuccess = (String) session.getAttribute("toastSuccess");
    session.removeAttribute("toastSuccess");
    DecimalFormat df = new DecimalFormat("#,###");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cổng Bưu Cục Phát - Hub Sorting</title>
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
            --portal-table-bg: #ffffff;
            --portal-table-border: #e2e8f0;
            --portal-table-th-bg: #f1f5f9;
            --portal-table-th-color: #475569;
            --portal-table-td-border: #f1f5f9;
            --portal-subtext: #64748b;
        }
        [data-bs-theme="dark"] {
            --portal-bg: #111827;
            --portal-text: #f9fafb;
            --portal-header-bg: #1f2937;
            --portal-header-border: #374151;
            --portal-card-bg: #1f2937;
            --portal-card-border: #374151;
            --portal-card-shadow: 0 4px 12px rgba(0,0,0,0.3);
            --portal-table-bg: #1f2937;
            --portal-table-border: #374151;
            --portal-table-th-bg: #374151;
            --portal-table-th-color: #d1d5db;
            --portal-table-td-border: #374151;
            --portal-subtext: #9ca3af;
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
        .card-stat { 
            background: var(--portal-card-bg); 
            border: 1px solid var(--portal-card-border); 
            box-shadow: var(--portal-card-shadow); 
            border-radius: 12px; 
            transition: all 0.3s ease;
        }
        .portal-table { 
            background: var(--portal-table-bg); 
            border: 1px solid var(--portal-table-border); 
            box-shadow: var(--portal-card-shadow); 
            border-radius: 12px; 
            overflow: hidden; 
            transition: all 0.3s ease;
        }
        .portal-table table { 
            color: var(--portal-text); 
            margin-bottom: 0; 
        }
        .portal-table th { 
            background: var(--portal-table-th-bg); 
            color: var(--portal-table-th-color); 
            border-color: var(--portal-table-border); 
            font-size: 0.85rem; 
            text-transform: uppercase; 
        }
        .portal-table td { 
            border-color: var(--portal-table-td-border); 
            vertical-align: middle; 
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
            border-color: #10b981;
        }
        .theme-icon-moon { display: block; color: #6366f1; font-size: 1.1rem; }
        .theme-icon-sun { display: none; color: #f59e0b; font-size: 1.15rem; }
        [data-bs-theme="dark"] .theme-icon-moon { display: none; }
        [data-bs-theme="dark"] .theme-icon-sun { display: block; }
    </style>
</head>
<body>
    <header class="portal-header py-3 px-4 mb-4">
        <div class="d-flex align-items-center justify-content-between">
            <div class="d-flex align-items-center gap-3">
                <span class="fs-4 fw-bold text-success"><i class="bi bi-buildings me-2"></i>BƯU CỤC PHÁT QUẬN 5</span>
                <span class="badge bg-secondary px-3 py-1 rounded-pill">Kho Phân Loại Chặng Cuối</span>
            </div>
            <div class="d-flex align-items-center gap-3">
                <!-- Nút Bật/Tắt Chế Độ Sáng / Tối -->
                <button class="theme-toggle-btn" id="themeToggleBtn" type="button" 
                        title="Chuyển chế độ Tối / Sáng" aria-label="Chuyển đổi giao diện Sáng / Tối">
                    <i class="bi bi-moon-stars-fill theme-icon-moon"></i>
                    <i class="bi bi-sun-fill theme-icon-sun"></i>
                </button>
                <div class="text-end">
                    <div class="fw-bold"><i class="bi bi-person-circle me-1"></i><%= currentUser.getFullname() %></div>
                    <span class="text-muted small"><%= currentUser.getRoleName() %></span>
                </div>
                <a href="${pageContext.request.contextPath}/logout" class="btn btn-sm btn-outline-danger rounded-pill"><i class="bi bi-box-arrow-right me-1"></i>Đăng xuất</a>
            </div>
        </div>
    </header>

    <div class="container-fluid px-4">
        <% if (toastSuccess != null) { %>
            <div class="alert alert-success alert-dismissible fade show" role="alert">
                <i class="bi bi-check-circle-fill me-2"></i><%= toastSuccess %>
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        <% } %>

        <div class="row g-3 mb-4">
            <div class="col-md-4">
                <div class="card card-stat p-3">
                    <div class="text-muted small fw-bold">1. HÀNG VỪA ĐẾN BƯU CỤC</div>
                    <div class="fs-3 fw-bold text-warning"><%= countArrived %></div>
                    <div class="small text-muted">Chờ kiểm đếm số lượng</div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="card card-stat p-3">
                    <div class="text-muted small fw-bold">2. ĐÃ KIỂM ĐẾM ĐỦ</div>
                    <div class="fs-3 fw-bold text-info"><%= countChecked %></div>
                    <div class="small text-muted">Sẵn sàng phân tuyến cho Shipper</div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="card card-stat p-3">
                    <div class="text-muted small fw-bold">3. ĐÃ GIAO CHO SHIPPER</div>
                    <div class="fs-3 fw-bold text-success"><%= countAssigned %></div>
                    <div class="small text-muted">Shipper đã nhận đi phát</div>
                </div>
            </div>
        </div>

        <div class="portal-table p-3">
            <h5 class="fw-bold mb-3"><i class="bi bi-boxes me-2 text-success"></i>Quản Lý Tiếp Nhận & Phân Tuyến Đơn Hàng Tại Kho Phát</h5>
            <div class="table-responsive">
                <table class="table table-hover align-middle">
                    <thead>
                        <tr>
                            <th>Mã Đơn</th>
                            <th>Người Nhận & Địa Chỉ</th>
                            <th>Tiền Thu COD</th>
                            <th>Trạng Thái</th>
                            <th class="text-end">Thao Tác Thủ Kho</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (orders == null || orders.isEmpty()) { %>
                            <tr><td colspan="5" class="text-center py-4 text-muted">Không có đơn hàng nào tại bưu cục phát.</td></tr>
                        <% } else { %>
                            <% for (Order o : orders) { 
                                String st = o.getStatus() != null ? o.getStatus().toUpperCase() : "";
                            %>
                                <tr>
                                    <td class="fw-bold text-info">#<%= o.getId() %></td>
                                    <td>
                                        <div class="fw-semibold"><%= o.getCustomerName() %> (<%= o.getPhone() %>)</div>
                                        <div class="small text-muted"><%= o.getAddress() %></div>
                                    </td>
                                    <td class="fw-bold text-danger"><%= df.format(o.getTotalMoney()) %> đ</td>
                                    <td>
                                        <span class="badge bg-info text-dark px-3 py-2 rounded-pill"><%= o.getStatusVietnamese() %></span>
                                    </td>
                                    <td class="text-end">
                                        <% if ("ARRIVED_HUB".equals(st)) { %>
                                            <a href="${pageContext.request.contextPath}/hub/dashboard?action=updateStatus&orderId=<%= o.getId() %>&status=HUB_CHECKED" 
                                               class="btn btn-sm btn-warning rounded-pill fw-bold px-3">
                                                <i class="bi bi-clipboard-check me-1"></i>Kiểm Tra & Xác Nhận Số Lượng
                                            </a>
                                        <% } else if ("HUB_CHECKED".equals(st)) { %>
                                            <a href="${pageContext.request.contextPath}/hub/dashboard?action=updateStatus&orderId=<%= o.getId() %>&status=SHIPPER_ASSIGNED" 
                                               class="btn btn-sm btn-success rounded-pill fw-bold px-3">
                                                <i class="bi bi-person-check-fill me-1"></i>Giao Cho Shipper Phát
                                            </a>
                                        <% } else if ("SHIPPER_ASSIGNED".equals(st)) { %>
                                            <span class="text-success small fw-semibold"><i class="bi bi-check-circle me-1"></i>Đã bàn giao cho Shipper</span>
                                        <% } %>
                                    </td>
                                </tr>
                            <% } %>
                        <% } %>
                    </tbody>
                </table>
            </div>
        </div>
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
