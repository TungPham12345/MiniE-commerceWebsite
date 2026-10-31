<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, model.Order, model.OrderDetail, java.text.NumberFormat, java.util.Locale" %>
<%
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

<div class="container my-4">
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
        <div class="d-flex gap-2">
            <a href="${pageContext.request.contextPath}/admin/products" class="btn btn-light rounded-pill fw-bold">
                <i class="bi bi-box-seam me-2"></i>Quản Lý Sản Phẩm
            </a>
            <a href="${pageContext.request.contextPath}/admin/stores" class="btn btn-outline-light rounded-pill fw-bold">
                <i class="bi bi-geo-alt-fill me-2"></i>Quản Lý Cửa Hàng & Bản Đồ
            </a>
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
                        <span class="text-muted small fw-bold">ĐƠN CHỜ XỬ LÝ</span>
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
                       onclick="return confirm('⚠️ CẢNH BÁO: Bạn có chắc chắn muốn XÓA TOÀN BỘ đơn hàng trong hệ thống không?');">
                        <i class="bi bi-trash3 me-1"></i>Xóa tất cả đơn
                    </a>
                <% } %>
            </div>
        </div>
        <div class="table-responsive">
            <table class="table table-hover align-middle">
                <thead class="table-light">
                    <tr>
                        <th>Mã Đơn</th>
                        <th>Khách Hàng</th>
                        <th>Số Điện Thoại</th>
                        <th>Địa Chỉ</th>
                        <th>Tổng Tiền</th>
                        <th>Thanh Toán</th>
                        <th>Trạng Thái</th>
                        <th class="text-end">Thao Tác</th>
                    </tr>
                </thead>
                <tbody>
                    <% if (recentOrders != null && !recentOrders.isEmpty()) {
                        for (Order o : recentOrders) { %>
                            <tr>
                                <td class="fw-bold text-dark">
                                    <a href="javascript:void(0)" class="text-decoration-none text-dark fw-bold" data-bs-toggle="modal" data-bs-target="#orderModal<%= o.getId() %>">
                                        #<%= o.getId() %>
                                    </a>
                                </td>
                                <td>
                                    <div class="fw-semibold text-dark"><%= o.getCustomerName() %></div>
                                    <% if (o.getEmail() != null && !o.getEmail().isEmpty()) { %>
                                        <div class="small text-muted"><%= o.getEmail() %></div>
                                    <% } %>
                                </td>
                                <td class="fw-medium"><%= o.getPhone() %></td>
                                <td class="small text-muted" style="max-width: 200px;"><%= o.getAddress() %></td>
                                <td class="fw-bold text-danger"><%= fmt.format(o.getTotalMoney()) %> đ</td>
                                <td><span class="badge bg-secondary"><%= o.getPaymentMethod() %></span></td>
                                <td>
                                    <% if ("DELIVERED".equals(o.getStatus()) || "COMPLETED".equals(o.getStatus())) { %>
                                        <span class="badge bg-success px-3 py-2 rounded-pill"><i class="bi bi-check-circle-fill me-1"></i>Đã giao</span>
                                    <% } else if ("CONFIRMED".equals(o.getStatus()) || "APPROVED".equals(o.getStatus())) { %>
                                        <span class="badge bg-primary px-3 py-2 rounded-pill"><i class="bi bi-patch-check-fill me-1"></i>Đã duyệt</span>
                                    <% } else if ("SHIPPING".equals(o.getStatus())) { %>
                                        <span class="badge bg-info text-dark px-3 py-2 rounded-pill"><i class="bi bi-truck me-1"></i>Đang giao</span>
                                    <% } else if ("CANCELLED".equals(o.getStatus())) { %>
                                        <span class="badge bg-danger px-3 py-2 rounded-pill"><i class="bi bi-x-circle-fill me-1"></i>Đã hủy</span>
                                    <% } else if ("PENDING".equals(o.getStatus())) { %>
                                        <span class="badge bg-warning text-dark px-3 py-2 rounded-pill"><i class="bi bi-hourglass-split me-1"></i>Chờ xử lý</span>
                                    <% } else { %>
                                        <span class="badge bg-secondary px-3 py-2 rounded-pill"><%= o.getStatus() %></span>
                                    <% } %>
                                </td>
                                <td class="text-end">
                                    <div class="d-inline-flex align-items-center gap-1">
                                        <!-- Nút Xem Chi Tiết -->
                                        <button type="button" class="btn btn-sm btn-outline-secondary rounded-pill px-2 py-1" 
                                                data-bs-toggle="modal" data-bs-target="#orderModal<%= o.getId() %>" title="Xem chi tiết đơn hàng">
                                            <i class="bi bi-eye"></i>
                                        </button>

                                        <!-- Thao tác Duyệt / Hủy theo trạng thái -->
                                        <% if ("PENDING".equals(o.getStatus())) { %>
                                            <a href="${pageContext.request.contextPath}/admin/dashboard?action=updateStatus&orderId=<%= o.getId() %>&status=DELIVERED" 
                                               class="btn btn-sm btn-success rounded-pill fw-bold px-3 py-1"
                                               onclick="return confirm('Xác nhận duyệt và hoàn tất đơn hàng #<%= o.getId() %>?');">
                                                <i class="bi bi-check-lg me-1"></i>Duyệt đơn
                                            </a>
                                            <a href="${pageContext.request.contextPath}/admin/dashboard?action=updateStatus&orderId=<%= o.getId() %>&status=CANCELLED" 
                                               class="btn btn-sm btn-outline-danger rounded-pill px-2 py-1"
                                               onclick="return confirm('Bạn có chắc chắn muốn hủy đơn hàng #<%= o.getId() %>?');" title="Hủy đơn hàng">
                                                <i class="bi bi-x-lg"></i>
                                            </a>
                                        <% } else if ("CANCELLED".equals(o.getStatus())) { %>
                                            <span class="text-danger small fw-semibold me-1"><i class="bi bi-x-circle me-1"></i>Đã hủy</span>
                                            <a href="${pageContext.request.contextPath}/admin/dashboard?action=updateStatus&orderId=<%= o.getId() %>&status=PENDING" 
                                               class="btn btn-sm btn-outline-primary rounded-pill px-2 py-1"
                                               onclick="return confirm('Khôi phục đơn hàng #<%= o.getId() %> về Chờ xử lý?');" title="Khôi phục đơn">
                                                <i class="bi bi-arrow-counterclockwise"></i>
                                            </a>
                                        <% } else { %>
                                            <span class="text-success small fw-semibold me-1"><i class="bi bi-check2-all me-1"></i>Đã duyệt</span>
                                            <a href="${pageContext.request.contextPath}/admin/dashboard?action=updateStatus&orderId=<%= o.getId() %>&status=PENDING" 
                                               class="btn btn-sm btn-outline-warning rounded-pill px-2 py-1"
                                               onclick="return confirm('Chuyển đơn hàng #<%= o.getId() %> về trạng thái Chờ xử lý?');" title="Khôi phục về Chờ xử lý">
                                                <i class="bi bi-arrow-counterclockwise"></i>
                                            </a>
                                        <% } %>

                                        <!-- Nút Xóa Đơn Hàng -->
                                        <a href="${pageContext.request.contextPath}/admin/dashboard?action=delete&orderId=<%= o.getId() %>" 
                                           class="btn btn-sm btn-outline-secondary rounded-pill px-2 py-1 ms-1 text-danger border-danger-subtle"
                                           onclick="return confirm('Bạn có chắc muốn xóa vĩnh viễn đơn hàng #<%= o.getId() %> khỏi hệ thống?');" title="Xóa vĩnh viễn đơn hàng">
                                            <i class="bi bi-trash3"></i>
                                        </a>
                                    </div>
                                </td>
                            </tr>
                    <%  }
                       } else { %>
                             <tr>
                                <td colspan="8" class="text-center py-5">
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
                                                            <% if (d.getProductImage() != null && !d.getProductImage().isEmpty()) { %>
                                                                <img src="<%= d.getProductImage() %>" alt="thumb" class="rounded border" style="width: 45px; height: 45px; object-fit: cover;">
                                                            <% } else { %>
                                                                <div class="bg-secondary rounded text-white d-flex align-items-center justify-content-center" style="width: 45px; height: 45px;">
                                                                    <i class="bi bi-image"></i>
                                                                </div>
                                                            <% } %>
                                                            <span class="fw-semibold"><%= d.getProductName() != null ? d.getProductName() : ("Sản phẩm #" + d.getProductId()) %></span>
                                                        </div>
                                                    </td>
                                                    <td>
                                                        <span class="badge bg-light text-dark border">Size: <%= d.getSize() %></span>
                                                        <span class="badge bg-light text-dark border">Màu: <%= d.getColor() %></span>
                                                    </td>
                                                    <td class="text-center fw-bold"><%= d.getQuantity() %></td>
                                                    <td class="text-end"><%= fmt.format(d.getPrice()) %> đ</td>
                                                    <td class="text-end fw-bold text-danger"><%= fmt.format(d.getTotalMoney()) %> đ</td>
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
                                <span class="fw-extrabold text-danger fs-5"><%= fmt.format(o.getTotalMoney()) %> đ</span>
                            </div>
                        </div>
                        <div class="modal-footer bg-light rounded-bottom-4">
                            <% if ("PENDING".equals(o.getStatus())) { %>
                                <a href="${pageContext.request.contextPath}/admin/dashboard?action=updateStatus&orderId=<%= o.getId() %>&status=DELIVERED" 
                                   class="btn btn-success rounded-pill fw-bold px-4"
                                   onclick="return confirm('Xác nhận duyệt và hoàn tất đơn hàng #<%= o.getId() %>?');">
                                    <i class="bi bi-check-lg me-1"></i>Duyệt Đơn Này
                                </a>
                                <a href="${pageContext.request.contextPath}/admin/dashboard?action=updateStatus&orderId=<%= o.getId() %>&status=CANCELLED" 
                                   class="btn btn-outline-danger rounded-pill fw-medium px-3"
                                   onclick="return confirm('Bạn có chắc chắn muốn hủy đơn hàng #<%= o.getId() %>?');">
                                    <i class="bi bi-x-lg me-1"></i>Hủy Đơn
                                </a>
                            <% } %>
                            <button type="button" class="btn btn-secondary rounded-pill px-4" data-bs-dismiss="modal">Đóng</button>
                        </div>
                    </div>
                </div>
            </div>
    <%  }
       } %>
</div>

<jsp:include page="includes/footer.jsp" />
