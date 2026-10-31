// Khai báo package chứa controller
package controller;

// Import CategoryDAO để lấy thông tin danh mục
import dao.CategoryDAO;
// Import ProductDAO để truy vấn chi tiết sản phẩm theo ID
import dao.ProductDAO;
// Import các mô hình dữ liệu Category và Product
import model.Category;
import model.Product;

// Import thư viện danh sách List và xử lý ngoại lệ I/O
import java.io.IOException;
import java.util.List;
// Import các thư viện Servlet
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * ProductDetailController: Điều khiển trang Chi tiết sản phẩm
 * Hiển thị thông tin chi tiết một sản phẩm, tùy chọn size, màu và các sản phẩm tương tự
 * Annotation @WebServlet ánh xạ URL "/product-detail" tới Servlet này
 */
@WebServlet(name = "ProductDetailController", urlPatterns = {"/product-detail", "/detail"})
public class ProductDetailController extends HttpServlet {

    /**
     * Phương thức doGet: Xử lý xem chi tiết sản phẩm khi người dùng nhấp vào một sản phẩm
     * @param request  Yêu cầu chứa tham số 'id' của sản phẩm
     * @param response Phản hồi chuyển tiếp sang trang detail.jsp
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Thiết lập kiểu dữ liệu phản hồi là HTML với bảng mã UTF-8
        response.setContentType("text/html;charset=UTF-8");
        // Thiết lập bộ mã ký tự UTF-8 cho request
        request.setCharacterEncoding("UTF-8");

        // Lấy mã sản phẩm (id) từ tham số URL (ví dụ: /product-detail?id=5)
        String idRaw = request.getParameter("id");
        int productId = 1;
        if (idRaw != null && !idRaw.isEmpty()) {
            try {
                productId = Integer.parseInt(idRaw);
            } catch (NumberFormatException e) {
                productId = 1;
            }
        }

        ProductDAO productDAO = new ProductDAO();
        CategoryDAO categoryDAO = new CategoryDAO();

        Product product = productDAO.getProductById(productId);
        if (product == null) {
            response.sendRedirect(request.getContextPath() + "/products");
            return;
        }

        List<Product> relatedProducts = productDAO.getProductsByCategoryId(product.getCategoryId());
        List<Category> categories = categoryDAO.getAllCategories();

        request.setAttribute("product", product);
        request.setAttribute("relatedProducts", relatedProducts);
        request.setAttribute("categories", categories);

        request.getRequestDispatcher("detail.jsp").forward(request, response);
    }
}
