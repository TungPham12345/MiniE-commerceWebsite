package controller;

import dao.UserDAO;
import dao.UserDAO.ProfileMutationResult;
import model.User;

import java.io.IOException;
import java.util.regex.Pattern;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "AccountController", urlPatterns = {"/account"})
public class AccountController extends HttpServlet {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User currentUser = getCurrentUser(request, response);
        if (currentUser == null) return;

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        User account = currentUser;
        try {
            User latestAccount = new UserDAO().getAccountById(currentUser.getId());
            if (latestAccount == null) {
                request.getSession().invalidate();
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }
            account = latestAccount;
            currentUser.setFullname(account.getFullname());
            currentUser.setEmail(account.getEmail());
            currentUser.setPhone(account.getPhone());
            currentUser.setAddress(account.getAddress());
            currentUser.setRole(account.getRole());
        } catch (IllegalStateException e) {
            request.getSession().setAttribute("toastError", e.getMessage());
        }
        request.setAttribute("account", account);
        request.getRequestDispatcher("/account.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User currentUser = getCurrentUser(request, response);
        if (currentUser == null) return;
        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();
        String action = request.getParameter("action");
        if ("profile".equals(action)) {
            updateProfile(request, session, currentUser);
        } else if ("password".equals(action)) {
            changePassword(request, session, currentUser);
        } else {
            session.setAttribute("toastError", "Thao tác tài khoản không hợp lệ.");
        }
        response.sendRedirect(request.getContextPath() + "/account");
    }

    private User getCurrentUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }
        return user;
    }

    private void updateProfile(HttpServletRequest request, HttpSession session, User currentUser) {
        String fullname = trimToNull(request.getParameter("fullname"));
        String email = trimToNull(request.getParameter("email"));
        String phone = trimToNull(request.getParameter("phone"));
        String address = trimToNull(request.getParameter("address"));
        if (fullname == null || fullname.length() > 100
                || email == null || email.length() > 100 || !EMAIL_PATTERN.matcher(email).matches()
                || (phone != null && phone.length() > 20)
                || (address != null && address.length() > 255)) {
            session.setAttribute("toastError", "Thông tin chưa hợp lệ. Hãy kiểm tra họ tên, email, số điện thoại và địa chỉ.");
            return;
        }

        User profile = new User();
        profile.setId(currentUser.getId());
        profile.setFullname(fullname);
        profile.setEmail(email);
        profile.setPhone(phone);
        profile.setAddress(address);
        ProfileMutationResult result = new UserDAO().updateProfile(profile);
        if (result == ProfileMutationResult.SUCCESS) {
            currentUser.setFullname(fullname);
            currentUser.setEmail(email);
            currentUser.setPhone(phone);
            currentUser.setAddress(address);
            session.setAttribute("toastSuccess", "Đã cập nhật thông tin cá nhân.");
        } else {
            session.setAttribute("toastError", "Không thể cập nhật thông tin. Vui lòng thử lại.");
        }
    }

    private void changePassword(HttpServletRequest request, HttpSession session, User currentUser) {
        String currentPassword = trimToNull(request.getParameter("currentPassword"));
        String newPassword = trimToNull(request.getParameter("newPassword"));
        String confirmPassword = trimToNull(request.getParameter("confirmPassword"));
        if (currentPassword == null || newPassword == null || confirmPassword == null
                || newPassword.length() < 6 || newPassword.length() > 255) {
            session.setAttribute("toastError", "Hãy nhập mật khẩu hiện tại và mật khẩu mới tối thiểu 6 ký tự.");
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            session.setAttribute("toastError", "Mật khẩu mới và xác nhận mật khẩu không khớp.");
            return;
        }
        if (currentPassword.equals(newPassword)) {
            session.setAttribute("toastError", "Mật khẩu mới phải khác mật khẩu hiện tại.");
            return;
        }

        ProfileMutationResult result = new UserDAO().changePassword(
                currentUser.getId(), currentPassword, newPassword);
        if (result == ProfileMutationResult.SUCCESS) {
            currentUser.setPassword(newPassword);
            session.setAttribute("toastSuccess", "Đã đổi mật khẩu thành công.");
        } else if (result == ProfileMutationResult.INVALID_PASSWORD) {
            session.setAttribute("toastError", "Mật khẩu hiện tại không chính xác.");
        } else {
            session.setAttribute("toastError", "Không thể đổi mật khẩu. Vui lòng thử lại.");
        }
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
