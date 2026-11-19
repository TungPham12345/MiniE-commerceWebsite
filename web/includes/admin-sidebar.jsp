<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String activeAdminPage = (String) request.getAttribute("adminActivePage");
%>
<aside class="admin-sidebar" aria-label="Menu quản trị">
    <div class="admin-sidebar-brand">
        <span class="admin-sidebar-brand-icon"><i class="bi bi-shield-lock-fill"></i></span>
        <div>
            <strong>ADMIN PANEL</strong>
            <span>Quản lý cửa hàng</span>
        </div>
    </div>

    <nav class="admin-tree-nav" aria-label="Điều hướng quản trị">
        <span class="admin-tree-heading">TỔNG QUAN</span>
        <a href="${pageContext.request.contextPath}/admin/dashboard"
           class="admin-tree-link <%= "dashboard".equals(activeAdminPage) ? "active" : "" %>"
           <%= "dashboard".equals(activeAdminPage) ? "aria-current=\"page\"" : "" %>>
            <i class="bi bi-speedometer2" aria-hidden="true"></i>
            <span>Bảng điều khiển</span>
        </a>

        <span class="admin-tree-heading admin-tree-heading-spaced">QUẢN LÝ</span>
        <div class="admin-tree-branch">
            <span class="admin-tree-parent">
                <i class="bi bi-folder2-open" aria-hidden="true"></i>
                <span>Danh mục quản lý</span>
            </span>
            <div class="admin-tree-children">
                <a href="${pageContext.request.contextPath}/admin/products"
                   class="admin-tree-link <%= "products".equals(activeAdminPage) ? "active" : "" %>"
                   <%= "products".equals(activeAdminPage) ? "aria-current=\"page\"" : "" %>>
                    <i class="bi bi-box-seam" aria-hidden="true"></i>
                    <span>Sản phẩm</span>
                </a>
                <a href="${pageContext.request.contextPath}/admin/stores"
                   class="admin-tree-link <%= "stores".equals(activeAdminPage) ? "active" : "" %>"
                   <%= "stores".equals(activeAdminPage) ? "aria-current=\"page\"" : "" %>>
                    <i class="bi bi-geo-alt" aria-hidden="true"></i>
                    <span>Cửa hàng & bản đồ</span>
                </a>
                <a href="${pageContext.request.contextPath}/admin/users"
                   class="admin-tree-link <%= "users".equals(activeAdminPage) ? "active" : "" %>"
                   <%= "users".equals(activeAdminPage) ? "aria-current=\"page\"" : "" %>>
                    <i class="bi bi-people" aria-hidden="true"></i>
                    <span>Quản lý tài khoản</span>
                </a>
            </div>
        </div>
    </nav>

    <a href="${pageContext.request.contextPath}/home" class="admin-sidebar-home">
        <i class="bi bi-box-arrow-up-right" aria-hidden="true"></i>
        <span>Trở về website</span>
    </a>
</aside>
