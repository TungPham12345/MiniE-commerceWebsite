<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, model.Order, java.text.NumberFormat, java.util.Locale" %>
<%
    Integer productsCount = (Integer) request.getAttribute("productsCount");
    Integer ordersCount = (Integer) request.getAttribute("ordersCount");
    Integer pendingOrdersCount = (Integer) request.getAttribute("pendingOrdersCount");
    Double totalRevenueObj = (Double) request.getAttribute("totalRevenue");
    double totalRevenue = totalRevenueObj != null ? totalRevenueObj : 0;
    List<Order> recentOrders = (List<Order>) request.getAttribute("recentOrders");
    NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
%>

<jsp:include page="includes/header.jsp" />
<jsp:include page="includes/navbar.jsp" />

<div class="container my-4">
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
                        <span class="text-muted small fw-bold">ĐƠN CHỜ CHỜ XỬ LÝ</span>
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
        <h4 class="fw-bold mb-4"><i class="bi bi-clock-history me-2"></i>Đơn Hàng Gần Đây</h4>
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
                    <% if (recentOrders != null) {
                        for (Order o : recentOrders) { %>
                            <tr>
                                <td class="fw-bold">#<%= o.getId() %></td>
                                <td><%= o.getCustomerName() %></td>
                                <td><%= o.getPhone() %></td>
                                <td class="small text-muted"><%= o.getAddress() %></td>
                                <td class="fw-bold text-danger"><%= fmt.format(o.getTotalMoney()) %> đ</td>
                                <td><span class="badge bg-secondary"><%= o.getPaymentMethod() %></span></td>
                                <td>
                                    <% if ("DELIVERED".equals(o.getStatus())) { %>
                                        <span class="badge bg-success"><i class="bi bi-check-circle me-1"></i>Đã giao</span>
                                    <% } else if ("PENDING".equals(o.getStatus())) { %>
                                        <span class="badge bg-warning text-dark"><i class="bi bi-hourglass-split me-1"></i>Chờ xử lý</span>
                                    <% } else { %>
                                        <span class="badge bg-info"><%= o.getStatus() %></span>
                                    <% } %>
                                </td>
                                <td class="text-end">
                                    <% if ("PENDING".equals(o.getStatus())) { %>
                                        <a href="${pageContext.request.contextPath}/admin/dashboard?action=updateStatus&orderId=<%= o.getId() %>&status=DELIVERED" 
                                           class="btn btn-sm btn-success rounded-pill fw-bold"
                                           onclick="return confirm('Xác nhận duyệt và hoàn tất đơn hàng #<%= o.getId() %>?');">
                                            <i class="bi bi-check-lg me-1"></i>Duyệt đơn
                                        </a>
                                    <% } else { %>
                                        <span class="text-success small fw-semibold"><i class="bi bi-check2-all me-1"></i>Đã duyệt</span>
                                    <% } %>
                                </td>
                            </tr>
                    <%  }
                       } %>
                </tbody>
            </table>
        </div>
    </div>
</div>

<jsp:include page="includes/footer.jsp" />
