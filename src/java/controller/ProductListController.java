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
        String catIdRaw = request.getParameter("categoryId");  // Mã danh mục (VD: 1, 2)
        String keyword = request.getParameter("keyword");       // Từ khóa tìm kiếm (VD: 'Áo sơ mi')
        String filter = request.getParameter("filter");         // Bộ lọc định sẵn ('bestseller', 'sale')
        String sort = request.getParameter("sort");             // Tiêu chí sắp xếp ('price_asc', 'price_desc', 'new')
        String size = request.getParameter("size");             // Bộ lọc kích cỡ ('S', 'M', 'L', 'XL')
        String color = request.getParameter("color");           // Bộ lọc màu sắc ('Trắng', 'Đen'...)
        String priceRange = request.getParameter("priceRange"); // Bộ lọc khoảng giá ('under_500k', '500k_1m'...)

        // Lấy danh sách toàn bộ sản phẩm mặc định ban đầu
        List<Product> products = productDAO.getAllProducts();
        Category currentCategory = null; // Danh mục hiện tại đang chọn

        // 2. Xử lý lọc theo Danh mục hoặc Tìm kiếm từ khóa
        if (catIdRaw != null && !catIdRaw.trim().isEmpty()) {
            try {
                // Chuyển mã danh mục từ chuỗi String sang số nguyên int
                int catId = Integer.parseInt(catIdRaw.trim());
                // Lấy các sản phẩm thuộc về danh mục này
                List<Product> catProducts = productDAO.getProductsByCategoryId(catId);
                if (catProducts != null && !catProducts.isEmpty()) {
                    products = catProducts;
                }
                // Lấy thông tin chi tiết của danh mục để hiển thị tiêu đề
                currentCategory = categoryDAO.getCategoryById(catId);
            } catch (Exception e) {}
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            // Tìm kiếm sản phẩm theo tên chứa từ khóa
            List<Product> searchProducts = productDAO.searchProductsByName(keyword.trim());
            if (searchProducts != null && !searchProducts.isEmpty()) {
                products = searchProducts;
            }
        }

        // 3. Xử lý các bộ lọc định sẵn (Preset Filters)
        if ("bestseller".equalsIgnoreCase(filter)) {
            // Lọc các sản phẩm bán chạy / nổi bật (isFeatured = true)
            List<Product> featured = productDAO.getFeaturedProducts();
            if (featured != null && !featured.isEmpty()) products = featured;
        } else if ("sale".equalsIgnoreCase(filter)) {
            // Lọc các sản phẩm đang có giá khuyến mãi (salePrice > 0)
            List<Product> sales = new ArrayList<>();
            for (Product p : products) {
                if (p.getSalePrice() > 0) sales.add(p);
            }
            if (!sales.isEmpty()) products = sales;
        }

        // 4. Xử lý lọc theo khoảng giá (Price Range Filter)
        if (priceRange != null && !priceRange.isEmpty()) {
            List<Product> filtered = new ArrayList<>();
            for (Product p : products) {
                double ep = p.getEffectivePrice(); // Lấy giá bán thực tế (ưu tiên giá sale)
                if ("under_500k".equals(priceRange) && ep < 500000) {
                    filtered.add(p); // Dưới 500.000 VNĐ
                } else if ("500k_1m".equals(priceRange) && ep >= 500000 && ep <= 1000000) {
                    filtered.add(p); // Từ 500.000 VNĐ đến 1.000.000 VNĐ
                } else if ("1m_2m".equals(priceRange) && ep >= 1000000 && ep <= 2000000) {
                    filtered.add(p); // Từ 1.000.000 VNĐ đến 2.000.000 VNĐ
                } else if ("2m_3m".equals(priceRange) && ep >= 2000000 && ep <= 3000000) {
                    filtered.add(p); // Từ 2.000.000 VNĐ đến 3.000.000 VNĐ
                } else if ("above_3m".equals(priceRange) && ep > 3000000) {
                    filtered.add(p); // Trên 3.000.000 VNĐ
                }
            }
            if (!filtered.isEmpty()) products = filtered;
        }

        // 5. Xử lý lọc theo Kích cỡ (Size Filter)
        if (size != null && !size.isEmpty()) {
            List<Product> filtered = new ArrayList<>();
            for (Product p : products) {
                // Kiểm tra chuỗi thuộc tính size của sản phẩm có chứa size người dùng chọn không
                if (p.getSize() != null && p.getSize().toUpperCase().contains(size.toUpperCase())) {
                    filtered.add(p);
                }
            }
            if (!filtered.isEmpty()) products = filtered;
        }

        // 6. Xử lý lọc theo Màu sắc (Color Filter)
        if (color != null && !color.isEmpty()) {
            List<Product> filtered = new ArrayList<>();
            for (Product p : products) {
                // Kiểm tra chuỗi màu của sản phẩm có chứa màu người dùng chọn không
                if (p.getColor() != null && p.getColor().toLowerCase().contains(color.toLowerCase())) {
                    filtered.add(p);
                }
            }
            if (!filtered.isEmpty()) products = filtered;
        }

        // 7. Xử lý sắp xếp sản phẩm (Sorting)
        if ("price_asc".equalsIgnoreCase(sort)) {
            // Sắp xếp giá tăng dần
            products.sort(Comparator.comparingDouble(Product::getEffectivePrice));
        } else if ("price_desc".equalsIgnoreCase(sort)) {
            // Sắp xếp giá giảm dần
            products.sort((p1, p2) -> Double.compare(p2.getEffectivePrice(), p1.getEffectivePrice()));
        } else if ("new".equalsIgnoreCase(sort)) {
            // Sắp xếp sản phẩm mới nhất (ID lớn hơn ở đầu)
            products.sort((p1, p2) -> Integer.compare(p2.getId(), p1.getId()));
        } else if ("bestseller".equalsIgnoreCase(sort) || "featured".equalsIgnoreCase(sort)) {
            // Đưa sản phẩm nổi bật lên đầu
            products.sort((p1, p2) -> Boolean.compare(p2.isIsFeatured(), p1.isIsFeatured()));
        }

        // 8. Cơ chế dự phòng đảm bảo danh sách không bao giờ bị rỗng
        if (products == null || products.isEmpty()) {
            products = productDAO.getMockProducts();
        }

        // Lấy tất cả danh mục để hiển thị thanh menu phân loại bên trái trang shop
        List<Category> categories = categoryDAO.getAllCategories();

        // 9. Đính kèm các thuộc tính vào request để hiển thị trên file JSP
        request.setAttribute("products", products);                    // Danh sách sản phẩm sau lọc
        request.setAttribute("categories", categories);                // Toàn bộ danh mục
        request.setAttribute("currentCategory", currentCategory);      // Danh mục đang chọn
        request.setAttribute("keyword", keyword);                      // Từ khóa tìm kiếm
        request.setAttribute("filter", filter);                        // Bộ lọc đặc biệt
        request.setAttribute("sort", sort);                            // Tiêu chí sắp xếp
        request.setAttribute("selectedSize", size);                    // Kích cỡ đang chọn
        request.setAttribute("selectedColor", color);                  // Màu sắc đang chọn
        request.setAttribute("selectedPriceRange", priceRange);        // Khoảng giá đang chọn

        // Chuyển tiếp tới trang giao diện shop.jsp
        request.getRequestDispatcher("shop.jsp").forward(request, response);
    }
}
