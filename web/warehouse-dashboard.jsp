<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.User, model.Order, model.OrderDetail, java.util.List, java.text.DecimalFormat" %>
<%
    User currentUser = (User) session.getAttribute("user");
    if (currentUser == null || (!currentUser.isWarehouse() && !currentUser.isAdmin())) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    List<Order> orders = (List<Order>) request.getAttribute("warehouseOrders");
    Integer countConfirmed = (Integer) request.getAttribute("countConfirmed");
    Integer countPicking = (Integer) request.getAttribute("countPicking");
    Integer countPacked = (Integer) request.getAttribute("countPacked");
    Integer countHandover = (Integer) request.getAttribute("countHandover");

    if (countConfirmed == null) countConfirmed = 0;
    if (countPicking == null) countPicking = 0;
    if (countPacked == null) countPacked = 0;
    if (countHandover == null) countHandover = 0;

    String toastSuccess = (String) session.getAttribute("toastSuccess");
    String toastError = (String) session.getAttribute("toastError");
    session.removeAttribute("toastSuccess");
    session.removeAttribute("toastError");

    DecimalFormat df = new DecimalFormat("#,###");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cổng Quản Lý Kho Shop - HighFashion</title>
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
            --portal-bg: #0f172a;
            --portal-text: #f8fafc;
            --portal-header-bg: #1e293b;
            --portal-header-border: #334155;
            --portal-card-bg: #1e293b;
            --portal-card-border: #334155;
            --portal-card-shadow: 0 4px 12px rgba(0,0,0,0.3);
            --portal-table-bg: #1e293b;
            --portal-table-border: #334155;
            --portal-table-th-bg: #334155;
            --portal-table-th-color: #cbd5e1;
            --portal-table-td-border: #334155;
            --portal-subtext: #94a3b8;
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
        .nav-tabs .nav-link { 
            color: var(--portal-subtext); 
            border: none; 
            font-weight: 600; 
        }
        .nav-tabs .nav-link.active { 
            color: #38bdf8; 
            background: transparent; 
            border-bottom: 3px solid #38bdf8; 
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
            border-color: #e11d48;
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
                <span class="fs-4 fw-bold text-danger"><i class="bi bi-box-seam me-2"></i>KHO SHOP HIGHFASHION</span>
                <span class="badge bg-primary px-3 py-1 rounded-pill">Kho Tổng Tân Bình</span>
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

        <!-- Thống kê trạng thái -->
        <div class="row g-3 mb-4">
            <div class="col-md-3">
                <div class="card card-stat p-3">
                    <div class="text-muted small fw-bold">1. CHỜ LẤY HÀNG</div>
                    <div class="fs-3 fw-bold text-info"><%= countConfirmed %></div>
                    <div class="small text-muted">Đơn Shop đã duyệt</div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="card card-stat p-3">
                    <div class="text-muted small fw-bold">2. ĐANG LẤY HÀNG</div>
                    <div class="fs-3 fw-bold text-warning"><%= countPicking %></div>
                    <div class="small text-muted">Đang nhặt từ kệ kho</div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="card card-stat p-3">
                    <div class="text-muted small fw-bold">3. ĐÃ ĐÓNG GÓI</div>
                    <div class="fs-3 fw-bold text-primary"><%= countPacked %></div>
                    <div class="small text-muted">Đã dán tem, chờ giao ĐVVC</div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="card card-stat p-3">
                    <div class="text-muted small fw-bold">4. CHỜ ĐVVC TIẾP NHẬN</div>
                    <div class="fs-3 fw-bold text-success"><%= countHandover %></div>
                    <div class="small text-muted">Đã tập kết tại cửa xuất</div>
                </div>
            </div>
        </div>

        <!-- Bảng danh sách đơn hàng kho -->
        <div class="portal-table p-3">
            <div class="d-flex align-items-center justify-content-between mb-3">
                <h5 class="fw-bold mb-0"><i class="bi bi-clipboard2-check me-2"></i>Danh Sách Đơn Hàng Cần Xử Lý Tại Kho</h5>
                <span class="text-muted small">Cập nhật theo thời gian thực</span>
            </div>

            <div class="table-responsive">
                <table class="table table-hover align-middle">
                    <thead>
                        <tr>
                            <th>Mã Đơn</th>
                            <th>Khách Hàng</th>
                            <th>Địa Chỉ Nhận</th>
                            <th>Sản Phẩm</th>
                            <th>Tổng Tiền</th>
                            <th>Trạng Thái Hiện Tại</th>
                            <th class="text-end">Thao Tác Kho</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (orders == null || orders.isEmpty()) { %>
                            <tr><td colspan="7" class="text-center py-4 text-muted">Hiện tại không có đơn hàng nào cần xử lý tại kho.</td></tr>
                        <% } else { %>
                            <% for (Order o : orders) { 
                                String st = o.getStatus() != null ? o.getStatus().toUpperCase() : "";
                            %>
                                <tr>
                                    <td class="fw-bold text-info">#<%= o.getId() %></td>
                                    <td>
                                        <div class="fw-semibold"><%= o.getCustomerName() %></div>
                                        <div class="small text-muted"><%= o.getPhone() %></div>
                                    </td>
                                    <td class="small" style="max-width: 200px;"><%= o.getAddress() %></td>
                                    <td>
                                        <% if (o.getOrderDetails() != null) { %>
                                            <span class="badge bg-secondary"><%= o.getOrderDetails().size() %> mặt hàng</span>
                                        <% } %>
                                    </td>
                                    <td class="fw-bold text-danger"><%= df.format(o.getTotalMoney()) %> đ</td>
                                    <td>
                                        <% if ("CONFIRMED".equals(st) || "CHO_LAY_HANG".equals(st)) { %>
                                            <span class="badge bg-info text-dark px-3 py-2 rounded-pill"><i class="bi bi-check2-circle me-1"></i>Đã xác nhận</span>
                                        <% } else if ("PICKING".equals(st)) { %>
                                            <span class="badge bg-warning text-dark px-3 py-2 rounded-pill"><i class="bi bi-box-arrow-in-down me-1"></i>Đang lấy hàng</span>
                                        <% } else if ("PACKED".equals(st)) { %>
                                            <span class="badge bg-primary px-3 py-2 rounded-pill"><i class="bi bi-box-seam me-1"></i>Đã đóng gói</span>
                                        <% } else if ("HANDOVER_PENDING".equals(st)) { %>
                                            <span class="badge bg-secondary px-3 py-2 rounded-pill"><i class="bi bi-hourglass-split me-1"></i>Chờ ĐVVC nhận</span>
                                        <% } else { %>
                                            <span class="badge bg-light text-dark px-3 py-2 rounded-pill"><%= o.getStatusVietnamese() %></span>
                                        <% } %>
                                    </td>
                                    <td class="text-end">
                                        <% if ("CONFIRMED".equals(st) || "CHO_LAY_HANG".equals(st)) { %>
                                            <a href="${pageContext.request.contextPath}/warehouse/dashboard?action=updateStatus&orderId=<%= o.getId() %>&status=PICKING" 
                                               class="btn btn-sm btn-warning rounded-pill fw-bold px-3">
                                                <i class="bi bi-box-arrow-in-down me-1"></i>Bắt Đầu Lấy Hàng
                                            </a>
                                        <% } else if ("PICKING".equals(st)) { %>
                                            <a href="${pageContext.request.contextPath}/warehouse/dashboard?action=updateStatus&orderId=<%= o.getId() %>&status=PACKED" 
                                               class="btn btn-sm btn-primary rounded-pill fw-bold px-3">
                                                <i class="bi bi-box-seam me-1"></i>Đóng Gói & In Tem
                                            </a>
                                        <% } else if ("PACKED".equals(st)) { %>
                                            <a href="${pageContext.request.contextPath}/warehouse/dashboard?action=updateStatus&orderId=<%= o.getId() %>&status=HANDOVER_PENDING" 
                                               class="btn btn-sm btn-success rounded-pill fw-bold px-3">
                                                <i class="bi bi-truck me-1"></i>Bàn Giao ĐVVC
                                            </a>
                                        <% } else if ("HANDOVER_PENDING".equals(st)) { %>
                                            <span class="text-success small fw-semibold"><i class="bi bi-check-circle me-1"></i>Đã sẵn sàng chờ ĐVVC</span>
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
