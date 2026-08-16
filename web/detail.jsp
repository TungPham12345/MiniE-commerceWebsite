<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, model.Product, model.Category, java.text.NumberFormat, java.util.Locale, model.CartItem" %>
<%
    Product product = (Product) request.getAttribute("product");
    List<Product> relatedProducts = (List<Product>) request.getAttribute("relatedProducts");
    NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
    
    // Check if user just added item to cart
    Boolean addedToCart = (Boolean) session.getAttribute("addedToCart");
    CartItem lastAddedItem = (CartItem) session.getAttribute("lastAddedItem");
    Integer cartSizeObj = (Integer) session.getAttribute("cartSize");
    Double cartTotalObj = (Double) session.getAttribute("cartTotal");
    int cartSize = cartSizeObj != null ? cartSizeObj : 0;
    double cartTotal = cartTotalObj != null ? cartTotalObj : 0;
    
    int catId = (product != null) ? product.getCategoryId() : 1;
%>

<jsp:include page="includes/header.jsp" />
<jsp:include page="includes/navbar.jsp" />

<div class="container my-4">
    <!-- Breadcrumb -->
    <nav aria-label="breadcrumb" class="mb-4">
        <ol class="breadcrumb">
            <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/home" class="text-decoration-none">Trang chủ</a></li>
            <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/products" class="text-decoration-none">Cửa hàng</a></li>
            <li class="breadcrumb-item active"><%= product != null ? product.getName() : "" %></li>
        </ol>
    </nav>

    <% if (product != null) { %>
    <div class="bg-white p-4 p-md-5 border mb-5">
        <div class="row g-5">
            <!-- Product Image -->
            <div class="col-lg-6">
                <div class="overflow-hidden border bg-light">
                    <img src="<%= product.getImage() %>" alt="<%= product.getName() %>" 
                         class="img-fluid w-100 object-fit-cover" style="max-height: 550px;"
                         onerror="this.src='https://images.unsplash.com/photo-1523381210434-271e8be1f52b?w=600'">
                </div>
            </div>

            <!-- Product Details -->
            <div class="col-lg-6 d-flex flex-column justify-content-center">
                <h2 class="fw-extrabold text-uppercase mb-2" style="letter-spacing: 0.5px;"><%= product.getName() %></h2>
                <div class="text-secondary small mb-3">
                    Thương hiệu: <span class="text-success fw-bold"><%= product.getCategoryName() != null ? product.getCategoryName().toUpperCase() : "HIGHFASHION" %></span> / Mã sản phẩm: <span class="text-success fw-bold">HF00<%= product.getId() %></span>
                </div>
                
                <div class="d-flex align-items-baseline gap-3 mb-4">
                    <span class="display-6 fw-extrabold text-danger">
                        <%= fmt.format(product.getEffectivePrice()) %>đ
                    </span>
                    <% if (product.getSalePrice() > 0 && product.getSalePrice() < product.getPrice()) { %>
                        <span class="fs-5 text-muted text-decoration-line-through">
                            <%= fmt.format(product.getPrice()) %>đ
                        </span>
                    <% } %>
                </div>

                <form action="${pageContext.request.contextPath}/cart" method="POST" id="buyForm">
                    <input type="hidden" name="action" value="add">
                    <input type="hidden" name="productId" value="<%= product.getId() %>">
                    <input type="hidden" name="size" id="selectedSizeInput" value="<%= catId == 4 ? "Freesize" : (catId == 3 ? "S" : "M") %>">
                    <input type="hidden" name="color" value="<%= product.getColor() != null ? product.getColor().split(",")[0].trim() : "Default" %>">

                    <!-- SIZE Selection Grid Khác Nhau Theo Loại Sản Phẩm -->
                    <div class="size-title">SIZE SẢN PHẨM</div>
                    <div class="size-grid" id="sizeGrid">
                        <% 
                            String[] currentSizes;
                            if (catId == 1) { // Áo Nam
                                currentSizes = new String[]{"S", "M", "L", "XL", "XXL"};
                            } else if (catId == 2) { // Quần Nam
                                currentSizes = new String[]{"28", "29", "30", "31", "32", "34"};
                            } else if (catId == 3) { // Thời Trang Nữ
                                currentSizes = new String[]{"XS", "S", "M", "L", "XL"};
                            } else { // Phụ Kiện
                                currentSizes = new String[]{"Freesize", "One Size"};
                            }
                            
                            for (int i = 0; i < currentSizes.length; i++) {
                                String sz = currentSizes[i];
                                boolean isDefaultActive = (i == 1 || (catId == 4 && i == 0));
                        %>
                            <div class="size-box <%= isDefaultActive ? "active" : "" %>" 
                                 onclick="selectSize('<%= sz %>', this)">
                                <%= sz %>
                            </div>
                        <% } %>
                    </div>

                    <!-- Quantity Stepper & Buy Now Button -->
                    <div class="d-flex align-items-center gap-3 mb-5">
                        <div class="quantity-stepper">
                            <button type="button" onclick="changeQty(-1)">-</button>
                            <input type="number" name="quantity" id="quantityInput" value="1" min="1" max="<%= product.getQuantity() %>" readonly>
                            <button type="button" onclick="changeQty(1)">+</button>
                        </div>

                        <button type="submit" class="btn-buy-now">
                            <i class="bi bi-bag-check-fill fs-5"></i> Mua ngay
                        </button>
                    </div>
                </form>

                <!-- Thanh Thông Tin Sản Phẩm Dạng Accordion (Tùy biến theo Áo Nam, Quần Nam, Nữ, Phụ Kiện) -->
                <div class="border-top">
                    <!-- Thanh 1: Thông tin chi tiết sản phẩm -->
                    <div class="info-accordion-item">
                        <div class="info-accordion-header" data-bs-toggle="collapse" data-bs-target="#infoDetailCollapse">
                            <span>Thông tin chi tiết sản phẩm</span>
                            <i class="bi bi-chevron-right fs-6"></i>
                        </div>
                        <div class="collapse show" id="infoDetailCollapse">
                            <div class="info-accordion-body">
                                <p class="mb-1">• XUẤT XỨ: VIỆT NAM (HIGHFASHION AUTHENTIC)</p>
                                <p class="mb-1">• SẢN PHẨM THUỘC CÔNG TY TNHH DICO STAR</p>
                                <p class="mb-3">• ĐỊA CHỈ: 561 SƯ VẠN HẠNH, PHƯỜNG HOÀ HƯNG, TP.HCM</p>
                                
                                <p class="fw-bold mb-1">CHI TIẾT VÀ BẢNG THÔNG SỐ KÍCH THƯỚC:</p>
                                <p class="mb-1">• CHẤT LIỆU: <%= catId == 1 ? "100% COTTON OXFORD THOÁNG MÁT" : (catId == 2 ? "DENIM KHẮC KHỔ CO GIÃN 4 CHIỀU" : (catId == 3 ? "LỤA SATIN / POLYESTER NỮ TÍNH" : "DA PU CAO CẤP / KIM LOẠI CHỐNG GỈ")) %></p>
                                <p class="mb-1">• FORM DÁNG: <%= catId == 3 ? "SLIM-FIT TÔN DÁNG QUYẾN RŨ" : "RELAXED FIT THỜI THƯỢNG" %></p>
                                <p class="mb-4">• ĐẶC ĐÍỂM: ĐƯỜNG MAY MŨI CHỈ TỈ MỈ CHUẨN XUẤT KHẨU</p>

                                <% if (catId == 1) { %>
                                    <!-- BẢNG THÔNG SỐ ÁO NAM -->
                                    <div class="text-center my-3 p-3 bg-light rounded border">
                                        <svg width="220" height="140" viewBox="0 0 200 140" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <path d="M50 30 L80 15 L120 15 L150 30 L170 65 L145 70 L140 45 L140 130 L60 130 L60 45 L55 70 L30 65 Z" stroke="#334155" stroke-width="2.5" fill="#ffffff"/>
                                            <line x1="180" y1="15" x2="180" y2="130" stroke="#e11d48" stroke-width="1.5" stroke-dasharray="3 3"/>
                                            <circle cx="180" cy="72" r="10" fill="#e11d48"/><text x="180" y="76" font-size="11" fill="#fff" text-anchor="middle" font-weight="bold">1</text>
                                            <line x1="60" y1="55" x2="140" y2="55" stroke="#e11d48" stroke-width="1.5"/>
                                            <circle cx="100" cy="55" r="10" fill="#e11d48"/><text x="100" y="59" font-size="11" fill="#fff" text-anchor="middle" font-weight="bold">2</text>
                                            <line x1="80" y1="15" x2="120" y2="15" stroke="#e11d48" stroke-width="1.5" stroke-dasharray="3 3"/>
                                            <circle cx="100" cy="15" r="10" fill="#e11d48"/><text x="100" y="19" font-size="11" fill="#fff" text-anchor="middle" font-weight="bold">3</text>
                                            <line x1="150" y1="30" x2="170" y2="65" stroke="#e11d48" stroke-width="1.5"/>
                                            <circle cx="160" cy="48" r="10" fill="#e11d48"/><text x="160" y="52" font-size="11" fill="#fff" text-anchor="middle" font-weight="bold">4</text>
                                        </svg>
                                    </div>
                                    <table class="specs-table text-center">
                                        <thead>
                                            <tr>
                                                <th>THÔNG SỐ ÁO NAM (CM)</th>
                                                <th>SIZE S</th>
                                                <th>SIZE M</th>
                                                <th>SIZE L</th>
                                                <th>SIZE XL</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <tr><td>(1) DÀI ÁO</td><td>68</td><td>70</td><td>72</td><td>74</td></tr>
                                            <tr><td>(2) VÒNG NGỰC</td><td>96</td><td>100</td><td>104</td><td>108</td></tr>
                                            <tr><td>(3) RỘNG VAI</td><td>42</td><td>44</td><td>46</td><td>48</td></tr>
                                            <tr><td>(4) DÀI TAY</td><td>20</td><td>21</td><td>22</td><td>23</td></tr>
                                        </tbody>
                                    </table>

                                <% } else if (catId == 2) { %>
                                    <!-- BẢNG THÔNG SỐ QUẦN NAM -->
                                    <div class="text-center my-3 p-3 bg-light rounded border">
                                        <svg width="220" height="140" viewBox="0 0 200 140" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <path d="M60 15 L140 15 L145 130 L110 130 L100 60 L90 130 L55 130 Z" stroke="#334155" stroke-width="2.5" fill="#ffffff"/>
                                            <line x1="160" y1="15" x2="160" y2="130" stroke="#e11d48" stroke-width="1.5" stroke-dasharray="3 3"/>
                                            <circle cx="160" cy="72" r="10" fill="#e11d48"/><text x="160" y="76" font-size="11" fill="#fff" text-anchor="middle" font-weight="bold">1</text>
                                            <line x1="60" y1="15" x2="140" y2="15" stroke="#e11d48" stroke-width="1.5"/>
                                            <circle cx="100" cy="15" r="10" fill="#e11d48"/><text x="100" y="19" font-size="11" fill="#fff" text-anchor="middle" font-weight="bold">2</text>
                                            <line x1="100" y1="15" x2="100" y2="60" stroke="#e11d48" stroke-width="1.5"/>
                                            <circle cx="100" cy="38" r="10" fill="#e11d48"/><text x="100" y="42" font-size="11" fill="#fff" text-anchor="middle" font-weight="bold">3</text>
                                            <line x1="55" y1="130" x2="90" y2="130" stroke="#e11d48" stroke-width="1.5"/>
                                            <circle cx="72" cy="130" r="10" fill="#e11d48"/><text x="72" y="134" font-size="11" fill="#fff" text-anchor="middle" font-weight="bold">4</text>
                                        </svg>
                                    </div>
                                    <table class="specs-table text-center">
                                        <thead>
                                            <tr>
                                                <th>THÔNG SỐ QUẦN NAM</th>
                                                <th>SIZE 29</th>
                                                <th>SIZE 30</th>
                                                <th>SIZE 31</th>
                                                <th>SIZE 32</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <tr><td>(1) DÀI QUẦN (CM)</td><td>96</td><td>98</td><td>100</td><td>102</td></tr>
                                            <tr><td>(2) LƯNG QUẦN (CM)</td><td>76</td><td>79</td><td>82</td><td>85</td></tr>
                                            <tr><td>(3) ĐÁY QUẦN (CM)</td><td>28</td><td>29</td><td>30</td><td>31</td></tr>
                                            <tr><td>(4) ỐNG QUẦN (CM)</td><td>16.5</td><td>17</td><td>17.5</td><td>18</td></tr>
                                        </tbody>
                                    </table>

                                <% } else if (catId == 3) { %>
                                    <!-- BẢNG THÔNG SỐ THỜI TRANG NỮ -->
                                    <div class="text-center my-3 p-3 bg-light rounded border">
                                        <svg width="220" height="140" viewBox="0 0 200 140" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <path d="M70 20 C70 20, 85 10, 100 10 C115 10, 130 20, 130 20 L145 45 C145 45, 125 50, 120 60 C115 70, 135 130, 135 130 L65 130 C65 130, 85 70, 80 60 C75 50, 55 45, 55 45 Z" stroke="#334155" stroke-width="2.5" fill="#ffffff"/>
                                            <line x1="160" y1="10" x2="160" y2="130" stroke="#e11d48" stroke-width="1.5" stroke-dasharray="3 3"/>
                                            <circle cx="160" cy="70" r="10" fill="#e11d48"/><text x="160" y="74" font-size="11" fill="#fff" text-anchor="middle" font-weight="bold">1</text>
                                            <line x1="70" y1="40" x2="130" y2="40" stroke="#e11d48" stroke-width="1.5"/>
                                            <circle cx="100" cy="40" r="10" fill="#e11d48"/><text x="100" y="44" font-size="11" fill="#fff" text-anchor="middle" font-weight="bold">2</text>
                                            <line x1="80" y1="65" x2="120" y2="65" stroke="#e11d48" stroke-width="1.5"/>
                                            <circle cx="100" cy="65" r="10" fill="#e11d48"/><text x="100" y="69" font-size="11" fill="#fff" text-anchor="middle" font-weight="bold">3</text>
                                            <line x1="70" y1="95" x2="130" y2="95" stroke="#e11d48" stroke-width="1.5"/>
                                            <circle cx="100" cy="95" r="10" fill="#e11d48"/><text x="100" y="99" font-size="11" fill="#fff" text-anchor="middle" font-weight="bold">4</text>
                                        </svg>
                                    </div>
                                    <table class="specs-table text-center">
                                        <thead>
                                            <tr>
                                                <th>THÔNG SỐ NỮ (CM)</th>
                                                <th>SIZE XS</th>
                                                <th>SIZE S</th>
                                                <th>SIZE M</th>
                                                <th>SIZE L</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <tr><td>(1) DÀI VÁY/ÁO</td><td>86</td><td>88</td><td>90</td><td>92</td></tr>
                                            <tr><td>(2) VÒNG NGỰC</td><td>80</td><td>84</td><td>88</td><td>92</td></tr>
                                            <tr><td>(3) VÒNG EO</td><td>62</td><td>66</td><td>70</td><td>74</td></tr>
                                            <tr><td>(4) VÒNG MÔNG</td><td>86</td><td>90</td><td>94</td><td>98</td></tr>
                                        </tbody>
                                    </table>

                                <% } else { %>
                                    <!-- BẢNG THÔNG SỐ PHỤ KIỆN -->
                                    <table class="specs-table text-center mt-3">
                                        <thead>
                                            <tr>
                                                <th>THÔNG SỐ PHỤ KIỆN</th>
                                                <th>TIÊU CHUẨN CAO CẤP</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <tr><td>KÍCH THƯỚC TIÊU CHUẨN</td><td>22 x 15 x 7 cm (Chuẩn Form)</td></tr>
                                            <tr><td>TRỌNG LƯỢNG</td><td>350g (Siêu Nhẹ)</td></tr>
                                            <tr><td>CHẤT LIỆU CAO CẤP</td><td>ĐẠT CHUẨN TIÊU CHUẨN CHÂU ÂU</td></tr>
                                        </tbody>
                                    </table>
                                <% } %>

                                <div class="text-secondary small italic mt-2">*Thông số thực tế được đo đạc chuẩn theo tiêu chuẩn bảng size cửa hàng thời trang Việt Nam (Chênh lệch 0.5 - 1cm)</div>
                            </div>
                        </div>
                    </div>

                    <!-- Thanh 2: Chính sách vận chuyển -->
                    <div class="info-accordion-item">
                        <div class="info-accordion-header" data-bs-toggle="collapse" data-bs-target="#shippingCollapse">
                            <span>Chính sách vận chuyển</span>
                            <i class="bi bi-chevron-right fs-6"></i>
                        </div>
                        <div class="collapse" id="shippingCollapse">
                            <div class="info-accordion-body">
                                <p class="mb-2"><i class="bi bi-truck text-danger me-2"></i> Giao hàng toàn quốc siêu tốc từ 2 - 4 ngày làm việc.</p>
                                <p class="mb-2"><i class="bi bi-gift text-danger me-2"></i> <strong>Miễn phí vận chuyển</strong> cho tất cả đơn hàng từ 500.000đ.</p>
                                <p class="mb-2"><i class="bi bi-shield-check text-danger me-2"></i> Khách hàng được kiểm tra hàng trước khi thanh toán (COD).</p>
                                <p class="mb-0"><i class="bi bi-arrow-counterclockwise text-danger me-2"></i> Đổi trả hàng miễn phí tận nhà trong vòng 7 ngày nếu lỗi từ nhà sản xuất.</p>
                            </div>
                        </div>
                    </div>

                    <!-- Thanh 3: Cửa hàng còn hàng -->
                    <div class="info-accordion-item">
                        <div class="info-accordion-header" data-bs-toggle="collapse" data-bs-target="#storeStockCollapse">
                            <span>Cửa hàng còn hàng</span>
                            <i class="bi bi-chevron-right fs-6"></i>
                        </div>
                        <div class="collapse" id="storeStockCollapse">
                            <div class="info-accordion-body">
                                <p class="mb-2"><i class="bi bi-geo-alt-fill text-danger me-2"></i> <strong>Chi nhánh 1:</strong> 561 Sư Vạn Hạnh, Phường Hòa Hưng, TP.HCM <span class="badge bg-success ms-2">Còn hàng</span></p>
                                <p class="mb-2"><i class="bi bi-geo-alt-fill text-danger me-2"></i> <strong>Chi nhánh 2:</strong> 123 Nguyễn Trãi, Quận 5, TP.HCM <span class="badge bg-success ms-2">Còn hàng</span></p>
                                <p class="mb-0"><i class="bi bi-geo-alt-fill text-danger me-2"></i> <strong>Chi nhánh 3:</strong> 456 Lê Lợi, Quận 1, TP.HCM <span class="badge bg-success ms-2">Còn hàng</span></p>
                            </div>
                        </div>
                    </div>
                </div>

            </div>
        </div>
    </div>
    <% } %>
</div>

<!-- Modal Thông Báo Thêm Vào Giỏ Hang -->
<% if (addedToCart != null && addedToCart && lastAddedItem != null) { 
    session.removeAttribute("addedToCart");
%>
<div class="modal fade show d-block" id="cartSuccessModal" tabindex="-1" style="background: rgba(0,0,0,0.5);">
    <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content rounded-0 border-0 shadow-lg">
            <div class="add-cart-header-bar bg-white">
                <div>
                    <i class="bi bi-check-lg text-dark me-2"></i><strong>Bạn vừa thêm sản phẩm này vào giỏ hàng</strong>
                </div>
                <div>
                    <i class="bi bi-bag-check me-2"></i>Giỏ hàng của bạn có <strong><%= cartSize %></strong> sản phẩm
                </div>
            </div>
            <div class="modal-body p-4 bg-white">
                <div class="row align-items-center g-4">
                    <div class="col-md-7">
                        <div class="d-flex align-items-center gap-3">
                            <img src="<%= lastAddedItem.getProduct().getImage() %>" alt="<%= lastAddedItem.getProduct().getName() %>" style="width: 70px; height: 85px; object-fit: cover;">
                            <div>
                                <h6 class="fw-bold text-uppercase m-0 text-dark"><%= lastAddedItem.getProduct().getName() %> - <%= lastAddedItem.getSize() %></h6>
                                <div class="text-danger fw-bold fs-5 mt-1"><%= fmt.format(lastAddedItem.getProduct().getEffectivePrice()) %>đ</div>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-5 text-end">
                        <div class="text-secondary small mb-1">Tổng tiền</div>
                        <div class="fs-4 fw-extrabold text-dark mb-3"><%= fmt.format(cartTotal) %>đ</div>
                        <a href="${pageContext.request.contextPath}/cart" class="btn-toi-gio-hang">
                            <i class="bi bi-bag-check-fill me-1"></i> TỚI GIỎ HÀNG
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<% } %>

<script>
function selectSize(sz, element) {
    document.getElementById('selectedSizeInput').value = sz;
    var boxes = document.querySelectorAll('.size-box');
    boxes.forEach(function(b) {
        if (!b.classList.contains('disabled')) {
            b.classList.remove('active');
        }
    });
    element.classList.add('active');
}

function changeQty(delta) {
    var input = document.getElementById('quantityInput');
    var val = parseInt(input.value) + delta;
    if (val >= 1) {
        input.value = val;
    }
}
</script>

<jsp:include page="includes/footer.jsp" />
