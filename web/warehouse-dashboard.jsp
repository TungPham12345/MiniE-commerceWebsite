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
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
    <style>
        body { background-color: #0f172a; color: #f8fafc; font-family: system-ui, -apple-system, sans-serif; }
        .portal-header { background: #1e293b; border-bottom: 1px solid #334155; }
        .card-stat { background: #1e293b; border: 1px solid #334155; border-radius: 12px; }
        .portal-table { background: #1e293b; border: 1px solid #334155; border-radius: 12px; overflow: hidden; }
        .portal-table table { color: #f8fafc; margin-bottom: 0; }
        .portal-table th { background: #334155; color: #cbd5e1; border-color: #475569; font-size: 0.85rem; text-transform: uppercase; }
        .portal-table td { border-color: #334155; vertical-align: middle; }
        .nav-tabs .nav-link { color: #94a3b8; border: none; font-weight: 600; }
        .nav-tabs .nav-link.active { color: #38bdf8; background: transparent; border-bottom: 3px solid #38bdf8; }
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
</body>
</html>
