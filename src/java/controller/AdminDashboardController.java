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
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // Khởi tạo các đối tượng DAO
        ProductDAO productDAO = new ProductDAO();
        OrderDAO orderDAO = new OrderDAO();

        // 2. Xử lý thao tác cập nhật trạng thái đơn hàng (Duyệt đơn hàng)
        String action = request.getParameter("action");
        if ("updateStatus".equals(action)) {
            String orderIdRaw = request.getParameter("orderId"); // Mã đơn hàng cần cập nhật
            String newStatus = request.getParameter("status");   // Trạng thái mới (ví dụ: 'DELIVERED', 'CONFIRMED', 'CANCELLED')
            if (orderIdRaw != null && newStatus != null) {
                try {
                    int orderId = Integer.parseInt(orderIdRaw);
                    newStatus = newStatus.trim().toUpperCase();
                    // Gọi hàm cập nhật trạng thái đơn hàng trong DAO
                    boolean success = orderDAO.updateOrderStatus(orderId, newStatus);
                    if (success) {
                        String msg = "Cập nhật đơn hàng #" + orderId + " thành công!";
                        if ("DELIVERED".equals(newStatus) || "CONFIRMED".equals(newStatus)) {
                            msg = "🎉 Duyệt thành công đơn hàng #" + orderId + "!";
                        } else if ("CANCELLED".equals(newStatus)) {
                            msg = "❌ Đã hủy đơn hàng #" + orderId + "!";
                        } else if ("PENDING".equals(newStatus)) {
                            msg = "🔄 Đã khôi phục đơn hàng #" + orderId + " về trạng thái Chờ xử lý!";
                        }
                        session.setAttribute("toastSuccess", msg);
                    } else {
                        session.setAttribute("toastError", "Không tìm thấy hoặc không thể cập nhật đơn hàng #" + orderId);
                    }
                } catch (Exception e) {
                    session.setAttribute("toastError", "Lỗi dữ liệu đơn hàng: " + e.getMessage());
                }
            }
            // Sau khi cập nhật xong, chuyển hướng tải lại trang dashboard để làm mới số liệu
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            return;
        } else if ("delete".equals(action)) {
            String orderIdRaw = request.getParameter("orderId");
            if (orderIdRaw != null) {
                try {
                    int orderId = Integer.parseInt(orderIdRaw);
                    boolean success = orderDAO.deleteOrder(orderId);
                    if (success) {
                        session.setAttribute("toastSuccess", "🗑️ Đã xóa vĩnh viễn đơn hàng #" + orderId + " thành công!");
                    } else {
                        session.setAttribute("toastError", "Không thể xóa đơn hàng #" + orderId);
                    }
                } catch (Exception e) {
                    session.setAttribute("toastError", "Lỗi xóa đơn hàng: " + e.getMessage());
                }
            }
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            return;
        } else if ("deleteAll".equals(action)) {
            boolean success = orderDAO.deleteAllOrders();
            if (success) {
                session.setAttribute("toastSuccess", "🧹 Đã xóa sạch toàn bộ đơn hàng trong hệ thống!");
            } else {
                session.setAttribute("toastError", "Lỗi khi xóa toàn bộ đơn hàng!");
            }
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
            // Không tính các đơn đã bị hủy vào doanh thu
            if (!"CANCELLED".equalsIgnoreCase(o.getStatus())) {
                totalRevenue += o.getTotalMoney(); // Cộng dồn tiền đơn hàng
            }
            if ("PENDING".equalsIgnoreCase(o.getStatus())) {
                pendingOrdersCount++;          // Tăng biến đếm đơn chờ xử lý
            }
        }

        // 4. Đính kèm các chỉ số thống kê vào request gửi sang file JSP
        request.setAttribute("productsCount", products.size());                // Tổng số lượng sản phẩm
        request.setAttribute("totalProducts", products.size());
        request.setAttribute("ordersCount", orders.size());                    // Tổng số đơn hàng
        request.setAttribute("totalOrders", orders.size());
        request.setAttribute("totalRevenue", totalRevenue);                    // Tổng số tiền doanh thu
        request.setAttribute("pendingOrdersCount", pendingOrdersCount);        // Số đơn hàng chờ xử lý
        // Lấy danh sách đơn hàng
        request.setAttribute("recentOrders", orders);

        // Chuyển tiếp tới trang giao diện quản trị admin-dashboard.jsp
        request.getRequestDispatcher("/admin-dashboard.jsp").forward(request, response);
    }

    /**
     * Phương thức doPost: Chuyển tiếp sang doGet để xử lý các form gửi trạng thái
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
