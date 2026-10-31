<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, model.CartItem, model.User, java.text.NumberFormat, java.util.Locale" %>
<%
    User sessionUser = (User) session.getAttribute("user");
    if (sessionUser != null && (sessionUser.isAdmin() || sessionUser.getRole() == 1)) {
        session.setAttribute("toastInfo", "Tài khoản Quản Trị Viên không sử dụng giỏ hàng mua sắm.");
        response.sendRedirect(request.getContextPath() + "/admin/dashboard");
        return;
    }

    List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
    Double cartTotalObj = (Double) session.getAttribute("cartTotal");
    double cartTotal = cartTotalObj != null ? cartTotalObj : 0;
    
    if (cart != null && cartTotal == 0) {
        for (CartItem item : cart) {
            cartTotal += item.getTotalMoney();
        }
    }
    
    NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
%>

<jsp:include page="includes/header.jsp" />
<jsp:include page="includes/navbar.jsp" />

<div class="container my-5">
    <div class="bg-white p-4 border mb-4">
        <h3 class="fw-bold text-uppercase m-0">Giỏ Hàng Của Bạn</h3>
    </div>

    <% if (cart != null && !cart.isEmpty()) { %>
        <div class="bg-white border p-4 shadow-sm mb-5">
            <!-- Header Table -->
            <div class="cart-table-header px-3">
                <div style="width: 45%;">Sản phẩm</div>
                <div style="width: 25%; text-align: center;">Số lượng</div>
                <div style="width: 20%; text-align: right;">Tổng tiền</div>
                <div style="width: 10%; text-align: right;">Xóa</div>
            </div>

            <!-- Cart Items Rows -->
            <% for (CartItem item : cart) { %>
                <div class="cart-item-row-custom px-3" 
                     data-price="<%= item.getProduct().getEffectivePrice() %>" 
                     data-id="<%= item.getProduct().getId() %>" 
                     data-size="<%= item.getSize() %>">
                    
                    <!-- Product Info -->
                    <div style="width: 45%;" class="d-flex align-items-center gap-4">
                        <%
                            String cartImg = item.getProduct().getImage();
                            if (cartImg != null && !cartImg.startsWith("http://") && !cartImg.startsWith("https://")) {
                                if (!cartImg.startsWith(request.getContextPath())) {
                                    cartImg = request.getContextPath() + (cartImg.startsWith("/") ? "" : "/") + cartImg;
                                }
                            }
                        %>
                        <img src="<%= cartImg %>" alt="<%= item.getProduct().getName() %>" style="width: 80px; height: 100px; object-fit: cover;" class="border">
                        <div>
                            <h6 class="fw-bold text-uppercase m-0 text-dark" style="letter-spacing: 0.5px;"><%= item.getProduct().getName() %></h6>
                            <div class="text-secondary small mt-1">Size: <span class="fw-bold text-dark"><%= item.getSize() %></span></div>
                            <div class="fw-bold text-danger mt-1"><%= fmt.format(item.getProduct().getEffectivePrice()) %>đ</div>
                        </div>
                    </div>

                    <!-- Quantity Stepper Voi Cap Nhat Real-Time Tức Thì -->
                    <div style="width: 25%;" class="d-flex justify-content-center">
                        <div class="quantity-stepper">
                            <button type="button" onclick="updateCartQuantity(this, -1, <%= item.getProduct().getId() %>, '<%= item.getSize() %>', <%= item.getProduct().getEffectivePrice() %>)">-</button>
                            <input type="number" class="qty-input" value="<%= item.getQuantity() %>" min="1" readonly>
                            <button type="button" onclick="updateCartQuantity(this, 1, <%= item.getProduct().getId() %>, '<%= item.getSize() %>', <%= item.getProduct().getEffectivePrice() %>)">+</button>
                        </div>
                    </div>

                    <!-- Item Total Price (Tự Động Nhảy Số Tức Thời) -->
                    <div style="width: 20%; text-align: right;" class="fw-bold fs-5 text-dark item-total-display">
                        <%= fmt.format(item.getTotalPrice()) %>đ
                    </div>

                    <!-- Remove Icon -->
                    <div style="width: 10%; text-align: right;">
                        <form action="${pageContext.request.contextPath}/cart" method="POST" class="d-inline">
                            <input type="hidden" name="action" value="remove">
                            <input type="hidden" name="productId" value="<%= item.getProduct().getId() %>">
                            <input type="hidden" name="size" value="<%= item.getSize() %>">
                            <button type="submit" class="btn border-0 p-0 fs-4 text-secondary hover-danger">
                                ✖
                            </button>
                        </form>
                    </div>
                </div>
            <% } %>

            <!-- Bottom Summary With Dynamic Real-Time Grand Total -->
            <div class="d-flex flex-column align-items-end mt-4 pt-3 border-top">
                <div class="fs-4 mb-4">
                    Tổng tiền <span class="fw-extrabold text-danger ms-2" id="grandTotalDisplay"><%= fmt.format(cartTotal) %>đ</span>
                </div>
                <div class="d-flex gap-3">
                    <a href="${pageContext.request.contextPath}/products" class="btn-continue-shopping">
                        Tiếp tục mua hàng
                    </a>
                    <a href="${pageContext.request.contextPath}/checkout" class="btn-checkout-red">
                        Tiến hành thanh toán
                    </a>
                </div>
            </div>
        </div>
    <% } else { %>
        <div class="text-center py-5 bg-white border">
            <i class="bi bi-bag-x fs-1 text-muted"></i>
            <h4 class="fw-bold mt-3">Giỏ hàng của bạn đang trống!</h4>
            <p class="text-muted">Hãy lướt xem các mẫu sản phẩm mới nhất và chọn sản phẩm yêu thích nhé.</p>
            <a href="${pageContext.request.contextPath}/products" class="btn btn-checkout-red mt-2">Khám Phá Cửa Hàng</a>
        </div>
    <% } %>
</div>

<script>
function updateCartQuantity(btn, delta, productId, size, unitPrice) {
    var row = btn.closest('.cart-item-row-custom');
    var input = row.querySelector('.qty-input');
    var currentQty = parseInt(input.value) || 1;
    var newQty = currentQty + delta;
    
    if (newQty < 1) return;
    input.value = newQty;
    
    // Tự động tính toán lại thành tiền dòng sản phẩm này ngay lập tức
    var itemTotal = unitPrice * newQty;
    row.querySelector('.item-total-display').innerText = formatVND(itemTotal) + 'đ';
    
    // Tự động tính toán lại Tổng Tiền Toàn Giỏ Hàng ngay lập tức
    calculateGrandTotal();
    
    // Đồng bộ với Server Session ngầm (AJAX Background)
    var formData = new URLSearchParams();
    formData.append('action', 'update');
    formData.append('productId', productId);
    formData.append('size', size);
    formData.append('quantity', newQty);
    
    fetch('${pageContext.request.contextPath}/cart', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'X-Requested-With': 'XMLHttpRequest'
        },
        body: formData.toString()
    }).catch(function(err) {
        console.log('Syncing cart error:', err);
    });
}

function calculateGrandTotal() {
    var total = 0;
    var rows = document.querySelectorAll('.cart-item-row-custom');
    rows.forEach(function(row) {
        var qty = parseInt(row.querySelector('.qty-input').value) || 0;
        var unitPrice = parseFloat(row.getAttribute('data-price')) || 0;
        total += (qty * unitPrice);
    });
    
    var grandTotalElement = document.getElementById('grandTotalDisplay');
    if (grandTotalElement) {
        grandTotalElement.innerText = formatVND(total) + 'đ';
    }
}

function formatVND(amount) {
    return new Intl.NumberFormat('vi-VN').format(amount);
}
</script>

<jsp:include page="includes/footer.jsp" />
