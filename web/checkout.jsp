<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, model.CartItem, model.User, java.text.NumberFormat, java.util.Locale" %>
<%
    List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
    Double cartTotalObj = (Double) session.getAttribute("cartTotal");
    double cartTotal = cartTotalObj != null ? cartTotalObj : 0;
    User currentUser = (User) session.getAttribute("user");
    String errorMessage = (String) request.getAttribute("errorMessage");
    NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
%>

<jsp:include page="includes/header.jsp" />
<jsp:include page="includes/navbar.jsp" />

<div class="container my-4">
    <div class="bg-white p-4 rounded-4 border mb-4 shadow-sm">
        <h2 class="fw-bold m-0"><i class="bi bi-credit-card me-2"></i>Thanh Toán Đơn Hàng</h2>
    </div>

    <% if (errorMessage != null) { %>
        <div class="alert alert-danger rounded-3 alert-dismissible fade show" role="alert">
            <%= errorMessage %>
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    <% } %>

    <form action="${pageContext.request.contextPath}/checkout" method="POST">
        <div class="row g-4">
            <!-- Delivery Info Form -->
            <div class="col-lg-7">
                <div class="bg-white p-4 p-md-5 rounded-4 border shadow-sm">
                    <h4 class="fw-bold mb-4">Thông Tin Giao Hàng</h4>
                    <div class="row g-3">
                        <div class="col-12">
                            <label class="form-label fw-bold">Họ và Tên người nhận <span class="text-danger fw-bold">*</span></label>
                            <input type="text" name="customerName" class="form-control rounded-3 py-2 required-input" required 
                                   value="<%= currentUser != null ? currentUser.getFullname() : "" %>">
                            <div class="invalid-feedback"><i class="bi bi-exclamation-circle me-1"></i>Đây là trường bắt buộc</div>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label fw-bold">Email nhận hóa đơn <span class="text-danger fw-bold">*</span></label>
                            <input type="email" name="email" class="form-control rounded-3 py-2 required-input" required 
                                   value="<%= currentUser != null ? currentUser.getEmail() : "" %>">
                            <div class="invalid-feedback"><i class="bi bi-exclamation-circle me-1"></i>Đây là trường bắt buộc</div>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label fw-bold">Số điện thoại giao hàng <span class="text-danger fw-bold">*</span></label>
                            <input type="tel" name="phone" class="form-control rounded-3 py-2 required-input" required 
                                   value="<%= currentUser != null ? currentUser.getPhone() : "" %>">
                            <div class="invalid-feedback"><i class="bi bi-exclamation-circle me-1"></i>Đây là trường bắt buộc</div>
                        </div>
                        <div class="col-12">
                            <label class="form-label fw-bold">Địa chỉ nhận hàng cụ thể <span class="text-danger fw-bold">*</span></label>
                            <input type="text" name="address" class="form-control rounded-3 py-2 required-input" required 
                                   placeholder="Ví dụ: Số 123 Đường Nguyễn Trãi, Phường 2, Quận 5, TP.HCM"
                                   value="<%= currentUser != null ? currentUser.getAddress() : "" %>">
                            <div class="invalid-feedback"><i class="bi bi-exclamation-circle me-1"></i>Đây là trường bắt buộc</div>
                        </div>
                        <div class="col-12">
                            <label class="form-label fw-bold">Ghi chú cho shipper (Tùy chọn)</label>
                            <textarea name="note" class="form-control rounded-3" rows="3" placeholder="Giao giờ hành chính, gọi trước 15p..."></textarea>
                        </div>
                    </div>

                    <h4 class="fw-bold mt-5 mb-3">Phương Thức Thanh Toán</h4>
                    <div class="row g-3">
                        <div class="col-md-6">
                            <div class="form-check border p-3 rounded-3">
                                <input class="form-check-input" type="radio" name="paymentMethod" id="cod" value="COD" checked>
                                <label class="form-check-label fw-bold" for="cod">
                                    <i class="bi bi-cash-stack text-success me-2 fs-5"></i>Thanh toán khi nhận hàng (COD)
                                </label>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="form-check border p-3 rounded-3">
                                <input class="form-check-input" type="radio" name="paymentMethod" id="momo" value="MOMO">
                                <label class="form-check-label fw-bold" for="momo">
                                    <i class="bi bi-wallet2 text-danger me-2 fs-5"></i>Ví MoMo / VNPay (Giả lập)
                                </label>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Order Items Preview -->
            <div class="col-lg-5">
                <div class="bg-white p-4 rounded-4 border shadow-sm sticky-top" style="top: 100px;">
                    <h4 class="fw-bold mb-4">Đơn Hàng Của Bạn</h4>
                    <div class="mb-3 overflow-y-auto" style="max-height: 350px;">
                        <% if (cart != null) {
                            for (CartItem item : cart) { %>
                                <div class="d-flex align-items-center gap-3 mb-3 border-bottom pb-3">
                                    <img src="<%= item.getProduct().getImage() %>" alt="<%= item.getProduct().getName() %>" class="rounded-3" style="width: 50px; height: 65px; object-fit: cover;">
                                    <div class="flex-grow-1">
                                        <h6 class="fw-bold m-0 small"><%= item.getProduct().getName() %></h6>
                                        <span class="text-muted extra-small">Phân loại: <%= item.getSize() %> / <%= item.getColor() %></span>
                                        <div class="text-secondary small">x<%= item.getQuantity() %></div>
                                    </div>
                                    <span class="fw-bold text-dark small"><%= fmt.format(item.getTotalPrice()) %> đ</span>
                                </div>
                        <%  }
                           } %>
                    </div>

                    <div class="d-flex justify-content-between mb-2">
                        <span class="text-secondary">Tạm tính:</span>
                        <span class="fw-bold"><%= fmt.format(cartTotal) %> đ</span>
                    </div>
                    <div class="d-flex justify-content-between mb-2">
                        <span class="text-secondary">Phí vận chuyển:</span>
                        <span class="text-success fw-bold">MIỄN PHÍ</span>
                    </div>
                    <hr>
                    <div class="d-flex justify-content-between mb-4">
                        <span class="fs-5 fw-bold">Tổng thanh toán:</span>
                        <span class="fs-4 fw-extrabold text-danger"><%= fmt.format(cartTotal) %> đ</span>
                    </div>

                    <button type="submit" class="btn btn-accent btn-lg w-100 rounded-pill fw-bold py-3">
                        <i class="bi bi-check-circle me-2"></i> Xác Nhận Đặt Hàng
                    </button>
                </div>
            </div>
        </div>
    </form>

    <script>
    document.addEventListener("DOMContentLoaded", function() {
        const form = document.querySelector("form[action*='checkout']");
        if (!form) return;
        const inputs = form.querySelectorAll(".required-input");
        function validate(input) {
            const val = input.value.trim();
            const feedback = input.parentElement.querySelector(".invalid-feedback");
            if (!val) {
                input.classList.add("is-invalid");
                if (feedback) feedback.style.display = "block";
                return false;
            } else {
                input.classList.remove("is-invalid");
                if (feedback) feedback.style.display = "none";
                return true;
            }
        }
        inputs.forEach(input => {
            input.addEventListener("blur", function() { validate(this); });
            input.addEventListener("input", function() { if (this.classList.contains("is-invalid")) validate(this); });
        });
        form.addEventListener("submit", function(e) {
            let valid = true;
            let firstInvalid = null;
            inputs.forEach(input => {
                if (!validate(input)) {
                    valid = false;
                    if (!firstInvalid) firstInvalid = input;
                }
            });
            if (!valid) {
                e.preventDefault();
                if (firstInvalid) firstInvalid.focus();
            }
        });
    });
    </script>
</div>

<jsp:include page="includes/footer.jsp" />
