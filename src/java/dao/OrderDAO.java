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

public class OrderDAO {

    private static final List<Order> mockOrders = new ArrayList<>();
    static {
        mockOrders.add(new Order(1001, 1, "Nguyễn Văn An", "an.nguyen@gmail.com", "0912345678", "123 Nguyễn Trãi, Quận 5, TP.HCM", "Giao trong giờ hành chính", 1030000.0, "COD", "PENDING", new java.sql.Timestamp(System.currentTimeMillis() - 3600000)));
        mockOrders.add(new Order(1002, 2, "Trần Thị Mai", "mai.tran@gmail.com", "0988776655", "456 Cầu Giấy, Hà Nội", "Gọi trước khi giao", 580000.0, "COD", "DELIVERED", new java.sql.Timestamp(System.currentTimeMillis() - 7200000)));
        mockOrders.add(new Order(1003, null, "Lê Hoàng Long", "long.le@yahoo.com", "0901234567", "88 Nguyễn Văn Linh, Đà Nẵng", "Đóng gói cẩn thận", 1450000.0, "COD", "PENDING", new java.sql.Timestamp(System.currentTimeMillis() - 86400000)));
    }

    public boolean createOrder(Order order, List<OrderDetail> orderDetails) {
        String sqlOrder = "INSERT INTO orders (user_id, customer_name, email, phone, address, note, total_money, payment_method, status) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlDetail = "INSERT INTO order_details (order_id, product_id, price, quantity, size, color, total_money) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = DBContext.getConnection();
            conn.setAutoCommit(false); // Enable transaction

            // 1. Insert Order
            PreparedStatement psOrder = conn.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS);
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
            ResultSet rsKeys = psOrder.getGeneratedKeys();
            int orderId = 1000 + mockOrders.size() + 1;
            if (rsKeys.next()) {
                orderId = rsKeys.getInt(1);
            }
            order.setId(orderId);

            // 2. Insert OrderDetails
            if (orderId > 0 && orderDetails != null) {
                PreparedStatement psDetail = conn.prepareStatement(sqlDetail);
                for (OrderDetail item : orderDetails) {
                    psDetail.setInt(1, orderId);
                    psDetail.setInt(2, item.getProductId());
                    psDetail.setDouble(3, item.getPrice());
                    psDetail.setInt(4, item.getQuantity());
                    psDetail.setString(5, item.getSize() != null ? item.getSize() : "Freesize");
                    psDetail.setString(6, item.getColor() != null ? item.getColor() : "Mặc định");
                    psDetail.setDouble(7, item.getTotalMoney());
                    psDetail.addBatch();
                }
                psDetail.executeBatch();
            }

            conn.commit(); // Commit transaction
            mockOrders.add(0, order);
            return true;
        } catch (Exception e) {
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
            return true;
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

    public int createOrder(Order order, List<CartItem> cartItems, boolean legacyMode) {
        List<OrderDetail> details = new ArrayList<>();
        if (cartItems != null) {
            for (CartItem ci : cartItems) {
                OrderDetail od = new OrderDetail();
                od.setProductId(ci.getProduct().getId());
                od.setPrice(ci.getProduct().getEffectivePrice());
                od.setQuantity(ci.getQuantity());
                od.setSize(ci.getSize());
                od.setColor(ci.getColor());
                od.setTotalMoney(ci.getTotalMoney());
                details.add(od);
            }
        }
        createOrder(order, details);
        return order.getId();
    }

    public List<Order> getAllOrders() {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT * FROM orders ORDER BY id DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
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
        } catch (Exception e) {
            System.err.println("⚠️ DB Query orders failed, loading Mock Orders: " + e.getMessage());
        }

        if (list.isEmpty()) {
            return new ArrayList<>(mockOrders);
        }
        return list;
    }

    public boolean updateOrderStatus(int orderId, String status) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("⚠️ DB update order status failed, updating Mock Orders: " + e.getMessage());
        }

        for (Order o : mockOrders) {
            if (o.getId() == orderId) {
                o.setStatus(status);
                return true;
            }
        }
        return false;
    }
}
