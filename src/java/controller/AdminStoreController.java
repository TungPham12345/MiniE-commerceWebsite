// Khai báo package chứa controller
package controller;

// Import StoreDAO để thao tác CRUD chi nhánh showroom và tọa độ GPS
import dao.StoreDAO;
// Import mô hình Store và User
import model.Store;
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
 * AdminStoreController: Quản lý hệ thống chuỗi cửa hàng và bản đồ số cho Admin
 * Hỗ trợ chức năng: Xem danh sách chi nhánh, Thêm chi nhánh (kèm kinh độ, vĩ độ GPS), Xóa chi nhánh
 * Annotation @WebServlet ánh xạ URL "/admin/stores" tới Servlet này
 */
@WebServlet(name = "AdminStoreController", urlPatterns = {"/admin/stores"})
public class AdminStoreController extends HttpServlet {

    /**
     * Hàm phụ trợ checkAdmin: Kiểm tra phiên đăng nhập và quyền Quản trị viên
     * @param request  Yêu cầu từ client
     * @param response Phản hồi chuyển hướng nếu không có quyền
     * @return true nếu là Admin (role = 1), false nếu không hợp lệ
     */
    private boolean checkAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        // Nếu chưa đăng nhập hoặc role != 1 thì chặn truy cập và chuyển hướng về trang login
        if (user == null || user.getRole() != 1) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        return true;
    }

    /**
     * Phương thức doGet: Xử lý hiển thị trang quản lý chi nhánh và bản đồ
     * @param request  Yêu cầu từ trình duyệt
     * @param response Phản hồi chuyển tiếp sang admin-stores.jsp
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Kiểm tra quyền Admin trước khi tiếp tục
        if (!checkAdmin(request, response)) return;

        // Cấu hình bảng mã UTF-8
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

        StoreDAO storeDAO = new StoreDAO();
        // Lấy danh sách toàn bộ các chi nhánh cửa hàng
        List<Store> stores = storeDAO.getAllStores();

        // Đính kèm danh sách cửa hàng vào request
        request.setAttribute("stores", stores);
        // Chuyển tiếp tới trang giao diện admin-stores.jsp
        request.getRequestDispatcher("/admin-stores.jsp").forward(request, response);
    }

    /**
     * Phương thức doPost: Xử lý form Thêm mới hoặc Xóa chi nhánh cửa hàng
     * @param request  Yêu cầu chứa dữ liệu chi nhánh và tọa độ
     * @param response Phản hồi chuyển hướng tải lại trang quản lý chi nhánh
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Kiểm tra quyền Admin
        if (!checkAdmin(request, response)) return;

        // Cấu hình bảng mã UTF-8
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        StoreDAO storeDAO = new StoreDAO();

        // 1. Trường hợp Thêm chi nhánh mới (action == 'add')
        if ("add".equals(action)) {
            String name = request.getParameter("name");                              // Tên chi nhánh
            String address = request.getParameter("address");                        // Địa chỉ đường, phường, quận
            String city = request.getParameter("city");                              // Thành phố (Hà Nội, TP.HCM, Đà Nẵng)
            String phone = request.getParameter("phone");                            // Số hotline chi nhánh
            String hours = request.getParameter("hours");                            // Khung giờ mở cửa
            double lat = Double.parseDouble(request.getParameter("latitude"));       // Vĩ độ địa lý GPS (Latitude)
            double lng = Double.parseDouble(request.getParameter("longitude"));      // Kinh độ địa lý GPS (Longitude)
            boolean isMain = "1".equals(request.getParameter("isMain"));             // Cờ chi nhánh chính Flagship

            // Khởi tạo đối tượng Store với các thông tin đã nhập
            Store s = new Store(0, name, address, city, phone, hours, lat, lng, isMain);
            // Gọi hàm thêm chi nhánh vào CSDL
            storeDAO.insertStore(s);

        // 2. Trường hợp Xóa chi nhánh (action == 'delete')
        } else if ("delete".equals(action)) {
            int id = Integer.parseInt(request.getParameter("id"));
            // Gọi hàm xóa chi nhánh theo ID
            storeDAO.deleteStore(id);
        }

        // Chuyển hướng quay trở lại trang quản lý chi nhánh
        response.sendRedirect(request.getContextPath() + "/admin/stores");
    }
}
