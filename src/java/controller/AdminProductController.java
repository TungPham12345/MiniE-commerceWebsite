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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
// Import các thư viện Servlet và Session
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

/**
 * AdminProductController: Quản lý sản phẩm dành cho Quản trị viên (Admin)
 * Hỗ trợ chức năng CRUD: Xem danh sách, Thêm sản phẩm, Sửa sản phẩm, Xóa sản phẩm
 * Annotation @WebServlet ánh xạ URL "/admin/products" tới Servlet này
 */
@WebServlet(name = "AdminProductController", urlPatterns = {"/admin/products"})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 5 * 1024 * 1024,
        maxRequestSize = 12 * 1024 * 1024
)
public class AdminProductController extends HttpServlet {

    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;

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

        HttpSession session = request.getSession();
        ProductDAO productDAO = new ProductDAO();
        String action;
        try {
            action = request.getParameter("action");
        } catch (IllegalStateException e) {
            setProductMessage(session, "Ảnh tải lên không được vượt quá 5 MB.");
            response.sendRedirect(request.getContextPath() + "/admin/products");
            return;
        }

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
                if (!"add".equals(action) && !"edit".equals(action)) {
                    throw new IllegalArgumentException("Thao tác không hợp lệ.");
                }
                Product p = new Product();
                p.setName(requiredParameter(request, "name"));
                p.setDescription(request.getParameter("description"));
                p.setPrice(Double.parseDouble(requiredParameter(request, "price")));
                String salePrice = request.getParameter("salePrice");
                p.setSalePrice(salePrice == null || salePrice.trim().isEmpty() ? 0 : Double.parseDouble(salePrice));
                p.setSize(requiredParameter(request, "size"));
                p.setColor(request.getParameter("color"));
                p.setCategoryId(Integer.parseInt(requiredParameter(request, "categoryId")));
                p.setIsFeatured("on".equals(request.getParameter("isFeatured"))
                        || "1".equals(request.getParameter("isFeatured")));
                if ("edit".equals(action)) {
                    p.setId(Integer.parseInt(requiredParameter(request, "id")));
                }

                Map<String, Integer> stockBySize = parseStockBySize(request);
                int totalQuantity = 0;
                for (Integer quantity : stockBySize.values()) {
                    totalQuantity = Math.addExact(totalQuantity, quantity);
                }
                p.setQuantity(totalQuantity);
                p.setImage(resolveProductImage(request));

                boolean saved;
                if ("add".equals(action)) {
                    saved = productDAO.insertProduct(p, stockBySize);
                } else {
                    saved = productDAO.updateProduct(p, stockBySize);
                }
                if (!saved) deleteUploadedProductImage(request);
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

    private String resolveProductImage(HttpServletRequest request) throws IOException, ServletException {
        Part imagePart;
        try {
            imagePart = request.getPart("imageFile");
        } catch (IllegalStateException e) {
            throw new IllegalArgumentException("Ảnh tải lên không được vượt quá 5 MB.", e);
        }

        if (imagePart == null || imagePart.getSize() == 0) {
            return requiredParameter(request, "image");
        }
        if (imagePart.getSize() > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException("Ảnh tải lên không được vượt quá 5 MB.");
        }

        String extension;
        try {
            extension = getImageExtension(imagePart);
        } catch (IOException e) {
            throw new IllegalArgumentException("Không thể đọc ảnh tải lên. Vui lòng thử lại.", e);
        }
        String realPath = getServletContext().getRealPath("/assets/images/products/uploads");
        if (realPath == null) {
            throw new IllegalArgumentException("Máy chủ hiện không hỗ trợ lưu ảnh tải lên.");
        }

        Path uploadDirectory = Paths.get(realPath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadDirectory);
        } catch (IOException e) {
            throw new IllegalArgumentException("Không thể tạo thư mục lưu ảnh trên máy chủ.", e);
        }

        String fileName = UUID.randomUUID().toString() + extension;
        Path destination = uploadDirectory.resolve(fileName);
        try (InputStream input = imagePart.getInputStream()) {
            Files.copy(input, destination);
            request.setAttribute("uploadedProductImageFile", destination);
        } catch (IOException e) {
            try {
                Files.deleteIfExists(destination);
            } catch (IOException cleanupError) {
                e.addSuppressed(cleanupError);
            }
            throw new IllegalArgumentException("Không thể lưu ảnh tải lên. Vui lòng thử lại.", e);
        }
        return "assets/images/products/uploads/" + fileName;
    }

    private String getImageExtension(Part imagePart) throws IOException {
        byte[] signature = new byte[12];
        int bytesRead = 0;
        try (InputStream input = imagePart.getInputStream()) {
            while (bytesRead < signature.length) {
                int count = input.read(signature, bytesRead, signature.length - bytesRead);
                if (count <= 0) break;
                bytesRead += count;
            }
        }

        String contentType = imagePart.getContentType();
        if ("image/jpeg".equalsIgnoreCase(contentType)
                && bytesRead >= 3 && signature[0] == (byte) 0xFF
                && signature[1] == (byte) 0xD8 && signature[2] == (byte) 0xFF) {
            return ".jpg";
        }
        if ("image/png".equalsIgnoreCase(contentType)
                && bytesRead >= 8 && signature[0] == (byte) 0x89
                && signature[1] == 0x50 && signature[2] == 0x4E && signature[3] == 0x47
                && signature[4] == 0x0D && signature[5] == 0x0A
                && signature[6] == 0x1A && signature[7] == 0x0A) {
            return ".png";
        }
        if ("image/gif".equalsIgnoreCase(contentType)
                && bytesRead >= 6 && signature[0] == 'G' && signature[1] == 'I'
                && signature[2] == 'F' && signature[3] == '8'
                && (signature[4] == '7' || signature[4] == '9') && signature[5] == 'a') {
            return ".gif";
        }
        if ("image/webp".equalsIgnoreCase(contentType)
                && bytesRead >= 12 && signature[0] == 'R' && signature[1] == 'I'
                && signature[2] == 'F' && signature[3] == 'F'
                && signature[8] == 'W' && signature[9] == 'E'
                && signature[10] == 'B' && signature[11] == 'P') {
            return ".webp";
        }
        throw new IllegalArgumentException("Chỉ chấp nhận ảnh JPEG, PNG, GIF hoặc WebP hợp lệ.");
    }

    private void deleteUploadedProductImage(HttpServletRequest request) {
        Object uploadedImage = request.getAttribute("uploadedProductImageFile");
        if (!(uploadedImage instanceof Path)) return;
        try {
            Files.deleteIfExists((Path) uploadedImage);
        } catch (IOException e) {
            System.err.println("⚠️ Product image cleanup failed: " + e.getMessage());
        }
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
