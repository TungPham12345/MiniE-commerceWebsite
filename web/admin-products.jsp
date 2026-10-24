<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, model.Product, model.Category, java.text.NumberFormat, java.util.Locale" %>
<%
    List<Product> products = (List<Product>) request.getAttribute("products");
    List<Category> categories = (List<Category>) request.getAttribute("categories");
    NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
%>

<jsp:include page="includes/header.jsp" />
<jsp:include page="includes/navbar.jsp" />

<div class="container my-4">
    <div class="d-flex justify-content-between align-items-center bg-white p-4 rounded-4 border shadow-sm mb-4">
        <div>
            <h2 class="fw-bold m-0"><i class="bi bi-box-seam me-2"></i>Quản Lý Sản Phẩm Quần Áo</h2>
            <p class="text-muted small m-0">Thêm mới, chỉnh sửa thông tin hoặc xóa sản phẩm khỏi kho hàng</p>
        </div>
        <button class="btn btn-accent rounded-pill px-4 fw-bold" data-bs-toggle="modal" data-bs-target="#addProductModal">
            <i class="bi bi-plus-lg me-1"></i> Thêm Sản Phẩm Mới
        </button>
    </div>

    <!-- Products Table -->
    <div class="bg-white rounded-4 border shadow-sm overflow-hidden">
        <table class="table table-hover align-middle m-0">
            <thead class="table-light">
                <tr>
                    <th class="ps-4">ID</th>
                    <th>Hình Ảnh</th>
                    <th>Tên Sản Phẩm</th>
                    <th>Danh Mục</th>
                    <th>Giá Gốc</th>
                    <th>Giá Khuyến Mãi</th>
                    <th>Kho</th>
                    <th>Nổi Bật</th>
                    <th class="pe-4 text-end">Thao Tác</th>
                </tr>
            </thead>
            <tbody>
                <% if (products != null) {
                    for (Product p : products) { %>
                        <tr>
                            <td class="ps-4 fw-bold">#<%= p.getId() %></td>
                            <td>
                                <%
                                    String admImg = p.getImage();
                                    if (admImg != null && !admImg.startsWith("http://") && !admImg.startsWith("https://")) {
                                        if (!admImg.startsWith(request.getContextPath())) {
                                            admImg = request.getContextPath() + (admImg.startsWith("/") ? "" : "/") + admImg;
                                        }
                                    }
                                %>
                                <img src="<%= admImg %>" alt="<%= p.getName() %>" class="rounded-3" style="width: 50px; height: 60px; object-fit: cover;">
                            </td>
                            <td class="fw-bold text-dark"><%= p.getName() %></td>
                            <td><span class="badge bg-light text-dark border"><%= p.getCategoryName() != null ? p.getCategoryName() : "Thời trang" %></span></td>
                            <td><%= fmt.format(p.getPrice()) %> đ</td>
                            <td class="text-danger fw-bold"><%= fmt.format(p.getEffectivePrice()) %> đ</td>
                            <td><span class="badge bg-info bg-opacity-10 text-info px-3 py-1 rounded-pill fw-bold"><%= p.getQuantity() %> cái</span></td>
                            <td>
                                <% if (p.isIsFeatured()) { %>
                                    <span class="badge bg-warning text-dark"><i class="bi bi-star-fill me-1"></i>Nổi bật</span>
                                <% } %>
                            </td>
                            <td class="pe-4 text-end">
                                <a href="${pageContext.request.contextPath}/admin/products?action=delete&id=<%= p.getId() %>" 
                                   class="btn btn-outline-danger btn-sm rounded-circle"
                                   onclick="return confirm('Bạn có chắc chắn muốn xóa sản phẩm #<%= p.getId() %> này?');">
                                    <i class="bi bi-trash"></i>
                                </a>
                            </td>
                        </tr>
                <%  }
                   } %>
            </tbody>
        </table>
    </div>
</div>

<!-- Modal Add Product -->
<div class="modal fade" id="addProductModal" tabindex="-1">
    <div class="modal-dialog modal-lg">
        <div class="modal-content rounded-4 border-0">
            <div class="modal-header border-bottom p-4">
                <h5 class="modal-title fw-bold"><i class="bi bi-plus-circle me-2"></i>Thêm Sản Phẩm Mới</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <form action="${pageContext.request.contextPath}/admin/products" method="POST">
                <input type="hidden" name="action" value="add">
                <div class="modal-body p-4">
                    <div class="row g-3">
                        <div class="col-md-8">
                            <label class="form-label fw-bold">Tên sản phẩm *</label>
                            <input type="text" name="name" class="form-control" required placeholder="Áo sơ mi nam...">
                        </div>
                        <div class="col-md-4">
                            <label class="form-label fw-bold">Danh mục *</label>
                            <select name="categoryId" class="form-select" required>
                                <% if (categories != null) {
                                    for (Category c : categories) { %>
                                        <option value="<%= c.getId() %>"><%= c.getName() %></option>
                                <%  }
                                   } %>
                            </select>
                        </div>
                        <div class="col-md-4">
                            <label class="form-label fw-bold">Giá bán gốc (đ) *</label>
                            <input type="number" name="price" class="form-control" required placeholder="450000">
                        </div>
                        <div class="col-md-4">
                            <label class="form-label fw-bold">Giá khuyến mãi (đ)</label>
                            <input type="number" name="salePrice" class="form-control" value="0">
                        </div>
                        <div class="col-md-4">
                            <label class="form-label fw-bold">Số lượng kho *</label>
                            <input type="number" name="quantity" class="form-control" value="50" required>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label fw-bold">Kích cỡ (Size)</label>
                            <input type="text" name="size" class="form-control" value="S, M, L, XL">
                        </div>
                        <div class="col-md-6">
                            <label class="form-label fw-bold">Màu sắc</label>
                            <input type="text" name="color" class="form-control" value="Trắng, Đen, Xanh">
                        </div>
                        <div class="col-12">
                            <label class="form-label fw-bold">Link Ảnh sản phẩm (URL Image) *</label>
                            <input type="url" name="image" class="form-control" required placeholder="https://images.unsplash.com/...">
                        </div>
                        <div class="col-12">
                            <label class="form-label fw-bold">Mô tả sản phẩm</label>
                            <textarea name="description" class="form-control" rows="3" placeholder="Chất liệu cotton thoáng mát..."></textarea>
                        </div>
                        <div class="col-12">
                            <div class="form-check">
                                <input class="form-check-input" type="checkbox" name="isFeatured" id="isFeatured">
                                <label class="form-check-label fw-bold" for="isFeatured">
                                    Hiển thị ở mục Sản phẩm Nổi Bật (Trang chủ)
                                </label>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="modal-footer border-top p-4">
                    <button type="button" class="btn btn-light rounded-pill px-4" data-bs-dismiss="modal">Hủy</button>
                    <button type="submit" class="btn btn-accent rounded-pill px-4 fw-bold">Lưu Sản Phẩm</button>
                </div>
            </form>
        </div>
    </div>
</div>

<jsp:include page="includes/footer.jsp" />
