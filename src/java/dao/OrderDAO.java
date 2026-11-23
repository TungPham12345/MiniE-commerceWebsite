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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        String sqlDetail = "INSERT INTO order_details (order_id, product_id, price, quantity, size, color, total_money) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = DBContext.getConnection();
            ensureStockTrackingColumn(conn);
            ProductStockDAO.ensureSchema(conn);
            conn.setAutoCommit(false);

            if (cartItems == null || cartItems.isEmpty()) {
                conn.rollback();
                return -1;
            }
            for (CartItem item : cartItems) {
                if (item == null || item.getProduct() == null || item.getQuantity() <= 0) {
                    throw new IllegalArgumentException("Thông tin sản phẩm trong giỏ hàng không hợp lệ.");
                }
                if (!ProductStockDAO.adjust(
                        conn,
                        item.getProduct().getId(),
                        item.getSize(),
                        item.getQuantity(),
                        false)) {
                    throw new StockUnavailableException("Size " + item.getSize() + " không đủ hàng trong kho.");
                }
            }

            String sqlOrder = "INSERT INTO orders (user_id, customer_name, email, phone, address, note, total_money, payment_method, status, stock_deducted) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 1)";
            try (PreparedStatement psOrder = conn.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS)) {
                if (order.getUserId() != null && order.getUserId() > 0) {
                    psOrder.setInt(1, order.getUserId());
                } else {
                    psOrder.setNull(1, java.sql.Types.INTEGER);
                }
                psOrder.setString(2, order.getCustomerName());
                psOrder.setString(3, order.getEmail());
                psOrder.setString(4, order.getPhone());
                psOrder.setString(5, order.getAddress());
                psOrder.setString(6, order.getNote());
                psOrder.setDouble(7, order.getTotalMoney());
                psOrder.setString(8, order.getPaymentMethod());
                psOrder.setString(9, "PENDING");
                psOrder.executeUpdate();

                int orderId;
                try (ResultSet rsKeys = psOrder.getGeneratedKeys()) {
                    if (!rsKeys.next()) {
                        throw new IllegalStateException("Không lấy được mã đơn hàng vừa tạo.");
                    }
                    orderId = rsKeys.getInt(1);
                }
                order.setId(orderId);

                try (PreparedStatement psDetail = conn.prepareStatement(sqlDetail)) {
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
                    psDetail.executeBatch();
                }
                conn.commit();
                return orderId;
            }
        } catch (StockUnavailableException e) {
            rollback(conn);
            throw e;
        } catch (Exception e) {
            // Nếu có lỗi kết nối CSDL hoặc lỗi truy vấn, hoàn tác và chuyển sang lưu vào danh sách mẫu (Mock mode)
            rollback(conn);
            System.err.println("⚠️ DB Order Creation failed, falling back to mock orders: " + e.getMessage());
            if (cartItems == null || cartItems.isEmpty()) {
                return -1;
            }
            int orderId = 1000 + mockOrders.size() + 1;
            order.setId(orderId);
            order.setCreatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
            List<OrderDetail> details = new ArrayList<>();
            int detailId = 1;
            for (CartItem ci : cartItems) {
                if (ci == null || ci.getProduct() == null) continue;
                OrderDetail od = new OrderDetail(
                    detailId++,
                    orderId,
                    ci.getProduct().getId(),
                    ci.getProduct().getEffectivePrice(),
                    ci.getQuantity(),
                    ci.getSize() != null ? ci.getSize() : "Freesize",
                    ci.getColor() != null ? ci.getColor() : "Mặc định",
                    ci.getTotalPrice()
                );
                od.setProductName(ci.getProduct().getName());
                od.setProductImage(ci.getProduct().getImage());
                details.add(od);
            }
            order.setOrderDetails(details);
            mockOrders.add(0, order);
            return orderId;
        } finally {
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

    private static class StockUnavailableException extends RuntimeException {
        private StockUnavailableException(String message) {
            super(message);
        }
    }

    private void ensureStockTrackingColumn(Connection conn) throws Exception {
        String sql = "IF COL_LENGTH('dbo.orders', 'stock_deducted') IS NULL "
                + "ALTER TABLE dbo.orders ADD stock_deducted BIT NOT NULL "
                + "CONSTRAINT DF_orders_stock_deducted DEFAULT (0)";
        try (Statement statement = conn.createStatement()) {
            statement.execute(sql);
        }
    }

    private void rollback(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (Exception e) {
                System.err.println("⚠️ Database rollback failed: " + e.getMessage());
            }
        }
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

        // Dự phòng nếu cơ sở dữ liệu chưa kết nối hoặc chưa có dữ liệu, trả về toàn bộ danh sách mẫu
        if (list.isEmpty() && !mockOrders.isEmpty()) {
            return new ArrayList<>(mockOrders);
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
        if ("CANCELLED".equalsIgnoreCase(status) || "DA_HUY".equalsIgnoreCase(status)) {
            return transitionOrderStock(orderId, "CANCELLED");
        }
        if ("RETURNED".equalsIgnoreCase(status) || "REFUNDED".equalsIgnoreCase(status) || "TRA_HANG".equalsIgnoreCase(status)) {
            return transitionOrderStock(orderId, "RETURNED");
        }
        if ("PENDING".equalsIgnoreCase(status) || "CHO_XAC_NHAN".equalsIgnoreCase(status)) {
            return transitionOrderStock(orderId, "PENDING");
        }

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
     * Cập nhật trạng thái đơn hàng kèm theo ghi vết nhật ký hành trình (Tracking History)
     */
    public boolean updateOrderStatus(int orderId, String status, model.User actor, String location, String note) {
        boolean ok = updateOrderStatus(orderId, status);
        if (ok) {
            try {
                OrderTrackingDAO trackingDAO = new OrderTrackingDAO();
                model.Order sample = new model.Order();
                sample.setStatus(status);
                String statusName = sample.getStatusVietnamese();
                Integer actorId = actor != null ? actor.getId() : null;
                String actorName = actor != null ? actor.getFullname() : "Hệ thống";
                Integer actorRole = actor != null ? actor.getRole() : null;
                trackingDAO.addTracking(orderId, status, statusName, actorId, actorName, actorRole, location, note);
            } catch (Exception e) {
                System.err.println("⚠️ Record tracking failed: " + e.getMessage());
            }
        }
        return ok;
    }

    private boolean transitionOrderStock(int orderId, String newStatus) {
        Connection conn = null;
        try {
            conn = DBContext.getConnection();
            ensureStockTrackingColumn(conn);
            ProductStockDAO.ensureSchema(conn);
            conn.setAutoCommit(false);
            String currentStatus;
            boolean stockDeducted;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT status, stock_deducted FROM orders WITH (UPDLOCK, ROWLOCK) WHERE id = ?")) {
                ps.setInt(1, orderId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return updateMockOrderStatus(orderId, newStatus);
                    }
                    currentStatus = rs.getString("status");
                    stockDeducted = rs.getBoolean("stock_deducted");
                }
            }

            if ("CANCELLED".equals(newStatus) || "RETURNED".equals(newStatus)) {
                if ("CANCELLED".equals(newStatus)) {
                    if (!"PENDING".equalsIgnoreCase(currentStatus)
                            && !"UNPAID".equalsIgnoreCase(currentStatus)
                            && !"CHO_XU_LY".equalsIgnoreCase(currentStatus)
                            && !"CHO_XAC_NHAN".equalsIgnoreCase(currentStatus)
                            && !"CONFIRMED".equalsIgnoreCase(currentStatus)
                            && !"CHO_LAY_HANG".equalsIgnoreCase(currentStatus)) {
                        conn.rollback();
                        return false;
                    }
                }
                if (stockDeducted) {
                    adjustOrderInventory(conn, orderId, true);
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE orders SET status = ?, stock_deducted = 0 WHERE id = ?")) {
                    ps.setString(1, newStatus);
                    ps.setInt(2, orderId);
                    if (ps.executeUpdate() == 0) {
                        conn.rollback();
                        return false;
                    }
                }
            } else {
                if (("CANCELLED".equalsIgnoreCase(currentStatus) || "RETURNED".equalsIgnoreCase(currentStatus)) && !stockDeducted) {
                    adjustOrderInventory(conn, orderId, false);
                    stockDeducted = true;
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE orders SET status = 'PENDING', stock_deducted = ? WHERE id = ?")) {
                    ps.setBoolean(1, stockDeducted);
                    ps.setInt(2, orderId);
                    if (ps.executeUpdate() == 0) {
                        conn.rollback();
                        return false;
                    }
                }
            }
            conn.commit();
            updateMockOrderStatus(orderId, newStatus);
            return true;
        } catch (StockUnavailableException e) {
            rollback(conn);
            System.err.println("⚠️ Order status update rejected: " + e.getMessage());
            return false;
        } catch (Exception e) {
            rollback(conn);
            System.err.println("⚠️ Order stock status update failed: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (Exception e) {
                    System.err.println("⚠️ Closing order status connection failed: " + e.getMessage());
                }
            }
        }
    }

    private void adjustOrderInventory(Connection conn, int orderId, boolean restore) throws Exception {
        String sql = "SELECT product_id, size, SUM(quantity) AS quantity FROM order_details "
                + "WHERE order_id = ? GROUP BY product_id, size";
        Map<Integer, Map<String, Integer>> inventory = new LinkedHashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    inventory.computeIfAbsent(rs.getInt("product_id"), id -> new LinkedHashMap<>())
                            .put(rs.getString("size"), rs.getInt("quantity"));
                }
            }
        }
        for (Map.Entry<Integer, Map<String, Integer>> product : inventory.entrySet()) {
            for (Map.Entry<String, Integer> size : product.getValue().entrySet()) {
                if (!ProductStockDAO.adjust(conn, product.getKey(), size.getKey(), size.getValue(), restore)) {
                    throw new IllegalStateException("Không thể cập nhật tồn kho size " + size.getKey() + ".");
                }
            }
        }
    }

    private boolean updateMockOrderStatus(int orderId, String status) {
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

        // Dự phòng nếu cơ sở dữ liệu chưa kết nối hoặc chưa có dữ liệu, lấy từ danh sách mẫu
        if (list.isEmpty() && !mockOrders.isEmpty()) {
            List<Order> userOrders = new ArrayList<>();
            for (Order o : mockOrders) {
                if (o.getUserId() != null && o.getUserId() == userId) {
                    userOrders.add(o);
                }
            }
            return userOrders;
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
        // Chỉ cho phép khách hủy khi đơn còn ở trạng thái Chờ xác nhận
        String currentStatus = o.getStatus() != null ? o.getStatus().toUpperCase() : "";
        if ("PENDING".equals(currentStatus) || "UNPAID".equals(currentStatus) || "CHO_XU_LY".equals(currentStatus) || "CHO_XAC_NHAN".equals(currentStatus)) {
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
        if ("SHIPPING".equals(currentStatus) || "VAN_CHUYEN".equals(currentStatus) || "CHO_GIAO_HANG".equals(currentStatus)) {
            return updateOrderStatus(orderId, "DELIVERED");
        }
        return false;
    }

    /**
     * Khách hàng yêu cầu trả hàng / hoàn tiền (khi đơn đã giao thành công)
     * @param orderId Mã đơn hàng
     * @param userId  Mã tài khoản người đặt hàng
     * @return true nếu gửi yêu cầu thành công
     */
    public boolean returnOrder(int orderId, int userId) {
        Order o = getOrderById(orderId);
        if (o == null) {
            return false;
        }
        if (o.getUserId() != null && o.getUserId() != userId) {
            return false;
        }
        String currentStatus = o.getStatus() != null ? o.getStatus().toUpperCase() : "";
        if ("DELIVERED".equals(currentStatus) || "COMPLETED".equals(currentStatus) || "DA_GIAO".equals(currentStatus)) {
            return updateOrderStatus(orderId, "RETURNED");
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
            ensureStockTrackingColumn(conn);
            ProductStockDAO.ensureSchema(conn);
            conn.setAutoCommit(false);
            if (isReservedPendingOrder(conn, orderId)) {
                adjustOrderInventory(conn, orderId, true);
            }
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
            ensureStockTrackingColumn(conn);
            ProductStockDAO.ensureSchema(conn);
            conn.setAutoCommit(false);
            String stockSql = "SELECT od.product_id, od.size, SUM(od.quantity) AS quantity "
                    + "FROM order_details od JOIN orders o ON o.id = od.order_id "
                    + "WHERE o.stock_deducted = 1 AND o.status IN ('PENDING', 'UNPAID', 'CHO_XU_LY') "
                    + "GROUP BY od.product_id, od.size";
            Map<Integer, Map<String, Integer>> inventory = new LinkedHashMap<>();
            try (Statement stockStatement = conn.createStatement();
                    ResultSet rs = stockStatement.executeQuery(stockSql)) {
                while (rs.next()) {
                    inventory.computeIfAbsent(rs.getInt("product_id"), id -> new LinkedHashMap<>())
                            .put(rs.getString("size"), rs.getInt("quantity"));
                }
            }
            for (Map.Entry<Integer, Map<String, Integer>> product : inventory.entrySet()) {
                for (Map.Entry<String, Integer> size : product.getValue().entrySet()) {
                    if (!ProductStockDAO.adjust(conn, product.getKey(), size.getKey(), size.getValue(), true)) {
                        throw new IllegalStateException("Không thể hoàn kho size " + size.getKey() + ".");
                    }
                }
            }
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

    private boolean isReservedPendingOrder(Connection conn, int orderId) throws Exception {
        String sql = "SELECT status, stock_deducted FROM orders WITH (UPDLOCK, ROWLOCK) WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || !rs.getBoolean("stock_deducted")) {
                    return false;
                }
                String status = rs.getString("status");
                return "PENDING".equalsIgnoreCase(status)
                        || "UNPAID".equalsIgnoreCase(status)
                        || "CHO_XU_LY".equalsIgnoreCase(status);
            }
        }
    }
}
