package dao;

import context.DBContext;
import model.Category;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp CategoryDAO: Quản lý các thao tác truy vấn dữ liệu cho bảng Danh mục sản phẩm (categories)
 * Bao gồm: Lấy tất cả danh mục, tìm danh mục theo ID, và dữ liệu mẫu dự phòng (mock data).
 */
public class CategoryDAO {

    /**
     * Lấy danh sách toàn bộ danh mục từ cơ sở dữ liệu
     * @return Danh sách các đối tượng Category
     */
    public List<Category> getAllCategories() {
        // Khởi tạo danh sách rỗng để chứa các danh mục
        List<Category> list = new ArrayList<>();
        // Câu truy vấn SQL: Lấy toàn bộ danh mục sắp xếp tăng dần theo ID
        String sql = "SELECT * FROM categories ORDER BY id ASC";
        
        // Sử dụng try-with-resources để tự động đóng Connection, PreparedStatement và ResultSet
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            // Duyệt từng dòng kết quả trả về từ cơ sở dữ liệu
            while (rs.next()) {
                // Tạo đối tượng Category từ dữ liệu các cột trong bảng categories
                Category c = new Category(
                        rs.getInt("id"),             // Lấy mã danh mục
                        rs.getString("name"),        // Lấy tên danh mục
                        rs.getString("description"), // Lấy mô tả chi tiết
                        rs.getString("icon")         // Lấy tên class icon hiển thị
                );
                // Thêm danh mục vừa tạo vào danh sách kết quả
                list.add(c);
            }
        } catch (Exception e) {
            // Ghi log thông báo lỗi nếu truy vấn cơ sở dữ liệu thất bại
            System.err.println("⚠️ Category Query failed: " + e.getMessage());
        }

        // Nếu cơ sở dữ liệu chưa có dữ liệu hoặc kết nối lỗi, trả về danh mục mẫu dự phòng
        if (list.isEmpty()) {
            return getMockCategories();
        }
        // Trả về danh sách danh mục lấy được từ cơ sở dữ liệu
        return list;
    }

    /**
     * Tìm kiếm một danh mục theo mã ID cụ thể
     * @param id Mã danh mục cần tìm
     * @return Đối tượng Category tìm thấy hoặc danh mục mặc định nếu không thấy
     */
    public Category getCategoryById(int id) {
        // Câu truy vấn SQL: Tìm danh mục theo tham số id
        String sql = "SELECT * FROM categories WHERE id = ?";
        
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            // Gán giá trị id vào dấu ? thứ 1 trong câu truy vấn
            ps.setInt(1, id);
            
            // Thực thi truy vấn lấy kết quả
            try (ResultSet rs = ps.executeQuery()) {
                // Nếu tìm thấy bản ghi tương ứng
                if (rs.next()) {
                    return new Category(
                            rs.getInt("id"),             // Lấy mã danh mục
                            rs.getString("name"),        // Lấy tên danh mục
                            rs.getString("description"), // Lấy mô tả danh mục
                            rs.getString("icon")         // Lấy icon danh mục
                    );
                }
            }
        } catch (Exception e) {
            // In thông báo lỗi ra console nếu câu lệnh thất bại
            System.err.println("⚠️ Category Query failed: " + e.getMessage());
        }

        // Tìm kiếm trong danh mục mẫu nếu database không có hoặc xảy ra lỗi
        for (Category c : getMockCategories()) {
            if (c.getId() == id) return c; // Trả về danh mục mẫu trùng khớp ID
        }
        // Trường hợp không tìm thấy, trả về danh mục đầu tiên trong danh mục mẫu
        return getMockCategories().get(0);
    }

    /**
     * Tạo dữ liệu mẫu các danh mục sản phẩm (dự phòng khi cơ sở dữ liệu chưa kết nối)
     * @return Danh sách các danh mục mẫu thời trang
     */
    public List<Category> getMockCategories() {
        List<Category> list = new ArrayList<>();
        // Danh mục 1: Áo Nam
        list.add(new Category(1, "Áo Nam", "Áo thun, áo sơ mi, áo khoác nam thời trang", "bi-person-badge"));
        // Danh mục 2: Quần Nam
        list.add(new Category(2, "Quần Nam", "Quần jeans, quần tây, quần short nam", "bi-gender-male"));
        // Danh mục 3: Thời Trang Nữ
        list.add(new Category(3, "Thời Trang Nữ", "Váy, đầm, áo kiểu, quần nữ cá tính", "bi-person-heart"));
        // Danh mục 4: Phụ Kiện Thời Trang
        list.add(new Category(4, "Phụ Kiện Thời Trang", "Mũ, thắt lưng, túi xách, ví da, đồng hồ", "bi-gem"));
        return list; // Trả về danh sách 4 danh mục thời trang mặc định
    }
}
