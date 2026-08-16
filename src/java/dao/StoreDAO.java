package dao;

import context.DBContext;
import model.Store;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class StoreDAO {

    public List<Store> getAllStores() {
        List<Store> list = new ArrayList<>();
        String sql = "SELECT * FROM stores ORDER BY id ASC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Store s = new Store(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getString("city"),
                        rs.getString("phone"),
                        rs.getString("hours"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude"),
                        rs.getBoolean("is_main")
                );
                list.add(s);
            }
        } catch (Exception e) {
            System.err.println("⚠️ Store query failed: " + e.getMessage());
        }

        if (list.isEmpty()) {
            return getMockStores();
        }
        return list;
    }

    public boolean insertStore(Store s) {
        String sql = "INSERT INTO stores (name, address, city, phone, hours, latitude, longitude, is_main) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getAddress());
            ps.setString(3, s.getCity());
            ps.setString(4, s.getPhone());
            ps.setString(5, s.getHours());
            ps.setDouble(6, s.getLatitude());
            ps.setDouble(7, s.getLongitude());
            ps.setBoolean(8, s.isIsMain());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteStore(int id) {
        String sql = "DELETE FROM stores WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Store> getMockStores() {
        List<Store> list = new ArrayList<>();
        // TP.HCM Stores
        list.add(new Store(1, "HighFashion Flagship Sư Vạn Hạnh", "561 Sư Vạn Hạnh, Phường Hòa Hưng, Quận 10", "TP.HCM", "0901 234 567", "08:30 - 22:00", 10.7769, 106.6675, true));
        list.add(new Store(2, "HighFashion Nguyễn Trãi Store", "123 Nguyễn Trãi, Phường 2, Quận 5", "TP.HCM", "0902 345 678", "09:00 - 21:30", 10.7554, 106.6792, false));
        list.add(new Store(3, "HighFashion Lê Lợi Boutique", "456 Lê Lợi, Phường Bến Thành, Quận 1", "TP.HCM", "0903 456 789", "08:30 - 22:00", 10.7731, 106.6983, false));

        // Hà Nội Stores
        list.add(new Store(4, "HighFashion Hà Nội Tràng Tiền", "24 Tràng Tiền, Quận Hoàn Kiếm", "Hà Nội", "0904 567 890", "08:30 - 22:00", 21.0253, 105.8544, false));
        list.add(new Store(5, "HighFashion Cầu Giấy Premium", "180 Cầu Giấy, Phường Quan Hoa, Quận Cầu Giấy", "Hà Nội", "0905 678 901", "09:00 - 21:30", 21.0362, 105.7905, false));

        // Đà Nẵng Store
        list.add(new Store(6, "HighFashion Đà Nẵng Nguyễn Văn Linh", "88 Nguyễn Văn Linh, Quận Hải Châu", "Đà Nẵng", "0906 789 012", "08:30 - 21:30", 16.0608, 108.2155, false));

        return list;
    }
}
