<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, model.Product, model.Category, java.text.NumberFormat, java.util.Locale, dao.ProductStockDAO" %>
<%
    List<Product> products = (List<Product>) request.getAttribute("products");
    List<Category> categories = (List<Category>) request.getAttribute("categories");
    NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
    String productMessage = (String) session.getAttribute("adminProductMessage");
    if (productMessage != null) { session.removeAttribute("adminProductMessage"); }
%>

<jsp:include page="includes/header.jsp" />
<jsp:include page="includes/navbar.jsp" />

<div class="container my-4">
    <% if (productMessage != null) { %>
        <div class="alert alert-info" role="alert"><%= productMessage %></div>
    <% } %>
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
                            <td>
                                <span class="badge bg-info bg-opacity-10 text-info px-3 py-1 rounded-pill fw-bold"><%= p.getQuantity() %> cái</span>
                                <div class="small text-muted mt-1">
                                    <% for (String sizeOption : ProductStockDAO.parseSizes(p.getSize())) { %>
                                        <span class="me-1"><%= sizeOption %>: <%= p.getStockForSize(sizeOption) %></span>
                                    <% } %>
                                </div>
                            </td>
                            <td>
                                <% if (p.isIsFeatured()) { %>
                                    <span class="badge bg-warning text-dark"><i class="bi bi-star-fill me-1"></i>Nổi bật</span>
                                <% } %>
                            </td>
                            <td class="pe-4 text-end">
                                <button type="button" class="btn btn-outline-primary btn-sm rounded-circle"
                                        data-bs-toggle="modal" data-bs-target="#stockModal<%= p.getId() %>"
                                        title="Quản lý tồn kho theo size">
                                    <i class="bi bi-boxes"></i>
                                </button>
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

<% if (products != null) {
    for (Product p : products) { %>
        <div class="modal fade" id="stockModal<%= p.getId() %>" tabindex="-1">
            <div class="modal-dialog">
                <div class="modal-content rounded-4 border-0">
                    <div class="modal-header">
                        <h5 class="modal-title fw-bold">Tồn kho theo size — <%= p.getName() %></h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                    </div>
                    <form action="${pageContext.request.contextPath}/admin/products" method="POST">
                        <input type="hidden" name="action" value="updateStock">
                        <input type="hidden" name="id" value="<%= p.getId() %>">
                        <div class="modal-body">
                            <% for (String sizeOption : ProductStockDAO.parseSizes(p.getSize())) { %>
                                <div class="row align-items-center mb-3">
                                    <div class="col-5">
                                        <label class="form-label fw-bold mb-0">Size <%= sizeOption %></label>
                                        <input type="hidden" name="stockSize" value="<%= sizeOption %>">
                                    </div>
                                    <div class="col-7">
                                        <input type="number" name="stockQuantity" class="form-control"
                                               value="<%= p.getStockForSize(sizeOption) %>" min="0" required>
                                    </div>
                                </div>
                            <% } %>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-light rounded-pill" data-bs-dismiss="modal">Hủy</button>
                            <button type="submit" class="btn btn-accent rounded-pill fw-bold">Lưu tồn kho</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
<%  }
   } %>

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
                        <div class="col-md-6">
                            <label class="form-label fw-bold">Kích cỡ (Size)</label>
                            <input type="text" name="size" id="addProductSizes" class="form-control" value="S, M, L, XL" required>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label fw-bold">Màu sắc</label>
                            <input type="text" name="color" class="form-control" value="Trắng, Đen, Xanh">
                        </div>
                        <div class="col-12">
                            <label class="form-label fw-bold">Số lượng tồn kho cho từng size *</label>
                            <div id="addSizeStockInputs" class="row g-2"></div>
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

<script>
function renderAddSizeStockInputs() {
    var sizeField = document.getElementById('addProductSizes');
    var container = document.getElementById('addSizeStockInputs');
    var seen = {};
    var sizes = [];
    container.textContent = '';
    sizeField.value.split(',').forEach(function(rawSize) {
        var size = rawSize.trim();
        var key = size.toLowerCase();
        if (!size || seen[key]) return;
        seen[key] = true;
        sizes.push(size);
    });

    var each = sizes.length ? Math.floor(50 / sizes.length) : 0;
    var remainder = sizes.length ? 50 % sizes.length : 0;
    sizes.forEach(function(size, index) {
        var column = document.createElement('div');
        column.className = 'col-sm-6';
        var row = document.createElement('div');
        row.className = 'input-group';
        var label = document.createElement('span');
        label.className = 'input-group-text';
        label.textContent = 'Size ' + size;
        var hiddenSize = document.createElement('input');
        hiddenSize.type = 'hidden';
        hiddenSize.name = 'stockSize';
        hiddenSize.value = size;
        var quantity = document.createElement('input');
        quantity.type = 'number';
        quantity.name = 'stockQuantity';
        quantity.className = 'form-control';
        quantity.min = '0';
        quantity.value = String(each + (index < remainder ? 1 : 0));
        quantity.required = true;

        row.appendChild(label);
        row.appendChild(hiddenSize);
        row.appendChild(quantity);
        column.appendChild(row);
        container.appendChild(column);
    });
}
document.getElementById('addProductSizes').addEventListener('input', renderAddSizeStockInputs);
renderAddSizeStockInputs();
</script>

<jsp:include page="includes/footer.jsp" />
