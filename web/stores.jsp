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
        <h2 class="fw-extrabold text-uppercase m-0">Hệ Thống 3 Chi Nhánh HighFashion Tại Hà Nội</h2>
        <p class="text-secondary mt-2 mb-0">Trải nghiệm mua sắm trực tiếp tại chuỗi showroom thời trang cao cấp tại thủ đô Hà Nội</p>
    </div>

    <div class="row g-4">
        <!-- Sidebar Danh Sách Chi Nhánh Cửa Hàng -->
        <div class="col-lg-4">
            <div class="bg-white border rounded-4 p-3 shadow-sm" style="max-height: 600px; overflow-y: auto;">
                <h5 class="fw-bold mb-3 px-2 border-bottom pb-2"><i class="bi bi-buildings text-danger me-2"></i> Chi Nhánh Hà Nội (<%= stores != null ? stores.size() : 0 %>)</h5>
                
                <% if (stores != null && !stores.isEmpty()) { 
                    for (int i = 0; i < stores.size(); i++) {
                        Store s = stores.get(i);
                        String destAddress = s.getAddress();
                        if (s.getCity() != null && !s.getCity().isEmpty() && !destAddress.toLowerCase().contains(s.getCity().toLowerCase())) {
                            destAddress += ", " + s.getCity();
                        }
                        String mapsUrl = "https://www.google.com/maps/dir/?api=1&destination=" + java.net.URLEncoder.encode(destAddress, "UTF-8");
                %>
                    <div class="p-3 border rounded-3 mb-3 bg-light hover-shadow cursor-pointer store-item <%= i == 0 ? "active" : "" %>"
                         id="store-item-<%= s.getId() %>"
                         onclick="focusMap(<%= s.getId() %>, <%= s.getLatitude() %>, <%= s.getLongitude() %>)">
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
                            <a href="<%= mapsUrl %>" 
                               target="_blank" class="btn btn-outline-danger btn-sm rounded-pill px-3 fw-medium" onclick="event.stopPropagation();">
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
var storeMarkers = {};

document.addEventListener("DOMContentLoaded", function() {
    // Khởi tạo bản đồ trung tâm Hà Nội
    map = L.map('map').setView([21.0285, 105.8342], 13);

    // Nạp gạch tile Esri World Street Map sắc nét, đầy đủ địa danh tiếng Việt
    L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/{z}/{y}/{x}', {
        maxZoom: 18,
        attribution: '&copy; Esri &mdash; HighFashion Hà Nội'
    }).addTo(map);

    setTimeout(function() {
        map.invalidateSize();
    }, 300);

    // Dynamic icon
    var redIcon = L.icon({
        iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png',
        shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
        iconSize: [25, 41],
        iconAnchor: [12, 41],
        popupAnchor: [1, -34],
        shadowSize: [41, 41]
    });

    var group = L.featureGroup();
    var firstStoreId = null;

    <% if (stores != null && !stores.isEmpty()) { 
        for (int i = 0; i < stores.size(); i++) {
            Store s = stores.get(i);
            String popupDest = s.getAddress();
            if (s.getCity() != null && !s.getCity().isEmpty() && !popupDest.toLowerCase().contains(s.getCity().toLowerCase())) {
                popupDest += ", " + s.getCity();
            }
            String popupMapsUrl = "https://www.google.com/maps/dir/?api=1&destination=" + java.net.URLEncoder.encode(popupDest, "UTF-8");
    %>
            <% if (i == 0) { %>
                firstStoreId = <%= s.getId() %>;
            <% } %>
            var marker_<%= s.getId() %> = L.marker([<%= s.getLatitude() %>, <%= s.getLongitude() %>], {icon: redIcon})
                .bindPopup("<div class='p-1'><strong><%= s.getName() %></strong><br><small class='text-muted'><%= s.getAddress() %></small><br><span class='badge bg-danger mt-1'>☎ <%= s.getPhone() %></span><br><small>⏰ <%= s.getHours() %></small><div class='mt-2'><a href='<%= popupMapsUrl %>' target='_blank' class='btn btn-danger btn-sm text-white w-100 py-1' style='font-size:12px;'><i class='bi bi-sign-turn-right-fill me-1'></i> Chỉ Đường Google Maps</a></div></div>");
            
            marker_<%= s.getId() %>.on('click', function() {
                highlightStoreItem(<%= s.getId() %>);
            });

            storeMarkers[<%= s.getId() %>] = marker_<%= s.getId() %>;
            group.addLayer(marker_<%= s.getId() %>);
    <% } } %>

    group.addTo(map);
    if (group.getLayers().length > 0) {
        map.fitBounds(group.getBounds().pad(0.25));
        if (firstStoreId && storeMarkers[firstStoreId]) {
            setTimeout(function() {
                storeMarkers[firstStoreId].openPopup();
            }, 600);
        }
    }
});

function highlightStoreItem(storeId) {
    document.querySelectorAll('.store-item').forEach(function(el) {
        el.classList.remove('active');
    });
    var el = document.getElementById('store-item-' + storeId);
    if (el) {
        el.classList.add('active');
        el.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }
}

function focusMap(storeId, lat, lng) {
    highlightStoreItem(storeId);
    map.flyTo([lat, lng], 16, {
        animate: true,
        duration: 1
    });
    
    if (storeMarkers[storeId]) {
        storeMarkers[storeId].openPopup();
    }
}
</script>

<style>
.store-item {
    transition: all 0.2s ease;
    border: 2px solid transparent !important;
}
.store-item:hover {
    background-color: #ffffff !important;
    border-color: #fca5a5 !important;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}
.store-item.active {
    background-color: #ffffff !important;
    border-color: #dc2626 !important;
    box-shadow: 0 4px 14px rgba(220, 38, 38, 0.15) !important;
}
.cursor-pointer {
    cursor: pointer;
}
</style>

<jsp:include page="includes/footer.jsp" />
