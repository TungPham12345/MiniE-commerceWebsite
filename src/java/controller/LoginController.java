// Khai báo package chứa controller
package controller;

// Import lớp UserDAO để xử lý kiểm tra đăng nhập người dùng từ database
import dao.UserDAO;
// Import lớp mô hình User
import model.User;

// Import các thư viện hỗ trợ I/O và Servlet
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
// Import HttpSession để quản lý phiên làm việc của người dùng
import jakarta.servlet.http.HttpSession;

/**
 * LoginController: Điều khiển chức năng đăng nhập tài khoản hệ thống
 * Annotation @WebServlet ánh xạ URL "/login" tới Servlet này
 */
@WebServlet(name = "LoginController", urlPatterns = {"/login"})
public class LoginController extends HttpServlet {

    /**
     * Phương thức doGet: Xử lý khi người dùng mở trang đăng nhập
     * @param request  Yêu cầu từ trình duyệt
     * @param response Phản hồi từ máy chủ
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String redirect = request.getParameter("redirect");
        if (redirect != null && !redirect.trim().isEmpty()) {
            request.getSession().setAttribute("redirectAfterLogin", redirect.trim());
        }
        // Chuyển tiếp (forward) yêu cầu đến trang login.jsp để hiển thị form đăng nhập
        request.getRequestDispatcher("login.jsp").forward(request, response);
    }

    /**
     * Phương thức doPost: Xử lý khi người dùng bấm nút "Đăng Nhập" (gửi username và password lên)
     * @param request  Yêu cầu chứa dữ liệu gửi lên
     * @param response Phản hồi trả về
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Đặt định dạng trả về là HTML với bảng mã UTF-8
        response.setContentType("text/html;charset=UTF-8");
        // Đặt bảng mã UTF-8 cho request để tiếp nhận dữ liệu không bị lỗi font tiếng Việt
        request.setCharacterEncoding("UTF-8");

        // Lấy thông tin tài khoản và mật khẩu từ form
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // Khởi tạo đối tượng UserDAO để truy vấn tài khoản trong CSDL
        UserDAO userDAO = new UserDAO();
        // Gọi hàm login để xác thực tài khoản và mật khẩu
        User user = userDAO.login(username, password);

        // Kiểm tra nếu tài khoản hợp lệ (tìm thấy trong CSDL hoặc dữ liệu mẫu)
        if (user != null) {
            // Lấy đối tượng phiên làm việc (HttpSession) hiện tại của người dùng
            HttpSession session = request.getSession();
            // Lưu đối tượng user vào Session để ghi nhớ trạng thái đã đăng nhập trên toàn hệ thống
            session.setAttribute("user", user);

            // Kiểm tra xem có trang đang chờ người dùng quay lại sau đăng nhập (ví dụ: chi tiết sản phẩm / thanh toán)
            String redirectUrl = (String) session.getAttribute("redirectAfterLogin");
            if (redirectUrl != null && !redirectUrl.trim().isEmpty()) {
                session.removeAttribute("redirectAfterLogin");
                if (user.isAdmin() && (redirectUrl.contains("my-orders") || redirectUrl.contains("cart") || redirectUrl.contains("checkout"))) {
                    response.sendRedirect(request.getContextPath() + "/admin/dashboard");
                    return;
                }
                response.sendRedirect(redirectUrl);
                return;
            }

            // Kiểm tra quyền hạn của người dùng
            if (user.isAdmin()) {
                // Nếu là tài khoản Admin (role = 1): Chuyển hướng sang trang quản trị Dashboard
                response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            } else {
                // Nếu là tài khoản Khách hàng (role = 0): Chuyển hướng về trang chủ mua sắm
                response.sendRedirect(request.getContextPath() + "/home");
            }
        } else {
            // Nếu đăng nhập thất bại: Gán thông báo lỗi vào request
            request.setAttribute("error", "Tên đăng nhập hoặc mật khẩu không chính xác!");
            request.setAttribute("errorMessage", "❌ Tên đăng nhập hoặc mật khẩu không chính xác!");
            // Giữ nguyên ở trang login.jsp và hiển thị thông báo lỗi
            request.getRequestDispatcher("login.jsp").forward(request, response);
        }
    }
}
