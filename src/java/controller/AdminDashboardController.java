// Khai báo package chứa controller
package controller;

// Import OrderDAO và ProductDAO để truy vấn thống kê doanh thu, đơn hàng và kho sản phẩm
import dao.OrderDAO;
import dao.ProductDAO;
// Import các mô hình dữ liệu cần thiết
import model.Order;
import model.Product;
import model.User;

// Import thư viện danh sách và xử lý ngoại lệ I/O
import java.io.IOException;
import java.util.List;
// Import các thư viện Servlet và Session
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * AdminDashboardController: Bảng điều khiển Quản trị viên (Admin Dashboard)
 * Thống kê tổng doanh thu, số đơn hàng chờ duyệt, tổng sản phẩm và duyệt đơn hàng
 * Annotation @WebServlet ánh xạ URL "/admin/dashboard" tới Servlet này
 */
@WebServlet(name = "AdminDashboardController", urlPatterns = {"/admin/dashboard"})
public class AdminDashboardController extends HttpServlet {

    /**
     * Phương thức doGet: Xử lý hiển thị bảng điều khiển và thao tác duyệt đơn hàng của Admin
     * @param request  Yêu cầu từ trình duyệt
     * @param response Phản hồi chuyển tiếp sang admin-dashboard.jsp
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 1. Kiểm tra xác thực quyền Admin
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        // Nếu người dùng chưa đăng nhập hoặc không phải là Admin (role != 1)
        if (user == null || !user.isAdmin()) {
            // Chặn truy cập và chuyển hướng người dùng về trang đăng nhập
            response.sendRedirect("../login");
            return;
        }

        // Khởi tạo các đối tượng DAO
        ProductDAO productDAO = new ProductDAO();
        OrderDAO orderDAO = new OrderDAO();

        // 2. Xử lý thao tác cập nhật trạng thái đơn hàng (Duyệt đơn hàng)
        String action = request.getParameter("action");
        if ("updateStatus".equals(action)) {
            String orderIdRaw = request.getParameter("orderId"); // Mã đơn hàng cần cập nhật
            String newStatus = request.getParameter("status");   // Trạng thái mới (ví dụ: 'DELIVERED')
            if (orderIdRaw != null && newStatus != null) {
                try {
                    int orderId = Integer.parseInt(orderIdRaw);
                    // Gọi hàm cập nhật trạng thái đơn hàng trong DAO
                    orderDAO.updateOrderStatus(orderId, newStatus);
                } catch (Exception ignored) {}
            }
            // Sau khi cập nhật xong, chuyển hướng tải lại trang dashboard để làm mới số liệu
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            return;
        }

        // 3. Lấy dữ liệu tổng hợp để thống kê
        List<Product> products = productDAO.getAllProducts(); // Lấy toàn bộ sản phẩm
        List<Order> orders = orderDAO.getAllOrders();         // Lấy toàn bộ đơn hàng

        double totalRevenue = 0;       // Tổng doanh thu bán hàng
        int pendingOrdersCount = 0;    // Số đơn hàng đang chờ duyệt

        // Duyệt qua từng đơn hàng để tính toán tổng doanh thu và đếm đơn chờ xử lý
        for (Order o : orders) {
            totalRevenue += o.getTotalMoney(); // Cộng dồn tiền đơn hàng
            if ("PENDING".equalsIgnoreCase(o.getStatus())) {
                pendingOrdersCount++;          // Tăng biến đếm đơn chờ xử lý
            }
        }

        // 4. Đính kèm các chỉ số thống kê vào request gửi sang file JSP
        request.setAttribute("productsCount", products.size());                // Tổng số lượng sản phẩm
        request.setAttribute("ordersCount", orders.size());                    // Tổng số đơn hàng
        request.setAttribute("totalRevenue", totalRevenue);                    // Tổng số tiền doanh thu
        request.setAttribute("pendingOrdersCount", pendingOrdersCount);        // Số đơn hàng chờ xử lý
        // Lấy tối đa 8 đơn hàng gần nhất để hiển thị trên bảng danh sách
        request.setAttribute("recentOrders", orders.size() > 8 ? orders.subList(0, 8) : orders);

        // Chuyển tiếp tới trang giao diện quản trị admin-dashboard.jsp
        request.getRequestDispatcher("/admin-dashboard.jsp").forward(request, response);
    }
}
