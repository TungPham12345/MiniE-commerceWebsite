// Khai báo package chứa controller
package controller;

// Import các thư viện Servlet và xử lý I/O
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
// Import HttpSession để thao tác hủy phiên đăng nhập
import jakarta.servlet.http.HttpSession;

/**
 * LogoutController: Xử lý chức năng đăng xuất khỏi hệ thống
 * Annotation @WebServlet ánh xạ URL "/logout" tới Servlet này
 */
@WebServlet(name = "LogoutController", urlPatterns = {"/logout"})
public class LogoutController extends HttpServlet {

    /**
     * Phương thức doGet: Xử lý khi người dùng nhấn vào nút "Đăng xuất"
     * @param request  Yêu cầu từ trình duyệt
     * @param response Phản hồi chuyển hướng về trang chủ
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Lấy phiên làm việc hiện tại, đối số false nghĩa là không tạo phiên mới nếu chưa tồn tại
        HttpSession session = request.getSession(false);
        // Kiểm tra nếu phiên làm việc đang tồn tại
        if (session != null) {
            // Hủy bỏ toàn bộ phiên làm việc (xóa sạch user, giỏ hàng khỏi session)
            session.invalidate();
        }
        // Chuyển hướng người dùng quay trở về trang chủ website
        response.sendRedirect("home");
    }
}
