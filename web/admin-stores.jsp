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
                                        <tr style="cursor: pointer;" onclick="focusAdminStore(<%= s.getId() %>, <%= s.getLatitude() %>, <%= s.getLongitude() %>)" title="Bấm để xem trên bản đồ">
                                            <td>
                                                <div class="fw-bold text-dark">
                                                    <i class="bi bi-geo-alt-fill text-danger me-1"></i><%= s.getName() %>
                                                    <% if (s.isIsMain()) { %>
                                                        <span class="badge bg-danger ms-1">Flagship</span>
                                                    <% } %>
                                                </div>
                                                <div class="text-muted small"><%= s.getAddress() %></div>
                                            </td>
                                            <td><span class="badge bg-danger"><%= s.getCity() %></span></td>
                                            <td class="small fw-semibold"><%= s.getPhone() %></td>
                                            <td onclick="event.stopPropagation();">
                                                <form action="${pageContext.request.contextPath}/admin/stores" method="POST" class="d-inline" onsubmit="return confirm('Bạn có chắc muốn xóa chi nhánh này?')">
                                                    <input type="hidden" name="action" value="delete">
                                                    <input type="hidden" name="id" value="<%= s.getId() %>">
                                                    <button type="submit" class="btn btn-outline-danger btn-sm border-0" title="Xóa chi nhánh">
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
                        <div id="adminMap" style="height: 535px; border-radius: 12px; z-index: 1;"></div>
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
                            <input type="text" name="name" class="form-control" placeholder="Ví dụ: HighFashion Tràng Tiền" required>
                        </div>
                        <div class="col-md-4">
                            <label class="form-label fw-bold">Thành Phố *</label>
                            <select name="city" class="form-select">
                                <option value="Hà Nội" selected>Hà Nội</option>
                                <option value="TP.HCM">TP.HCM</option>
                                <option value="Đà Nẵng">Đà Nẵng</option>
                                <option value="Khác">Khác</option>
                            </select>
                        </div>
                        <div class="col-md-12">
                            <label class="form-label fw-bold">Địa Chỉ Chi Tiết *</label>
                            <input type="text" name="address" class="form-control" placeholder="Số nhà, tên đường, phường/xã, quận/huyện, Hà Nội" required>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label fw-bold">Số Điện Thoại Hotline *</label>
                            <input type="text" name="phone" class="form-control" value="0904 567 890" required>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label fw-bold">Giờ Mở Cửa *</label>
                            <input type="text" name="hours" class="form-control" value="08:30 - 22:00" required>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label fw-bold">Vĩ Độ (Latitude) *</label>
                            <input type="number" step="any" name="latitude" class="form-control" value="21.0253" required>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label fw-bold">Kinh Độ (Longitude) *</label>
                            <input type="number" step="any" name="longitude" class="form-control" value="105.8544" required>
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
    // Khởi tạo bản đồ trung tâm Hà Nội
    var adminMap = L.map('adminMap').setView([21.0285, 105.8342], 12);

    L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/{z}/{y}/{x}', {
        maxZoom: 18,
        attribution: '&copy; Esri &mdash; HighFashion Hà Nội'
    }).addTo(adminMap);

    setTimeout(function() {
        adminMap.invalidateSize();
    }, 300);

    var redIcon = L.icon({
        iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png',
        shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
        iconSize: [25, 41],
        iconAnchor: [12, 41],
        popupAnchor: [1, -34],
        shadowSize: [41, 41]
    });

    var markersGroup = L.featureGroup();
    var adminMarkers = {};

    <% if (stores != null && !stores.isEmpty()) { 
        for (Store s : stores) { 
            String popupDest = s.getAddress();
            if (s.getCity() != null && !s.getCity().isEmpty() && !popupDest.toLowerCase().contains(s.getCity().toLowerCase())) {
                popupDest += ", " + s.getCity();
            }
            String popupMapsUrl = "https://www.google.com/maps/dir/?api=1&destination=" + java.net.URLEncoder.encode(popupDest, "UTF-8");
    %>
            var marker_<%= s.getId() %> = L.marker([<%= s.getLatitude() %>, <%= s.getLongitude() %>], {icon: redIcon})
                .bindPopup("<div class='p-1'><strong><%= s.getName() %></strong><br><small class='text-muted'><%= s.getAddress() %></small><br><span class='badge bg-danger mt-1'>☎ <%= s.getPhone() %></span><br><small>⏰ <%= s.getHours() %></small><div class='mt-2'><a href='<%= popupMapsUrl %>' target='_blank' class='btn btn-danger btn-sm text-white w-100 py-1' style='font-size:12px;'><i class='bi bi-sign-turn-right-fill me-1'></i> Chỉ Đường Google Maps</a></div></div>");
            adminMarkers[<%= s.getId() %>] = marker_<%= s.getId() %>;
            markersGroup.addLayer(marker_<%= s.getId() %>);
    <% } } %>

    markersGroup.addTo(adminMap);
    if (markersGroup.getLayers().length > 0) {
        adminMap.fitBounds(markersGroup.getBounds().pad(0.3));
    }

    window.focusAdminStore = function(storeId, lat, lng) {
        adminMap.flyTo([lat, lng], 15, { animate: true, duration: 1 });
        if (adminMarkers[storeId]) {
            adminMarkers[storeId].openPopup();
        }
    };
});
</script>

<jsp:include page="includes/footer.jsp" />
