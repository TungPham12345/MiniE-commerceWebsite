<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, model.CartItem, java.text.NumberFormat, java.util.Locale" %>
<%
    List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
    Double cartTotalObj = (Double) session.getAttribute("cartTotal");
    double cartTotal = cartTotalObj != null ? cartTotalObj : 0;
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
            <!-- Header Table Matching Screenshot 5 -->
            <div class="cart-table-header px-3">
                <div style="width: 50%;">Sản phẩm</div>
                <div style="width: 20%; text-align: center;">Số lượng</div>
                <div style="width: 20%; text-align: right;">Tổng tiền</div>
                <div style="width: 10%; text-align: right;">Xóa</div>
            </div>

            <!-- Cart Items Rows -->
            <% for (CartItem item : cart) { %>
                <div class="cart-item-row-custom px-3">
                    <!-- Product Info -->
                    <div style="width: 50%;" class="d-flex align-items-center gap-4">
                        <img src="<%= item.getProduct().getImage() %>" alt="<%= item.getProduct().getName() %>" style="width: 80px; height: 100px; object-fit: cover;" class="border">
                        <div>
                            <h6 class="fw-bold text-uppercase m-0 text-dark" style="letter-spacing: 0.5px;"><%= item.getProduct().getName() %></h6>
                            <div class="text-secondary small mt-1"><%= item.getSize() %></div>
                            <div class="fw-bold text-danger mt-1"><%= fmt.format(item.getProduct().getEffectivePrice()) %>đ</div>
                        </div>
                    </div>

                    <!-- Quantity Stepper -->
                    <div style="width: 20%;" class="d-flex justify-content-center">
                        <form action="${pageContext.request.contextPath}/cart" method="POST" class="d-inline">
                            <input type="hidden" name="action" value="update">
                            <input type="hidden" name="productId" value="<%= item.getProduct().getId() %>">
                            <div class="quantity-stepper">
                                <button type="submit" onclick="this.form.quantity.value = parseInt(this.form.quantity.value) - 1;">-</button>
                                <input type="number" name="quantity" value="<%= item.getQuantity() %>" min="0" readonly>
                                <button type="submit" onclick="this.form.quantity.value = parseInt(this.form.quantity.value) + 1;">+</button>
                            </div>
                        </form>
                    </div>

                    <!-- Item Total Price -->
                    <div style="width: 20%; text-align: right;" class="fw-bold fs-5 text-dark">
                        <%= fmt.format(item.getTotalPrice()) %>đ
                    </div>

                    <!-- Remove Icon -->
                    <div style="width: 10%; text-align: right;">
                        <form action="${pageContext.request.contextPath}/cart" method="POST" class="d-inline">
                            <input type="hidden" name="action" value="remove">
                            <input type="hidden" name="productId" value="<%= item.getProduct().getId() %>">
                            <button type="submit" class="btn border-0 p-0 fs-4 text-secondary hover-danger">
                                ✖
                            </button>
                        </form>
                    </div>
                </div>
            <% } %>

            <!-- Bottom Summary Matching Screenshot 5 -->
            <div class="d-flex flex-column align-items-end mt-4 pt-3">
                <div class="fs-4 mb-4">
                    Tổng tiền <span class="fw-extrabold text-danger ms-2"><%= fmt.format(cartTotal) %>đ</span>
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

<jsp:include page="includes/footer.jsp" />
