// Khai báo package chứa controller
package controller;

// Import CategoryDAO để lấy danh sách danh mục cho ô chọn dropdown khi thêm/sửa sản phẩm
import dao.CategoryDAO;
// Import ProductDAO để thực hiện các thao tác CRUD (Xem, Thêm, Sửa, Xóa) sản phẩm
import dao.ProductDAO;
// Import các mô hình liên quan
import model.Category;
import model.Product;
import model.User;

// Import thư viện danh sách và xử lý I/O
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
 * AdminProductController: Quản lý sản phẩm dành cho Quản trị viên (Admin)
 * Hỗ trợ chức năng CRUD: Xem danh sách, Thêm sản phẩm, Sửa sản phẩm, Xóa sản phẩm
 * Annotation @WebServlet ánh xạ URL "/admin/products" tới Servlet này
 */
@WebServlet(name = "AdminProductController", urlPatterns = {"/admin/products"})
public class AdminProductController extends HttpServlet {

    /**
     * Phương thức doGet: Xử lý hiển thị danh sách sản phẩm và thực hiện xóa sản phẩm
     * @param request  Yêu cầu từ trình duyệt
     * @param response Phản hồi chuyển tiếp sang admin-products.jsp
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 1. Kiểm tra quyền Admin
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        // Nếu chưa đăng nhập hoặc không phải Admin (role != 1), chuyển về trang login
        if (user == null || !user.isAdmin()) {
            response.sendRedirect("../login");
            return;
        }

        ProductDAO productDAO = new ProductDAO();
        CategoryDAO categoryDAO = new CategoryDAO();

        // 2. Kiểm tra thao tác Xóa sản phẩm (action == 'delete')
        String action = request.getParameter("action");
        if ("delete".equals(action)) {
            // Lấy ID sản phẩm cần xóa từ URL
            int id = Integer.parseInt(request.getParameter("id"));
            // Gọi hàm xóa sản phẩm trong DAO
            productDAO.deleteProduct(id);
            // Chuyển hướng tải lại trang danh sách sản phẩm quản trị
            response.sendRedirect("products");
            return;
        }

        // 3. Lấy toàn bộ sản phẩm và danh mục để hiển thị lên bảng quản trị
        List<Product> products = productDAO.getAllProducts();
        List<Category> categories = categoryDAO.getAllCategories();

        request.setAttribute("products", products);
        request.setAttribute("categories", categories);

        // Chuyển tiếp tới trang giao diện admin-products.jsp
        request.getRequestDispatcher("/admin-products.jsp").forward(request, response);
    }

    /**
     * Phương thức doPost: Xử lý gửi form Thêm mới hoặc Cập nhật chỉnh sửa sản phẩm
     * @param request  Yêu cầu chứa dữ liệu sản phẩm từ form
     * @param response Phản hồi chuyển hướng sau khi lưu thành công
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Cấu hình bảng mã UTF-8 cho phản hồi và request để đọc tiếng Việt có dấu
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

        // 1. Kiểm tra quyền Admin
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null || !user.isAdmin()) {
            response.sendRedirect("../login");
            return;
        }

        ProductDAO productDAO = new ProductDAO();
        // Lấy tên hành động từ form ('add' - thêm mới, hoặc 'edit' - chỉnh sửa)
        String action = request.getParameter("action");

        // 2. Đọc các trường thông tin sản phẩm nhập từ form
        String idRaw = request.getParameter("id");                                   // Mã ID sản phẩm (dùng khi sửa)
        String name = request.getParameter("name");                                 // Tên sản phẩm
        String description = request.getParameter("description");                   // Mô tả chi tiết
        double price = Double.parseDouble(request.getParameter("price"));           // Giá niêm yết gốc
        double salePrice = Double.parseDouble(request.getParameter("salePrice"));   // Giá khuyến mãi (sale)
        int quantity = Integer.parseInt(request.getParameter("quantity"));          // Số lượng trong kho
        String image = request.getParameter("image");                               // Link ảnh sản phẩm
        String size = request.getParameter("size");                                 // Danh sách size (S, M, L...)
        String color = request.getParameter("color");                               // Danh sách màu sắc
        int categoryId = Integer.parseInt(request.getParameter("categoryId"));      // Mã danh mục
        // Kiểm tra checkbox nổi bật ('on' hoặc '1' là true)
        boolean isFeatured = "on".equals(request.getParameter("isFeatured")) || "1".equals(request.getParameter("isFeatured"));

        // 3. Khởi tạo đối tượng Product và thiết lập các thuộc tính
        Product p = new Product();
        p.setName(name);
        p.setDescription(description);
        p.setPrice(price);
        p.setSalePrice(salePrice);
        p.setQuantity(quantity);
        p.setImage(image);
        p.setSize(size);
        p.setColor(color);
        p.setCategoryId(categoryId);
        p.setIsFeatured(isFeatured);

        // 4. Phân loại thực thi thêm mới hoặc chỉnh sửa
        if ("add".equals(action)) {
            // Thêm sản phẩm mới vào CSDL
            productDAO.insertProduct(p);
        } else if ("edit".equals(action) && idRaw != null && !idRaw.isEmpty()) {
            // Gán lại ID sản phẩm và gọi hàm cập nhật
            p.setId(Integer.parseInt(idRaw));
            productDAO.updateProduct(p);
        }

        // Chuyển hướng quay về trang danh sách sản phẩm quản trị
        response.sendRedirect("products");
    }
}
