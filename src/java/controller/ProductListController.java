// Khai báo package chứa controller
package controller;

// Import CategoryDAO để truy vấn danh mục
import dao.CategoryDAO;
// Import ProductDAO để truy vấn và lọc danh sách sản phẩm
import dao.ProductDAO;
// Import các mô hình dữ liệu
import model.Category;
import model.Product;

// Import các thư viện tiện ích danh sách, so sánh và xử lý I/O
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
// Import các thư viện Servlet
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * ProductListController: Điều khiển trang Cửa hàng (Shop)
 * Hỗ trợ tìm kiếm, lọc đa tiêu chí (danh mục, giá, size, màu sắc) và sắp xếp
 * Annotation @WebServlet ánh xạ URL "/products" tới Servlet này
 */
@WebServlet(name = "ProductListController", urlPatterns = {"/products"})
public class ProductListController extends HttpServlet {

    /**
     * Phương thức doGet: Xử lý tìm kiếm, lọc và phân loại sản phẩm
     * @param request  Yêu cầu chứa các tham số lọc từ URL (Query Parameters)
     * @param response Phản hồi trả về trang shop.jsp
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Cấu hình kiểu dữ liệu trả về là HTML với bảng mã UTF-8
        response.setContentType("text/html;charset=UTF-8");
        // Thiết lập bộ mã UTF-8 cho request để đọc tiếng Việt có dấu chính xác
        request.setCharacterEncoding("UTF-8");

        // Khởi tạo các đối tượng DAO để truy vấn CSDL
        ProductDAO productDAO = new ProductDAO();
        CategoryDAO categoryDAO = new CategoryDAO();

        // 1. Nhận các tham số lọc từ URL query string
        String catIdRaw = request.getParameter("categoryId");
        String keyword = request.getParameter("keyword");
        String filter = request.getParameter("filter");
        String sort = request.getParameter("sort");
        String size = request.getParameter("size");
        String color = request.getParameter("color");
        String priceRange = request.getParameter("priceRange");

        List<Product> products = null;
        Category currentCategory = null;

        // 2. Xử lý lọc theo Danh mục hoặc Tìm kiếm từ khóa hoặc Bộ lọc đặc biệt
        if (catIdRaw != null && !catIdRaw.trim().isEmpty()) {
            try {
                int catId = Integer.parseInt(catIdRaw.trim());
                products = productDAO.getProductsByCategoryId(catId);
                currentCategory = categoryDAO.getCategoryById(catId);
            } catch (Exception ignored) {}
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            products = productDAO.searchProductsByName(keyword.trim());
        } else if ("sale".equalsIgnoreCase(filter)) {
            List<Product> all = productDAO.getAllProducts();
            products = new ArrayList<>();
            for (Product p : all) {
                if (p.getSalePrice() > 0 && p.getSalePrice() < p.getPrice()) {
                    products.add(p);
                }
            }
        } else if ("bestseller".equalsIgnoreCase(filter)) {
            products = productDAO.getFeaturedProducts();
        }

        if (products == null) {
            products = productDAO.getAllProducts();
        }

        // 3. Xử lý lọc theo Kích cỡ (Size Filter)
        if (size != null && !size.isEmpty()) {
            List<Product> filtered = new ArrayList<>();
            for (Product p : products) {
                if (p.getSize() != null && p.getSize().toUpperCase().contains(size.toUpperCase())) {
                    filtered.add(p);
                }
            }
            products = filtered;
        }

        // 4. Xử lý lọc theo Màu sắc (Color Filter)
        if (color != null && !color.isEmpty()) {
            List<Product> filtered = new ArrayList<>();
            for (Product p : products) {
                if (p.getColor() != null && p.getColor().toLowerCase().contains(color.toLowerCase())) {
                    filtered.add(p);
                }
            }
            products = filtered;
        }

        // 5. Xử lý lọc theo khoảng giá (Price Range Filter)
        if (priceRange != null && !priceRange.isEmpty()) {
            List<Product> filtered = new ArrayList<>();
            for (Product p : products) {
                double effPrice = p.getEffectivePrice();
                if ("under_500k".equals(priceRange) && effPrice < 500000) {
                    filtered.add(p);
                } else if ("500k_1m".equals(priceRange) && effPrice >= 500000 && effPrice <= 1000000) {
                    filtered.add(p);
                } else if ("1m_2m".equals(priceRange) && effPrice > 1000000 && effPrice <= 2000000) {
                    filtered.add(p);
                } else if ("2m_3m".equals(priceRange) && effPrice > 2000000 && effPrice <= 3000000) {
                    filtered.add(p);
                } else if ("above_3m".equals(priceRange) && effPrice > 3000000) {
                    filtered.add(p);
                }
            }
            products = filtered;
        }

        // 6. Xử lý sắp xếp sản phẩm (Sorting)
        if ("price_asc".equalsIgnoreCase(sort)) {
            products.sort(Comparator.comparingDouble(Product::getEffectivePrice));
        } else if ("price_desc".equalsIgnoreCase(sort)) {
            products.sort(Comparator.comparingDouble(Product::getEffectivePrice).reversed());
        } else if ("new".equalsIgnoreCase(sort)) {
            products.sort(Comparator.comparingInt(Product::getId).reversed());
        } else if ("bestseller".equalsIgnoreCase(sort) || "featured".equalsIgnoreCase(sort)) {
            List<Product> featured = new ArrayList<>();
            for (Product p : products) {
                if (p.isIsFeatured()) featured.add(p);
            }
            if (!featured.isEmpty()) products = featured;
        }

        // 7. Lấy danh mục để hiển thị thanh menu phân loại
        List<Category> categories = categoryDAO.getAllCategories();

        // 8. Đính kèm các thuộc tính vào request
        request.setAttribute("products", products);
        request.setAttribute("categories", categories);
        request.setAttribute("currentCategory", currentCategory);
        request.setAttribute("keyword", keyword);
        request.setAttribute("filter", filter);
        request.setAttribute("sort", sort);
        request.setAttribute("selectedSize", size);
        request.setAttribute("selectedColor", color);
        request.setAttribute("selectedPriceRange", priceRange);

        // Chuyển tiếp tới trang giao diện shop.jsp
        request.getRequestDispatcher("shop.jsp").forward(request, response);
    }
}
