<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, model.Product, model.Category, java.text.NumberFormat, java.util.Locale, dao.ProductStockDAO" %>
<%!
    private String escapeHtml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
%>
<%
    List<Product> products = (List<Product>) request.getAttribute("products");
    List<Category> categories = (List<Category>) request.getAttribute("categories");
    NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
    String productMessage = (String) session.getAttribute("adminProductMessage");
    boolean productMessageSuccess = productMessage != null && productMessage.startsWith("Đã ");
    if (productMessage != null) { session.removeAttribute("adminProductMessage"); }
%>

<jsp:include page="includes/header.jsp" />
<jsp:include page="includes/navbar.jsp" />

<% if (productMessage != null) { %>
    <div id="adminProductToast"
         class="admin-product-toast <%= productMessageSuccess ? "is-success" : "is-error" %>"
         role="status" aria-live="<%= productMessageSuccess ? "polite" : "assertive" %>">
        <span class="admin-product-toast-icon" aria-hidden="true">
            <i class="bi <%= productMessageSuccess ? "bi-check-lg" : "bi-exclamation-lg" %>"></i>
        </span>
        <div class="admin-product-toast-content">
            <strong><%= productMessageSuccess ? "Thành công" : "Không thành công" %></strong>
            <span><%= escapeHtml(productMessage) %></span>
        </div>
        <button type="button" class="admin-product-toast-close" aria-label="Đóng thông báo">
            <i class="bi bi-x-lg" aria-hidden="true"></i>
        </button>
    </div>
<% } %>

<div class="container-fluid admin-products-page px-3 px-xl-4 my-4">
    <div class="d-flex justify-content-between align-items-center bg-white p-4 rounded-4 border shadow-sm mb-4">
        <div>
            <h2 class="fw-bold m-0"><i class="bi bi-box-seam me-2"></i>Quản Lý Sản Phẩm Quần Áo</h2>
            <p class="text-muted small m-0">Thêm mới, chỉnh sửa thông tin hoặc xóa sản phẩm khỏi kho hàng</p>
        </div>
        <button class="btn btn-admin-save rounded-pill px-4 fw-bold" data-bs-toggle="modal" data-bs-target="#addProductModal">
            <i class="bi bi-plus-lg me-1"></i> Thêm Sản Phẩm Mới
        </button>
    </div>

    <!-- Products Table -->
    <div class="bg-white rounded-4 border shadow-sm admin-products-table-wrap">
        <table class="table table-hover align-middle m-0 admin-products-table">
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
                            <td class="admin-product-price"><%= fmt.format(p.getPrice()) %> đ</td>
                            <td class="text-danger fw-bold admin-product-price"><%= fmt.format(p.getEffectivePrice()) %> đ</td>
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
                            <td class="pe-4 admin-product-actions">
                                <div class="admin-product-action-buttons">
                                    <button type="button" class="btn btn-outline-primary btn-sm rounded-circle"
                                            data-bs-toggle="modal" data-bs-target="#editProductModal<%= p.getId() %>"
                                            title="Chỉnh sửa thông tin sản phẩm" aria-label="Chỉnh sửa sản phẩm #<%= p.getId() %>">
                                        <i class="bi bi-pencil-square"></i>
                                    </button>
                                    <a href="${pageContext.request.contextPath}/admin/products?action=delete&id=<%= p.getId() %>"
                                       class="btn btn-outline-danger btn-sm rounded-circle"
                                       onclick="return confirm('Bạn có chắc chắn muốn xóa sản phẩm #<%= p.getId() %> này?');"
                                       title="Xóa sản phẩm" aria-label="Xóa sản phẩm #<%= p.getId() %>">
                                        <i class="bi bi-trash"></i>
                                    </a>
                                </div>
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
        <div class="modal fade" id="editProductModal<%= p.getId() %>" tabindex="-1" aria-hidden="true">
            <div class="modal-dialog modal-xl modal-dialog-scrollable">
                <div class="modal-content rounded-4 border-0">
                    <div class="modal-header border-bottom p-4">
                        <div>
                            <h5 class="modal-title fw-bold"><i class="bi bi-pencil-square me-2"></i>Chỉnh sửa sản phẩm</h5>
                            <div class="small text-muted">Cập nhật thông tin và tồn kho cho từng size</div>
                        </div>
                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                    </div>
                    <form action="${pageContext.request.contextPath}/admin/products" method="POST"
                          enctype="multipart/form-data" class="admin-product-image-form"
                          data-image-url-required="true" novalidate>
                        <input type="hidden" name="action" value="edit">
                        <input type="hidden" name="id" value="<%= p.getId() %>">
                        <div class="modal-body p-4">
                            <div class="alert alert-danger py-2 mb-3 d-none" data-form-error role="alert">
                                Vui lòng nhập đầy đủ các trường bắt buộc trước khi lưu.
                            </div>
                            <div class="row g-3">
                                <div class="col-md-8">
                                    <label class="form-label fw-bold">Tên sản phẩm <span class="required-star">*</span></label>
                                    <input type="text" name="name" class="form-control" value="<%= escapeHtml(p.getName()) %>" required>
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label fw-bold">Danh mục <span class="required-star">*</span></label>
                                    <select name="categoryId" class="form-select" required>
                                        <% if (categories != null) {
                                            for (Category c : categories) { %>
                                                <option value="<%= c.getId() %>" <%= c.getId() == p.getCategoryId() ? "selected" : "" %>><%= escapeHtml(c.getName()) %></option>
                                        <%  }
                                           } %>
                                    </select>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label fw-bold">Giá bán gốc (đ) <span class="required-star">*</span></label>
                                    <input type="number" name="price" class="form-control" min="0" step="any" value="<%= p.getPrice() %>" required>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label fw-bold">Giá khuyến mãi (đ)</label>
                                    <input type="number" name="salePrice" class="form-control" min="0" step="any" value="<%= p.getSalePrice() %>">
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label fw-bold">Kích cỡ (phân cách bằng dấu phẩy) <span class="required-star">*</span></label>
                                    <input type="text" name="size" id="editProductSizes<%= p.getId() %>" class="form-control"
                                           data-stock-inputs="editSizeStockInputs<%= p.getId() %>"
                                           value="<%= escapeHtml(p.getSize()) %>" required>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label fw-bold">Màu sắc</label>
                                    <input type="text" name="color" class="form-control" value="<%= escapeHtml(p.getColor()) %>">
                                </div>
                                <div class="col-12">
                                    <label class="form-label fw-bold">Tồn kho theo size <span class="required-star">*</span></label>
                                    <div id="editSizeStockInputs<%= p.getId() %>" class="row g-2">
                                        <% for (String sizeOption : ProductStockDAO.parseSizes(p.getSize())) { %>
                                            <div class="col-sm-6">
                                                <div class="input-group">
                                                    <span class="input-group-text">Size <%= escapeHtml(sizeOption) %></span>
                                                    <input type="hidden" name="stockSize" data-stock-size="<%= escapeHtml(sizeOption) %>"
                                                           value="<%= escapeHtml(sizeOption) %>">
                                                    <input type="number" name="stockQuantity" class="form-control"
                                                           value="<%= p.getStockForSize(sizeOption) %>" min="0" required>
                                                </div>
                                            </div>
                                        <% } %>
                                    </div>
                                </div>
                                <div class="col-12">
                                    <label class="form-label fw-bold">Ảnh sản phẩm <span class="required-star">*</span></label>
                                    <input type="text" name="image" class="form-control" data-image-url
                                           value="<%= escapeHtml(p.getImage()) %>" placeholder="Nhập URL hoặc đường dẫn ảnh">
                                    <div class="form-text mb-2">Giữ ảnh hiện tại, nhập đường dẫn khác, hoặc tải/dán ảnh mới ở bên dưới.</div>
                                    <div class="admin-product-image-upload">
                                        <input type="file" name="imageFile" class="form-control" data-image-file
                                               accept="image/png,image/jpeg,image/gif,image/webp">
                                        <div class="admin-product-image-dropzone mt-2" data-image-dropzone
                                             tabindex="0" role="region" aria-label="Vùng dán ảnh từ bộ nhớ tạm">
                                            <i class="bi bi-image me-2" aria-hidden="true"></i>
                                            Kéo thả ảnh vào đây hoặc nhấp vào vùng này rồi nhấn Ctrl+V để dán ảnh
                                        </div>
                                        <div class="admin-product-image-preview mt-2" data-image-preview-container hidden>
                                            <img data-image-preview alt="Xem trước ảnh sản phẩm">
                                            <span data-image-file-name></span>
                                            <button type="button" class="admin-product-image-remove"
                                                    data-image-remove aria-label="Xóa ảnh đã chọn" title="Xóa ảnh đã chọn">
                                                <i class="bi bi-trash" aria-hidden="true"></i>
                                            </button>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-12">
                                    <label class="form-label fw-bold">Mô tả sản phẩm</label>
                                    <textarea name="description" class="form-control" rows="4"><%= escapeHtml(p.getDescription()) %></textarea>
                                </div>
                                <div class="col-12">
                                    <div class="form-check">
                                        <input class="form-check-input" type="checkbox" name="isFeatured"
                                               id="editFeatured<%= p.getId() %>" <%= p.isIsFeatured() ? "checked" : "" %>>
                                        <label class="form-check-label fw-bold" for="editFeatured<%= p.getId() %>">
                                            Hiển thị ở mục Sản phẩm Nổi Bật
                                        </label>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="modal-footer border-top p-4">
                            <button type="button" class="btn btn-admin-cancel rounded-pill px-4" data-bs-dismiss="modal">Hủy</button>
                            <button type="submit" class="btn btn-admin-save rounded-pill px-4 fw-bold">Lưu thay đổi</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
<%  }
   } %>

<!-- Modal Add Product -->
<div class="modal fade" id="addProductModal" tabindex="-1">
    <div class="modal-dialog modal-lg modal-dialog-scrollable">
        <div class="modal-content rounded-4 border-0">
            <div class="modal-header border-bottom p-4">
                <h5 class="modal-title fw-bold"><i class="bi bi-plus-circle me-2"></i>Thêm Sản Phẩm Mới</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <form action="${pageContext.request.contextPath}/admin/products" method="POST"
                  enctype="multipart/form-data" class="admin-product-image-form"
                  data-image-url-required="true" novalidate>
                <input type="hidden" name="action" value="add">
                <div class="modal-body p-4">
                    <div class="alert alert-danger py-2 mb-3 d-none" data-form-error role="alert">
                        Vui lòng nhập đầy đủ các trường bắt buộc trước khi lưu.
                    </div>
                    <div class="row g-3">
                        <div class="col-md-8">
                            <label class="form-label fw-bold">Tên sản phẩm <span class="required-star">*</span></label>
                            <input type="text" name="name" class="form-control" required placeholder="Áo sơ mi nam...">
                        </div>
                        <div class="col-md-4">
                            <label class="form-label fw-bold">Danh mục <span class="required-star">*</span></label>
                            <select name="categoryId" class="form-select" required>
                                <% if (categories != null) {
                                    for (Category c : categories) { %>
                                        <option value="<%= c.getId() %>"><%= c.getName() %></option>
                                <%  }
                                   } %>
                            </select>
                        </div>
                        <div class="col-md-4">
                            <label class="form-label fw-bold">Giá bán gốc (đ) <span class="required-star">*</span></label>
                            <input type="number" name="price" class="form-control" required placeholder="450000">
                        </div>
                        <div class="col-md-4">
                            <label class="form-label fw-bold">Giá khuyến mãi (đ)</label>
                            <input type="number" name="salePrice" class="form-control" value="0">
                        </div>
                        <div class="col-md-6">
                            <label class="form-label fw-bold">Kích cỡ (Size) <span class="required-star">*</span></label>
                            <input type="text" name="size" id="addProductSizes" class="form-control" required
                                   placeholder="Ví dụ: S, M, L, XL">
                            <div class="form-text">Tự nhập các size, cách nhau bằng dấu phẩy. Tồn kho theo size sẽ hiện bên dưới.</div>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label fw-bold">Màu sắc</label>
                            <input type="text" name="color" class="form-control" value="Trắng, Đen, Xanh">
                        </div>
                        <div class="col-12">
                            <label class="form-label fw-bold">Số lượng tồn kho cho từng size <span class="required-star">*</span></label>
                            <div id="addSizeStockInputs" class="row g-2"></div>
                        </div>
                        <div class="col-12">
                            <label class="form-label fw-bold">Ảnh sản phẩm <span class="required-star">*</span></label>
                            <input type="url" name="image" class="form-control" data-image-url
                                   required placeholder="Nhập URL ảnh, ví dụ: https://...">
                            <div class="form-text mb-2">Hoặc chọn tệp ảnh, kéo thả vào vùng bên dưới, hay nhấn Ctrl+V để dán ảnh.</div>
                            <div class="admin-product-image-upload">
                                <input type="file" name="imageFile" class="form-control" data-image-file
                                       accept="image/png,image/jpeg,image/gif,image/webp">
                                <div class="admin-product-image-dropzone mt-2" data-image-dropzone
                                     tabindex="0" role="region" aria-label="Vùng dán ảnh từ bộ nhớ tạm">
                                    <i class="bi bi-image me-2" aria-hidden="true"></i>
                                    Kéo thả ảnh vào đây hoặc nhấp vào vùng này rồi nhấn Ctrl+V để dán ảnh
                                </div>
                                <div class="admin-product-image-preview mt-2" data-image-preview-container hidden>
                                    <img data-image-preview alt="Xem trước ảnh sản phẩm">
                                    <span data-image-file-name></span>
                                    <button type="button" class="admin-product-image-remove"
                                            data-image-remove aria-label="Xóa ảnh đã chọn" title="Xóa ảnh đã chọn">
                                        <i class="bi bi-trash" aria-hidden="true"></i>
                                    </button>
                                </div>
                            </div>
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
                    <button type="button" class="btn btn-admin-cancel rounded-pill px-4" data-bs-dismiss="modal">Hủy</button>
                    <button type="submit" class="btn btn-admin-save rounded-pill px-4 fw-bold">Lưu Sản Phẩm</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
function renderSizeStockInputs(sizeField, container) {
    var previousStocks = Object.create(null);
    var hasExistingInputs = false;
    container.querySelectorAll('[data-stock-size]').forEach(function(hiddenSize) {
        var quantityInput = hiddenSize.parentElement.querySelector('[name="stockQuantity"]');
        if (quantityInput) {
            hasExistingInputs = true;
            previousStocks[hiddenSize.dataset.stockSize.toLowerCase()] = quantityInput.value;
        }
    });

    var seen = Object.create(null);
    var sizes = [];
    container.textContent = '';
    sizeField.value.split(',').forEach(function(rawSize) {
        var size = rawSize.trim();
        var key = size.toLowerCase();
        if (!size || seen[key]) return;
        seen[key] = true;
        sizes.push(size);
    });

    sizes.forEach(function(size) {
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
        hiddenSize.dataset.stockSize = size;
        var quantity = document.createElement('input');
        quantity.type = 'number';
        quantity.name = 'stockQuantity';
        quantity.className = 'form-control';
        quantity.min = '0';
        var key = size.toLowerCase();
        quantity.value = Object.prototype.hasOwnProperty.call(previousStocks, key)
                ? previousStocks[key]
                : '';
        quantity.required = true;

        row.appendChild(label);
        row.appendChild(hiddenSize);
        row.appendChild(quantity);
        column.appendChild(row);
        container.appendChild(column);
    });
}

document.querySelectorAll('[data-stock-inputs]').forEach(function(sizeField) {
    var container = document.getElementById(sizeField.dataset.stockInputs);
    sizeField.addEventListener('input', function() {
        renderSizeStockInputs(sizeField, container);
    });
});

var addProductSizes = document.getElementById('addProductSizes');
var addSizeStockInputs = document.getElementById('addSizeStockInputs');
addProductSizes.addEventListener('input', function() {
    renderSizeStockInputs(addProductSizes, addSizeStockInputs);
});
renderSizeStockInputs(addProductSizes, addSizeStockInputs);

document.querySelectorAll('.admin-product-image-form').forEach(function(form) {
    var imageUrl = form.querySelector('[data-image-url]');
    var imageFile = form.querySelector('[data-image-file]');
    var dropzone = form.querySelector('[data-image-dropzone]');
    var previewContainer = form.querySelector('[data-image-preview-container]');
    var previewImage = form.querySelector('[data-image-preview]');
    var fileName = form.querySelector('[data-image-file-name]');
    var removeImageButton = form.querySelector('[data-image-remove]');
    var formError = form.querySelector('[data-form-error]');
    var imageUrlType = imageUrl.type;

    function syncImageRequirement() {
        var hasFile = imageFile.files && imageFile.files.length > 0;
        imageUrl.required = !hasFile && form.dataset.imageUrlRequired === 'true';
        imageUrl.type = hasFile && imageUrlType === 'url' ? 'text' : imageUrlType;
    }

    function assignImageFile(file) {
        if (!file || !file.type || file.type.indexOf('image/') !== 0) return;
        var transfer = new DataTransfer();
        transfer.items.add(file);
        imageFile.files = transfer.files;
        imageFile.dispatchEvent(new Event('change', { bubbles: true }));
    }

    function updateImagePreview() {
        syncImageRequirement();
        if (!imageFile.files.length) {
            if (previewImage.dataset.objectUrl) {
                URL.revokeObjectURL(previewImage.dataset.objectUrl);
                delete previewImage.dataset.objectUrl;
            }
            previewImage.removeAttribute('src');
            fileName.textContent = '';
            previewContainer.hidden = true;
            return;
        }
        if (previewImage.dataset.objectUrl) {
            URL.revokeObjectURL(previewImage.dataset.objectUrl);
        }
        previewImage.dataset.objectUrl = URL.createObjectURL(imageFile.files[0]);
        previewImage.src = previewImage.dataset.objectUrl;
        fileName.textContent = imageFile.files[0].name;
        previewContainer.hidden = false;
    }

    imageFile.addEventListener('change', updateImagePreview);
    removeImageButton.addEventListener('click', function() {
        imageFile.value = '';
        updateImagePreview();
    });
    form.addEventListener('submit', function(event) {
        syncImageRequirement();
        if (form.checkValidity()) return;

        event.preventDefault();
        event.stopPropagation();
        form.classList.add('was-validated');
        formError.classList.remove('d-none');
        var firstInvalidField = form.querySelector(':invalid');
        if (firstInvalidField) {
            firstInvalidField.focus({ preventScroll: true });
            formError.scrollIntoView({ block: 'center', behavior: 'smooth' });
        }
    });
    form.addEventListener('input', function() {
        if (form.checkValidity()) {
            form.classList.remove('was-validated');
            formError.classList.add('d-none');
        }
    });
    form.addEventListener('change', function() {
        if (form.checkValidity()) {
            form.classList.remove('was-validated');
            formError.classList.add('d-none');
        }
    });

    dropzone.addEventListener('dragover', function(event) {
        event.preventDefault();
        dropzone.classList.add('is-dragging');
    });
    dropzone.addEventListener('dragleave', function() {
        dropzone.classList.remove('is-dragging');
    });
    dropzone.addEventListener('drop', function(event) {
        event.preventDefault();
        dropzone.classList.remove('is-dragging');
        if (event.dataTransfer.files.length) {
            assignImageFile(event.dataTransfer.files[0]);
        }
    });
    dropzone.addEventListener('paste', function(event) {
        var items = event.clipboardData && event.clipboardData.items;
        if (!items) return;
        for (var index = 0; index < items.length; index++) {
            if (items[index].kind === 'file' && items[index].type.indexOf('image/') === 0) {
                event.preventDefault();
                assignImageFile(items[index].getAsFile());
                return;
            }
        }
    });
});

var adminProductToast = document.getElementById('adminProductToast');
if (adminProductToast) {
    var dismissAdminProductToast = function() {
        adminProductToast.classList.remove('show');
        window.setTimeout(function() {
            adminProductToast.remove();
        }, 250);
    };
    var toastTimer = window.setTimeout(dismissAdminProductToast, 5000);
    adminProductToast.querySelector('.admin-product-toast-close').addEventListener('click', function() {
        window.clearTimeout(toastTimer);
        dismissAdminProductToast();
    });
    window.requestAnimationFrame(function() {
        adminProductToast.classList.add('show');
    });
}
</script>

<jsp:include page="includes/footer.jsp" />
