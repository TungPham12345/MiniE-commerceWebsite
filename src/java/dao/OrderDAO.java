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

    public int createOrder(Order order, List<CartItem> cartItems) {
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
            int orderId = 0;
            if (rsKeys.next()) {
                orderId = rsKeys.getInt(1);
            }

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
                psDetail.executeBatch();
            }

            conn.commit(); // Commit transaction
            return orderId;
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
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
        return 0;
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
            e.printStackTrace();
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
            e.printStackTrace();
        }
        return false;
    }
}
