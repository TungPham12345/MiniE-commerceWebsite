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

        // Lấy thông tin tài khoản (username) nhập từ form
        String u = request.getParameter("username");
        // Lấy mật khẩu (password) nhập từ form
        String p = request.getParameter("password");

        // Khởi tạo đối tượng UserDAO để truy vấn tài khoản trong CSDL
        UserDAO userDAO = new UserDAO();
        // Gọi hàm login để xác thực tài khoản và mật khẩu
        User user = userDAO.login(u, p);

        // Kiểm tra nếu tài khoản hợp lệ (tìm thấy trong CSDL hoặc dữ liệu mẫu)
        if (user != null) {
            // Lấy đối tượng phiên làm việc (HttpSession) hiện tại của người dùng
            HttpSession session = request.getSession();
            // Lưu đối tượng user vào Session để ghi nhớ trạng thái đã đăng nhập trên toàn hệ thống
            session.setAttribute("user", user);

            // Kiểm tra quyền hạn của người dùng
            if (user.isAdmin()) {
                // Nếu là tài khoản Admin (role = 1): Chuyển hướng sang trang quản trị Dashboard
                response.sendRedirect("admin/dashboard");
            } else {
                // Nếu là tài khoản Khách hàng (role = 0): Chuyển hướng về trang chủ mua sắm
                response.sendRedirect("home");
            }
        } else {
            // Nếu đăng nhập thất bại: Gán thông báo lỗi vào request
            request.setAttribute("errorMessage", "❌ Tên đăng nhập hoặc mật khẩu không chính xác!");
            // Giữ nguyên ở trang login.jsp và hiển thị thông báo lỗi
            request.getRequestDispatcher("login.jsp").forward(request, response);
        }
    }
}
