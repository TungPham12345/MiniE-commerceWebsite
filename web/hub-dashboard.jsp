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
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
    <style>
        body { background-color: #111827; color: #f9fafb; font-family: system-ui, -apple-system, sans-serif; }
        .portal-header { background: #1f2937; border-bottom: 1px solid #374151; }
        .card-stat { background: #1f2937; border: 1px solid #374151; border-radius: 12px; }
        .portal-table { background: #1f2937; border: 1px solid #374151; border-radius: 12px; overflow: hidden; }
        .portal-table table { color: #f9fafb; margin-bottom: 0; }
        .portal-table th { background: #374151; color: #d1d5db; font-size: 0.85rem; text-transform: uppercase; }
        .portal-table td { border-color: #374151; vertical-align: middle; }
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
</body>
</html>
