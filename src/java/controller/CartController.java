// Khai báo package chứa controller
package controller;

// Import ProductDAO để lấy thông tin sản phẩm khi thêm vào giỏ
import dao.ProductDAO;
// Import mô hình món hàng trong giỏ (CartItem) và sản phẩm (Product)
import model.CartItem;
import model.Product;

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
            // Gán giá trị mặc định nếu người dùng chưa chọn size hoặc màu
            if (size == null || size.trim().isEmpty()) size = "M";
            if (color == null || color.trim().isEmpty()) color = "Đen";

            // Tìm sản phẩm trong CSDL theo ID
            Product p = productDAO.getProductById(productId);
            if (p != null) {
                boolean exists = false;
                // Kiểm tra xem sản phẩm cùng ID, cùng Size và cùng Màu đã có sẵn trong giỏ chưa
                for (CartItem item : cart) {
                    if (item.getProduct().getId() == productId 
                        && (size.equals(item.getSize())) 
                        && (color.equals(item.getColor()))) {
                        // Nếu đã có: Chỉ việc cộng dồn thêm số lượng mua mới
                        item.setQuantity(item.getQuantity() + quantity);
                        addedItem = item;
                        exists = true;
                        break;
                    }
                }
                // Nếu chưa có món này với size/màu tương ứng: Tạo mới một CartItem và thêm vào giỏ
                if (!exists) {
                    addedItem = new CartItem(p, quantity, size, color);
                    cart.add(addedItem);
                }
            }

            // Tính toán lại tổng tiền của toàn bộ giỏ hàng
            double totalMoney = 0;
            for (CartItem item : cart) {
                totalMoney += item.getTotalPrice();
            }

            // Lưu các thông tin cập nhật vào Session để sử dụng xuyên suốt các trang
            session.setAttribute("cart", cart);                     // Danh sách món hàng
            session.setAttribute("cartTotal", totalMoney);          // Tổng số tiền
            session.setAttribute("cartSize", cart.size());          // Số loại món hàng
            session.setAttribute("addedToCart", true);              // Cờ báo hiệu vừa thêm thành công
            session.setAttribute("lastAddedItem", addedItem);       // Món hàng vừa thêm

            // Quay trở lại trang chi tiết của sản phẩm vừa xem
            response.sendRedirect("product-detail?id=" + productId);
            return;

        // ========================================================
        // 2. TRƯỜNG HỢP: CẬP NHẬT SỐ LƯỢNG MÓN HÀNG (action == 'update')
        // ========================================================
        } else if ("update".equals(action)) {
            int productId = Integer.parseInt(request.getParameter("productId"));
            int quantity = Integer.parseInt(request.getParameter("quantity"));
            for (CartItem item : cart) {
                if (item.getProduct().getId() == productId) {
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
            // Xóa tất cả các CartItem có ID trùng khớp khỏi danh sách giỏ hàng
            cart.removeIf(item -> item.getProduct().getId() == productId);
        }

        // Tính lại tổng tiền sau khi cập nhật hoặc xóa
        double totalMoney = 0;
        for (CartItem item : cart) {
            totalMoney += item.getTotalPrice();
        }

        // Đồng bộ dữ liệu giỏ hàng mới vào Session
        session.setAttribute("cart", cart);
        session.setAttribute("cartTotal", totalMoney);
        session.setAttribute("cartSize", cart.size());

        // Chuyển hướng người dùng về trang xem giỏ hàng
        response.sendRedirect("cart");
    }
}
