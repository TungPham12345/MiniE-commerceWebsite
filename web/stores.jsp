<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, model.Store" %>
<%
    List<Store> stores = (List<Store>) request.getAttribute("stores");
%>

<jsp:include page="includes/header.jsp" />
<jsp:include page="includes/navbar.jsp" />

<!-- Leaflet CSS & JS cho Bản Đồ Tương Tác -->
<link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
<script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>

<div class="container my-4">
    <!-- Header -->
    <div class="bg-white p-4 rounded-4 border mb-4 shadow-sm text-center">
        <span class="badge bg-danger px-3 py-2 rounded-pill fs-6 mb-2"><i class="bi bi-geo-alt-fill me-1"></i> Hệ Thống Cửa Hàng</span>
        <h2 class="fw-extrabold text-uppercase m-0">Tìm Cửa Hàng HighFashion Gần Bạn</h2>
        <p class="text-secondary mt-2 mb-0">Trải nghiệm mua sắm trực tiếp tại hệ thống chuỗi cửa hàng cao cấp toàn quốc</p>
    </div>

    <div class="row g-4">
        <!-- Sidebar Danh Sách Chi Nhánh Cửa Hàng -->
        <div class="col-lg-4">
            <div class="bg-white border rounded-4 p-3 shadow-sm" style="max-height: 600px; overflow-y: auto;">
                <h5 class="fw-bold mb-3 px-2 border-bottom pb-2"><i class="bi bi-buildings text-danger me-2"></i> Danh Sách Chi Nhánh</h5>
                
                <% if (stores != null && !stores.isEmpty()) { 
                    for (int i = 0; i < stores.size(); i++) {
                        Store s = stores.get(i);
                %>
                    <div class="p-3 border rounded-3 mb-3 bg-light hover-shadow cursor-pointer store-item"
                         onclick="focusMap(<%= s.getLatitude() %>, <%= s.getLongitude() %>, '<%= s.getName() %>', '<%= s.getAddress() %>')">
                        <div class="d-flex justify-content-between align-items-center mb-1">
                            <h6 class="fw-bold m-0 text-dark"><%= s.getName() %></h6>
                            <% if (s.isIsMain()) { %>
                                <span class="badge bg-danger">Flagship</span>
                            <% } %>
                        </div>
                        <p class="text-secondary small mb-2"><i class="bi bi-geo-alt text-danger me-1"></i> <%= s.getAddress() %></p>
                        <div class="d-flex justify-content-between align-items-center text-muted small">
                            <span><i class="bi bi-telephone text-primary me-1"></i> <%= s.getPhone() %></span>
                            <span><i class="bi bi-clock me-1"></i> <%= s.getHours() %></span>
                        </div>
                        <div class="mt-3 text-end">
                            <a href="https://www.google.com/maps/dir/?api=1&destination=<%= s.getLatitude() %>,<%= s.getLongitude() %>" 
                               target="_blank" class="btn btn-outline-danger btn-sm rounded-pill px-3">
                                <i class="bi bi-sign-turn-right-fill me-1"></i> Chỉ Đường
                            </a>
                        </div>
                    </div>
                <% } } %>
            </div>
        </div>

        <!-- Khung Bản Đồ Tương Tác (Interactive Map) -->
        <div class="col-lg-8">
            <div class="bg-white border rounded-4 p-2 shadow-sm">
                <div id="map" style="height: 580px; border-radius: 12px; z-index: 1;"></div>
            </div>
        </div>
    </div>
</div>

<script>
var map;
var markers = [];

document.addEventListener("DOMContentLoaded", function() {
    // Khoi tao ban do trung tam TP.HCM
    map = L.map('map').setView([10.7769, 106.6675], 12);

    // Nãp gạch tile OpenStreetMap mượt mà
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '© OpenStreetMap HighFashion'
    }).addTo(map);

    // Dynamic icon
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
            var marker = L.marker([<%= s.getLatitude() %>, <%= s.getLongitude() %>], {icon: redIcon})
                .addTo(map)
                .bindPopup("<b><%= s.getName() %></b><br><%= s.getAddress() %><br>☎ <%= s.getPhone() %><br>⏰ <%= s.getHours() %>");
            markers.push(marker);
    <% } } %>
});

function focusMap(lat, lng, name, address) {
    map.flyTo([lat, lng], 16, {
        animate: true,
        duration: 1.5
    });
    
    // Open popup
    markers.forEach(function(m) {
        if (m.getLatLng().lat === lat && m.getLatLng().lng === lng) {
            m.openPopup();
        }
    });
}
</script>

<style>
.store-item:hover {
    background-color: #ffffff !important;
    border-color: #e11d48 !important;
    transform: translateY(-2px);
    transition: all 0.2s ease;
}
.cursor-pointer {
    cursor: pointer;
}
</style>

<jsp:include page="includes/footer.jsp" />
