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
@WebServlet(name = "ProductDetailController", urlPatterns = {"/product-detail"})
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
        // Kiểm tra nếu không có tham số ID hoặc ID bị để trống
        if (idRaw == null || idRaw.isEmpty()) {
            // Chuyển hướng người dùng về trang danh sách sản phẩm
            response.sendRedirect("products");
            return;
        }

        try {
            // Chuyển đổi mã sản phẩm từ chuỗi String sang số nguyên int
            int id = Integer.parseInt(idRaw);
            // Khởi tạo đối tượng DAO
            ProductDAO productDAO = new ProductDAO();
            CategoryDAO categoryDAO = new CategoryDAO();

            // Truy vấn lấy thông tin chi tiết của sản phẩm theo ID
            Product product = productDAO.getProductById(id);
            // Nếu không tìm thấy sản phẩm này trong hệ thống
            if (product == null) {
                // Chuyển hướng quay lại trang danh sách sản phẩm
                response.sendRedirect("products");
                return;
            }

            // Lấy danh sách các sản phẩm liên quan (cùng danh mục với sản phẩm hiện tại)
            List<Product> relatedProducts = productDAO.getProductsByCategoryId(product.getCategoryId());
            // Lấy danh sách toàn bộ danh mục để hiển thị thanh điều hướng
            List<Category> categories = categoryDAO.getAllCategories();

            // Đính kèm thông tin sản phẩm chi tiết vào request
            request.setAttribute("product", product);
            // Đính kèm danh sách sản phẩm tương tự để gợi ý cho khách hàng
            request.setAttribute("relatedProducts", relatedProducts);
            // Đính kèm danh mục sản phẩm vào request
            request.setAttribute("categories", categories);

            // Chuyển tiếp (forward) dữ liệu sang trang giao diện detail.jsp
            request.getRequestDispatcher("detail.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            // Bắt lỗi nếu người dùng cố ý nhập ID không phải là số (ví dụ: id=abc)
            response.sendRedirect("products");
        }
    }
}
