package controller;

import dao.UserDAO;
import dao.UserDAO.ProfileMutationResult;
import jakarta.mail.MessagingException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import service.PasswordResetMailService;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.regex.Pattern;

@WebServlet(name = "ForgotPasswordController", urlPatterns = {"/forgot-password"})
public class ForgotPasswordController extends HttpServlet {

    private static final String RESET_STATE_KEY = "passwordResetState";
    private static final String LAST_CODE_SENT_KEY = "passwordResetLastCodeSent";
    private static final long CODE_LIFETIME_MILLIS = 10 * 60 * 1000L;
    private static final long VERIFIED_LIFETIME_MILLIS = 5 * 60 * 1000L;
    private static final long RESEND_WAIT_MILLIS = 60 * 1000L;
    private static final int MAX_CODE_ATTEMPTS = 5;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        ResetState state = getValidState(request.getSession(), System.currentTimeMillis());
        if (state == null) {
            setStep(request, "email", null, null);
        } else {
            setStep(request, state.verified ? "reset" : "verify", null, maskEmail(state.email));
        }
        request.getRequestDispatcher("/forgot-password.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        HttpSession session = request.getSession();
        String action = request.getParameter("action");
        if ("sendCode".equals(action)) {
            sendCode(request, session);
        } else if ("verifyCode".equals(action)) {
            verifyCode(request, session);
        } else if ("resetPassword".equals(action)) {
            if (resetPassword(request, response, session)) return;
        } else {
            request.setAttribute("errorMessage", "Thao tác không hợp lệ.");
            setStep(request, "email", null, null);
        }
        request.getRequestDispatcher("/forgot-password.jsp").forward(request, response);
    }

    private void sendCode(HttpServletRequest request, HttpSession session) {
        ResetState previousState = getValidState(session, System.currentTimeMillis());
        String email = trimToNull(request.getParameter("email"));
        if (email == null && previousState != null) email = previousState.email;
        if (email == null || email.length() > 100 || !EMAIL_PATTERN.matcher(email).matches()) {
            request.setAttribute("errorMessage", "Vui lòng nhập địa chỉ email hợp lệ.");
            setStep(request, "email", email, null);
            return;
        }

        if (!PasswordResetMailService.isConfigured()) {
            request.setAttribute("errorMessage",
                    "Chức năng gửi mã chưa được cấu hình. Vui lòng liên hệ quản trị viên.");
            setStep(request, "email", email, null);
            return;
        }

        long now = System.currentTimeMillis();
        Object lastSentValue = session.getAttribute(LAST_CODE_SENT_KEY);
        if (lastSentValue instanceof Long && now - (Long) lastSentValue < RESEND_WAIT_MILLIS) {
            request.setAttribute("errorMessage", "Vui lòng đợi 60 giây trước khi yêu cầu gửi mã mới.");
            setStep(request, previousState == null ? "email" : "verify",
                    email, previousState == null ? null : maskEmail(previousState.email));
            return;
        }

        User user;
        try {
            user = new UserDAO().findAccountByEmail(email);
        } catch (IllegalStateException e) {
            request.setAttribute("errorMessage", "Không thể tra cứu tài khoản lúc này. Vui lòng thử lại sau.");
            setStep(request, "email", email, null);
            return;
        }

        if (user == null) {
            request.setAttribute("successMessage",
                    "Nếu email đã được đăng ký, mã xác nhận sẽ được gửi đến hộp thư đó.");
            setStep(request, "email", email, null);
            return;
        }

        String code = String.format(Locale.ROOT, "%06d", RANDOM.nextInt(1_000_000));
        ResetState state = new ResetState();
        state.userId = user.getId();
        state.email = email;
        state.salt = new byte[16];
        RANDOM.nextBytes(state.salt);
        state.codeHash = hashCode(state.salt, code);
        state.expiresAt = now + CODE_LIFETIME_MILLIS;

        try {
            PasswordResetMailService.sendResetCode(email, code);
        } catch (MessagingException | RuntimeException e) {
            System.err.println("⚠️ Không thể gửi mã đặt lại mật khẩu: " + e.getMessage());
            request.setAttribute("errorMessage", "Không thể gửi email lúc này. Vui lòng thử lại sau.");
            setStep(request, "email", email, null);
            return;
        }

        session.setAttribute(RESET_STATE_KEY, state);
        session.setAttribute(LAST_CODE_SENT_KEY, now);
        request.setAttribute("successMessage",
                "Nếu email đã được đăng ký, mã xác nhận sẽ được gửi đến hộp thư đó.");
        setStep(request, "verify", null, maskEmail(email));
    }

    private void verifyCode(HttpServletRequest request, HttpSession session) {
        ResetState state = getValidState(session, System.currentTimeMillis());
        if (state == null || state.verified) {
            request.setAttribute("errorMessage", "Mã xác nhận đã hết hạn. Vui lòng yêu cầu mã mới.");
            setStep(request, "email", null, null);
            return;
        }

        String code = trimToNull(request.getParameter("code"));
        if (code == null || !code.matches("\\d{6}")
                || !MessageDigest.isEqual(state.codeHash, hashCode(state.salt, code))) {
            state.attempts++;
            if (state.attempts >= MAX_CODE_ATTEMPTS) {
                session.removeAttribute(RESET_STATE_KEY);
                request.setAttribute("errorMessage", "Bạn đã nhập sai mã quá nhiều lần. Hãy yêu cầu mã mới.");
                setStep(request, "email", null, null);
            } else {
                request.setAttribute("errorMessage", "Mã xác nhận không chính xác hoặc đã hết hạn.");
                setStep(request, "verify", null, maskEmail(state.email));
            }
            return;
        }

        state.verified = true;
        state.verifiedUntil = System.currentTimeMillis() + VERIFIED_LIFETIME_MILLIS;
        request.setAttribute("successMessage", "Xác nhận thành công. Hãy đặt mật khẩu mới.");
        setStep(request, "reset", null, maskEmail(state.email));
    }

    private boolean resetPassword(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException {
        ResetState state = getValidState(session, System.currentTimeMillis());
        if (state == null || !state.verified) {
            request.setAttribute("errorMessage", "Phiên xác nhận đã hết hạn. Vui lòng yêu cầu mã mới.");
            setStep(request, "email", null, null);
            return false;
        }

        String newPassword = trimToNull(request.getParameter("newPassword"));
        String confirmPassword = trimToNull(request.getParameter("confirmPassword"));
        if (newPassword == null || newPassword.length() < 6 || newPassword.length() > 255) {
            request.setAttribute("errorMessage", "Mật khẩu mới cần có từ 6 đến 255 ký tự.");
            setStep(request, "reset", null, maskEmail(state.email));
            return false;
        }
        if (!newPassword.equals(confirmPassword)) {
            request.setAttribute("errorMessage", "Mật khẩu mới và xác nhận mật khẩu không khớp.");
            setStep(request, "reset", null, maskEmail(state.email));
            return false;
        }

        ProfileMutationResult result = new UserDAO().resetPassword(state.userId, newPassword);
        if (result != ProfileMutationResult.SUCCESS) {
            request.setAttribute("errorMessage", "Không thể cập nhật mật khẩu. Vui lòng thử lại.");
            setStep(request, "reset", null, maskEmail(state.email));
            return false;
        }

        session.removeAttribute(RESET_STATE_KEY);
        session.removeAttribute(LAST_CODE_SENT_KEY);
        session.setAttribute("successMessage", "Đặt lại mật khẩu thành công. Bạn có thể đăng nhập bằng mật khẩu mới.");
        response.sendRedirect(request.getContextPath() + "/login");
        return true;
    }

    private ResetState getValidState(HttpSession session, long now) {
        Object value = session.getAttribute(RESET_STATE_KEY);
        if (!(value instanceof ResetState)) return null;
        ResetState state = (ResetState) value;
        long expiresAt = state.verified ? state.verifiedUntil : state.expiresAt;
        if (now > expiresAt) {
            session.removeAttribute(RESET_STATE_KEY);
            return null;
        }
        return state;
    }

    private void setStep(HttpServletRequest request, String step, String email, String maskedEmail) {
        request.setAttribute("forgotStep", step);
        request.setAttribute("forgotEmail", email == null ? "" : email);
        request.setAttribute("maskedEmail", maskedEmail == null ? "" : maskedEmail);
    }

    private String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 0) return "***";
        String local = email.substring(0, at);
        String visible = local.length() == 1 ? "*" : local.substring(0, 1) + "***";
        return visible + email.substring(at);
    }

    private byte[] hashCode(byte[] salt, String code) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            return digest.digest(code.getBytes(StandardCharsets.UTF_8));
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("Không hỗ trợ thuật toán xác thực mã.", e);
        }
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static final class ResetState implements Serializable {
        private static final long serialVersionUID = 1L;
        private int userId;
        private String email;
        private byte[] salt;
        private byte[] codeHash;
        private long expiresAt;
        private long verifiedUntil;
        private int attempts;
        private boolean verified;
    }
}
