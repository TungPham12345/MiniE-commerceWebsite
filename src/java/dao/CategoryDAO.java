package dao;

import context.DBContext;
import model.Category;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    public List<Category> getAllCategories() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT * FROM categories ORDER BY id ASC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Category c = new Category(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("icon")
                );
                list.add(c);
            }
        } catch (Exception e) {
            System.err.println("⚠️ Category Query failed: " + e.getMessage());
        }

        if (list.isEmpty()) {
            return getMockCategories();
        }
        return list;
    }

    public Category getCategoryById(int id) {
        String sql = "SELECT * FROM categories WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Category(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getString("icon")
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Category Query failed: " + e.getMessage());
        }

        for (Category c : getMockCategories()) {
            if (c.getId() == id) return c;
        }
        return getMockCategories().get(0);
    }

    public List<Category> getMockCategories() {
        List<Category> list = new ArrayList<>();
        list.add(new Category(1, "Áo Nam", "Áo thun, áo sơ mi, áo khoác nam thời trang", "bi-person-badge"));
        list.add(new Category(2, "Quần Nam", "Quần jeans, quần tây, quần short nam", "bi-gender-male"));
        list.add(new Category(3, "Thời Trang Nữ", "Váy, đầm, áo kiểu, quần nữ cá tính", "bi-person-heart"));
        list.add(new Category(4, "Phụ Kiện Thời Trang", "Mũ, thắt lưng, túi xách, ví da, đồng hồ", "bi-gem"));
        return list;
    }
}
