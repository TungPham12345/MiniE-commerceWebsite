package dao;

import context.DBContext;
import model.Store;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp StoreDAO: Quản lý truy xuất dữ liệu danh sách cửa hàng / chi nhánh (stores)
 * Bao gồm:
 * - Lấy danh sách toàn bộ hệ thống chi nhánh cửa hàng
 * - Thêm mới cửa hàng (dành cho Admin)
 * - Xóa cửa hàng theo ID
 * - Cung cấp dữ liệu mẫu dự phòng tại TP.HCM, Hà Nội, Đà Nẵng
 */
public class StoreDAO {

    /**
     * Lấy toàn bộ danh sách cửa hàng trong hệ thống từ cơ sở dữ liệu
     * @return Danh sách các cửa hàng (List<Store>)
     */
    public List<Store> getAllStores() {
        // Khởi tạo danh sách cửa hàng rỗng
        List<Store> list = new ArrayList<>();
        // Câu lệnh SQL: Lấy tất cả cửa hàng, sắp xếp theo ID tăng dần
        String sql = "SELECT * FROM stores ORDER BY id ASC";
        
        // Quản lý kết nối tự động với try-with-resources
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            // Duyệt từng bản ghi cửa hàng
            while (rs.next()) {
                // Tạo đối tượng Store từ dữ liệu các cột trong database
                Store s = new Store(
                        rs.getInt("id"),              // Mã cửa hàng
                        rs.getString("name"),         // Tên chi nhánh
                        rs.getString("address"),      // Địa chỉ chi tiết
                        rs.getString("city"),         // Thành phố / Tỉnh thành
                        rs.getString("phone"),        // Hotline liên hệ
                        rs.getString("hours"),        // Giờ mở cửa - đóng cửa
                        rs.getDouble("latitude"),     // Tọa độ vĩ độ (bản đồ)
                        rs.getDouble("longitude"),    // Tọa độ kinh độ (bản đồ)
                        rs.getBoolean("is_main")      // Cờ đánh dấu cửa hàng Flagship/Trụ sở chính
                );
                list.add(s); // Thêm cửa hàng vào danh sách
            }
        } catch (Exception e) {
            // Ghi nhận lỗi khi truy vấn database thất bại
            System.err.println("⚠️ Store query failed: " + e.getMessage());
        }

        // Nếu database chưa có dữ liệu hoặc không kết nối được, trả về danh sách cửa hàng mẫu
        if (list.isEmpty()) {
            return getMockStores();
        }
        return list;
    }

    /**
     * Thêm mới một chi nhánh cửa hàng vào hệ thống
     * @param s Đối tượng Store chứa thông tin chi nhánh cần thêm
     * @return true nếu thêm thành công, ngược lại false
     */
    public boolean insertStore(Store s) {
        String sql = "INSERT INTO stores (name, address, city, phone, hours, latitude, longitude, is_main) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            // Gán các tham số cho câu lệnh INSERT
            ps.setString(1, s.getName());          // Tên chi nhánh
            ps.setString(2, s.getAddress());       // Địa chỉ
            ps.setString(3, s.getCity());          // Khu vực thành phố
            ps.setString(4, s.getPhone());         // Số điện thoại
            ps.setString(5, s.getHours());         // Thời gian hoạt động
            ps.setDouble(6, s.getLatitude());      // Vĩ độ
            ps.setDouble(7, s.getLongitude());     // Kinh độ
            ps.setBoolean(8, s.isIsMain());        // Là cửa hàng chính hay không
            return ps.executeUpdate() > 0;         // Trả về true nếu ghi thành công vào database
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Xóa một chi nhánh cửa hàng theo ID
     * @param id Mã cửa hàng cần xóa
     * @return true nếu xóa thành công, ngược lại false
     */
    public boolean deleteStore(int id) {
        String sql = "DELETE FROM stores WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id); // Gán mã ID cần xóa
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Danh sách cửa hàng mẫu (Mock Stores) tại 3 thành phố lớn: TP.HCM, Hà Nội, Đà Nẵng
     * Dùng hiển thị khi cơ sở dữ liệu chưa sẵn sàng
     * @return Danh sách 6 chi nhánh mẫu
     */
    public List<Store> getMockStores() {
        List<Store> list = new ArrayList<>();
        // Chi nhánh tại TP.HCM
        list.add(new Store(1, "HighFashion Flagship Sư Vạn Hạnh", "561 Sư Vạn Hạnh, Phường Hòa Hưng, Quận 10", "TP.HCM", "0901 234 567", "08:30 - 22:00", 10.7769, 106.6675, true));
        list.add(new Store(2, "HighFashion Nguyễn Trãi Store", "123 Nguyễn Trãi, Phường 2, Quận 5", "TP.HCM", "0902 345 678", "09:00 - 21:30", 10.7554, 106.6792, false));
        list.add(new Store(3, "HighFashion Lê Lợi Boutique", "456 Lê Lợi, Phường Bến Thành, Quận 1", "TP.HCM", "0903 456 789", "08:30 - 22:00", 10.7731, 106.6983, false));

        // Chi nhánh tại Hà Nội
        list.add(new Store(4, "HighFashion Hà Nội Tràng Tiền", "24 Tràng Tiền, Quận Hoàn Kiếm", "Hà Nội", "0904 567 890", "08:30 - 22:00", 21.0253, 105.8544, false));
        list.add(new Store(5, "HighFashion Cầu Giấy Premium", "180 Cầu Giấy, Phường Quan Hoa, Quận Cầu Giấy", "Hà Nội", "0905 678 901", "09:00 - 21:30", 21.0362, 105.7905, false));

        // Chi nhánh tại Đà Nẵng
        list.add(new Store(6, "HighFashion Đà Nẵng Nguyễn Văn Linh", "88 Nguyễn Văn Linh, Quận Hải Châu", "Đà Nẵng", "0906 789 012", "08:30 - 21:30", 16.0608, 108.2155, false));

        return list;
    }
}
