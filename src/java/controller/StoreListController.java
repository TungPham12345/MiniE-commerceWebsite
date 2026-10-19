// Khai báo package chứa controller
package controller;

// Import StoreDAO để truy vấn danh sách cửa hàng và tọa độ bản đồ
import dao.StoreDAO;
// Import mô hình Store đại diện cho chi nhánh cửa hàng
import model.Store;

// Import các thư viện danh sách và xử lý I/O
import java.io.IOException;
import java.util.List;
// Import các thư viện Servlet
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * StoreListController: Điều khiển trang Hệ thống Cửa hàng & Bản đồ tương tác
 * Lấy danh sách showroom kèm tọa độ GPS (Latitude, Longitude) để hiển thị trên Leaflet Map
 * Annotation @WebServlet ánh xạ URL "/stores" tới Servlet này
 */
@WebServlet(name = "StoreListController", urlPatterns = {"/stores"})
public class StoreListController extends HttpServlet {

    /**
     * Phương thức doGet: Xử lý khi người dùng nhấn vào menu "Cửa Hàng & Bản Đồ"
     * @param request  Yêu cầu từ trình duyệt
     * @param response Phản hồi chuyển tiếp sang trang stores.jsp
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Cấu hình bảng mã UTF-8 cho phản hồi và yêu cầu
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

        // Khởi tạo đối tượng StoreDAO để truy vấn danh sách showroom
        StoreDAO storeDAO = new StoreDAO();
        // Lấy tất cả các chi nhánh cửa hàng từ CSDL
        List<Store> stores = storeDAO.getAllStores();

        // Đính kèm danh sách chi nhánh vào request để truyền sang file JSP
        request.setAttribute("stores", stores);
        // Chuyển tiếp (forward) sang trang giao diện stores.jsp có nhúng bản đồ Leaflet Map
        request.getRequestDispatcher("stores.jsp").forward(request, response);
    }
}
