<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, model.User" %>
<%!
    private String escapeHtml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                    .replace("\"", "&quot;").replace("'", "&#39;");
    }
%>
<%
    request.setAttribute("adminActivePage", "users");
    List<User> users = (List<User>) request.getAttribute("users");
    User currentUser = (User) session.getAttribute("user");
    String searchQuery = (String) request.getAttribute("searchQuery");
    String toastSuccess = (String) session.getAttribute("toastSuccess");
    String toastError = (String) session.getAttribute("toastError");
    if (toastSuccess != null) session.removeAttribute("toastSuccess");
    if (toastError != null) session.removeAttribute("toastError");
%>

<jsp:include page="includes/header.jsp" />
<jsp:include page="includes/navbar.jsp" />

<main class="admin-dashboard-page">
    <% if (toastSuccess != null) { %>
        <div class="alert alert-success alert-dismissible fade show rounded-4 shadow-sm" role="alert">
            <i class="bi bi-check-circle-fill me-2"></i><%= escapeHtml(toastSuccess) %>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Đóng"></button>
        </div>
    <% } %>
    <% if (toastError != null) { %>
        <div class="alert alert-danger alert-dismissible fade show rounded-4 shadow-sm" role="alert">
            <i class="bi bi-exclamation-triangle-fill me-2"></i><%= escapeHtml(toastError) %>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Đóng"></button>
        </div>
    <% } %>

    <div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
        <div>
            <span class="badge bg-danger mb-2">QUẢN TRỊ HỆ THỐNG</span>
            <h2 class="fw-bold mb-1">Quản lý tài khoản</h2>
            <p class="text-secondary mb-0">Tra cứu, cập nhật hồ sơ và phân quyền người dùng.</p>
        </div>
        <form action="${pageContext.request.contextPath}/admin/users" method="get" class="d-flex gap-2 admin-user-search">
            <label class="visually-hidden" for="userSearch">Tìm tài khoản</label>
            <input id="userSearch" type="search" name="q" class="form-control"
                   placeholder="Tên, email hoặc số điện thoại" value="<%= escapeHtml(searchQuery) %>">
            <button class="btn btn-dark px-3" type="submit"><i class="bi bi-search me-1"></i>Tìm</button>
            <% if (searchQuery != null && !searchQuery.isEmpty()) { %>
                <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/admin/users">Xóa lọc</a>
            <% } %>
        </form>
    </div>

    <section class="bg-white border rounded-4 shadow-sm p-3 p-lg-4">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <h5 class="fw-bold mb-0">Danh sách tài khoản</h5>
            <span class="badge rounded-pill bg-light text-dark border"><%= users == null ? 0 : users.size() %> tài khoản</span>
        </div>
        <div class="table-responsive">
            <table class="table table-hover align-middle admin-users-table">
                <thead class="table-light">
                    <tr>
                        <th>Tài khoản</th>
                        <th>Thông tin liên hệ</th>
                        <th>Địa chỉ</th>
                        <th>Vai trò</th>
                        <th class="text-end">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <% if (users != null && !users.isEmpty()) {
                        for (User account : users) {
                            boolean isCurrentUser = currentUser != null && account.getId() == currentUser.getId();
                    %>
                        <tr>
                            <td>
                                <div class="fw-semibold"><%= escapeHtml(account.getFullname()) %></div>
                                <div class="small text-muted">@<%= escapeHtml(account.getUsername()) %><% if (isCurrentUser) { %> <span class="badge bg-secondary">Bạn</span><% } %></div>
                            </td>
                            <td>
                                <div><%= escapeHtml(account.getEmail()) %></div>
                                <div class="small text-muted"><%= escapeHtml(account.getPhone()) %></div>
                            </td>
                            <td class="admin-user-address"><%= escapeHtml(account.getAddress()) %></td>
                            <td><span class="badge <%= account.isAdmin() ? "bg-danger" : "bg-primary" %>"><%= account.isAdmin() ? "Quản trị viên" : "Khách hàng" %></span></td>
                            <td class="text-end text-nowrap">
                                <button type="button" class="btn btn-outline-primary btn-sm"
                                        data-bs-toggle="modal" data-bs-target="#editUserModal"
                                        data-id="<%= account.getId() %>"
                                        data-username="<%= escapeHtml(account.getUsername()) %>"
                                        data-fullname="<%= escapeHtml(account.getFullname()) %>"
                                        data-email="<%= escapeHtml(account.getEmail()) %>"
                                        data-phone="<%= escapeHtml(account.getPhone()) %>"
                                        data-address="<%= escapeHtml(account.getAddress()) %>"
                                        data-role="<%= account.getRole() %>"
                                        data-self="<%= isCurrentUser %>">
                                    <i class="bi bi-pencil-square"></i> Sửa
                                </button>
                                <% if (!isCurrentUser) { %>
                                    <button type="button" class="btn btn-outline-danger btn-sm"
                                            data-bs-toggle="modal" data-bs-target="#deleteUserModal"
                                            data-id="<%= account.getId() %>"
                                            data-username="<%= escapeHtml(account.getUsername()) %>">
                                        <i class="bi bi-trash"></i> Xóa
                                    </button>
                                <% } %>
                            </td>
                        </tr>
                    <%  }
                       } else { %>
                        <tr><td colspan="5" class="text-center text-secondary py-5">Không tìm thấy tài khoản phù hợp.</td></tr>
                    <% } %>
                </tbody>
            </table>
        </div>
    </section>
</main>

<div class="modal fade" id="editUserModal" tabindex="-1" aria-labelledby="editUserTitle" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 rounded-4 shadow">
            <form action="${pageContext.request.contextPath}/admin/users" method="post">
                <input type="hidden" name="action" value="update">
                <input type="hidden" name="id" id="editUserId">
                <div class="modal-header bg-dark text-white">
                    <div>
                        <h5 class="modal-title fw-bold" id="editUserTitle">Cập nhật tài khoản</h5>
                        <div class="small text-white-50" id="editUsername"></div>
                    </div>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Đóng"></button>
                </div>
                <div class="modal-body p-4">
                    <div class="row g-3">
                        <div class="col-md-6">
                            <label class="form-label fw-semibold" for="editFullname">Họ và tên</label>
                            <input class="form-control" id="editFullname" name="fullname" maxlength="100" required>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label fw-semibold" for="editEmail">Email</label>
                            <input class="form-control" id="editEmail" name="email" type="email" maxlength="100" required>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label fw-semibold" for="editPhone">Số điện thoại</label>
                            <input class="form-control" id="editPhone" name="phone" maxlength="20">
                        </div>
                        <div class="col-md-6">
                            <label class="form-label fw-semibold" for="editRole">Vai trò</label>
                            <select class="form-select" id="editRole" name="role" required>
                                <option value="0">Khách hàng</option>
                                <option value="1">Quản trị viên</option>
                            </select>
                            <small class="text-muted" id="selfRoleNote" hidden>Không thể tự hạ quyền quản trị của mình.</small>
                        </div>
                        <div class="col-12">
                            <label class="form-label fw-semibold" for="editAddress">Địa chỉ</label>
                            <textarea class="form-control" id="editAddress" name="address" maxlength="255" rows="2"></textarea>
                        </div>
                        <div class="col-12">
                            <label class="form-label fw-semibold" for="editPassword">Đặt lại mật khẩu <span class="text-muted fw-normal">(để trống nếu không đổi)</span></label>
                            <input class="form-control" id="editPassword" name="newPassword" type="password" minlength="6" autocomplete="new-password">
                        </div>
                    </div>
                </div>
                <div class="modal-footer border-0 px-4 pb-4">
                    <button type="button" class="btn btn-light" data-bs-dismiss="modal">Hủy</button>
                    <button type="submit" class="btn btn-danger px-4 fw-bold">Lưu thay đổi</button>
                </div>
            </form>
        </div>
    </div>
</div>

<div class="modal fade" id="deleteUserModal" tabindex="-1" aria-labelledby="deleteUserTitle" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 rounded-4 shadow">
            <form action="${pageContext.request.contextPath}/admin/users" method="post">
                <input type="hidden" name="action" value="delete">
                <input type="hidden" name="id" id="deleteUserId">
                <div class="modal-header border-0 pb-0">
                    <h5 class="modal-title fw-bold" id="deleteUserTitle">Xác nhận xóa tài khoản</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Đóng"></button>
                </div>
                <div class="modal-body">
                    <p class="mb-0">Bạn có chắc muốn xóa <strong id="deleteUsername"></strong>? Các đơn hàng trước đây vẫn được giữ lại.</p>
                </div>
                <div class="modal-footer border-0 pt-0">
                    <button type="button" class="btn btn-light" data-bs-dismiss="modal">Không xóa</button>
                    <button type="submit" class="btn btn-danger fw-bold">Xóa tài khoản</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
document.getElementById("editUserModal").addEventListener("show.bs.modal", function (event) {
    var button = event.relatedTarget;
    document.getElementById("editUserId").value = button.dataset.id;
    document.getElementById("editUsername").textContent = "@" + button.dataset.username;
    document.getElementById("editFullname").value = button.dataset.fullname;
    document.getElementById("editEmail").value = button.dataset.email;
    document.getElementById("editPhone").value = button.dataset.phone;
    document.getElementById("editAddress").value = button.dataset.address;
    var role = document.getElementById("editRole");
    role.value = button.dataset.role;
    var isSelf = button.dataset.self === "true";
    role.disabled = isSelf;
    document.getElementById("selfRoleNote").hidden = !isSelf;
    if (isSelf) {
        var existingRole = document.querySelector('#editUserModal input[name="role"]');
        if (!existingRole) {
            existingRole = document.createElement("input");
            existingRole.type = "hidden";
            existingRole.name = "role";
            document.querySelector("#editUserModal form").appendChild(existingRole);
        }
        existingRole.value = button.dataset.role;
    } else {
        var hiddenRole = document.querySelector('#editUserModal input[name="role"]');
        if (hiddenRole) hiddenRole.remove();
    }
    document.getElementById("editPassword").value = "";
});

document.getElementById("deleteUserModal").addEventListener("show.bs.modal", function (event) {
    var button = event.relatedTarget;
    document.getElementById("deleteUserId").value = button.dataset.id;
    document.getElementById("deleteUsername").textContent = "@" + button.dataset.username;
});
</script>

<jsp:include page="includes/footer.jsp" />
