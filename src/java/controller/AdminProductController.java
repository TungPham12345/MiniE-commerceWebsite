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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
            try {
                int id = Integer.parseInt(idRaw);
                setProductMessage(request.getSession(), productDAO.deleteProduct(id)
                        ? "Đã xóa sản phẩm."
                        : "Không thể xóa sản phẩm.");
            } catch (NumberFormatException e) {
                setProductMessage(request.getSession(), "Mã sản phẩm không hợp lệ.");
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
        HttpSession session = request.getSession();

        if ("delete".equals(action)) {
            doGet(request, response);
            return;
        }

        try {
            if ("updateStock".equals(action)) {
                int productId = Integer.parseInt(request.getParameter("id"));
                Map<String, Integer> stockBySize = parseStockBySize(request);
                boolean updated = productDAO.updateSizeStock(productId, stockBySize);
                setProductMessage(session, updated
                        ? "Đã cập nhật tồn kho theo size."
                        : "Không thể cập nhật tồn kho. Hãy kiểm tra lại các size và số lượng.");
            } else {
                Product p = new Product();
                p.setName(requiredParameter(request, "name"));
                p.setDescription(request.getParameter("description"));
                p.setPrice(Double.parseDouble(requiredParameter(request, "price")));
                String salePrice = request.getParameter("salePrice");
                p.setSalePrice(salePrice == null || salePrice.trim().isEmpty() ? 0 : Double.parseDouble(salePrice));
                p.setImage(requiredParameter(request, "image"));
                p.setSize(requiredParameter(request, "size"));
                p.setColor(request.getParameter("color"));
                p.setCategoryId(Integer.parseInt(requiredParameter(request, "categoryId")));
                p.setIsFeatured("on".equals(request.getParameter("isFeatured"))
                        || "1".equals(request.getParameter("isFeatured")));

                Map<String, Integer> stockBySize = parseStockBySize(request);
                int totalQuantity = 0;
                for (Integer quantity : stockBySize.values()) {
                    totalQuantity = Math.addExact(totalQuantity, quantity);
                }
                p.setQuantity(totalQuantity);

                boolean saved;
                if ("add".equals(action)) {
                    saved = productDAO.insertProduct(p, stockBySize);
                } else if ("edit".equals(action)) {
                    p.setId(Integer.parseInt(requiredParameter(request, "id")));
                    saved = productDAO.updateProduct(p, stockBySize);
                } else {
                    throw new IllegalArgumentException("Thao tác không hợp lệ.");
                }
                setProductMessage(session, saved
                        ? "Đã lưu sản phẩm và tồn kho theo size."
                        : "Không thể lưu sản phẩm. Hãy kiểm tra lại thông tin và số lượng từng size.");
            }
        } catch (IllegalArgumentException e) {
            setProductMessage(session, e.getMessage());
        } catch (ArithmeticException e) {
            setProductMessage(session, "Tổng số lượng tồn kho vượt giới hạn cho phép.");
        }

        response.sendRedirect(request.getContextPath() + "/admin/products");
    }

    private Map<String, Integer> parseStockBySize(HttpServletRequest request) {
        String[] sizes = request.getParameterValues("stockSize");
        String[] quantities = request.getParameterValues("stockQuantity");
        if (sizes == null || quantities == null || sizes.length == 0 || sizes.length != quantities.length) {
            throw new IllegalArgumentException("Vui lòng nhập tồn kho cho từng size.");
        }
        Map<String, Integer> stocks = new LinkedHashMap<>();
        for (int i = 0; i < sizes.length; i++) {
            String size = sizes[i] != null ? sizes[i].trim() : "";
            if (size.isEmpty()) {
                throw new IllegalArgumentException("Size không được để trống.");
            }
            int quantity;
            try {
                quantity = Integer.parseInt(quantities[i]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Số lượng tồn kho phải là số nguyên không âm.");
            }
            if (quantity < 0) {
                throw new IllegalArgumentException("Số lượng tồn kho không được âm.");
            }
            for (String existingSize : stocks.keySet()) {
                if (existingSize.equalsIgnoreCase(size)) {
                    throw new IllegalArgumentException("Không được nhập trùng size " + size + ".");
                }
            }
            stocks.put(size, quantity);
        }
        return stocks;
    }

    private String requiredParameter(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập đầy đủ thông tin bắt buộc.");
        }
        return value.trim();
    }

    private void setProductMessage(HttpSession session, String message) {
        session.setAttribute("adminProductMessage", message);
    }
}
