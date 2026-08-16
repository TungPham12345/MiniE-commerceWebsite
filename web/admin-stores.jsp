<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, model.Store, model.User" %>
<%
    User adminUser = (User) session.getAttribute("user");
    List<Store> stores = (List<Store>) request.getAttribute("stores");
%>

<jsp:include page="includes/header.jsp" />
<jsp:include page="includes/navbar.jsp" />

<link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
<script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>

<div class="container-fluid my-4">
    <div class="row">
        <!-- Sidebar Navigation Admin -->
        <div class="col-md-3 col-lg-2 bg-white border-end min-vh-100 p-3">
            <h5 class="fw-bold text-danger mb-4"><i class="bi bi-shield-lock-fill me-2"></i> ADMIN PANEL</h5>
            <div class="nav flex-column nav-pills gap-2">
                <a href="${pageContext.request.contextPath}/admin/dashboard" class="nav-link text-dark fw-semibold">
                    <i class="bi bi-speedometer2 me-2"></i> Dashboard
                </a>
                <a href="${pageContext.request.contextPath}/admin/products" class="nav-link text-dark fw-semibold">
                    <i class="bi bi-box-seam me-2"></i> Quản Lý Sản Phẩm
                </a>
                <a href="${pageContext.request.contextPath}/admin/stores" class="nav-link active bg-danger fw-semibold">
                    <i class="bi bi-geo-alt-fill me-2"></i> Quản Lý Bản Đồ Cửa Hàng
                </a>
                <a href="${pageContext.request.contextPath}/home" class="nav-link text-dark fw-semibold mt-4 border-top pt-3">
                    <i class="bi bi-box-arrow-left me-2"></i> Trở Về Website
                </a>
            </div>
        </div>

        <!-- Main Content -->
        <div class="col-md-9 col-lg-10 p-4">
            <div class="d-flex justify-content-between align-items-center mb-4">
                <div>
                    <h3 class="fw-extrabold m-0">Quản Lý Hệ Thống Bản Đồ Cửa Hàng</h3>
                    <p class="text-secondary small mb-0">Thêm, bớt chi nhánh và quản lý tọa độ vị trí trên bản đồ toàn quốc</p>
                </div>
                <button class="btn btn-danger rounded-pill px-4 fw-bold" data-bs-toggle="modal" data-bs-target="#addStoreModal">
                    <i class="bi bi-plus-lg me-1"></i> Thêm Chi Nhánh Mới
                </button>
            </div>

            <div class="row g-4 mb-4">
                <!-- Danh Sách Bảng Admin -->
                <div class="col-lg-6">
                    <div class="bg-white border rounded-4 p-3 shadow-sm" style="max-height: 550px; overflow-y: auto;">
                        <h5 class="fw-bold mb-3 border-bottom pb-2">Danh Sách Chi Nhánh (<%= stores != null ? stores.size() : 0 %>)</h5>
                        <table class="table table-hover align-middle">
                            <thead class="table-light">
                                <tr>
                                    <th>Chi Nhánh</th>
                                    <th>Thành Phố</th>
                                    <th>Hotline</th>
                                    <th>Thao Tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <% if (stores != null) { 
                                    for (Store s : stores) { %>
                                        <tr>
                                            <td>
                                                <div class="fw-bold text-dark"><%= s.getName() %></div>
                                                <div class="text-muted small"><%= s.getAddress() %></div>
                                            </td>
                                            <td><span class="badge bg-secondary"><%= s.getCity() %></span></td>
                                            <td class="small"><%= s.getPhone() %></td>
                                            <td>
                                                <form action="${pageContext.request.contextPath}/admin/stores" method="POST" class="d-inline" onsubmit="return confirm('Bạn có chắc muốn xóa chi nhánh này?')">
                                                    <input type="hidden" name="action" value="delete">
                                                    <input type="hidden" name="id" value="<%= s.getId() %>">
                                                    <button type="submit" class="btn btn-outline-danger btn-sm border-0">
                                                        <i class="bi bi-trash-fill"></i>
                                                    </button>
                                                </form>
                                            </td>
                                        </tr>
                                <% } } %>
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- Live Interactive Map for Admin -->
                <div class="col-lg-6">
                    <div class="bg-white border rounded-4 p-2 shadow-sm">
                        <div id="adminMap" style="height: 535px; border-radius: 12px;"></div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Modal Thêm Chi Nhánh Mới -->
<div class="modal fade" id="addStoreModal" tabindex="-1">
    <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content rounded-4 border-0">
            <div class="modal-header bg-danger text-white">
                <h5 class="modal-header-title fw-bold m-0"><i class="bi bi-geo-alt me-2"></i>Thêm Chi Nhánh Cửa Hàng Mới</h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
            </div>
            <form action="${pageContext.request.contextPath}/admin/stores" method="POST">
                <input type="hidden" name="action" value="add">
                <div class="modal-body p-4">
                    <div class="row g-3">
                        <div class="col-md-8">
                            <label class="form-label fw-bold">Tên Chi Nhánh *</label>
                            <input type="text" name="name" class="form-control" placeholder="Ví dụ: HighFashion Sư Vạn Hạnh" required>
                        </div>
                        <div class="col-md-4">
                            <label class="form-label fw-bold">Thành Phố *</label>
                            <select name="city" class="form-select">
                                <option value="TP.HCM">TP.HCM</option>
                                <option value="Hà Nội">Hà Nội</option>
                                <option value="Đà Nẵng">Đà Nẵng</option>
                                <option value="Khác">Khác</option>
                            </select>
                        </div>
                        <div class="col-md-12">
                            <label class="form-label fw-bold">Địa Chỉ Chi Tiết *</label>
                            <input type="text" name="address" class="form-control" placeholder="Số nhà, tên đường, phường/xã, quận/huyện" required>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label fw-bold">Số Điện Thoại Hotline *</label>
                            <input type="text" name="phone" class="form-control" value="0901 234 567" required>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label fw-bold">Giờ Mở Cửa *</label>
                            <input type="text" name="hours" class="form-control" value="08:30 - 22:00" required>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label fw-bold">Vĩ Độ (Latitude) *</label>
                            <input type="number" step="any" name="latitude" class="form-control" value="10.7769" required>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label fw-bold">Kinh Độ (Longitude) *</label>
                            <input type="number" step="any" name="longitude" class="form-control" value="106.6675" required>
                        </div>
                        <div class="col-md-12">
                            <div class="form-check form-switch mt-2">
                                <input class="form-check-input" type="checkbox" name="isMain" value="1" id="isMainCheck">
                                <label class="form-check-label fw-bold" for="isMainCheck">Đặt làm Cửa Hàng Flagship Chính</label>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="modal-footer border-0">
                    <button type="button" class="btn btn-light" data-bs-dismiss="modal">Hủy</button>
                    <button type="submit" class="btn btn-danger px-4 fw-bold">Lưu Vị Trí Cửa Hàng</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
document.addEventListener("DOMContentLoaded", function() {
    var adminMap = L.map('adminMap').setView([10.7769, 106.6675], 11);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19
    }).addTo(adminMap);

    var redIcon = L.icon({
        iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png',
        shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
        iconSize: [25, 41],
        iconAnchor: [12, 41],
        popupAnchor: [1, -34],
        shadowSize: [41, 41]
    });

    <% if (stores != null) { 
        for (Store s : stores) { %>
            L.marker([<%= s.getLatitude() %>, <%= s.getLongitude() %>], {icon: redIcon})
                .addTo(adminMap)
                .bindPopup("<b><%= s.getName() %></b><br><%= s.getAddress() %>");
    <% } } %>
});
</script>

<jsp:include page="includes/footer.jsp" />
