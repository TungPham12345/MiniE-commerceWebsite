// Khai báo package chứa controller
package controller;

// Import CategoryDAO để lấy danh sách các danh mục sản phẩm thời trang
import dao.CategoryDAO;
// Import ProductDAO để lấy danh sách sản phẩm nổi bật và sản phẩm mới
import dao.ProductDAO;
// Import mô hình Category và Product
import model.Category;
import model.Product;

// Import các lớp hỗ trợ danh sách List và xử lý ngoại lệ I/O
import java.io.IOException;
import java.util.List;
// Import các thư viện Servlet
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * HomeController: Điều khiển hiển thị Trang chủ của hệ thống bán hàng
 * Annotation @WebServlet ánh xạ URL "/home" và URL gốc rỗng "" tới Servlet này
 */
@WebServlet(name = "HomeController", urlPatterns = {"/home", ""})
public class HomeController extends HttpServlet {

    /**
     * Phương thức doGet: Xử lý khi người dùng truy cập trang chủ website
     * @param request  Yêu cầu từ trình duyệt
     * @param response Phản hồi trả về trang giao diện
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Thiết lập kiểu nội dung trả về là HTML có hỗ trợ UTF-8
        response.setContentType("text/html;charset=UTF-8");
        // Thiết lập bộ mã ký tự UTF-8 cho request
        request.setCharacterEncoding("UTF-8");

        // Khởi tạo đối tượng ProductDAO để truy vấn dữ liệu sản phẩm
        ProductDAO productDAO = new ProductDAO();
        // Khởi tạo đối tượng CategoryDAO để truy vấn dữ liệu danh mục
        CategoryDAO categoryDAO = new CategoryDAO();

        // 1. Lấy danh sách các sản phẩm nổi bật (Featured / Bán chạy)
        List<Product> featuredProducts = productDAO.getFeaturedProducts();

        // 2. Lấy toàn bộ danh sách sản phẩm để chọn ra các sản phẩm mới nhất
        List<Product> latestProducts = productDAO.getAllProducts();
        // Nếu có nhiều hơn 8 sản phẩm, chỉ lấy 8 sản phẩm đầu tiên để hiển thị trên trang chủ cho gọn đẹp
        if (latestProducts.size() > 8) {
            latestProducts = latestProducts.subList(0, 8);
        }

        // 3. Lấy tất cả danh mục sản phẩm (Áo Nam, Quần Nam, Nữ, Phụ kiện)
        List<Category> categories = categoryDAO.getAllCategories();

        // Đính kèm danh sách sản phẩm nổi bật vào request để gửi sang file JSP
        request.setAttribute("featuredProducts", featuredProducts);
        // Đính kèm danh sách sản phẩm mới nhất vào request
        request.setAttribute("latestProducts", latestProducts);
        // Đính kèm danh sách danh mục vào request
        request.setAttribute("categories", categories);

        // Chuyển tiếp (forward) toàn bộ dữ liệu sang trang giao diện index.jsp để render HTML
        request.getRequestDispatcher("index.jsp").forward(request, response);
    }
}
