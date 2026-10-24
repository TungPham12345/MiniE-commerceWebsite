// Khai báo package chứa controller
package controller;

// Import ProductDAO để lấy thông tin sản phẩm khi thêm vào giỏ
import dao.ProductDAO;
// Import mô hình món hàng trong giỏ (CartItem), sản phẩm (Product) và người dùng (User)
import model.CartItem;
import model.Product;
import model.User;

// Import các thư viện tiện ích danh sách và xử lý I/O
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
 * CartController: Điều khiển Giỏ hàng mua sắm của khách hàng
 * Xử lý: Thêm sản phẩm, cập nhật số lượng, xóa sản phẩm và tính toán tổng tiền
 * Annotation @WebServlet ánh xạ URL "/cart" tới Servlet này
 */
@WebServlet(name = "CartController", urlPatterns = {"/cart"})
public class CartController extends HttpServlet {

    /**
     * Phương thức doGet: Xử lý hiển thị trang giỏ hàng khi người dùng bấm vào biểu tượng Giỏ hàng
     * @param request  Yêu cầu từ trình duyệt
     * @param response Phản hồi chuyển tiếp sang trang cart.jsp
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();

        // Kiểm tra bắt buộc đăng nhập để xem giỏ hàng
        User user = (User) session.getAttribute("user");
        if (user == null) {
            session.setAttribute("redirectAfterLogin", request.getContextPath() + "/cart");
            session.setAttribute("errorMessage", "⚠️ Bạn chưa đăng nhập! Vui lòng đăng nhập để xem và quản lý giỏ hàng của bạn.");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new ArrayList<>();
        }

        double totalMoney = 0;
        int totalCount = 0;
        for (CartItem item : cart) {
            totalMoney += item.getTotalMoney();
            totalCount += item.getQuantity();
        }

        session.setAttribute("cartTotal", totalMoney);
        session.setAttribute("cartSize", totalCount);
        request.setAttribute("cart", cart);
        request.setAttribute("totalMoney", totalMoney);

        // Chuyển tiếp tới trang cart.jsp để hiển thị danh sách các món hàng đã chọn
        request.getRequestDispatcher("cart.jsp").forward(request, response);
    }

    /**
     * Phương thức doPost: Xử lý các thao tác giỏ hàng (action: 'add', 'update', 'remove')
     * @param request  Yêu cầu chứa hành động và tham số món hàng
     * @param response Phản hồi sau khi cập nhật giỏ hàng
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Thiết lập bảng mã UTF-8 cho phản hồi và yêu cầu
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

        // Lấy tên hành động cần thực hiện (ví dụ: 'add' - thêm, 'update' - sửa, 'remove' - xóa)
        String action = request.getParameter("action");
        // Lấy phiên làm việc hiện tại của người dùng
        HttpSession session = request.getSession();

        // KIỂM TRA BẮT BUỘC ĐĂNG NHẬP: Người dùng phải đăng nhập mới được mua hàng hoặc thêm vào giỏ
        User user = (User) session.getAttribute("user");
        if (user == null) {
            String productIdStr = request.getParameter("productId");
            if (productIdStr != null && !productIdStr.trim().isEmpty()) {
                session.setAttribute("redirectAfterLogin", request.getContextPath() + "/product-detail?id=" + productIdStr.trim());
            } else {
                session.setAttribute("redirectAfterLogin", request.getContextPath() + "/cart");
            }
            session.setAttribute("errorMessage", "⚠️ Bạn chưa đăng nhập! Vui lòng đăng nhập tài khoản trước khi mua hàng hoặc thêm vào giỏ hàng.");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // Lấy giỏ hàng từ Session ra (danh sách các CartItem)
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        // Nếu giỏ hàng trong Session chưa có thì khởi tạo một danh sách mới rỗng
        if (cart == null) {
            cart = new ArrayList<>();
        }

        ProductDAO productDAO = new ProductDAO();
        CartItem addedItem = null; // Biến lưu món hàng vừa mới thêm

        // ========================================================
        // 1. TRƯỜNG HỢP: THÊM SẢN PHẨM VÀO GIỎ HÀNG (action == 'add')
        // ========================================================
        if ("add".equals(action)) {
            // Lấy ID sản phẩm, số lượng, size và màu sắc người dùng chọn từ form
            int productId = Integer.parseInt(request.getParameter("productId"));
            int quantity = Integer.parseInt(request.getParameter("quantity"));
            String size = request.getParameter("size");
            String color = request.getParameter("color");
            String buyNow = request.getParameter("buyNow");

            // Gán giá trị mặc định nếu người dùng chưa chọn size hoặc màu
            if (size == null || size.trim().isEmpty()) size = "M";
            if (color == null || color.trim().isEmpty()) color = "Đen";

            // Tìm sản phẩm trong CSDL theo ID
            Product p = productDAO.getProductById(productId);
            if (p != null) {
                boolean found = false;
                for (CartItem item : cart) {
                    if (item.getProduct().getId() == productId 
                        && item.getSize().equalsIgnoreCase(size) 
                        && item.getColor().equalsIgnoreCase(color)) {
                        item.setQuantity(item.getQuantity() + quantity);
                        found = true;
                        addedItem = item;
                        break;
                    }
                }
                if (!found) {
                    addedItem = new CartItem(p, quantity, size, color);
                    cart.add(addedItem);
                }

                // Update session totals
                double total = 0;
                int totalCount = 0;
                for (CartItem ci : cart) {
                    total += ci.getTotalMoney();
                    totalCount += ci.getQuantity();
                }

                session.setAttribute("addedToCart", true);
                session.setAttribute("lastAddedItem", addedItem);
                session.setAttribute("cartSize", totalCount);
                session.setAttribute("cartTotal", total);
            }
            session.setAttribute("cart", cart);

            // Nếu người dùng chọn "Mua ngay", chuyển thẳng tới trang Thanh toán (Checkout)
            if ("true".equalsIgnoreCase(buyNow) || "1".equals(buyNow)) {
                response.sendRedirect(request.getContextPath() + "/checkout");
            } else {
                // Nếu chọn "Thêm vào giỏ hàng", giữ người dùng ở lại trang sản phẩm để tiếp tục mua sắm
                response.sendRedirect(request.getContextPath() + "/product-detail?id=" + productId);
            }
            return;

        // ========================================================
        // 2. TRƯỜNG HỢP: CẬP NHẬT SỐ LƯỢNG MÓN HÀNG (action == 'update')
        // ========================================================
        } else if ("update".equals(action)) {
            int productId = Integer.parseInt(request.getParameter("productId"));
            int quantity = Integer.parseInt(request.getParameter("quantity"));
            String size = request.getParameter("size");

            for (CartItem item : cart) {
                if (item.getProduct().getId() == productId && (size == null || item.getSize().equalsIgnoreCase(size))) {
                    if (quantity <= 0) {
                        // Nếu số lượng chỉnh về <= 0 thì tự động xóa món này khỏi giỏ
                        cart.remove(item);
                    } else {
                        // Cập nhật số lượng mới
                        item.setQuantity(quantity);
                    }
                    break;
                }
            }

        // ========================================================
        // 3. TRƯỜNG HỢP: XÓA MỘT MÓN HÀNG KHỎI GIỎ (action == 'remove')
        // ========================================================
        } else if ("remove".equals(action)) {
            int productId = Integer.parseInt(request.getParameter("productId"));
            String size = request.getParameter("size");

            if (size != null && !size.isEmpty()) {
                cart.removeIf(item -> item.getProduct().getId() == productId && item.getSize().equalsIgnoreCase(size));
            } else {
                cart.removeIf(item -> item.getProduct().getId() == productId);
            }
        }

        // Tính lại tổng tiền sau khi cập nhật hoặc xóa
        double total = 0;
        int totalCount = 0;
        for (CartItem ci : cart) {
            total += ci.getTotalMoney();
            totalCount += ci.getQuantity();
        }

        // Đồng bộ dữ liệu giỏ hàng mới vào Session
        session.setAttribute("cart", cart);
        session.setAttribute("cartTotal", total);
        session.setAttribute("cartSize", totalCount);

        // If request is AJAX, respond with OK status
        String requestedWith = request.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // Chuyển hướng người dùng về trang xem giỏ hàng
        response.sendRedirect(request.getContextPath() + "/cart");
    }
}
