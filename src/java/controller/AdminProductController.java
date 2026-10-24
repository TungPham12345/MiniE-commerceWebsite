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

    private boolean checkAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        return true;
    }

    /**
     * Phương thức doGet: Xử lý hiển thị danh sách sản phẩm và thực hiện xóa sản phẩm
     * @param request  Yêu cầu từ trình duyệt
     * @param response Phản hồi chuyển tiếp sang admin-products.jsp
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!checkAdmin(request, response)) return;

        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

        ProductDAO productDAO = new ProductDAO();
        CategoryDAO categoryDAO = new CategoryDAO();

        // 2. Kiểm tra thao tác Xóa sản phẩm qua query param (action == 'delete')
        String action = request.getParameter("action");
        if ("delete".equals(action)) {
            String idRaw = request.getParameter("id");
            if (idRaw != null) {
                try {
                    int id = Integer.parseInt(idRaw);
                    productDAO.deleteProduct(id);
                } catch (Exception ignored) {}
            }
            response.sendRedirect(request.getContextPath() + "/admin/products");
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
     * Phương thức doPost: Xử lý gửi form Thêm mới, Cập nhật hoặc Xóa sản phẩm
     * @param request  Yêu cầu chứa dữ liệu sản phẩm từ form
     * @param response Phản hồi chuyển hướng sau khi lưu thành công
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!checkAdmin(request, response)) return;

        // Cấu hình bảng mã UTF-8 cho phản hồi và request để đọc tiếng Việt có dấu
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

        ProductDAO productDAO = new ProductDAO();
        String action = request.getParameter("action");

        if ("delete".equals(action)) {
            String idRaw = request.getParameter("id");
            if (idRaw != null) {
                try {
                    int id = Integer.parseInt(idRaw);
                    productDAO.deleteProduct(id);
                } catch (Exception ignored) {}
            }
            response.sendRedirect(request.getContextPath() + "/admin/products");
            return;
        }

        // 2. Đọc các trường thông tin sản phẩm nhập từ form
        String idRaw = request.getParameter("id");
        String name = request.getParameter("name");
        String description = request.getParameter("description");
        double price = 0;
        double salePrice = 0;
        int quantity = 0;
        try {
            if (request.getParameter("price") != null) price = Double.parseDouble(request.getParameter("price"));
            if (request.getParameter("salePrice") != null) salePrice = Double.parseDouble(request.getParameter("salePrice"));
            if (request.getParameter("quantity") != null) quantity = Integer.parseInt(request.getParameter("quantity"));
        } catch (Exception ignored) {}

        String image = request.getParameter("image");
        String size = request.getParameter("size");
        String color = request.getParameter("color");
        int categoryId = 1;
        try {
            if (request.getParameter("categoryId") != null) {
                categoryId = Integer.parseInt(request.getParameter("categoryId"));
            }
        } catch (Exception ignored) {}

        boolean isFeatured = "on".equals(request.getParameter("isFeatured")) || "1".equals(request.getParameter("isFeatured"));

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

        if ("add".equals(action)) {
            productDAO.insertProduct(p);
        } else if ("edit".equals(action) && idRaw != null && !idRaw.isEmpty()) {
            p.setId(Integer.parseInt(idRaw));
            productDAO.updateProduct(p);
        }

        response.sendRedirect(request.getContextPath() + "/admin/products");
    }
}
