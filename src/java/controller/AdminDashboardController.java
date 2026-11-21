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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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
                        if ("CONFIRMED".equals(newStatus)) {
                            msg = "🎉 Đã xác nhận đơn hàng #" + orderId + "! Đơn chuyển sang trạng thái 'Chờ lấy hàng'.";
                        } else if ("SHIPPING".equals(newStatus)) {
                            msg = "🚚 Đã chuyển đơn hàng #" + orderId + " sang trạng thái 'Chờ giao hàng'!";
                        } else if ("DELIVERED".equals(newStatus)) {
                            msg = "✅ Đã xác nhận giao thành công đơn hàng #" + orderId + " (Đã giao)!";
                        } else if ("RETURNED".equals(newStatus)) {
                            msg = "🔄 Đã chuyển đơn hàng #" + orderId + " sang trạng thái 'Trả hàng'!";
                        } else if ("CANCELLED".equals(newStatus)) {
                            msg = "❌ Đã hủy đơn hàng #" + orderId + "!";
                        } else if ("PENDING".equals(newStatus)) {
                            msg = "🔄 Đã đưa đơn hàng #" + orderId + " về trạng thái 'Chờ xác nhận'!";
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
            if ("PENDING".equalsIgnoreCase(o.getStatus()) || "CHO_XAC_NHAN".equalsIgnoreCase(o.getStatus()) || "UNPAID".equalsIgnoreCase(o.getStatus())) {
                pendingOrdersCount++;          // Tăng biến đếm đơn chờ xác nhận
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
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        if ("advanceSelected".equals(request.getParameter("action"))) {
            processSelectedOrders(request, response, session);
            return;
        }

        doGet(request, response);
    }

    private void processSelectedOrders(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException {
        String[] selectedIds = request.getParameterValues("orderIds");
        if (selectedIds == null || selectedIds.length == 0) {
            session.setAttribute("toastError", "Vui lòng chọn ít nhất một đơn hàng.");
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            return;
        }

        Set<Integer> orderIds = new LinkedHashSet<>();
        int invalidIdCount = 0;
        for (String value : selectedIds) {
            try {
                int orderId = Integer.parseInt(value);
                if (orderId > 0) orderIds.add(orderId);
                else invalidIdCount++;
            } catch (NumberFormatException ignored) {
                invalidIdCount++;
            }
        }
        if (orderIds.isEmpty()) {
            session.setAttribute("toastError", invalidIdCount > 0
                    ? "Danh sách chọn không chứa mã đơn hàng hợp lệ."
                    : "Không tìm thấy đơn hàng hợp lệ để xử lý.");
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            return;
        }

        OrderDAO orderDAO = new OrderDAO();
        List<Order> orders = orderDAO.getAllOrders();
        List<Integer> updatedIds = new ArrayList<>();
        List<Integer> skippedIds = new ArrayList<>();
        List<Integer> failedIds = new ArrayList<>();
        int invalidCount = invalidIdCount;

        for (Integer orderId : orderIds) {
            Order selectedOrder = null;
            for (Order order : orders) {
                if (order.getId() == orderId) {
                    selectedOrder = order;
                    break;
                }
            }

            String nextStatus = selectedOrder == null ? null : nextProcessingStatus(selectedOrder.getStatus());
            if (nextStatus == null) {
                skippedIds.add(orderId);
            } else if (orderDAO.updateOrderStatus(orderId, nextStatus)) {
                updatedIds.add(orderId);
            } else {
                failedIds.add(orderId);
            }
        }

        if (!updatedIds.isEmpty()) {
            session.setAttribute("toastSuccess",
                    "Đã chuyển trạng thái " + updatedIds.size() + " đơn: " + formatOrderIds(updatedIds) + ".");
        }
        if (!skippedIds.isEmpty() || !failedIds.isEmpty() || invalidCount > 0) {
            StringBuilder message = new StringBuilder();
            if (!skippedIds.isEmpty()) {
                message.append("Bỏ qua ").append(skippedIds.size())
                        .append(" đơn không tìm thấy hoặc không có bước giao hàng tiếp theo: ")
                        .append(formatOrderIds(skippedIds)).append(". ");
            }
            if (!failedIds.isEmpty()) {
                message.append("Không cập nhật được ").append(failedIds.size())
                        .append(" đơn: ").append(formatOrderIds(failedIds)).append(".");
            }
            if (invalidCount > 0) {
                message.append(" Bỏ qua ").append(invalidCount).append(" mã đơn không hợp lệ.");
            }
            session.setAttribute("toastError", message.toString().trim());
        } else if (updatedIds.isEmpty()) {
            session.setAttribute("toastInfo", "Không có đơn nào cần chuyển trạng thái.");
        }

        response.sendRedirect(request.getContextPath() + "/admin/dashboard");
    }

    private String nextProcessingStatus(String currentStatus) {
        if (currentStatus == null) return null;
        switch (currentStatus.trim().toUpperCase()) {
            case "PENDING":
            case "UNPAID":
            case "CHO_XU_LY":
            case "CHO_XAC_NHAN":
                return "CONFIRMED";
            case "CONFIRMED":
            case "CHO_LAY_HANG":
            case "PROCESSING":
                return "SHIPPING";
            case "SHIPPING":
            case "CHO_GIAO_HANG":
            case "VAN_CHUYEN":
                return "DELIVERED";
            default:
                return null;
        }
    }

    private String formatOrderIds(List<Integer> orderIds) {
        StringBuilder formatted = new StringBuilder();
        for (Integer orderId : orderIds) {
            if (formatted.length() > 0) formatted.append(", ");
            formatted.append("#").append(orderId);
        }
        return formatted.toString();
    }
}
