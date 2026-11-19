<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, model.Order, model.OrderDetail, java.text.NumberFormat, java.util.Locale" %>
<%
    request.setAttribute("adminActivePage", "dashboard");
    Integer productsCount = (Integer) request.getAttribute("productsCount");
    Integer ordersCount = (Integer) request.getAttribute("ordersCount");
    Integer pendingOrdersCount = (Integer) request.getAttribute("pendingOrdersCount");
    Double totalRevenueObj = (Double) request.getAttribute("totalRevenue");
    double totalRevenue = totalRevenueObj != null ? totalRevenueObj : 0;
    List<Order> recentOrders = (List<Order>) request.getAttribute("recentOrders");
    NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));

    String toastSuccess = (String) session.getAttribute("toastSuccess");
    if (toastSuccess != null) { session.removeAttribute("toastSuccess"); }
    String toastError = (String) session.getAttribute("toastError");
    if (toastError != null) { session.removeAttribute("toastError"); }
    String toastInfo = (String) session.getAttribute("toastInfo");
    if (toastInfo != null) { session.removeAttribute("toastInfo"); }
%>

<jsp:include page="includes/header.jsp" />
<jsp:include page="includes/navbar.jsp" />

<div class="admin-dashboard-page">
    <!-- Thông báo kết quả thao tác duyệt / hủy đơn hoặc thông báo điều hướng vai trò -->
    <% if (toastInfo != null) { %>
        <div class="alert alert-info alert-dismissible fade show rounded-4 shadow-sm mb-4 d-flex align-items-center" role="alert">
            <i class="bi bi-info-circle-fill fs-4 me-3 text-info"></i>
            <div class="fw-bold"><%= toastInfo %></div>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    <% } %>
    <% if (toastSuccess != null) { %>
        <div class="alert alert-success alert-dismissible fade show rounded-4 shadow-sm mb-4 d-flex align-items-center" role="alert">
            <i class="bi bi-check-circle-fill fs-4 me-3 text-success"></i>
            <div class="fw-bold"><%= toastSuccess %></div>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    <% } %>
    <% if (toastError != null) { %>
        <div class="alert alert-danger alert-dismissible fade show rounded-4 shadow-sm mb-4 d-flex align-items-center" role="alert">
            <i class="bi bi-exclamation-triangle-fill fs-4 me-3 text-danger"></i>
            <div class="fw-bold"><%= toastError %></div>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    <% } %>

    <div class="d-flex justify-content-between align-items-center bg-dark text-white p-4 rounded-4 shadow-sm mb-4">
        <div>
            <span class="badge bg-danger mb-1">ADMINISTRATOR CONTROL PANEL</span>
            <h2 class="fw-bold m-0 text-white">Bảng Quản Trị Hệ Thống</h2>
        </div>
    </div>

    <!-- Stat Cards -->
    <div class="row g-4 mb-5">
        <div class="col-md-3">
            <div class="bg-white p-4 rounded-4 border shadow-sm">
                <div class="d-flex align-items-center justify-content-between">
                    <div>
                        <span class="text-muted small fw-bold">TỔNG SẢN PHẨM</span>
                        <h2 class="fw-extrabold text-dark m-0 mt-1"><%= productsCount != null ? productsCount : 0 %></h2>
                    </div>
                    <div class="bg-primary bg-opacity-10 p-3 rounded-circle text-primary">
                        <i class="bi bi-box-seam fs-3"></i>
                    </div>
                </div>
            </div>
        </div>

        <div class="col-md-3">
            <div class="bg-white p-4 rounded-4 border shadow-sm">
                <div class="d-flex align-items-center justify-content-between">
                    <div>
                        <span class="text-muted small fw-bold">TỔNG ĐƠN HÀNG</span>
                        <h2 class="fw-extrabold text-dark m-0 mt-1"><%= ordersCount != null ? ordersCount : 0 %></h2>
                    </div>
                    <div class="bg-success bg-opacity-10 p-3 rounded-circle text-success">
                        <i class="bi bi-receipt fs-3"></i>
                    </div>
                </div>
            </div>
        </div>

        <div class="col-md-3">
            <div class="bg-white p-4 rounded-4 border shadow-sm">
                <div class="d-flex align-items-center justify-content-between">
                    <div>
                        <span class="text-muted small fw-bold">ĐƠN CHỜ XÁC NHẬN</span>
                        <h2 class="fw-extrabold text-warning m-0 mt-1"><%= pendingOrdersCount != null ? pendingOrdersCount : 0 %></h2>
                    </div>
                    <div class="bg-warning bg-opacity-10 p-3 rounded-circle text-warning">
                        <i class="bi bi-hourglass-split fs-3"></i>
                    </div>
                </div>
            </div>
        </div>

        <div class="col-md-3">
            <div class="bg-white p-4 rounded-4 border shadow-sm">
                <div class="d-flex align-items-center justify-content-between">
                    <div>
                        <span class="text-muted small fw-bold">TỔNG DOANH THU</span>
                        <h3 class="fw-extrabold text-danger m-0 mt-1">
                            <%= fmt.format(totalRevenue) %> đ
                        </h3>
                    </div>
                    <div class="bg-danger bg-opacity-10 p-3 rounded-circle text-danger">
                        <i class="bi bi-currency-dollar fs-3"></i>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Recent Orders Table -->
    <div class="bg-white p-4 rounded-4 border shadow-sm">
        <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
            <h4 class="fw-bold m-0"><i class="bi bi-clock-history me-2 text-danger"></i>Đơn Hàng Gần Đây</h4>
            <div class="d-flex align-items-center gap-2">
                <span class="badge bg-light text-secondary border px-3 py-2 rounded-pill">
                    Hiển thị <%= recentOrders != null ? recentOrders.size() : 0 %> đơn hàng
                </span>
                <% if (recentOrders != null && !recentOrders.isEmpty()) { %>
                    <a href="${pageContext.request.contextPath}/admin/dashboard?action=deleteAll" 
                       class="btn btn-outline-danger btn-sm rounded-pill px-3 py-1 fw-bold"
                       data-confirm-message="CẢNH BÁO: Bạn có chắc chắn muốn xóa toàn bộ đơn hàng trong hệ thống không?"
                       data-confirm-label="Xóa tất cả" data-confirm-variant="danger">
                        <i class="bi bi-trash3 me-1"></i>Xóa tất cả đơn
                    </a>
                <% } %>
            </div>
        </div>
        <div class="table-responsive">
            <table class="table table-hover align-middle admin-orders-table">
                <colgroup>
                    <col style="width: 78px">
                    <col style="width: 140px">
                    <col style="width: 190px">
                    <col style="width: 118px">
                    <col style="width: 170px">
                    <col style="width: 96px">
                    <col style="width: 90px">
                    <col style="width: 148px">
                    <col style="width: 230px">
                </colgroup>
                <thead class="table-light">
                    <tr>
                        <th class="admin-order-id">Mã Đơn</th>
                        <th class="admin-order-customer">Khách Hàng</th>
                        <th class="admin-order-email">Email</th>
                        <th class="admin-order-phone">Số Điện Thoại</th>
                        <th class="admin-order-address">Địa Chỉ</th>
                        <th class="admin-order-total">Tổng Tiền</th>
                        <th class="admin-order-payment">Thanh Toán</th>
                        <th class="admin-order-status">Trạng Thái</th>
                        <th class="admin-order-actions text-end">Thao Tác</th>
                    </tr>
                </thead>
                <tbody>
                    <% if (recentOrders != null && !recentOrders.isEmpty()) {
                        for (Order o : recentOrders) { %>
                            <tr>
                                <td class="admin-order-id fw-bold text-dark">
                                    <a href="javascript:void(0)" class="text-decoration-none text-dark fw-bold" data-bs-toggle="modal" data-bs-target="#orderModal<%= o.getId() %>">
                                        #<%= o.getId() %>
                                    </a>
                                </td>
                                <td class="admin-order-customer">
                                    <div class="fw-semibold text-dark"><%= o.getCustomerName() %></div>
                                </td>
                                <td class="admin-order-email small text-muted">
                                    <%= o.getEmail() != null && !o.getEmail().isEmpty() ? o.getEmail() : "—" %>
                                </td>
                                <td class="admin-order-phone fw-medium"><%= o.getPhone() %></td>
                                <td class="admin-order-address small text-muted"><%= o.getAddress() %></td>
                                <td class="admin-order-total fw-bold text-danger"><%= fmt.format(o.getTotalMoney()) %> đ</td>
                                <td class="admin-order-payment"><span class="badge bg-secondary"><%= o.getPaymentMethod() %></span></td>
                                <td class="admin-order-status">
                                    <% 
                                        String ost = o.getStatus() != null ? o.getStatus().toUpperCase() : "";
                                        if ("DELIVERED".equals(ost) || "COMPLETED".equals(ost) || "DA_GIAO".equals(ost)) { 
                                    %>
                                        <span class="badge bg-success px-3 py-2 rounded-pill"><i class="bi bi-check-circle-fill me-1"></i>Đã giao</span>
                                    <% } else if ("CONFIRMED".equals(ost) || "CHO_LAY_HANG".equals(ost) || "PROCESSING".equals(ost)) { %>
                                        <span class="badge bg-info text-dark px-3 py-2 rounded-pill"><i class="bi bi-box-seam me-1"></i>Chờ lấy hàng</span>
                                    <% } else if ("SHIPPING".equals(ost) || "CHO_GIAO_HANG".equals(ost) || "VAN_CHUYEN".equals(ost)) { %>
                                        <span class="badge bg-primary px-3 py-2 rounded-pill"><i class="bi bi-truck me-1"></i>Chờ giao hàng</span>
                                    <% } else if ("RETURNED".equals(ost) || "REFUNDED".equals(ost) || "TRA_HANG".equals(ost)) { %>
                                        <span class="badge bg-secondary px-3 py-2 rounded-pill"><i class="bi bi-arrow-counterclockwise me-1"></i>Trả hàng</span>
                                    <% } else if ("CANCELLED".equals(ost) || "DA_HUY".equals(ost)) { %>
                                        <span class="badge bg-danger px-3 py-2 rounded-pill"><i class="bi bi-x-circle-fill me-1"></i>Đã hủy</span>
                                    <% } else { %>
                                        <span class="badge bg-warning text-dark px-3 py-2 rounded-pill"><i class="bi bi-hourglass-split me-1"></i>Chờ xác nhận</span>
                                    <% } %>
                                </td>
                                <td class="admin-order-actions text-end">
                                    <div class="admin-order-action-group d-inline-flex align-items-center gap-1">
                                        <!-- Nút Xem Chi Tiết -->
                                        <button type="button" class="btn btn-sm btn-outline-secondary rounded-pill px-2 py-1" 
                                                data-bs-toggle="modal" data-bs-target="#orderModal<%= o.getId() %>" title="Xem chi tiết đơn hàng">
                                            <i class="bi bi-eye"></i>
                                        </button>

                                        <!-- Thao tác Duyệt / Chuyển tiếp theo trạng thái -->
                                        <% if ("PENDING".equals(ost) || "CHO_XAC_NHAN".equals(ost) || "UNPAID".equals(ost)) { %>
                                            <!-- 1. Đơn mới đặt -> Admin xác nhận đơn -> Chuyển sang Chờ lấy hàng -->
                                            <a href="${pageContext.request.contextPath}/admin/dashboard?action=updateStatus&orderId=<%= o.getId() %>&status=CONFIRMED" 
                                               class="btn btn-sm btn-success rounded-pill fw-bold px-2 py-1 shadow-sm text-nowrap"
                                               data-confirm-message="Xác nhận duyệt đơn hàng #<%= o.getId() %> và chuyển sang 'Chờ lấy hàng'?"
                                               data-confirm-label="Xác nhận đơn" data-confirm-variant="success">
                                                <i class="bi bi-check-lg me-1"></i>Xác nhận
                                            </a>
                                            <a href="${pageContext.request.contextPath}/admin/dashboard?action=updateStatus&orderId=<%= o.getId() %>&status=CANCELLED" 
                                               class="btn btn-sm btn-outline-danger rounded-pill px-2 py-1"
                                               data-confirm-message="Bạn có chắc chắn muốn hủy đơn hàng #<%= o.getId() %>?"
                                               data-confirm-label="Hủy đơn" data-confirm-variant="danger" title="Hủy đơn hàng">
                                                <i class="bi bi-x-lg"></i>
                                            </a>
                                        <% } else if ("CONFIRMED".equals(ost) || "CHO_LAY_HANG".equals(ost) || "PROCESSING".equals(ost)) { %>
                                            <!-- 2. Đã xác nhận -> Giao cho đơn vị vận chuyển -> Chờ giao hàng -->
                                            <a href="${pageContext.request.contextPath}/admin/dashboard?action=updateStatus&orderId=<%= o.getId() %>&status=SHIPPING" 
                                               class="btn btn-sm btn-primary rounded-pill fw-bold px-3 py-1 shadow-sm"
                                               data-confirm-message="Giao đơn hàng #<%= o.getId() %> cho ĐVVC (Chuyển sang 'Chờ giao hàng')?"
                                               data-confirm-label="Giao cho ĐVVC" data-confirm-variant="primary">
                                                <i class="bi bi-truck me-1"></i>Giao hàng
                                            </a>
                                            <a href="${pageContext.request.contextPath}/admin/dashboard?action=updateStatus&orderId=<%= o.getId() %>&status=CANCELLED" 
                                               class="btn btn-sm btn-outline-danger rounded-pill px-2 py-1"
                                               data-confirm-message="Bạn có chắc muốn hủy đơn hàng #<%= o.getId() %>?"
                                               data-confirm-label="Hủy đơn" data-confirm-variant="danger" title="Hủy đơn hàng">
                                                <i class="bi bi-x-lg"></i>
                                            </a>
                                        <% } else if ("SHIPPING".equals(ost) || "CHO_GIAO_HANG".equals(ost) || "VAN_CHUYEN".equals(ost)) { %>
                                            <!-- 3. Đang giao -> Đã giao thành công -->
                                            <a href="${pageContext.request.contextPath}/admin/dashboard?action=updateStatus&orderId=<%= o.getId() %>&status=DELIVERED" 
                                               class="btn btn-sm btn-success rounded-pill fw-bold px-3 py-1 shadow-sm"
                                               data-confirm-message="Xác nhận đơn hàng #<%= o.getId() %> đã giao thành công?"
                                               data-confirm-label="Đã giao" data-confirm-variant="success">
                                                <i class="bi bi-check2-circle me-1"></i>Đã giao
                                            </a>
                                            <a href="${pageContext.request.contextPath}/admin/dashboard?action=updateStatus&orderId=<%= o.getId() %>&status=RETURNED" 
                                               class="btn btn-sm btn-outline-warning rounded-pill px-2 py-1"
                                               data-confirm-message="Giao thất bại hoặc khách trả hàng? Chuyển đơn #<%= o.getId() %> sang Trả hàng?"
                                               data-confirm-label="Trả hàng" data-confirm-variant="warning" title="Chuyển sang Trả hàng">
                                                <i class="bi bi-arrow-counterclockwise"></i>
                                            </a>
                                        <% } else if ("DELIVERED".equals(ost) || "COMPLETED".equals(ost) || "DA_GIAO".equals(ost)) { %>
                                            <a href="${pageContext.request.contextPath}/admin/dashboard?action=updateStatus&orderId=<%= o.getId() %>&status=RETURNED" 
                                               class="btn btn-sm btn-outline-warning rounded-pill px-2 py-1"
                                               data-confirm-message="Khách trả hàng? Chuyển đơn hàng #<%= o.getId() %> sang Trả hàng?"
                                               data-confirm-label="Trả hàng" data-confirm-variant="warning" title="Trả hàng">
                                                <i class="bi bi-arrow-counterclockwise"></i>
                                            </a>
                                        <% } else if ("RETURNED".equals(ost) || "REFUNDED".equals(ost) || "TRA_HANG".equals(ost)) { %>
                                            <a href="${pageContext.request.contextPath}/admin/dashboard?action=updateStatus&orderId=<%= o.getId() %>&status=PENDING" 
                                               class="btn btn-sm btn-outline-primary rounded-pill px-2 py-1"
                                               data-confirm-message="Khôi phục đơn hàng #<%= o.getId() %> về Chờ xác nhận?"
                                               data-confirm-label="Khôi phục" data-confirm-variant="primary" title="Khôi phục về Chờ xác nhận">
                                                <i class="bi bi-arrow-counterclockwise"></i>
                                            </a>
                                        <% } else { %>
                                            <a href="${pageContext.request.contextPath}/admin/dashboard?action=updateStatus&orderId=<%= o.getId() %>&status=PENDING" 
                                               class="btn btn-sm btn-outline-primary rounded-pill px-2 py-1"
                                               data-confirm-message="Khôi phục đơn hàng #<%= o.getId() %> về Chờ xác nhận?"
                                               data-confirm-label="Khôi phục" data-confirm-variant="primary" title="Khôi phục về Chờ xác nhận">
                                                <i class="bi bi-arrow-counterclockwise"></i>
                                            </a>
                                        <% } %>

                                        <!-- Nút Xóa Đơn Hàng -->
                                        <a href="${pageContext.request.contextPath}/admin/dashboard?action=delete&orderId=<%= o.getId() %>" 
                                           class="btn btn-sm btn-outline-secondary rounded-pill px-2 py-1 ms-1 admin-order-delete-btn"
                                           data-confirm-message="Bạn có chắc muốn xóa vĩnh viễn đơn hàng #<%= o.getId() %> khỏi hệ thống?"
                                           data-confirm-label="Xóa đơn" data-confirm-variant="danger" title="Xóa vĩnh viễn đơn hàng">
                                            <i class="bi bi-trash3"></i>
                                        </a>
                                    </div>
                                </td>
                            </tr>
                    <%  }
                       } else { %>
                             <tr>
                                <td colspan="9" class="text-center py-5">
                                    <div class="py-4">
                                        <div class="bg-light d-inline-flex p-4 rounded-circle mb-3 shadow-sm border text-muted">
                                            <i class="bi bi-inbox fs-1 text-secondary"></i>
                                        </div>
                                        <h5 class="fw-bold text-dark mb-1">Chưa có đơn hàng nào trong hệ thống</h5>
                                        <p class="text-muted small mb-0">Khi khách hàng tiến hành đặt hàng trên website, danh sách đơn hàng sẽ xuất hiện tại đây để bạn kiểm tra và duyệt đơn.</p>
                                    </div>
                                </td>
                            </tr>
                    <% } %>
                </tbody>
            </table>
        </div>
    </div>

    <!-- Modals Chi Tiết Đơn Hàng (Đặt ngoài Table để đúng chuẩn HTML và không bị vỡ layout) -->
    <% if (recentOrders != null && !recentOrders.isEmpty()) {
        for (Order o : recentOrders) { %>
            <div class="modal fade" id="orderModal<%= o.getId() %>" tabindex="-1" aria-hidden="true">
                <div class="modal-dialog modal-dialog-centered modal-lg">
                    <div class="modal-content rounded-4 border-0 shadow">
                        <div class="modal-header bg-dark text-white rounded-top-4">
                            <h5 class="modal-title fw-bold">
                                <i class="bi bi-receipt me-2 text-danger"></i>Chi Tiết Đơn Hàng #<%= o.getId() %>
                            </h5>
                            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                        </div>
                        <div class="modal-body p-4 text-start">
                            <!-- Thông tin khách hàng & vận chuyển -->
                            <div class="row g-3 mb-4">
                                <div class="col-md-6">
                                    <div class="p-3 bg-light rounded-3 border">
                                        <h6 class="fw-bold mb-2 text-danger"><i class="bi bi-person-fill me-1"></i> Thông Tin Khách Hàng</h6>
                                        <div><strong>Họ tên:</strong> <%= o.getCustomerName() %></div>
                                        <div><strong>Số điện thoại:</strong> <%= o.getPhone() %></div>
                                        <div><strong>Email:</strong> <%= o.getEmail() != null ? o.getEmail() : "Chưa cung cấp" %></div>
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <div class="p-3 bg-light rounded-3 border">
                                        <h6 class="fw-bold mb-2 text-danger"><i class="bi bi-truck me-1"></i> Thông Tin Giao Hàng</h6>
                                        <div><strong>Địa chỉ:</strong> <%= o.getAddress() %></div>
                                        <div><strong>Thanh toán:</strong> <%= o.getPaymentMethod() %></div>
                                        <div><strong>Ghi chú:</strong> <%= (o.getNote() != null && !o.getNote().isEmpty()) ? o.getNote() : "Không có" %></div>
                                    </div>
                                </div>
                            </div>

                            <!-- Danh sách sản phẩm mua -->
                            <h6 class="fw-bold mb-2"><i class="bi bi-bag-check-fill text-danger me-1"></i> Danh Sách Sản Phẩm Mua</h6>
                            <div class="table-responsive border rounded-3 mb-3">
                                <table class="table table-hover align-middle m-0">
                                    <thead class="table-light">
                                        <tr>
                                            <th>Sản Phẩm</th>
                                            <th>Phân Loại</th>
                                            <th class="text-center">Số Lượng</th>
                                            <th class="text-end">Đơn Giá</th>
                                            <th class="text-end">Thành Tiền</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <% if (o.getOrderDetails() != null && !o.getOrderDetails().isEmpty()) {
                                            for (OrderDetail d : o.getOrderDetails()) { %>
                                                <tr>
                                                    <td>
                                                        <div class="d-flex align-items-center gap-2">
                                                            <% if (d.getProductImage() != null && !d.getProductImage().isEmpty()) {
                                                                String orderImg = d.getProductImage();
                                                                if (!orderImg.startsWith("http://") && !orderImg.startsWith("https://")
                                                                        && !orderImg.startsWith(request.getContextPath())) {
                                                                    orderImg = request.getContextPath() + (orderImg.startsWith("/") ? "" : "/") + orderImg;
                                                                }
                                                            %>
                                                                <img src="<%= orderImg %>" alt="Ảnh sản phẩm"
                                                                     class="rounded border flex-shrink-0"
                                                                     style="width: 45px; height: 45px; object-fit: cover;"
                                                                     onerror="this.onerror=null;this.src='https://images.unsplash.com/photo-1523381210434-271e8be1f52b?w=600';">
                                                            <% } else { %>
                                                                <div class="bg-secondary rounded text-white d-flex align-items-center justify-content-center flex-shrink-0" style="width: 45px; height: 45px;">
                                                                    <i class="bi bi-image"></i>
                                                                </div>
                                                            <% } %>
                                                            <span class="fw-semibold" style="min-width: 0;"><%= d.getProductName() != null ? d.getProductName() : ("Sản phẩm #" + d.getProductId()) %></span>
                                                        </div>
                                                    </td>
                                                    <td>
                                                        <span class="badge bg-light text-dark border">Size: <%= d.getSize() %></span>
                                                        <span class="badge bg-light text-dark border">Màu: <%= d.getColor() %></span>
                                                    </td>
                                                    <td class="text-center fw-bold"><%= d.getQuantity() %></td>
                                                    <td class="text-end text-nowrap"><%= fmt.format(d.getPrice()) %>&nbsp;đ</td>
                                                    <td class="text-end text-nowrap fw-bold text-danger"><%= fmt.format(d.getTotalMoney()) %>&nbsp;đ</td>
                                                </tr>
                                        <%  }
                                           } else { %>
                                                <tr>
                                                    <td colspan="5" class="text-center text-muted py-3">Không có chi tiết mặt hàng</td>
                                                </tr>
                                        <% } %>
                                    </tbody>
                                </table>
                            </div>

                            <div class="d-flex justify-content-between align-items-center p-3 bg-light rounded-3 border">
                                <span class="fw-bold fs-6">TỔNG CỘNG THANH TOÁN:</span>
                                <span class="fw-extrabold text-danger fs-5 text-nowrap"><%= fmt.format(o.getTotalMoney()) %>&nbsp;đ</span>
                            </div>
                        </div>
                        <div class="modal-footer bg-light rounded-bottom-4 d-flex justify-content-between flex-wrap gap-2">
                            <div>
                                <span class="small text-muted me-1">Trạng thái:</span>
                                <span class="badge <%= o.getStatusBadgeClass() %> px-3 py-1 fw-bold"><%= o.getStatusVietnamese() %></span>
                            </div>
                            <div class="d-flex gap-2 flex-wrap">
                                <% if ("PENDING".equalsIgnoreCase(o.getStatus()) || "CHO_XAC_NHAN".equalsIgnoreCase(o.getStatus()) || "UNPAID".equalsIgnoreCase(o.getStatus())) { %>
                                    <a href="${pageContext.request.contextPath}/admin/dashboard?action=updateStatus&orderId=<%= o.getId() %>&status=CONFIRMED" 
                                       class="btn btn-success rounded-pill fw-bold px-3"
                                       data-confirm-message="Xác nhận đơn hàng #<%= o.getId() %> và chuyển sang 'Chờ lấy hàng'?"
                                       data-confirm-label="Xác nhận đơn" data-confirm-variant="success">
                                        <i class="bi bi-check-lg me-1"></i>Xác Nhận Đơn
                                    </a>
                                    <a href="${pageContext.request.contextPath}/admin/dashboard?action=updateStatus&orderId=<%= o.getId() %>&status=CANCELLED" 
                                       class="btn btn-outline-danger rounded-pill fw-medium px-3"
                                       data-confirm-message="Bạn có chắc chắn muốn hủy đơn hàng #<%= o.getId() %>?"
                                       data-confirm-label="Hủy đơn" data-confirm-variant="danger">
                                        <i class="bi bi-x-lg me-1"></i>Hủy Đơn
                                    </a>
                                <% } else if ("CONFIRMED".equalsIgnoreCase(o.getStatus()) || "CHO_LAY_HANG".equalsIgnoreCase(o.getStatus()) || "PROCESSING".equalsIgnoreCase(o.getStatus())) { %>
                                    <a href="${pageContext.request.contextPath}/admin/dashboard?action=updateStatus&orderId=<%= o.getId() %>&status=SHIPPING" 
                                       class="btn btn-primary rounded-pill fw-bold px-3"
                                       data-confirm-message="Giao đơn hàng #<%= o.getId() %> cho ĐVVC (Chuyển sang 'Chờ giao hàng')?"
                                       data-confirm-label="Giao cho ĐVVC" data-confirm-variant="primary">
                                        <i class="bi bi-truck me-1"></i>Giao Cho ĐVVC
                                    </a>
                                    <a href="${pageContext.request.contextPath}/admin/dashboard?action=updateStatus&orderId=<%= o.getId() %>&status=CANCELLED" 
                                       class="btn btn-outline-danger rounded-pill fw-medium px-3"
                                       data-confirm-message="Hủy đơn hàng #<%= o.getId() %>?"
                                       data-confirm-label="Hủy đơn" data-confirm-variant="danger">
                                        <i class="bi bi-x-lg me-1"></i>Hủy Đơn
                                    </a>
                                <% } else if ("SHIPPING".equalsIgnoreCase(o.getStatus()) || "CHO_GIAO_HANG".equalsIgnoreCase(o.getStatus()) || "VAN_CHUYEN".equalsIgnoreCase(o.getStatus())) { %>
                                    <a href="${pageContext.request.contextPath}/admin/dashboard?action=updateStatus&orderId=<%= o.getId() %>&status=DELIVERED" 
                                       class="btn btn-success rounded-pill fw-bold px-3"
                                       data-confirm-message="Xác nhận đơn hàng #<%= o.getId() %> đã giao thành công?"
                                       data-confirm-label="Đã giao" data-confirm-variant="success">
                                        <i class="bi bi-check2-circle me-1"></i>Xác Nhận Đã Giao
                                    </a>
                                    <a href="${pageContext.request.contextPath}/admin/dashboard?action=updateStatus&orderId=<%= o.getId() %>&status=RETURNED" 
                                       class="btn btn-outline-warning rounded-pill fw-medium px-3"
                                       data-confirm-message="Chuyển đơn hàng #<%= o.getId() %> sang Trả hàng?"
                                       data-confirm-label="Trả hàng" data-confirm-variant="warning">
                                        <i class="bi bi-arrow-counterclockwise me-1"></i>Trả Hàng
                                    </a>
                                <% } else if ("DELIVERED".equalsIgnoreCase(o.getStatus()) || "COMPLETED".equalsIgnoreCase(o.getStatus()) || "DA_GIAO".equalsIgnoreCase(o.getStatus())) { %>
                                    <a href="${pageContext.request.contextPath}/admin/dashboard?action=updateStatus&orderId=<%= o.getId() %>&status=RETURNED" 
                                       class="btn btn-outline-warning rounded-pill fw-medium px-3"
                                       data-confirm-message="Khách trả hàng? Chuyển đơn hàng #<%= o.getId() %> sang Trả hàng?"
                                       data-confirm-label="Trả hàng" data-confirm-variant="warning">
                                        <i class="bi bi-arrow-counterclockwise me-1"></i>Chuyển Sang Trả Hàng
                                    </a>
                                <% } else { %>
                                    <a href="${pageContext.request.contextPath}/admin/dashboard?action=updateStatus&orderId=<%= o.getId() %>&status=PENDING" 
                                       class="btn btn-outline-primary rounded-pill fw-medium px-3"
                                       data-confirm-message="Khôi phục đơn hàng #<%= o.getId() %> về Chờ xác nhận?"
                                       data-confirm-label="Khôi phục" data-confirm-variant="primary">
                                        <i class="bi bi-arrow-counterclockwise me-1"></i>Khôi Phục Về Chờ Xác Nhận
                                    </a>
                                <% } %>
                                <button type="button" class="btn btn-secondary rounded-pill px-4" data-bs-dismiss="modal">Đóng</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
    <%  }
       } %>

    <dialog id="orderActionConfirm" class="order-confirm-dialog rounded-4 border-0 shadow p-0"
            aria-labelledby="orderConfirmTitle" aria-describedby="orderConfirmMessage">
        <div class="p-4">
            <div class="d-flex align-items-center gap-3 mb-3">
                <span class="order-confirm-icon rounded-circle d-flex align-items-center justify-content-center text-warning bg-warning bg-opacity-10">
                    <i class="bi bi-exclamation-circle-fill fs-4"></i>
                </span>
                <h5 id="orderConfirmTitle" class="fw-bold mb-0">Xác nhận thao tác</h5>
            </div>
            <p id="orderConfirmMessage" class="text-body-secondary mb-4"></p>
            <div class="d-flex justify-content-end gap-2">
                <button type="button" class="btn btn-light border rounded-pill px-4" data-confirm-cancel>Hủy bỏ</button>
                <button type="button" class="btn btn-danger rounded-pill px-4" data-confirm-submit>Đồng ý</button>
            </div>
        </div>
    </dialog>

    <style>
        .order-confirm-dialog {
            width: min(420px, calc(100% - 2rem));
            max-width: none;
            color: var(--bs-body-color);
            background: var(--bs-body-bg);
        }

        .order-confirm-dialog::backdrop {
            background: rgba(0, 0, 0, 0.6);
            backdrop-filter: blur(2px);
        }

        .order-confirm-icon {
            width: 48px;
            height: 48px;
            flex: 0 0 48px;
        }
    </style>

    <script>
        (function() {
            const dialog = document.getElementById('orderActionConfirm');
            const message = document.getElementById('orderConfirmMessage');
            const submitButton = dialog.querySelector('[data-confirm-submit]');
            const cancelButton = dialog.querySelector('[data-confirm-cancel]');
            let pendingUrl = null;
            const buttonVariants = ['btn-danger', 'btn-success', 'btn-primary'];

            document.addEventListener('click', function(event) {
                const trigger = event.target.closest('a[data-confirm-message]');
                if (!trigger) return;

                event.preventDefault();
                pendingUrl = trigger.href;
                message.textContent = trigger.dataset.confirmMessage;
                submitButton.textContent = trigger.dataset.confirmLabel || 'Đồng ý';
                submitButton.classList.remove(...buttonVariants);
                submitButton.classList.add('btn-' + (trigger.dataset.confirmVariant || 'danger'));
                dialog.showModal();
                cancelButton.focus();
            });

            cancelButton.addEventListener('click', function() {
                pendingUrl = null;
                dialog.close();
            });

            submitButton.addEventListener('click', function() {
                if (pendingUrl) window.location.assign(pendingUrl);
            });

            dialog.addEventListener('close', function() {
                pendingUrl = null;
            });
        })();
    </script>
    <script>
        (function() {
            const table = document.querySelector('.admin-orders-table');
            if (!table) return;

            const columns = Array.from(table.querySelectorAll('colgroup col'));
            const headers = Array.from(table.querySelectorAll('thead th'));
            const minimumWidths = [72, 140, 190, 118, 170, 80, 80, 148, 230];
            if (columns.length !== headers.length || columns.length !== minimumWidths.length) return;

            const widths = columns.map(function(column) {
                return parseFloat(column.style.width) || column.getBoundingClientRect().width;
            });

            function applyWidth(index, width) {
                widths[index] = Math.max(minimumWidths[index], Math.round(width));
                columns[index].style.width = widths[index] + 'px';
                table.style.width = widths.reduce(function(total, current) {
                    return total + current;
                }, 0) + 'px';
            }

            headers.forEach(function(header, index) {
                const handle = document.createElement('span');
                handle.className = 'admin-column-resizer';
                handle.setAttribute('role', 'separator');
                handle.setAttribute('aria-orientation', 'vertical');
                handle.setAttribute('aria-label', 'Kéo để chỉnh độ rộng cột ' + header.textContent.trim());
                handle.setAttribute('tabindex', '0');
                handle.title = 'Kéo để chỉnh độ rộng cột';

                let startX = 0;
                let startWidth = 0;

                handle.addEventListener('pointerdown', function(event) {
                    if (event.button !== 0) return;
                    event.preventDefault();
                    startX = event.clientX;
                    startWidth = widths[index];
                    handle.setPointerCapture(event.pointerId);
                    document.body.classList.add('admin-column-resizing');
                });

                handle.addEventListener('pointermove', function(event) {
                    if (!handle.hasPointerCapture(event.pointerId)) return;
                    applyWidth(index, startWidth + event.clientX - startX);
                });

                function finishResize(event) {
                    if (handle.hasPointerCapture(event.pointerId)) {
                        handle.releasePointerCapture(event.pointerId);
                    }
                    document.body.classList.remove('admin-column-resizing');
                }

                handle.addEventListener('pointerup', finishResize);
                handle.addEventListener('pointercancel', finishResize);
                handle.addEventListener('lostpointercapture', function() {
                    document.body.classList.remove('admin-column-resizing');
                });
                handle.addEventListener('keydown', function(event) {
                    const adjustment = event.key === 'ArrowRight' ? 10 : event.key === 'ArrowLeft' ? -10 : 0;
                    if (!adjustment) return;
                    event.preventDefault();
                    applyWidth(index, widths[index] + adjustment);
                });

                header.appendChild(handle);
            });

            table.style.width = widths.reduce(function(total, current) {
                return total + current;
            }, 0) + 'px';
        })();
    </script>
</div>

<jsp:include page="includes/footer.jsp" />
