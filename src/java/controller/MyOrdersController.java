package controller;

import dao.OrderDAO;
import dao.ProductDAO;
import model.CartItem;
import model.Order;
import model.OrderDetail;
import model.Product;
import model.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * MyOrdersController: Quản lý xem và theo dõi đơn mua của khách hàng
 * Hỗ trợ các tab trạng thái Shopee-style:
 * - Tất cả
 * - Chờ thanh toán (UNPAID)
 * - Vận chuyển (SHIPPING)
 * - Chờ giao hàng (PENDING, CONFIRMED, PROCESSING)
 * - Hoàn thành (DELIVERED)
 * - Đã hủy (CANCELLED)
 * - Trả hàng/Hoàn tiền (REFUNDED)
 * Hỗ trợ tìm kiếm theo ID đơn hàng, tên sản phẩm, tên shop.
 * Hỗ trợ hủy đơn (PENDING), xác nhận nhận hàng (SHIPPING), và mua lại (repurchase).
 */
@WebServlet(name = "MyOrdersController", urlPatterns = {"/my-orders", "/user/orders"})
public class MyOrdersController extends HttpServlet {

    private final OrderDAO orderDAO = new OrderDAO();
    private final ProductDAO productDAO = new ProductDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("user");

        // Yêu cầu đăng nhập trước khi xem đơn mua
        if (currentUser == null) {
            session.setAttribute("redirectAfterLogin", request.getContextPath() + "/my-orders");
            session.setAttribute("errorMessage", "⚠️ Vui lòng đăng nhập để xem danh sách đơn mua của bạn!");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // Quyền hạn: Tài khoản Quản trị viên (Admin) không có mục Đơn mua cá nhân
        if (currentUser.isAdmin() || currentUser.getRole() == 1) {
            session.setAttribute("toastInfo", "Tài khoản Quản Trị Viên quản lý và kiểm duyệt toàn bộ đơn hàng tại Bảng Quản Trị Hệ Thống.");
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            return;
        }

        // Lấy thông báo Toast (nếu có từ phiên làm việc)
        String toastSuccess = (String) session.getAttribute("toastSuccess");
        String toastError = (String) session.getAttribute("toastError");
        if (toastSuccess != null) {
            request.setAttribute("toastSuccess", toastSuccess);
            session.removeAttribute("toastSuccess");
        }
        if (toastError != null) {
            request.setAttribute("toastError", toastError);
            session.removeAttribute("toastError");
        }

        // Lấy toàn bộ đơn hàng của người dùng hiện tại
        List<Order> userOrders = orderDAO.getOrdersByUserId(currentUser.getId());
        if (userOrders == null || userOrders.isEmpty()) {
            // Fallback: nếu userOrders chưa có trong DB theo userId, so khớp theo email
            List<Order> all = orderDAO.getAllOrders();
            userOrders = new ArrayList<>();
            for (Order o : all) {
                if (o.getUserId() != null && o.getUserId() == currentUser.getId()) {
                    userOrders.add(o);
                } else if (currentUser.getEmail() != null && currentUser.getEmail().equalsIgnoreCase(o.getEmail())) {
                    userOrders.add(o);
                }
            }
        }

        // Đếm số lượng đơn cho từng Tab theo 6 trạng thái + Tất cả
        int countAll = userOrders.size();
        int countPending = 0;    // 1. Chờ xác nhận (khi vừa mua hàng)
        int countConfirmed = 0;  // 2. Chờ lấy hàng (sau khi admin duyệt)
        int countShipping = 0;   // 3. Chờ giao hàng (đang vận chuyển)
        int countDelivered = 0;  // 4. Đã giao (hoàn tất)
        int countReturned = 0;   // 5. Trả hàng / hoàn tiền
        int countCancelled = 0;  // 6. Đã hủy

        for (Order o : userOrders) {
            String st = o.getStatus() != null ? o.getStatus().toUpperCase() : "";
            switch (st) {
                case "CONFIRMED":
                case "CHO_LAY_HANG":
                case "PROCESSING":
                    countConfirmed++;
                    break;
                case "SHIPPING":
                case "CHO_GIAO_HANG":
                case "VAN_CHUYEN":
                    countShipping++;
                    break;
                case "DELIVERED":
                case "COMPLETED":
                case "DA_GIAO":
                    countDelivered++;
                    break;
                case "RETURNED":
                case "REFUNDED":
                case "TRA_HANG":
                    countReturned++;
                    break;
                case "CANCELLED":
                case "DA_HUY":
                    countCancelled++;
                    break;
                case "PENDING":
                case "CHO_XAC_NHAN":
                case "UNPAID":
                default:
                    countPending++;
                    break;
            }
        }

        // Lấy tab đang chọn (mặc định: 'all')
        String tab = request.getParameter("tab");
        if (tab == null || tab.trim().isEmpty()) {
            tab = "all";
        } else {
            tab = tab.trim().toLowerCase();
        }

        // Lấy từ khóa tìm kiếm (theo ID đơn, tên sản phẩm, tên shop)
        String keyword = request.getParameter("keyword");
        if (keyword == null || keyword.trim().isEmpty()) {
            keyword = request.getParameter("search");
        }
        if (keyword != null) {
            keyword = keyword.trim();
        }

        // Lọc danh sách theo Tab: Tất cả - chờ xác nhận - chờ lấy hàng - chờ giao hàng - đã giao - trả hàng - đã hủy
        List<Order> tabOrders = new ArrayList<>();
        List<Order> filteredList = new ArrayList<>();
        for (Order o : userOrders) {
            String st = o.getStatus() != null ? o.getStatus().toUpperCase() : "";
            boolean matchTab = false;

            switch (tab) {
                case "pending":
                case "cho-xac-nhan":
                case "unpaid":
                    matchTab = "PENDING".equals(st) || "CHO_XAC_NHAN".equals(st) || "UNPAID".equals(st);
                    break;
                case "confirmed":
                case "cho-lay-hang":
                    matchTab = "CONFIRMED".equals(st) || "CHO_LAY_HANG".equals(st) || "PROCESSING".equals(st);
                    break;
                case "shipping":
                case "cho-giao-hang":
                    matchTab = "SHIPPING".equals(st) || "CHO_GIAO_HANG".equals(st) || "VAN_CHUYEN".equals(st);
                    break;
                case "delivered":
                case "completed":
                case "da-giao":
                    matchTab = "DELIVERED".equals(st) || "COMPLETED".equals(st) || "DA_GIAO".equals(st);
                    break;
                case "returned":
                case "refunded":
                case "tra-hang":
                    matchTab = "RETURNED".equals(st) || "REFUNDED".equals(st) || "TRA_HANG".equals(st);
                    break;
                case "cancelled":
                case "da-huy":
                    matchTab = "CANCELLED".equals(st) || "DA_HUY".equals(st);
                    break;
                case "all":
                default:
                    matchTab = true;
                    break;
            }

            if (!matchTab) {
                continue;
            }

            // Thêm vào danh sách toàn bộ đơn thuộc tab hiện tại
            tabOrders.add(o);

            // Lọc tiếp theo từ khóa tìm kiếm (nếu có)
            if (keyword != null && !keyword.isEmpty()) {
                String kw = keyword.toLowerCase();
                boolean matchKeyword = false;

                // Khớp mã đơn
                if (String.valueOf(o.getId()).contains(kw) || ("#" + o.getId()).contains(kw)) {
                    matchKeyword = true;
                }

                // Khớp tên shop
                if ("highfashion".contains(kw) || "highfashion official".contains(kw)) {
                    matchKeyword = true;
                }

                // Khớp tên khách hoặc số điện thoại
                if (o.getCustomerName() != null && o.getCustomerName().toLowerCase().contains(kw)) {
                    matchKeyword = true;
                }

                // Khớp tên bất kỳ sản phẩm nào trong chi tiết đơn
                if (!matchKeyword && o.getOrderDetails() != null) {
                    for (OrderDetail d : o.getOrderDetails()) {
                        if (d.getProductName() != null && d.getProductName().toLowerCase().contains(kw)) {
                            matchKeyword = true;
                            break;
                        }
                    }
                }

                if (!matchKeyword) {
                    continue;
                }
            }

            filteredList.add(o);
        }

        // Gắn dữ liệu vào request:
        // Sử dụng tabOrders để đảm bảo giao diện luôn nạp đầy đủ đơn của tab hiện tại,
        // giúp tính năng Tìm kiếm gõ đến đâu hiện đến đó và xóa hết thì trở về ban đầu tức thì
        request.setAttribute("orders", tabOrders);
        request.setAttribute("tabOrders", tabOrders);
        request.setAttribute("activeTab", tab);
        request.setAttribute("keyword", keyword != null ? keyword : "");
        request.setAttribute("countAll", countAll);
        request.setAttribute("countPending", countPending);
        request.setAttribute("countConfirmed", countConfirmed);
        request.setAttribute("countShipping", countShipping);
        request.setAttribute("countDelivered", countDelivered);
        request.setAttribute("countReturned", countReturned);
        request.setAttribute("countCancelled", countCancelled);
        // Tương thích ngược
        request.setAttribute("countUnpaid", countPending);
        request.setAttribute("countCompleted", countDelivered);
        request.setAttribute("countRefunded", countReturned);

        // Chuyển tiếp tới giao diện my-orders.jsp
        request.getRequestDispatcher("my-orders.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        if (currentUser.isAdmin() || currentUser.getRole() == 1) {
            session.setAttribute("toastInfo", "Tài khoản Quản Trị Viên quản lý đơn hàng tại Bảng Quản Trị Hệ Thống.");
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            return;
        }

        String action = request.getParameter("action");
        String orderIdRaw = request.getParameter("orderId");
        int orderId = 0;
        try {
            if (orderIdRaw != null) {
                orderId = Integer.parseInt(orderIdRaw.trim());
            }
        } catch (NumberFormatException e) {
            orderId = 0;
        }

        if ("cancel".equalsIgnoreCase(action) && orderId > 0) {
            // Xử lý khách hàng hủy đơn hàng (khi đơn còn ở trạng thái Chờ xác nhận)
            boolean success = orderDAO.cancelOrder(orderId, currentUser.getId());
            if (success) {
                session.setAttribute("toastSuccess", "🎉 Đã hủy đơn hàng #" + orderId + " thành công!");
            } else {
                session.setAttribute("toastError", "❌ Không thể hủy đơn #" + orderId + ". Đơn đã được xác nhận hoặc đang chuẩn bị giao!");
            }
            response.sendRedirect(request.getContextPath() + "/my-orders?tab=cancelled");
            return;
        }

        if ("confirmReceived".equalsIgnoreCase(action) && orderId > 0) {
            // Xử lý khách hàng xác nhận đã nhận hàng (chuyển sang DELIVERED)
            boolean success = orderDAO.confirmReceived(orderId, currentUser.getId());
            if (success) {
                session.setAttribute("toastSuccess", "🎉 Cảm ơn bạn! Đã xác nhận nhận hàng thành công cho đơn #" + orderId + ".");
            } else {
                session.setAttribute("toastError", "❌ Thao tác không hợp lệ hoặc đơn hàng không ở trạng thái giao hàng!");
            }
            response.sendRedirect(request.getContextPath() + "/my-orders?tab=delivered");
            return;
        }

        if ("returnOrder".equalsIgnoreCase(action) && orderId > 0) {
            // Xử lý khách hàng yêu cầu trả hàng
            boolean success = orderDAO.returnOrder(orderId, currentUser.getId());
            if (success) {
                session.setAttribute("toastSuccess", "📦 Đã gửi yêu cầu trả hàng cho đơn #" + orderId + " thành công!");
            } else {
                session.setAttribute("toastError", "❌ Không thể gửi yêu cầu trả hàng cho đơn #" + orderId + "!");
            }
            response.sendRedirect(request.getContextPath() + "/my-orders?tab=returned");
            return;
        }

        if ("repurchase".equalsIgnoreCase(action) && orderId > 0) {
            // Mua lại: Thêm các mặt hàng trong đơn vào lại giỏ hàng hiện tại
            Order order = orderDAO.getOrderById(orderId);
            if (order != null && order.getOrderDetails() != null && !order.getOrderDetails().isEmpty()) {
                @SuppressWarnings("unchecked")
                List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
                if (cart == null) {
                    cart = new ArrayList<>();
                }

                int addedCount = 0;
                for (OrderDetail d : order.getOrderDetails()) {
                    Product p = productDAO.getProductById(d.getProductId());
                    if (p != null) {
                        boolean exists = false;
                        for (CartItem ci : cart) {
                            if (ci.getProduct().getId() == p.getId() &&
                                ci.getSize().equalsIgnoreCase(d.getSize()) &&
                                ci.getColor().equalsIgnoreCase(d.getColor())) {
                                ci.setQuantity(ci.getQuantity() + d.getQuantity());
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) {
                            cart.add(new CartItem(p, d.getQuantity(), d.getSize(), d.getColor()));
                        }
                        addedCount += d.getQuantity();
                    }
                }

                session.setAttribute("cart", cart);
                session.setAttribute("toastSuccess", "🛒 Đã thêm " + addedCount + " sản phẩm từ đơn #" + orderId + " vào giỏ hàng của bạn!");
                response.sendRedirect(request.getContextPath() + "/cart");
                return;
            } else {
                session.setAttribute("toastError", "❌ Không tìm thấy thông tin sản phẩm trong đơn hàng để mua lại!");
                response.sendRedirect(request.getContextPath() + "/my-orders");
                return;
            }
        }

        // Mặc định chuyển về trang danh sách đơn mua
        response.sendRedirect(request.getContextPath() + "/my-orders");
    }
}
