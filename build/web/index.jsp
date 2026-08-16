<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, model.Product, model.Category, java.text.NumberFormat, java.util.Locale" %>
<%
    List<Product> products = (List<Product>) request.getAttribute("products");
    List<Product> featuredProducts = (List<Product>) request.getAttribute("featuredProducts");
    List<Category> categories = (List<Category>) request.getAttribute("categories");
    NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
%>

<jsp:include page="includes/header.jsp" />
<jsp:include page="includes/navbar.jsp" />

<!-- Hero Banner Header -->
<div class="container my-4">
    <div class="hero-banner shadow-lg">
        <div class="row align-items-center">
            <div class="col-lg-7">
                <span class="hero-badge mb-3"><i class="bi bi-fire me-1"></i> Bộ Sưu Tập Mùa Hè 2026</span>
                <h1 class="display-4 fw-extrabold text-white mb-3">Nâng Tầm Phong Cách Thời Trang Của Bạn</h1>
                <p class="fs-5 text-light opacity-90 mb-4">Khám phá hơn 1,000+ mẫu quần áo, váy đầm và phụ kiện hàng hiệu mới nhất. Giảm giá lên tới 50% tuần này!</p>
                <div class="d-flex gap-3 flex-wrap align-items-center">
                    <a href="${pageContext.request.contextPath}/products" class="btn btn-accent btn-lg rounded-pill px-4 py-3 fw-bold">
                        <i class="bi bi-bag-fill me-2"></i> Mua Sắm Ngay
                    </a>
                    <a href="${pageContext.request.contextPath}/products?filter=sale" class="btn btn-outline-light btn-lg rounded-pill px-4 py-3 fw-bold">
                        <i class="bi bi-tag-fill me-2"></i> Khuyến Mãi Hot
                    </a>
                </div>
            </div>
            
            <!-- Slideshow Banner Right Side -->
            <div class="col-lg-5 d-none d-lg-block">
                <div id="heroBannerCarousel" class="carousel slide carousel-fade shadow-lg rounded-4 overflow-hidden" data-bs-ride="carousel" data-bs-interval="3000">
                    <div class="carousel-inner">
                        <div class="carousel-item active">
                            <img src="https://images.unsplash.com/photo-1483985988355-763728e1935b?w=700" class="d-block w-100 object-fit-cover" style="height: 380px;" alt="Banner 1">
                        </div>
                        <div class="carousel-item">
                            <img src="https://images.unsplash.com/photo-1490481651871-ab68de25d43d?w=700" class="d-block w-100 object-fit-cover" style="height: 380px;" alt="Banner 2">
                        </div>
                        <div class="carousel-item">
                            <img src="https://images.unsplash.com/photo-1445205170230-053b83016050?w=700" class="d-block w-100 object-fit-cover" style="height: 380px;" alt="Banner 3">
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Section 1: Categories -->
<div class="container my-5">
    <div class="mb-4">
        <h2 class="fw-extrabold m-0" style="letter-spacing: -0.5px;">Danh Mục Nổi Bật</h2>
        <p class="text-secondary small mt-1 mb-0">Lựa chọn outfit theo phong cách của bạn</p>
    </div>
    
    <div class="d-flex flex-wrap gap-3 align-items-center">
        <a href="${pageContext.request.contextPath}/products" class="category-pill active">
            <i class="bi bi-grid-fill"></i> Tất cả
        </a>
        <% if (categories != null) { 
            for (Category cat : categories) { 
                String iconClass = "bi-tag-fill";
                if (cat.getId() == 1) iconClass = "bi-person-badge-fill";
                else if (cat.getId() == 2) iconClass = "bi-gender-male";
                else if (cat.getId() == 3) iconClass = "bi-person-heart";
                else if (cat.getId() == 4) iconClass = "bi-gem";
        %>
            <a href="${pageContext.request.contextPath}/products?categoryId=<%= cat.getId() %>" class="category-pill">
                <i class="bi <%= iconClass %>"></i> <%= cat.getName() %>
            </a>
        <% } } %>
    </div>
</div>

<!-- Section 2: Carousel Slide Sản Phẩm Cửa Hàng -->
<div class="container my-5">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h2 class="fw-extrabold m-0">Sản Phẩm Cửa Hàng <i class="bi bi-bag-heart-fill text-danger ms-1"></i></h2>
        </div>
        <a href="${pageContext.request.contextPath}/products" class="text-danger fw-bold text-decoration-none fs-6">
            Xem tất cả <i class="bi bi-chevron-right"></i>
        </a>
    </div>

    <% 
        List<Product> displayProducts = (featuredProducts != null && !featuredProducts.isEmpty()) ? featuredProducts : products;
        if (displayProducts != null && !displayProducts.isEmpty()) { 
            int total = displayProducts.size();
            int itemsPerSlide = 4;
            int numSlides = (int) Math.ceil((double) total / itemsPerSlide);
    %>
    <div class="carousel-wrapper-padded">
        <!-- 2 Mũi Tên Nằm Ở 2 Lề 50px Tuyệt Đối Không Đè Lên Thẻ Sản Phẩm -->
        <button class="carousel-control-prev carousel-arrow-custom prev-arrow" type="button" data-bs-target="#productCarousel" data-bs-slide="prev">
            <i class="bi bi-chevron-left"></i>
            <span class="visually-hidden">Previous</span>
        </button>
        <button class="carousel-control-next carousel-arrow-custom next-arrow" type="button" data-bs-target="#productCarousel" data-bs-slide="next">
            <i class="bi bi-chevron-right"></i>
            <span class="visually-hidden">Next</span>
        </button>

        <div id="productCarousel" class="carousel slide" data-bs-ride="carousel" data-bs-interval="3500">
            <!-- Main Product Grid Items -->
            <div class="carousel-inner">
                <% for (int s = 0; s < numSlides; s++) { %>
                    <div class="carousel-item <%= s == 0 ? "active" : "" %>">
                        <div class="row g-4">
                            <% 
                                int start = s * itemsPerSlide;
                                int end = Math.min(start + itemsPerSlide, total);
                                for (int i = start; i < end; i++) {
                                    Product p = displayProducts.get(i);
                            %>
                                <div class="col-md-3 col-sm-6">
                                    <div class="product-card">
                                        <div class="product-img-wrapper">
                                            <% if (p.getSalePrice() > 0 && p.getSalePrice() < p.getPrice()) { 
                                                int percent = (int) Math.round((1.0 - (p.getSalePrice() / p.getPrice())) * 100.0);
                                            %>
                                                <span class="product-badge">-<%= percent %>% SALE</span>
                                            <% } %>
                                            <a href="${pageContext.request.contextPath}/product-detail?id=<%= p.getId() %>">
                                                <img src="<%= p.getImage() %>" alt="<%= p.getName() %>" 
                                                     onerror="this.src='https://images.unsplash.com/photo-1523381210434-271e8be1f52b?w=600'" loading="lazy">
                                            </a>
                                        </div>
                                        
                                        <!-- Nút TÙY CHỌN Đỏ -->
                                        <a href="${pageContext.request.contextPath}/product-detail?id=<%= p.getId() %>" class="btn-tuy-chon">
                                            TÙY CHỌN
                                        </a>

                                        <div class="product-info-box">
                                            <div class="product-brand"><%= p.getCategoryName() != null ? p.getCategoryName() : "HIGHFASHION" %></div>
                                            <a href="${pageContext.request.contextPath}/product-detail?id=<%= p.getId() %>" class="product-title-custom">
                                                <%= p.getName() %>
                                            </a>
                                            <div class="product-price-box-custom">
                                                <span class="price-red">
                                                    <%= fmt.format(p.getEffectivePrice()) %>đ
                                                </span>
                                                <% if (p.getSalePrice() > 0 && p.getSalePrice() < p.getPrice()) { %>
                                                    <span class="price-old">
                                                        <%= fmt.format(p.getPrice()) %>đ
                                                    </span>
                                                <% } %>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            <% } %>
                        </div>
                    </div>
                <% } %>
            </div>

            <!-- Nút Chấm Đỏ Tự Động Đồng Bộ Khi Chuyển Slide -->
            <div class="carousel-indicators carousel-indicators-custom">
                <% for (int s = 0; s < numSlides; s++) { %>
                    <button type="button" data-bs-target="#productCarousel" data-bs-slide-to="<%= s %>" class="<%= s == 0 ? "active" : "" %>" aria-current="<%= s == 0 ? "true" : "false" %>" aria-label="Slide <%= s + 1 %>"></button>
                <% } %>
            </div>
        </div>
    </div>
    <% } %>
</div>

<script>
document.addEventListener("DOMContentLoaded", function () {
    var myCarousel = document.getElementById('productCarousel');
    if (myCarousel) {
        myCarousel.addEventListener('slid.bs.carousel', function (event) {
            var activeIndex = event.to;
            var dots = document.querySelectorAll('#productCarousel .carousel-indicators-custom button');
            dots.forEach(function(dot, idx) {
                if (idx === activeIndex) {
                    dot.classList.add('active');
                    dot.setAttribute('aria-current', 'true');
                } else {
                    dot.classList.remove('active');
                    dot.removeAttribute('aria-current');
                }
            });
        });
    }
});
</script>

<jsp:include page="includes/footer.jsp" />
