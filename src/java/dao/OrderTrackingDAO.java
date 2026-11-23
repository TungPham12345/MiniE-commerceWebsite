package dao;

import context.DBContext;
import model.OrderTracking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrderTrackingDAO {
    private static final List<OrderTracking> mockTrackings = Collections.synchronizedList(new ArrayList<>());
    private static int mockIdCounter = 1;

    /**
     * Thêm bản ghi nhật ký hành trình đơn hàng
     */
    public boolean addTracking(int orderId, String status, String statusName, Integer actorId, String actorName, Integer actorRole, String location, String note) {
        String sql = "INSERT INTO order_tracking (order_id, status, status_name, actor_id, actor_name, actor_role, location, note, created_at) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Timestamp now = new Timestamp(System.currentTimeMillis());
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setString(2, status);
            ps.setString(3, statusName);
            if (actorId != null) ps.setInt(4, actorId); else ps.setNull(4, java.sql.Types.INTEGER);
            ps.setString(5, actorName);
            if (actorRole != null) ps.setInt(6, actorRole); else ps.setNull(6, java.sql.Types.INTEGER);
            ps.setString(7, location);
            ps.setString(8, note);
            ps.setTimestamp(9, now);
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("⚠️ DB add tracking failed (fallback mock): " + e.getMessage());
        }

        // Lưu vào mock list để luôn có dữ liệu tức thì
        OrderTracking t = new OrderTracking(mockIdCounter++, orderId, status, statusName, actorId, actorName, actorRole, location, note, now);
        mockTrackings.add(t);
        return true;
    }

    /**
     * Lấy toàn bộ hành trình của một đơn hàng (sắp xếp tăng dần theo thời gian)
     */
    public List<OrderTracking> getTrackingsByOrderId(int orderId) {
        List<OrderTracking> list = new ArrayList<>();
        String sql = "SELECT * FROM order_tracking WHERE order_id = ? ORDER BY created_at ASC, id ASC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderTracking t = new OrderTracking();
                    t.setId(rs.getInt("id"));
                    t.setOrderId(rs.getInt("order_id"));
                    t.setStatus(rs.getString("status"));
                    t.setStatusName(rs.getString("status_name"));
                    int aid = rs.getInt("actor_id");
                    t.setActorId(rs.wasNull() ? null : aid);
                    t.setActorName(rs.getString("actor_name"));
                    int arole = rs.getInt("actor_role");
                    t.setActorRole(rs.wasNull() ? null : arole);
                    t.setLocation(rs.getString("location"));
                    t.setNote(rs.getString("note"));
                    t.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(t);
                }
            }
            if (!list.isEmpty()) {
                return list;
            }
        } catch (Exception e) {
            System.err.println("⚠️ DB get tracking failed: " + e.getMessage());
        }

        // Fallback mock
        synchronized (mockTrackings) {
            for (OrderTracking t : mockTrackings) {
                if (t.getOrderId() == orderId) {
                    list.add(t);
                }
            }
        }
        return list;
    }
}
