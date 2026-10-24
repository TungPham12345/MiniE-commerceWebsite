<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    Boolean isHomePage = (Boolean) request.getAttribute("isHomePage");
    if (isHomePage == null) {
        String uri = request.getRequestURI();
        isHomePage = uri != null && (uri.endsWith("/home") || uri.endsWith("/index.jsp") || uri.endsWith("/ban_hang/") || uri.endsWith("/ban_hang"));
    }
%>

<% if (Boolean.TRUE.equals(isHomePage)) { %>
<!-- Chân trang (Footer) Hệ thống HighFashion: Chỉ hiển thị ở Trang Chủ -->
<footer class="bg-dark text-white pt-5 pb-4 mt-5">
    <div class="container">
        <div class="row g-4">
            <!-- Cột 1: Thông tin thương hiệu & Mạng xã hội -->
            <div class="col-lg-4 col-md-6">
                <h5 class="brand-logo text-white mb-3">HIGH<span class="text-danger">FASHION</span></h5>
                <p class="text-secondary small">Hệ thống thời trang nam nữ hàng đầu với chất liệu cao cấp, kiểu dáng hiện đại tôn vinh cá tính người mặc.</p>
                <div class="d-flex gap-3 text-secondary fs-5 mt-3">
                    <a href="#" class="text-secondary" aria-label="Facebook"><i class="bi bi-facebook"></i></a>
                    <a href="#" class="text-secondary" aria-label="Instagram"><i class="bi bi-instagram"></i></a>
                    <a href="#" class="text-secondary" aria-label="TikTok"><i class="bi bi-tiktok"></i></a>
                </div>
            </div>

            <!-- Cột 2: Các liên kết điều hướng nhanh -->
            <div class="col-lg-2 col-md-6">
                <h6 class="text-uppercase fw-bold mb-3">Liên Kết</h6>
                <ul class="list-unstyled text-secondary small">
                    <li class="mb-2"><a href="${pageContext.request.contextPath}/home" class="text-secondary text-decoration-none">Trang chủ</a></li>
                    <li class="mb-2"><a href="${pageContext.request.contextPath}/products" class="text-secondary text-decoration-none">Cửa hàng</a></li>
                    <li class="mb-2"><a href="${pageContext.request.contextPath}/cart" class="text-secondary text-decoration-none">Giỏ hàng</a></li>
                </ul>
            </div>

            <!-- Cột 3: Chính sách khách hàng -->
            <div class="col-lg-3 col-md-6">
                <h6 class="text-uppercase fw-bold mb-3">Chính Sách</h6>
                <ul class="list-unstyled text-secondary small">
                    <li class="mb-2"><a href="#" class="text-secondary text-decoration-none">Chính sách đổi trả 30 ngày</a></li>
                    <li class="mb-2"><a href="#" class="text-secondary text-decoration-none">Giao hàng miễn phí toàn quốc</a></li>
                    <li class="mb-2"><a href="#" class="text-secondary text-decoration-none">Bảo mật thông tin khách hàng</a></li>
                </ul>
            </div>

            <!-- Cột 4: Thông tin liên hệ cửa hàng -->
            <div class="col-lg-3 col-md-6">
                <h6 class="text-uppercase fw-bold mb-3">Liên Hệ</h6>
                <p class="text-secondary small mb-1"><i class="bi bi-geo-alt me-2"></i>123 Đường Thời Trang, Quận 1, TP.HCM</p>
                <p class="text-secondary small mb-1"><i class="bi bi-telephone me-2"></i>Hotline: 1900 8888</p>
                <p class="text-secondary small"><i class="bi bi-envelope me-2"></i>Email: support@highfashion.vn</p>
            </div>
        </div>

        <hr class="border-secondary my-4">

        <!-- Dòng bản quyền -->
        <div class="text-center text-secondary small">
            &copy; 2026 HighFashion. All rights reserved.
        </div>
    </div>
</footer>
<% } %>

<!-- Thư viện Javascript Bootstrap 5 -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

<!-- Script xử lý Chuyển đổi Chế Độ Sáng / Tối (Dark Mode Toggle) toàn hệ thống -->
<script>
    document.addEventListener('DOMContentLoaded', function() {
        const toggleBtn = document.getElementById('themeToggleBtn');
        if (!toggleBtn) return;

        // Cập nhật tooltip khi thay đổi theme
        function updateToggleState(theme) {
            if (theme === 'dark') {
                toggleBtn.setAttribute('title', 'Chuyển sang chế độ Sáng (Light Mode)');
                toggleBtn.setAttribute('aria-label', 'Chuyển sang chế độ Sáng');
            } else {
                toggleBtn.setAttribute('title', 'Chuyển sang chế độ Tối (Dark Mode)');
                toggleBtn.setAttribute('aria-label', 'Chuyển sang chế độ Tối');
            }
        }

        // Đọc chủ đề hiện tại từ thuộc tính thẻ html
        const currentTheme = document.documentElement.getAttribute('data-bs-theme') || 'light';
        updateToggleState(currentTheme);

        // Lắng nghe sự kiện click vào nút chuyển theme
        toggleBtn.addEventListener('click', function() {
            const activeTheme = document.documentElement.getAttribute('data-bs-theme');
            const newTheme = activeTheme === 'dark' ? 'light' : 'dark';
            
            // Cập nhật thuộc tính trên thẻ html
            document.documentElement.setAttribute('data-bs-theme', newTheme);
            
            // Lưu lựa chọn vào localStorage của trình duyệt để duy trì qua các trang
            try {
                localStorage.setItem('theme', newTheme);
            } catch (e) {
                console.warn('Không thể lưu theme vào localStorage:', e);
            }

            // Cập nhật tooltip
            updateToggleState(newTheme);
        });
    });
</script>
</body>
</html>
