// Khai báo package chứa controller
package controller;

// Import OrderDAO để thực hiện lưu đơn hàng và chi tiết đơn hàng vào CSDL
import dao.OrderDAO;
// Import các mô hình liên quan: món hàng giỏ, đơn hàng và người dùng
import model.CartItem;
import model.Order;
import model.OrderDetail;
import model.User;

// Import các thư viện danh sách và xử lý I/O
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
// Import các thư viện Servlet và Session
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * CheckoutController: Điều khiển quy trình Đặt hàng và Thanh toán (COD)
 * Nhận thông tin người nhận hàng, lưu đơn hàng vào bảng 'orders' và 'order_details', xóa giỏ hàng
 * Annotation @WebServlet ánh xạ URL "/checkout" tới Servlet này
 */
@WebServlet(name = "CheckoutController", urlPatterns = {"/checkout"})
public class CheckoutController extends HttpServlet {

    /**
     * Phương thức doGet: Xử lý khi người dùng nhấn "Tiến Hành Đặt Hàng" từ giỏ hàng
     * @param request  Yêu cầu từ trình duyệt
     * @param response Phản hồi chuyển tiếp sang trang checkout.jsp
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Lấy phiên làm việc hiện tại
        HttpSession session = request.getSession();
        // Lấy danh sách các món hàng trong giỏ
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");

        // Kiểm tra nếu giỏ hàng rỗng (chưa có sản phẩm nào)
        if (cart == null || cart.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        double totalMoney = 0;
        for (CartItem item : cart) {
            totalMoney += item.getTotalMoney();
        }

        request.setAttribute("cart", cart);
        request.setAttribute("totalMoney", totalMoney);

        // Nếu có sản phẩm, chuyển tiếp sang giao diện nhập thông tin thanh toán checkout.jsp
        request.getRequestDispatcher("checkout.jsp").forward(request, response);
    }

    /**
     * Phương thức doPost: Xử lý khi người dùng nhấn nút "Xác Nhận Đặt Hàng" trên form thanh toán
     * @param request  Yêu cầu chứa thông tin khách hàng và địa chỉ giao hàng
     * @param response Phản hồi chuyển tiếp sang trang báo đặt hàng thành công
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Cấu hình bảng mã UTF-8 cho phản hồi và yêu cầu để không lỗi font tiếng Việt
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

        // Lấy đối tượng phiên làm việc (Session)
        HttpSession session = request.getSession();
        // Lấy danh sách sản phẩm trong giỏ hàng và tổng tiền
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        User user = (User) session.getAttribute("user");

        // Kiểm tra lại nếu giỏ hàng bị rỗng thì không cho đặt
        if (cart == null || cart.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        // Lấy thông tin nhận hàng từ form giao diện checkout
        String customerName = request.getParameter("customerName");
        if (customerName == null || customerName.trim().isEmpty()) {
            customerName = request.getParameter("fullname");
        }
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String address = request.getParameter("address");
        String note = request.getParameter("note");
        String paymentMethod = request.getParameter("paymentMethod");

        double totalMoney = 0;
        for (CartItem item : cart) {
            totalMoney += item.getTotalMoney();
        }

        // Khởi tạo đối tượng đơn hàng Order và gán các thông tin
        Order order = new Order();
        if (user != null) {
            // Nếu khách hàng đã đăng nhập, liên kết mã user_id vào đơn hàng
            order.setUserId(user.getId());
        }
        order.setCustomerName(customerName != null ? customerName : "Khách Hàng");
        order.setEmail(email);
        order.setPhone(phone);
        order.setAddress(address);
        order.setNote(note);
        order.setTotalMoney(totalMoney);
        order.setPaymentMethod(paymentMethod != null ? paymentMethod : "COD");
        order.setStatus("PENDING");

        List<OrderDetail> orderDetails = new ArrayList<>();
        for (CartItem ci : cart) {
            OrderDetail od = new OrderDetail();
            od.setProductId(ci.getProduct().getId());
            od.setPrice(ci.getProduct().getEffectivePrice());
            od.setQuantity(ci.getQuantity());
            od.setSize(ci.getSize() != null ? ci.getSize() : "Freesize");
            od.setColor(ci.getColor() != null ? ci.getColor() : "Mặc định");
            od.setTotalMoney(ci.getTotalMoney());
            orderDetails.add(od);
        }

        // Khởi tạo đối tượng OrderDAO để lưu đơn hàng vào CSDL
        OrderDAO orderDAO = new OrderDAO();
        boolean success = orderDAO.createOrder(order, orderDetails);

        if (success) {
            int orderId = order.getId();
            // Xóa sạch giỏ hàng trong Session sau khi đã đặt hàng thành công
            session.removeAttribute("cart");
            session.removeAttribute("cartTotal");
            session.removeAttribute("cartSize");

            // Đính kèm mã đơn hàng và thông báo chúc mừng vào request
            request.setAttribute("orderSuccess", true);
            request.setAttribute("orderId", orderId);
            request.setAttribute("successMessage", "🎉 Đặt hàng thành công! Mã đơn hàng của bạn là #" + orderId);
            // Chuyển tiếp tới trang order-success.jsp để thông báo cho khách hàng
            request.getRequestDispatcher("order-success.jsp").forward(request, response);
        } else {
            // Nếu lưu đơn hàng thất bại: Báo lỗi và quay lại trang checkout.jsp
            request.setAttribute("errorMessage", "❌ Đặt hàng thất bại. Vui lòng thử lại!");
            request.setAttribute("error", "Có lỗi xảy ra khi xử lý đơn hàng. Vui lòng thử lại!");
            doGet(request, response);
        }
    }
}
