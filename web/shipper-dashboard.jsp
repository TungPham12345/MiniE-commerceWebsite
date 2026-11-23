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
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
    <style>
        body { background-color: #0d1117; color: #f0f6fc; font-family: system-ui, -apple-system, sans-serif; }
        .portal-header { background: #161b22; border-bottom: 1px solid #30363d; }
        .order-card { background: #161b22; border: 1px solid #30363d; border-radius: 12px; margin-bottom: 1rem; }
        .stat-badge { background: #21262d; border: 1px solid #30363d; border-radius: 8px; padding: 0.5rem 1rem; }
    </style>
</head>
<body>
    <header class="portal-header py-3 px-3 px-md-4 mb-3">
        <div class="d-flex align-items-center justify-content-between">
            <div class="d-flex align-items-center gap-2">
                <i class="bi bi-bicycle text-danger fs-3"></i>
                <div>
                    <div class="fw-bold fs-5 text-white">SHIPPER APP</div>
                    <div class="small text-muted">Tuyến Giao Quận 5</div>
                </div>
            </div>
            <div class="d-flex align-items-center gap-3">
                <span class="d-none d-md-inline fw-semibold"><i class="bi bi-person-fill text-warning me-1"></i><%= currentUser.getFullname() %></span>
                <a href="${pageContext.request.contextPath}/logout" class="btn btn-sm btn-outline-danger rounded-pill"><i class="bi bi-box-arrow-right"></i></a>
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
</body>
</html>
