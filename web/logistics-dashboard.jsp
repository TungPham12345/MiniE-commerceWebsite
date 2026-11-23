<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.User, model.Order, java.util.List, java.text.DecimalFormat" %>
<%
    User currentUser = (User) session.getAttribute("user");
    if (currentUser == null || (!currentUser.isLogistics() && !currentUser.isAdmin())) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    List<Order> orders = (List<Order>) request.getAttribute("logisticsOrders");
    Integer countHandover = (Integer) request.getAttribute("countHandover");
    Integer countReceived = (Integer) request.getAttribute("countReceived");
    Integer countChecked = (Integer) request.getAttribute("countChecked");
    Integer countInTransit = (Integer) request.getAttribute("countInTransit");
    Integer countArrived = (Integer) request.getAttribute("countArrived");

    if (countHandover == null) countHandover = 0;
    if (countReceived == null) countReceived = 0;
    if (countChecked == null) countChecked = 0;
    if (countInTransit == null) countInTransit = 0;
    if (countArrived == null) countArrived = 0;

    String toastSuccess = (String) session.getAttribute("toastSuccess");
    session.removeAttribute("toastSuccess");
    DecimalFormat df = new DecimalFormat("#,###");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cổng Điều Phối ĐVVC Express</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
    <style>
        body { background-color: #0b1329; color: #f8fafc; font-family: system-ui, -apple-system, sans-serif; }
        .portal-header { background: #1c2541; border-bottom: 1px solid #3a506b; }
        .card-stat { background: #1c2541; border: 1px solid #3a506b; border-radius: 12px; }
        .portal-table { background: #1c2541; border: 1px solid #3a506b; border-radius: 12px; overflow: hidden; }
        .portal-table table { color: #f8fafc; margin-bottom: 0; }
        .portal-table th { background: #3a506b; color: #cbd5e1; border-color: #5bc0be; font-size: 0.85rem; text-transform: uppercase; }
        .portal-table td { border-color: #3a506b; vertical-align: middle; }
    </style>
</head>
<body>
    <header class="portal-header py-3 px-4 mb-4">
        <div class="d-flex align-items-center justify-content-between">
            <div class="d-flex align-items-center gap-3">
                <span class="fs-4 fw-bold text-warning"><i class="bi bi-truck me-2"></i>TRUNG TÂM KHAI THÁC ĐVVC EXPRESS</span>
                <span class="badge bg-info text-dark px-3 py-1 rounded-pill">Hub Trung Chuyển Liên Tỉnh</span>
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
            <div class="col">
                <div class="card card-stat p-3 text-center">
                    <div class="text-muted small fw-bold">1. CHỜ TIẾP NHẬN</div>
                    <div class="fs-3 fw-bold text-warning"><%= countHandover %></div>
                </div>
            </div>
            <div class="col">
                <div class="card card-stat p-3 text-center">
                    <div class="text-muted small fw-bold">2. ĐÃ TIẾP NHẬN</div>
                    <div class="fs-3 fw-bold text-info"><%= countReceived %></div>
                </div>
            </div>
            <div class="col">
                <div class="card card-stat p-3 text-center">
                    <div class="text-muted small fw-bold">3. ĐÃ KIỂM TRA</div>
                    <div class="fs-3 fw-bold text-primary"><%= countChecked %></div>
                </div>
            </div>
            <div class="col">
                <div class="card card-stat p-3 text-center">
                    <div class="text-muted small fw-bold">4. ĐANG VẬN CHUYỂN</div>
                    <div class="fs-3 fw-bold text-primary"><%= countInTransit %></div>
                </div>
            </div>
            <div class="col">
                <div class="card card-stat p-3 text-center">
                    <div class="text-muted small fw-bold">5. ĐÃ ĐẾN BƯU CỤC</div>
                    <div class="fs-3 fw-bold text-success"><%= countArrived %></div>
                </div>
            </div>
        </div>

        <div class="portal-table p-3">
            <h5 class="fw-bold mb-3"><i class="bi bi-geo-alt-fill me-2 text-warning"></i>Quản Lý Vận Chuyển Đơn Hàng (Linehaul)</h5>
            <div class="table-responsive">
                <table class="table table-hover align-middle">
                    <thead>
                        <tr>
                            <th>Mã Đơn</th>
                            <th>Khách Hàng & Điểm Đến</th>
                            <th>COD Cần Thu</th>
                            <th>Trạng Thái Vận Chuyển</th>
                            <th class="text-end">Hành Động ĐVVC</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (orders == null || orders.isEmpty()) { %>
                            <tr><td colspan="5" class="text-center py-4 text-muted">Không có đơn hàng nào cần điều phối vận chuyển.</td></tr>
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
                                        <% if ("HANDOVER_PENDING".equals(st)) { %>
                                            <a href="${pageContext.request.contextPath}/logistics/dashboard?action=updateStatus&orderId=<%= o.getId() %>&status=CARRIER_RECEIVED" 
                                               class="btn btn-sm btn-warning rounded-pill fw-bold px-3">
                                                <i class="bi bi-box-arrow-in-right me-1"></i>Tiếp Nhận Kiện Hàng
                                            </a>
                                        <% } else if ("CARRIER_RECEIVED".equals(st)) { %>
                                            <a href="${pageContext.request.contextPath}/logistics/dashboard?action=updateStatus&orderId=<%= o.getId() %>&status=CARRIER_CHECKED" 
                                               class="btn btn-sm btn-info rounded-pill fw-bold px-3 text-dark">
                                                <i class="bi bi-shield-check me-1"></i>Xác Nhận Kiểm Tra / Cân Đo
                                            </a>
                                        <% } else if ("CARRIER_CHECKED".equals(st)) { %>
                                            <a href="${pageContext.request.contextPath}/logistics/dashboard?action=updateStatus&orderId=<%= o.getId() %>&status=IN_TRANSIT" 
                                               class="btn btn-sm btn-primary rounded-pill fw-bold px-3">
                                                <i class="bi bi-truck me-1"></i>Tiến Hành Vận Chuyển
                                            </a>
                                        <% } else if ("IN_TRANSIT".equals(st)) { %>
                                            <a href="${pageContext.request.contextPath}/logistics/dashboard?action=updateStatus&orderId=<%= o.getId() %>&status=ARRIVED_HUB" 
                                               class="btn btn-sm btn-success rounded-pill fw-bold px-3">
                                                <i class="bi bi-building-down me-1"></i>Chuyển Đến Bưu Cục Phát
                                            </a>
                                        <% } else if ("ARRIVED_HUB".equals(st)) { %>
                                            <span class="text-success small fw-semibold"><i class="bi bi-check-circle me-1"></i>Đã tới bưu cục phát</span>
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
