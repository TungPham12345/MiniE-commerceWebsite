<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, model.Product, model.Category, java.text.NumberFormat, java.util.Locale" %>
<%
    List<Product> products = (List<Product>) request.getAttribute("products");
    List<Category> categories = (List<Category>) request.getAttribute("categories");
    Category currentCategory = (Category) request.getAttribute("currentCategory");
    String keyword = (String) request.getAttribute("keyword");
    String sort = (String) request.getAttribute("sort");
    String selectedSize = (String) request.getAttribute("selectedSize");
    String selectedColor = (String) request.getAttribute("selectedColor");
    String selectedPriceRange = (String) request.getAttribute("selectedPriceRange");
    if (sort == null) sort = "";
    if (selectedSize == null) selectedSize = "";
    if (selectedColor == null) selectedColor = "";
    if (selectedPriceRange == null) selectedPriceRange = "";
    
    NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
%>

<jsp:include page="includes/header.jsp" />
<jsp:include page="includes/navbar.jsp" />

<div class="container my-4">
    <!-- Breadcrumb & Header -->
    <div class="bg-white p-4 rounded-4 border mb-4 shadow-sm">
        <nav aria-label="breadcrumb">
            <ol class="breadcrumb m-0">
                <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/home" class="text-decoration-none">Trang chủ</a></li>
                <li class="breadcrumb-item active" aria-current="page">
                    <% if (currentCategory != null) { %>
                        <%= currentCategory.getName() %>
                    <% } else if (keyword != null && !keyword.isEmpty()) { %>
                        Kết quả tìm kiếm: "<%= keyword %>"
                    <% } else { %>
                        Tất cả sản phẩm
                    <% } %>
                </li>
            </ol>
        </nav>
        <h2 class="fw-bold mt-2 mb-0">
            <% if (currentCategory != null) { %>
                <%= currentCategory.getName() %>
            <% } else if (keyword != null && !keyword.isEmpty()) { %>
                Kết Quả Tìm Kiếm: "<%= keyword %>"
            <% } else { %>
                Bộ Sưu Tập Thời Trang HighFashion
            <% } %>
        </h2>
    </div>

    <div class="row g-4">
        <!-- Sidebar Filter -->
        <div class="col-lg-3">
            <form action="${pageContext.request.contextPath}/products" method="GET" class="filter-card">
                <% if (currentCategory != null) { %>
                    <input type="hidden" name="categoryId" value="<%= currentCategory.getId() %>">
                <% } %>
                <% if (keyword != null && !keyword.isEmpty()) { %>
                    <input type="hidden" name="keyword" value="<%= keyword %>">
                <% } %>

                <!-- 1. SẮP XẾP -->
                <div class="filter-group">
                    <h6 class="filter-title mb-3" data-bs-toggle="collapse" data-bs-target="#sortCollapse">
                        SẮP XẾP <i class="bi bi-chevron-up"></i>
                    </h6>
                    <div class="collapse show" id="sortCollapse">
                        <label class="filter-item-row">
                            <span>Sản phẩm nổi bật</span>
                            <input type="radio" name="sort" value="featured" class="filter-checkbox" <%= "featured".equals(sort) ? "checked" : "" %>>
                        </label>
                        <label class="filter-item-row">
                            <span>Sản phẩm mới</span>
                            <input type="radio" name="sort" value="new" class="filter-checkbox" <%= "new".equals(sort) ? "checked" : "" %>>
                        </label>
                        <label class="filter-item-row">
                            <span>Bán chạy nhất</span>
                            <input type="radio" name="sort" value="bestseller" class="filter-checkbox" <%= "bestseller".equals(sort) ? "checked" : "" %>>
                        </label>
                        <label class="filter-item-row">
                            <span>Giá cao nhất</span>
                            <input type="radio" name="sort" value="price_desc" class="filter-checkbox" <%= "price_desc".equals(sort) ? "checked" : "" %>>
                        </label>
                        <label class="filter-item-row">
                            <span>Giá thấp nhất</span>
                            <input type="radio" name="sort" value="price_asc" class="filter-checkbox" <%= "price_asc".equals(sort) ? "checked" : "" %>>
                        </label>
                    </div>
                </div>

                <!-- 2. KÍCH THƯỚC -->
                <div class="filter-group">
                    <h6 class="filter-title mb-3" data-bs-toggle="collapse" data-bs-target="#sizeCollapse">
                        KÍCH THƯỚC <i class="bi bi-chevron-up"></i>
                    </h6>
                    <div class="collapse show" id="sizeCollapse">
                        <% String[] sizes = {"XS", "S", "M", "L", "XL", "XXL", "Freesize", "One Size"};
                           for (String sz : sizes) { %>
                            <label class="filter-item-row">
                                <span><%= sz %></span>
                                <input type="radio" name="size" value="<%= sz %>" class="filter-checkbox" <%= sz.equalsIgnoreCase(selectedSize) ? "checked" : "" %>>
                            </label>
                        <% } %>
                    </div>
                </div>

                <!-- 3. MÀU SẮC -->
                <div class="filter-group">
                    <h6 class="filter-title mb-2" data-bs-toggle="collapse" data-bs-target="#colorCollapse">
                        MÀU SẮC <i class="bi bi-chevron-up"></i>
                    </h6>
                    <div class="collapse show" id="colorCollapse">
                        <div class="color-swatch-grid">
                            <% 
                                String[][] colors = {
                                    {"#000000", "Đen"}, {"#ffffff", "Trắng"}, {"#d1d5db", "Xám"}, 
                                    {"#fef3c7", "Kem"}, {"#fbcfe8", "Hồng"}, {"#fef08a", "Vàng"},
                                    {"#bae6fd", "Xanh Lam"}, {"#1e1b4b", "Xanh Navy"}, {"#eab308", "Vàng Vàng"},
                                    {"#dc2626", "Đỏ"}, {"#0f172a", "Đen Đậm"}, {"#78350f", "Nâu"},
                                    {"#65a30d", "Xanh Lá"}, {"#ea580c", "Cam"}, {"#d6d3d1", "Be"},
                                    {"#334155", "Ghi"}, {"#64748b", "Ghi Sáng"}, {"#581c87", "Tím"}
                                };
                                for (String[] col : colors) {
                                    boolean isActive = col[1].equalsIgnoreCase(selectedColor);
                            %>
                                <label class="color-circle <%= isActive ? "active" : "" %>" style="background-color: <%= col[0] %>;" title="<%= col[1] %>">
                                    <input type="radio" name="color" value="<%= col[1] %>" <%= isActive ? "checked" : "" %> onchange="this.form.submit()">
                                </label>
                            <% } %>
                        </div>
                    </div>
                </div>

                <!-- 4. KHOẢNG GIÁ -->
                <div class="filter-group">
                    <h6 class="filter-title mb-3" data-bs-toggle="collapse" data-bs-target="#priceCollapse">
                        KHOẢNG GIÁ <i class="bi bi-chevron-up"></i>
                    </h6>
                    <div class="collapse show" id="priceCollapse">
                        <label class="filter-item-row">
                            <span>Dưới 500.000đ</span>
                            <input type="radio" name="priceRange" value="under_500k" class="filter-checkbox" <%= "under_500k".equals(selectedPriceRange) ? "checked" : "" %>>
                        </label>
                        <label class="filter-item-row">
                            <span>500.000đ - 1.000.000đ</span>
                            <input type="radio" name="priceRange" value="500k_1m" class="filter-checkbox" <%= "500k_1m".equals(selectedPriceRange) ? "checked" : "" %>>
                        </label>
                        <label class="filter-item-row">
                            <span>1.000.000đ - 2.000.000đ</span>
                            <input type="radio" name="priceRange" value="1m_2m" class="filter-checkbox" <%= "1m_2m".equals(selectedPriceRange) ? "checked" : "" %>>
                        </label>
                        <label class="filter-item-row">
                            <span>2.000.000đ - 3.000.000đ</span>
                            <input type="radio" name="priceRange" value="2m_3m" class="filter-checkbox" <%= "2m_3m".equals(selectedPriceRange) ? "checked" : "" %>>
                        </label>
                        <label class="filter-item-row">
                            <span>Trên 3.000.000đ</span>
                            <input type="radio" name="priceRange" value="above_3m" class="filter-checkbox" <%= "above_3m".equals(selectedPriceRange) ? "checked" : "" %>>
                        </label>
                    </div>
                </div>

                <!-- Action Buttons -->
                <div class="d-flex flex-column gap-2 mt-4">
                    <a href="${pageContext.request.contextPath}/products" class="btn btn-filter-clear text-center text-decoration-none">XOÁ BỘ LỌC</a>
                    <button type="submit" class="btn btn-filter-apply">XEM KẾT QUẢ</button>
                </div>
            </form>
        </div>

        <!-- Products Grid (Anh 3 Fix: Chỉ sản phẩm giam gia moi hien badge va hien % SALE!) -->
        <div class="col-lg-9">
            <% if (products != null && !products.isEmpty()) { %>
                <div class="row g-4">
                    <% for (Product p : products) { %>
                        <div class="col-md-4 col-sm-6">
                            <div class="product-card">
                                <div class="product-img-wrapper">
                                    <% if (p.getSalePrice() > 0 && p.getSalePrice() < p.getPrice()) { 
                                        int percent = (int) Math.round((1.0 - (p.getSalePrice() / p.getPrice())) * 100.0);
                                    %>
                                        <span class="product-badge">-<%= percent %>% SALE</span>
                                    <% } %>
                                    <a href="${pageContext.request.contextPath}/product-detail?id=<%= p.getId() %>">
                                        <%
                                            String pImg = p.getImage();
                                            if (pImg != null && !pImg.startsWith("http://") && !pImg.startsWith("https://")) {
                                                if (!pImg.startsWith(request.getContextPath())) {
                                                    pImg = request.getContextPath() + (pImg.startsWith("/") ? "" : "/") + pImg;
                                                }
                                            }
                                        %>
                                        <img src="<%= pImg %>" alt="<%= p.getName() %>" loading="lazy"
                                             onerror="this.src='https://images.unsplash.com/photo-1523381210434-271e8be1f52b?w=600'">
                                    </a>
                                </div>
                                
                                <!-- Nút TÙY CHỌN Đỏ -->
                                <a href="${pageContext.request.contextPath}/product-detail?id=<%= p.getId() %>" class="btn-tuy-chon">
                                    TÙY CHỌN
                                </a>

                                <div class="product-info-box">
                                    <a href="${pageContext.request.contextPath}/product-detail?id=<%= p.getId() %>" class="product-title-custom">
                                        <%= p.getName() %>
                                    </a>
                                    <div class="product-brand"><%= p.getCategoryName() != null ? p.getCategoryName() : "HIGHFASHION" %></div>
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
            <% } else { %>
                <div class="text-center py-5 bg-white border">
                    <i class="bi bi-search fs-1 text-muted"></i>
                    <h4 class="fw-bold mt-3">Không tìm thấy sản phẩm nào!</h4>
                    <p class="text-muted">Rất tiếc, chưa có sản phẩm nào thuộc bộ lọc này. Hãy thử bấm "XOÁ BỘ LỌC".</p>
                    <a href="${pageContext.request.contextPath}/products" class="btn btn-accent rounded-pill px-4 mt-2">Xem Tất Cả Sản Phẩm</a>
                </div>
            <% } %>
        </div>
    </div>
</div>

<jsp:include page="includes/footer.jsp" />
