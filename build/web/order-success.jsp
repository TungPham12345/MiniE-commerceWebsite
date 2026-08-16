<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String successMessage = (String) request.getAttribute("successMessage");
    if (successMessage == null) successMessage = "Đặt hàng thành công!";
%>

<jsp:include page="includes/header.jsp" />
<jsp:include page="includes/navbar.jsp" />

<div class="container my-5">
    <div class="row justify-content-center">
        <div class="col-md-8 text-center">
            <div class="bg-white p-5 rounded-4 border shadow-lg">
                <div class="mb-4">
                    <i class="bi bi-check-circle-fill text-success" style="font-size: 5rem;"></i>
                </div>
                <h2 class="fw-extrabold text-dark mb-2">ĐẶT HÀNG THÀNH CÔNG!</h2>
                <p class="lead text-muted mb-4"><%= successMessage %></p>

                <div class="bg-light p-4 rounded-3 text-start mb-4">
                    <h6 class="fw-bold mb-2">Lưu ý đơn hàng:</h6>
                    <ul class="text-secondary small m-0 ps-3">
                        <li>HighFashion đã tiếp nhận đơn hàng của bạn và đang tiến hành xử lý.</li>
                        <li>Nhân viên tư vấn sẽ liên hệ số điện thoại của bạn để xác nhận đơn hàng trong ít phút.</li>
                        <li>Thời gian giao hàng dự kiến từ 1 - 3 ngày làm việc.</li>
                    </ul>
                </div>

                <div class="d-flex justify-content-center gap-3">
                    <a href="${pageContext.request.contextPath}/home" class="btn btn-outline-dark rounded-pill px-4 py-2 fw-bold">Về Trang Chủ</a>
                    <a href="${pageContext.request.contextPath}/products" class="btn btn-accent rounded-pill px-4 py-2 fw-bold">Tiếp Tục Mua Sắm</a>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="includes/footer.jsp" />
