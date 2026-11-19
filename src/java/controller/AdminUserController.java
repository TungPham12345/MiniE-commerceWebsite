package controller;

import dao.UserDAO;
import dao.UserDAO.AccountMutationResult;
import model.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "AdminUserController", urlPatterns = {"/admin/users"})
public class AdminUserController extends HttpServlet {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private User getAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }
        return user;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (getAdmin(request, response) == null) return;
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        String query = request.getParameter("q");
        if (query == null) query = "";
        query = query.trim();

        try {
            List<User> users = new UserDAO().getAllUsers();
            if (!query.isEmpty()) {
                String search = query.toLowerCase(Locale.ROOT);
                List<User> filteredUsers = new ArrayList<>();
                for (User user : users) {
                    if (contains(user.getUsername(), search)
                            || contains(user.getFullname(), search)
                            || contains(user.getEmail(), search)
                            || contains(user.getPhone(), search)) {
                        filteredUsers.add(user);
                    }
                }
                users = filteredUsers;
            }
            request.setAttribute("users", users);
            request.setAttribute("searchQuery", query);
        } catch (IllegalStateException e) {
            request.getSession().setAttribute("toastError", e.getMessage());
            request.setAttribute("users", new ArrayList<User>());
            request.setAttribute("searchQuery", query);
        }
        request.getRequestDispatcher("/admin-users.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User admin = getAdmin(request, response);
        if (admin == null) return;
        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();
        String action = request.getParameter("action");
        if ("delete".equals(action)) {
            deleteAccount(request, session, admin);
        } else if ("update".equals(action)) {
            updateAccount(request, session, admin);
        } else {
            session.setAttribute("toastError", "Thao tác tài khoản không hợp lệ.");
        }
        response.sendRedirect(request.getContextPath() + "/admin/users");
    }

    private void updateAccount(HttpServletRequest request, HttpSession session, User admin) {
        try {
            int userId = Integer.parseInt(request.getParameter("id"));
            String fullname = trimToNull(request.getParameter("fullname"));
            String email = trimToNull(request.getParameter("email"));
            String phone = trimToNull(request.getParameter("phone"));
            String address = trimToNull(request.getParameter("address"));
            String password = request.getParameter("newPassword");
            String roleValue = request.getParameter("role");
            int role = Integer.parseInt(roleValue);

            if (userId <= 0 || fullname == null || fullname.length() > 100
                    || email == null || email.length() > 100 || !EMAIL_PATTERN.matcher(email).matches()
                    || (phone != null && phone.length() > 20)
                    || (address != null && address.length() > 255)
                    || (role != 0 && role != 1)
                    || (password != null && !password.isEmpty() && password.length() < 6)) {
                session.setAttribute("toastError", "Thông tin không hợp lệ. Hãy kiểm tra họ tên, email, số điện thoại, địa chỉ và mật khẩu (tối thiểu 6 ký tự).");
                return;
            }
            if (userId == admin.getId() && role != 1) {
                session.setAttribute("toastError", "Bạn không thể tự hạ quyền quản trị của mình.");
                return;
            }

            User updatedUser = new User();
            updatedUser.setId(userId);
            updatedUser.setFullname(fullname);
            updatedUser.setEmail(email);
            updatedUser.setPhone(phone);
            updatedUser.setAddress(address);
            updatedUser.setRole(role);
            AccountMutationResult result = new UserDAO().updateManagedAccount(
                    updatedUser, password == null || password.isEmpty() ? null : password);
            if (result == AccountMutationResult.SUCCESS && userId == admin.getId()) {
                admin.setFullname(fullname);
                admin.setEmail(email);
                admin.setPhone(phone);
                admin.setAddress(address);
            }
            setResultMessage(session, result,
                    "Cập nhật tài khoản thành công. Nếu đã đổi quyền, người dùng cần đăng nhập lại để áp dụng.");
        } catch (NumberFormatException e) {
            session.setAttribute("toastError", "Mã tài khoản hoặc quyền tài khoản không hợp lệ.");
        }
    }

    private void deleteAccount(HttpServletRequest request, HttpSession session, User admin) {
        try {
            int userId = Integer.parseInt(request.getParameter("id"));
            if (userId <= 0) {
                session.setAttribute("toastError", "Mã tài khoản không hợp lệ.");
                return;
            }
            if (userId == admin.getId()) {
                session.setAttribute("toastError", "Bạn không thể xóa tài khoản đang đăng nhập.");
                return;
            }
            AccountMutationResult result = new UserDAO().deleteAccount(userId);
            setResultMessage(session, result, "Đã xóa tài khoản. Các đơn hàng cũ vẫn được giữ lại.");
        } catch (NumberFormatException e) {
            session.setAttribute("toastError", "Mã tài khoản không hợp lệ.");
        }
    }

    private void setResultMessage(HttpSession session, AccountMutationResult result, String successMessage) {
        switch (result) {
            case SUCCESS:
                session.setAttribute("toastSuccess", successMessage);
                break;
            case NOT_FOUND:
                session.setAttribute("toastError", "Không tìm thấy tài khoản cần thao tác.");
                break;
            case LAST_ADMIN:
                session.setAttribute("toastError", "Không thể hạ quyền hoặc xóa quản trị viên cuối cùng.");
                break;
            default:
                session.setAttribute("toastError", "Không thể lưu thay đổi tài khoản. Vui lòng thử lại.");
                break;
        }
    }

    private boolean contains(String value, String search) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(search);
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
