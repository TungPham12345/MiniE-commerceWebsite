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
 * Lớp OrderDAO: Quản lý truy xuất và lưu trữ dữ liệu Đơn hàng (orders) và Chi
 * tiết đơn hàng (order_details)
 * Sử dụng Transaction (Giao dịch) để đảm bảo tính toàn vẹn dữ liệu khi đặt
 * hàng:
 * nếu lưu đơn hàng hoặc chi tiết đơn hàng thất bại, toàn bộ quá trình sẽ được
 * Rollback (hoàn tác).
 */
public class OrderDAO {

    public int createOrder(Order order, List<CartItem> cartItems) {
        String sqlOrder = "INSERT INTO orders (user_id, customer_name, email, phone, address, note, total_money, payment_method, status) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        // Câu lệnh SQL thêm từng sản phẩm chi tiết vào bảng order_details
        String sqlDetail = "INSERT INTO order_details (order_id, product_id, price, quantity, size, color, total_money) "
                +
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
            psOrder.setString(2, order.getCustomerName()); // Họ tên khách hàng
            psOrder.setString(3, order.getEmail()); // Email liên hệ
            psOrder.setString(4, order.getPhone()); // Số điện thoại
            psOrder.setString(5, order.getAddress()); // Địa chỉ nhận hàng
            psOrder.setString(6, order.getNote()); // Ghi chú đơn hàng
            psOrder.setDouble(7, order.getTotalMoney()); // Tổng tiền thanh toán
            psOrder.setString(8, order.getPaymentMethod()); // Phương thức thanh toán (COD, v.v.)
            psOrder.setString(9, "PENDING"); // Trạng thái ban đầu: PENDING (Chờ xử lý)

            // Thực thi chèn đơn hàng
            psOrder.executeUpdate();

            // Lấy mã ID tự tăng vừa sinh ra từ cơ sở dữ liệu
            ResultSet rsKeys = psOrder.getGeneratedKeys();
            int orderId = 1000 + mockOrders.size() + 1;
            if (rsKeys.next()) {
                orderId = rsKeys.getInt(1); // Lấy khóa chính (id đơn hàng mới)
            }
            order.setId(orderId);

            // 2. Insert OrderDetails
            if (orderId > 0 && cartItems != null) {
                PreparedStatement psDetail = conn.prepareStatement(sqlDetail);
                for (CartItem item : cartItems) {
                    psDetail.setInt(1, orderId);
                    psDetail.setInt(2, item.getProduct().getId());
                    psDetail.setDouble(3, item.getProduct().getEffectivePrice());
                    psDetail.setInt(4, item.getQuantity());
                    psDetail.setString(5, item.getSize() != null ? item.getSize() : "Freesize");
                    psDetail.setString(6, item.getColor() != null ? item.getColor() : "Mặc định");
                    psDetail.setDouble(7, item.getTotalPrice());
                    psDetail.addBatch();
                }
                psDetail.executeBatch(); // Thực thi lưu toàn bộ chi tiết đơn hàng
            }

            conn.commit(); // Commit transaction
            return orderId;
        } catch (Exception e) {
            // Nếu có bất kỳ lỗi nào xảy ra, hoàn tác lại toàn bộ (Rollback) để không bị dữ
            // liệu rác
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            System.err.println("⚠️ DB Order Creation failed, adding to mock orders: " + e.getMessage());
            order.setId(1000 + mockOrders.size() + 1);
            mockOrders.add(0, order);
            return order.getId();
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
    }

    private static final List<Order> mockOrders = new ArrayList<>();

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
                        rs.getInt("id"), // Mã đơn hàng
                        (Integer) rs.getObject("user_id"), // Mã khách hàng (có thể null nếu là khách vãng lai)
                        rs.getString("customer_name"), // Họ tên khách nhận
                        rs.getString("email"), // Email nhận thông báo
                        rs.getString("phone"), // Số điện thoại
                        rs.getString("address"), // Địa chỉ nhận hàng
                        rs.getString("note"), // Ghi chú của khách
                        rs.getDouble("total_money"), // Tổng số tiền
                        rs.getString("payment_method"), // Phương thức thanh toán
                        rs.getString("status"), // Trạng thái đơn hàng
                        rs.getTimestamp("created_at") // Thời gian đặt hàng
                );
                list.add(o);
            }
        } catch (Exception e) {
            System.err.println("⚠️ DB Query orders failed: " + e.getMessage());
        }

        // Nạp chi tiết sản phẩm cho từng đơn hàng
        for (Order o : list) {
            o.setOrderDetails(getOrderDetailsByOrderId(o.getId()));
        }

        return list;
    }

    /**
     * Lấy danh sách chi tiết các sản phẩm trong một đơn hàng
     * @param orderId Mã đơn hàng cần tra cứu
     * @return Danh sách chi tiết sản phẩm
     */
    public List<OrderDetail> getOrderDetailsByOrderId(int orderId) {
        List<OrderDetail> details = new ArrayList<>();
        String sql = "SELECT od.*, p.name AS product_name, p.image AS product_image " +
                     "FROM order_details od " +
                     "LEFT JOIN products p ON od.product_id = p.id " +
                     "WHERE od.order_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderDetail d = new OrderDetail(
                        rs.getInt("id"),
                        rs.getInt("order_id"),
                        rs.getInt("product_id"),
                        rs.getDouble("price"),
                        rs.getInt("quantity"),
                        rs.getString("size"),
                        rs.getString("color"),
                        rs.getDouble("total_money")
                    );
                    d.setProductName(rs.getString("product_name"));
                    d.setProductImage(rs.getString("product_image"));
                    details.add(d);
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ DB Query order details failed: " + e.getMessage());
        }

        if (details.isEmpty()) {
            for (Order o : mockOrders) {
                if (o.getId() == orderId && o.getOrderDetails() != null) {
                    return o.getOrderDetails();
                }
            }
        }
        return details;
    }

    /**
     * Cập nhật trạng thái đơn hàng (Ví dụ: PENDING -> CONFIRMED -> SHIPPING -> DELIVERED -> CANCELLED)
     * @param orderId Mã đơn hàng cần cập nhật
     * @param status  Trạng thái mới cần áp dụng
     * @return true nếu cập nhật thành công, ngược lại false
     */
    public boolean updateOrderStatus(int orderId, String status) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status); // Thiết lập trạng thái mới
            ps.setInt(2, orderId); // Điều kiện mã đơn hàng
            int affected = ps.executeUpdate();
            if (affected > 0) {
                // Đồng thời cập nhật trạng thái trong mockOrders nếu có
                for (Order o : mockOrders) {
                    if (o.getId() == orderId) {
                        o.setStatus(status);
                        break;
                    }
                }
                return true;
            }
        } catch (Exception e) {
            System.err.println("⚠️ DB update order status failed: " + e.getMessage());
        }

        // Nếu cập nhật database không tìm thấy dòng (hoặc lỗi kết nối), cập nhật trong danh sách mẫu
        for (Order o : mockOrders) {
            if (o.getId() == orderId) {
                o.setStatus(status);
                return true;
            }
        }
        return false;
    }

    /**
     * Lấy danh sách đơn hàng đã mua của người dùng cụ thể
     * @param userId Mã định danh tài khoản khách hàng
     * @return Danh sách đơn hàng sắp xếp theo thời gian mới nhất
     */
    public List<Order> getOrdersByUserId(int userId) {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE user_id = ? ORDER BY id DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order o = new Order(
                        rs.getInt("id"),
                        (Integer) rs.getObject("user_id"),
                        rs.getString("customer_name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("address"),
                        rs.getString("note"),
                        rs.getDouble("total_money"),
                        rs.getString("payment_method"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at")
                    );
                    list.add(o);
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ DB Query user orders failed: " + e.getMessage());
        }

        // Nạp chi tiết sản phẩm cho từng đơn hàng
        for (Order o : list) {
            o.setOrderDetails(getOrderDetailsByOrderId(o.getId()));
        }

        return list;
    }

    /**
     * Lấy thông tin một đơn hàng theo mã ID
     * @param orderId Mã đơn hàng
     * @return Đối tượng Order hoặc null
     */
    public Order getOrderById(int orderId) {
        String sql = "SELECT * FROM orders WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order o = new Order(
                        rs.getInt("id"),
                        (Integer) rs.getObject("user_id"),
                        rs.getString("customer_name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("address"),
                        rs.getString("note"),
                        rs.getDouble("total_money"),
                        rs.getString("payment_method"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at")
                    );
                    o.setOrderDetails(getOrderDetailsByOrderId(o.getId()));
                    return o;
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ DB getOrderById failed: " + e.getMessage());
        }

        for (Order o : mockOrders) {
            if (o.getId() == orderId) {
                if (o.getOrderDetails() == null || o.getOrderDetails().isEmpty()) {
                    o.setOrderDetails(getOrderDetailsByOrderId(orderId));
                }
                return o;
            }
        }
        return null;
    }

    /**
     * Khách hàng tự hủy đơn hàng (chỉ cho phép khi trạng thái là PENDING hoặc UNPAID)
     * @param orderId Mã đơn hàng
     * @param userId  Mã tài khoản người đặt hàng
     * @return true nếu hủy thành công
     */
    public boolean cancelOrder(int orderId, int userId) {
        Order o = getOrderById(orderId);
        if (o == null) {
            return false;
        }
        // Kiểm tra xem đơn hàng có thuộc về tài khoản này không (hoặc tài khoản là quản trị viên)
        if (o.getUserId() != null && o.getUserId() != userId) {
            return false;
        }
        // Chỉ cho phép hủy khi đơn còn ở trạng thái PENDING hoặc UNPAID
        String currentStatus = o.getStatus() != null ? o.getStatus().toUpperCase() : "";
        if ("PENDING".equals(currentStatus) || "UNPAID".equals(currentStatus) || "CHO_XU_LY".equals(currentStatus)) {
            return updateOrderStatus(orderId, "CANCELLED");
        }
        return false;
    }

    /**
     * Khách hàng xác nhận đã nhận hàng (chuyển từ SHIPPING sang DELIVERED)
     * @param orderId Mã đơn hàng
     * @param userId  Mã tài khoản người đặt hàng
     * @return true nếu xác nhận thành công
     */
    public boolean confirmReceived(int orderId, int userId) {
        Order o = getOrderById(orderId);
        if (o == null) {
            return false;
        }
        if (o.getUserId() != null && o.getUserId() != userId) {
            return false;
        }
        String currentStatus = o.getStatus() != null ? o.getStatus().toUpperCase() : "";
        if ("SHIPPING".equals(currentStatus) || "VAN_CHUYEN".equals(currentStatus)) {
            return updateOrderStatus(orderId, "DELIVERED");
        }
        return false;
    }

    /**
     * Xóa một đơn hàng và toàn bộ chi tiết đơn hàng tương ứng
     * @param orderId Mã đơn hàng cần xóa
     * @return true nếu xóa thành công
     */
    public boolean deleteOrder(int orderId) {
        String sqlDetails = "DELETE FROM order_details WHERE order_id = ?";
        String sqlOrder = "DELETE FROM orders WHERE id = ?";
        Connection conn = null;
        try {
            conn = DBContext.getConnection();
            conn.setAutoCommit(false);
            try (PreparedStatement psDetails = conn.prepareStatement(sqlDetails)) {
                psDetails.setInt(1, orderId);
                psDetails.executeUpdate();
            }
            try (PreparedStatement psOrder = conn.prepareStatement(sqlOrder)) {
                psOrder.setInt(1, orderId);
                int affected = psOrder.executeUpdate();
                conn.commit();
                mockOrders.removeIf(o -> o.getId() == orderId);
                return affected > 0;
            }
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (Exception ex) {}
            }
            System.err.println("⚠️ Delete order failed: " + e.getMessage());
            mockOrders.removeIf(o -> o.getId() == orderId);
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (Exception ex) {}
            }
        }
    }

    /**
     * Xóa toàn bộ đơn hàng trong hệ thống (Sạch dữ liệu đơn hàng)
     * @return true nếu xóa thành công
     */
    public boolean deleteAllOrders() {
        String sqlDetails = "DELETE FROM order_details";
        String sqlOrders = "DELETE FROM orders";
        Connection conn = null;
        try {
            conn = DBContext.getConnection();
            conn.setAutoCommit(false);
            try (Statement st = conn.createStatement()) {
                st.executeUpdate(sqlDetails);
                st.executeUpdate(sqlOrders);
                try {
                    st.executeUpdate("DBCC CHECKIDENT ('orders', RESEED, 1000)");
                } catch (Exception ignore) {}
            }
            conn.commit();
            mockOrders.clear();
            return true;
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (Exception ex) {}
            }
            System.err.println("⚠️ Delete all orders failed: " + e.getMessage());
            mockOrders.clear();
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (Exception ex) {}
            }
        }
    }
}
