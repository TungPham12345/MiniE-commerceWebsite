package dao;

import context.DBContext;
import model.CartItem;
import model.Order;
import model.OrderDetail;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp OrderDAO: Quản lý truy xuất và lưu trữ dữ liệu Đơn hàng (orders) và Chi tiết đơn hàng (order_details)
 * Sử dụng Transaction (Giao dịch) để đảm bảo tính toàn vẹn dữ liệu khi đặt hàng:
 * nếu lưu đơn hàng hoặc chi tiết đơn hàng thất bại, toàn bộ quá trình sẽ được Rollback (hoàn tác).
 */
public class OrderDAO {

    /**
     * Tạo mới một đơn hàng cùng với danh sách chi tiết các mặt hàng trong giỏ
     * @param order Đối tượng đơn hàng chứa thông tin người nhận, địa chỉ, tổng tiền
     * @param cartItems Danh sách các sản phẩm có trong giỏ hàng
     * @return Mã ID của đơn hàng vừa tạo nếu thành công, hoặc 0 nếu thất bại
     */
    public int createOrder(Order order, List<CartItem> cartItems) {
        // Câu lệnh SQL thêm đơn hàng chính vào bảng orders
        String sqlOrder = "INSERT INTO orders (user_id, customer_name, email, phone, address, note, total_money, payment_method, status) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        // Câu lệnh SQL thêm từng sản phẩm chi tiết vào bảng order_details
        String sqlDetail = "INSERT INTO order_details (order_id, product_id, price, quantity, size, color, total_money) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        try {
            // Mở kết nối tới cơ sở dữ liệu
            conn = DBContext.getConnection();
            // Tắt chế độ Auto-commit để bắt đầu Transaction thủ công
            conn.setAutoCommit(false); 

            // ================= 1. Thêm Đơn Hàng Chính (Order) =================
            // Sử dụng RETURN_GENERATED_KEYS để lấy ID tự tăng của bản ghi vừa chèn
            PreparedStatement psOrder = conn.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS);
            // Kiểm tra xem đơn hàng có thuộc về tài khoản đã đăng nhập không
            if (order.getUserId() != null && order.getUserId() > 0) {
                psOrder.setInt(1, order.getUserId());
            } else {
                psOrder.setNull(1, java.sql.Types.INTEGER); // Đặt NULL nếu là khách vãng lai mua hàng
            }
            psOrder.setString(2, order.getCustomerName());  // Họ tên khách hàng
            psOrder.setString(3, order.getEmail());          // Email liên hệ
            psOrder.setString(4, order.getPhone());          // Số điện thoại
            psOrder.setString(5, order.getAddress());        // Địa chỉ nhận hàng
            psOrder.setString(6, order.getNote());           // Ghi chú đơn hàng
            psOrder.setDouble(7, order.getTotalMoney());     // Tổng tiền thanh toán
            psOrder.setString(8, order.getPaymentMethod());  // Phương thức thanh toán (COD, v.v.)
            psOrder.setString(9, "PENDING");                 // Trạng thái ban đầu: PENDING (Chờ xử lý)

            // Thực thi chèn đơn hàng
            psOrder.executeUpdate();
            
            // Lấy mã ID tự tăng vừa sinh ra từ cơ sở dữ liệu
            ResultSet rsKeys = psOrder.getGeneratedKeys();
            int orderId = 0;
            if (rsKeys.next()) {
                orderId = rsKeys.getInt(1); // Lấy khóa chính (id đơn hàng mới)
            }

            // ================= 2. Thêm Chi Tiết Đơn Hàng (OrderDetails) =================
            if (orderId > 0 && cartItems != null) {
                PreparedStatement psDetail = conn.prepareStatement(sqlDetail);
                // Duyệt qua từng sản phẩm trong giỏ hàng và gom vào batch (lô) để chèn 1 lần
                for (CartItem item : cartItems) {
                    psDetail.setInt(1, orderId);                                             // Khóa ngoại liên kết tới đơn hàng
                    psDetail.setInt(2, item.getProduct().getId());                           // Mã sản phẩm
                    psDetail.setDouble(3, item.getProduct().getEffectivePrice());           // Giá bán tại thời điểm mua
                    psDetail.setInt(4, item.getQuantity());                                  // Số lượng mua
                    psDetail.setString(5, item.getSize() != null ? item.getSize() : "Freesize"); // Kích thước đã chọn
                    psDetail.setString(6, item.getColor() != null ? item.getColor() : "Mặc định"); // Màu sắc đã chọn
                    psDetail.setDouble(7, item.getTotalPrice());                             // Tổng thành tiền của mục này
                    psDetail.addBatch(); // Thêm vào hàng đợi thực thi hàng loạt
                }
                psDetail.executeBatch(); // Thực thi lưu toàn bộ chi tiết đơn hàng
            }

            // Xác nhận lưu toàn bộ thay đổi vào cơ sở dữ liệu (Hoàn tất Transaction)
            conn.commit(); 
            return orderId; // Trả về ID đơn hàng thành công
        } catch (Exception e) {
            // Nếu có bất kỳ lỗi nào xảy ra, hoàn tác lại toàn bộ (Rollback) để không bị dữ liệu rác
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
        } finally {
            // Đảm bảo bật lại auto-commit và đóng kết nối giải phóng tài nguyên
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return 0; // Trả về 0 biểu thị việc tạo đơn thất bại
    }

    // Danh sách đơn hàng mẫu tĩnh dự phòng khi chưa kết nối database hoặc database rỗng
    private static final List<Order> mockOrders = new ArrayList<>();
    static {
        mockOrders.add(new Order(1001, 1, "Nguyễn Văn An", "an.nguyen@gmail.com", "0912345678", "123 Nguyễn Trãi, Quận 5, TP.HCM", "Giao trong giờ hành chính", 1030000.0, "COD", "PENDING", new java.sql.Timestamp(System.currentTimeMillis() - 3600000)));
        mockOrders.add(new Order(1002, 2, "Trần Thị Mai", "mai.tran@gmail.com", "0988776655", "456 Cầu Giấy, Hà Nội", "Gọi trước khi giao", 580000.0, "COD", "DELIVERED", new java.sql.Timestamp(System.currentTimeMillis() - 7200000)));
        mockOrders.add(new Order(1003, null, "Lê Hoàng Long", "long.le@yahoo.com", "0901234567", "88 Nguyễn Văn Linh, Đà Nẵng", "Đóng gói cẩn thận", 1450000.0, "COD", "PENDING", new java.sql.Timestamp(System.currentTimeMillis() - 86400000)));
    }

    /**
     * Lấy toàn bộ danh sách đơn hàng để hiển thị cho trang quản trị (Admin Dashboard)
     * @return Danh sách các đơn hàng trong hệ thống
     */
    public List<Order> getAllOrders() {
        List<Order> list = new ArrayList<>();
        // Truy vấn sắp xếp đơn hàng mới nhất lên đầu (id DESC)
        String sql = "SELECT * FROM orders ORDER BY id DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Order o = new Order(
                        rs.getInt("id"),                            // Mã đơn hàng
                        (Integer) rs.getObject("user_id"),          // Mã khách hàng (có thể null nếu là khách vãng lai)
                        rs.getString("customer_name"),              // Họ tên khách nhận
                        rs.getString("email"),                      // Email nhận thông báo
                        rs.getString("phone"),                      // Số điện thoại
                        rs.getString("address"),                    // Địa chỉ nhận hàng
                        rs.getString("note"),                       // Ghi chú của khách
                        rs.getDouble("total_money"),                // Tổng số tiền
                        rs.getString("payment_method"),             // Phương thức thanh toán
                        rs.getString("status"),                     // Trạng thái đơn hàng
                        rs.getTimestamp("created_at")               // Thời gian đặt hàng
                );
                list.add(o);
            }
        } catch (Exception e) {
            System.err.println("⚠️ DB Query orders failed, loading Mock Orders: " + e.getMessage());
        }

        // Nếu database chưa có đơn hàng nào hoặc lỗi kết nối, trả về danh sách mẫu
        if (list.isEmpty()) {
            return new ArrayList<>(mockOrders);
        }
        return list;
    }

    /**
     * Cập nhật trạng thái đơn hàng (Ví dụ: PENDING -> CONFIRMED -> SHIPPING -> DELIVERED -> CANCELLED)
     * @param orderId Mã đơn hàng cần cập nhật
     * @param status Trạng thái mới cần áp dụng
     * @return true nếu cập nhật thành công, ngược lại false
     */
    public boolean updateOrderStatus(int orderId, String status) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);   // Thiết lập trạng thái mới
            ps.setInt(2, orderId);     // Điều kiện mã đơn hàng
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("⚠️ DB update order status failed, updating Mock Orders: " + e.getMessage());
        }

        // Nếu cập nhật database thất bại, cập nhật trạng thái trong danh sách mẫu
        for (Order o : mockOrders) {
            if (o.getId() == orderId) {
                o.setStatus(status);
                return true;
            }
        }
        return false;
    }
}
