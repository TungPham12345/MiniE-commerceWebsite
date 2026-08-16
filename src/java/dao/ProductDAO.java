package dao;

import context.DBContext;
import model.Product;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT p.*, c.name AS category_name FROM products p " +
                     "LEFT JOIN categories c ON p.category_id = c.id ORDER BY p.id DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Product p = mapProduct(rs);
                list.add(p);
            }
        } catch (Exception e) {
            System.err.println("⚠️ DB Query failed/not connected, loading Mock Products: " + e.getMessage());
        }

        if (list.isEmpty()) {
            return getMockProducts();
        }
        return list;
    }

    public List<Product> getFeaturedProducts() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT p.*, c.name AS category_name FROM products p " +
                     "LEFT JOIN categories c ON p.category_id = c.id WHERE p.is_featured = 1 ORDER BY p.id DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapProduct(rs));
            }
        } catch (Exception e) {
            System.err.println("⚠️ DB Query failed: " + e.getMessage());
        }

        if (list.isEmpty()) {
            List<Product> featured = new ArrayList<>();
            for (Product p : getMockProducts()) {
                if (p.isIsFeatured()) featured.add(p);
            }
            return featured;
        }
        return list;
    }

    public List<Product> getProductsByCategoryId(int catId) {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT p.*, c.name AS category_name FROM products p " +
                     "LEFT JOIN categories c ON p.category_id = c.id WHERE p.category_id = ? ORDER BY p.id DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, catId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapProduct(rs));
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ DB Query failed: " + e.getMessage());
        }

        if (list.isEmpty()) {
            List<Product> catList = new ArrayList<>();
            for (Product p : getMockProducts()) {
                if (p.getCategoryId() == catId) catList.add(p);
            }
            return catList.isEmpty() ? getMockProducts() : catList;
        }
        return list;
    }

    public List<Product> searchProductsByName(String keyword) {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT p.*, c.name AS category_name FROM products p " +
                     "LEFT JOIN categories c ON p.category_id = c.id WHERE p.name LIKE ? ORDER BY p.id DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapProduct(rs));
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ DB Query failed: " + e.getMessage());
        }

        if (list.isEmpty()) {
            List<Product> matched = new ArrayList<>();
            for (Product p : getMockProducts()) {
                if (p.getName().toLowerCase().contains(keyword.toLowerCase())) {
                    matched.add(p);
                }
            }
            return matched.isEmpty() ? getMockProducts() : matched;
        }
        return list;
    }

    public Product getProductById(int id) {
        String sql = "SELECT p.*, c.name AS category_name FROM products p " +
                     "LEFT JOIN categories c ON p.category_id = c.id WHERE p.id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapProduct(rs);
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ DB Query failed: " + e.getMessage());
        }

        for (Product p : getMockProducts()) {
            if (p.getId() == id) return p;
        }
        return getMockProducts().get(0);
    }

    public boolean insertProduct(Product p) {
        String sql = "INSERT INTO products (name, description, price, sale_price, quantity, image, size, color, category_id, is_featured) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getDescription());
            ps.setDouble(3, p.getPrice());
            ps.setDouble(4, p.getSalePrice());
            ps.setInt(5, p.getQuantity());
            ps.setString(6, p.getImage());
            ps.setString(7, p.getSize());
            ps.setString(8, p.getColor());
            ps.setInt(9, p.getCategoryId());
            ps.setBoolean(10, p.isIsFeatured());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateProduct(Product p) {
        String sql = "UPDATE products SET name=?, description=?, price=?, sale_price=?, quantity=?, image=?, size=?, color=?, category_id=?, is_featured=? WHERE id=?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getDescription());
            ps.setDouble(3, p.getPrice());
            ps.setDouble(4, p.getSalePrice());
            ps.setInt(5, p.getQuantity());
            ps.setString(6, p.getImage());
            ps.setString(7, p.getSize());
            ps.setString(8, p.getColor());
            ps.setInt(9, p.getCategoryId());
            ps.setBoolean(10, p.isIsFeatured());
            ps.setInt(11, p.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteProduct(int id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private Product mapProduct(ResultSet rs) throws Exception {
        Product p = new Product();
        p.setId(rs.getInt("id"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getDouble("price"));
        p.setSalePrice(rs.getDouble("sale_price"));
        p.setQuantity(rs.getInt("quantity"));
        p.setImage(rs.getString("image"));
        p.setSize(rs.getString("size"));
        p.setColor(rs.getString("color"));
        p.setCategoryId(rs.getInt("category_id"));
        p.setCategoryName(rs.getString("category_name"));
        p.setIsFeatured(rs.getBoolean("is_featured"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        return p;
    }

    public List<Product> getMockProducts() {
        List<Product> list = new ArrayList<>();
        
        // Category 1: Áo Nam (Chỉ sản phẩm 1 có giảm giá SALE, các sản phẩm còn lại salePrice = 0)
        list.add(new Product(1, "Áo Sơ Mi Nam Oxford Premium", "Áo sơ mi oxford cotton cao cấp thoáng mát, form dáng Slimfit trẻ trung.", 450000, 380000, 50, "https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=600", "35, 36, 37, 38, 39, 40, S, M, L, XL", "Trắng, Xanh Lam, Đen", 1, true));
        list.add(new Product(2, "Áo Thun Polo Nam Basic Cotton", "Áo thun polo cá sấu 100% cotton co giãn 4 chiều, thấm hút mồ hôi tốt.", 290000, 0, 80, "https://images.unsplash.com/photo-1625910513413-4ec2b1418f4a?w=600", "35, 36, 37, 38, M, L, XL, XXL", "Đen, Trắng, Xanh Navy, Ghi", 1, true));
        list.add(new Product(3, "Áo Khoác Denim Jacket Classic", "Áo khoác jeans bò cao cấp bụi bặm phong cách streetwear cá tính.", 650000, 0, 30, "https://images.unsplash.com/photo-1576995853123-5a10305d93c0?w=600", "38, 39, 40, M, L, XL", "Xanh Navy, Đen", 1, true));
        list.add(new Product(4, "Áo Leather Biker Jacket Nam", "Áo khoác da cừu cao cấp phong cách Biker thời thượng đẳng cấp.", 3200000, 0, 15, "https://images.unsplash.com/photo-1551028719-00167b16eac5?w=600", "S, M, L, XL", "Đen, Nâu", 1, true));
        list.add(new Product(5, "Áo Măng Tô Dạ Nam High-End", "Áo măng tô dạ dáng dài phong cách quý ông Châu Âu siêu ấm áp.", 4500000, 0, 10, "https://images.unsplash.com/photo-1544441893-675973e31985?w=600", "M, L, XL, XXL", "Be, Ghi, Đen", 1, false));
        list.add(new Product(6, "Áo Hoodie Oversize Unisex Streetwear", "Áo hoodie nỉ bông form rộng năng động trẻ trung cá tính.", 480000, 390000, 60, "https://images.unsplash.com/photo-1556905055-8f358a7a47b2?w=600", "XS, S, M, L, XL", "Cam, Xanh Lá, Hồng, Vàng", 1, true));

        // Category 2: Quần Nam (Tất cả sản phẩm bán đúng giá gốc, salePrice = 0)
        list.add(new Product(7, "Quần Jeans Nam Slimfit Co Giãn", "Quần jeans nam chất liệu denim co giãn thoải mái, màu xanh trẻ trung.", 520000, 0, 45, "https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=600", "S, M, L, XL", "Xanh Lam, Đen", 2, true));
        list.add(new Product(8, "Quần Tây Nam Hàn Quốc Uniqlo", "Quần tây nam form ống đứng chuẩn phong cách Hàn Quốc chống nhăn.", 480000, 0, 60, "https://images.unsplash.com/photo-1479064555552-3ef4979f8908?w=600", "S, M, L, XL", "Đen, Xám, Kem", 2, false));
        list.add(new Product(9, "Quần Short Cargo Nam Pocket", "Quần short túi hộp cá tính chất liệu kaki dày dặn siêu bền.", 390000, 0, 50, "https://images.unsplash.com/photo-1591195853828-11db59a44f6b?w=600", "M, L, XL", "Be, Xanh Lá, Đen", 2, false));
        list.add(new Product(10, "Quần Jogger Nam Fleece Thể Thao", "Quần jogger thun nỉ co giãn đi chơi và tập luyện thể thao.", 490000, 0, 40, "https://images.unsplash.com/photo-1552902865-b72c031ac5ea?w=600", "S, M, L, XL", "Ghi, Đen, Xanh Navy", 2, false));
        list.add(new Product(11, "Quần Trouser Da Cừu Luxury", "Quần da cừu hàng hiệu may thủ công tinh tế dành cho doanh nhân.", 1950000, 0, 12, "https://images.unsplash.com/photo-1624378439575-d8705ad7ae80?w=600", "M, L, XL", "Đen, Nâu", 2, true));
        list.add(new Product(12, "Quần Vải Linen Nam Mùa Hè", "Quần đũi thoáng mát phong cách dạo biển lịch lãm.", 680000, 0, 35, "https://images.unsplash.com/photo-1507679799987-c73779587ccf?w=600", "S, M, L", "Trắng, Kem, Be", 2, false));

        // Category 3: Thời Trang Nữ (Chỉ sản phẩm 13 có giảm giá SALE)
        list.add(new Product(13, "Đầm Xòe Nữ Vintage Dáng Dài", "Đầm nữ phong cách vintage nhẹ nhàng nữ tính, chất lụa satin mềm mịn.", 580000, 490000, 25, "https://images.unsplash.com/photo-1539109136881-3be0616acf4b?w=600", "XS, S, M, L", "Trắng, Hồng, Kem", 3, true));
        list.add(new Product(14, "Áo Blazer Nữ Form Rộng Style Hàn", "Áo blazer nữ thanh lịch hiện đại, phù hợp đi làm và dạo phố.", 720000, 0, 20, "https://images.unsplash.com/photo-1584273143981-41c073dfe8f8?w=600", "S, M, L", "Be, Ghi, Đen", 3, true));
        list.add(new Product(15, "Váy Dạ Hội Sparkle Sequins Elegant", "Váy dạ hội đính kim tuyến lấp lánh đi tiệc sang trọng kiêu sa.", 2800000, 0, 15, "https://images.unsplash.com/photo-1566174053879-31528523f8ae?w=600", "XS, S, M", "Đỏ, Vàng, Đen", 3, true));
        list.add(new Product(16, "Váy Lụa Tơ Tằm Haute Couture Nữ", "Thiết kế váy lụa tơ tằm may đo thủ công độc bản quyến rũ.", 5500000, 0, 8, "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=600", "S, M", "Đỏ, Trắng, Tím", 3, false));
        list.add(new Product(17, "Áo Croptop Nữ Dệt Kim Năng Động", "Áo croptop len dệt kim ôm body khoe đường cong quyến rũ.", 220000, 0, 70, "https://images.unsplash.com/photo-1503342217505-b0a15ec3261c?w=600", "XS, S, M", "Vàng, Cam, Xanh Lam", 3, false));
        list.add(new Product(18, "Set Trang Phục Vest Nữ Công Sở Luxe", "Set áo vest và quần tây nữ sang trọng chuẩn style doanh nhân thành đạt.", 2100000, 0, 18, "https://images.unsplash.com/photo-1487222477894-8943e31ef7b2?w=600", "S, M, L", "Trắng, Đen, Be", 3, true));

        // Category 4: Phụ Kiện Thời Trang (Chỉ sản phẩm 19 có giảm giá SALE)
        list.add(new Product(19, "Túi Xách Da Nữ Crossbody Luxe", "Túi xách da PU cao cấp khóa mạ vàng sang trọng ngăn chứa rộng rãi.", 390000, 320000, 40, "https://images.unsplash.com/photo-1584917865442-de89df76afd3?w=600", "Freesize", "Đen, Nâu, Trắng", 4, true));
        list.add(new Product(20, "Thắt Lưng Da Bò Nam Mặt Kim Loại", "Thắt lưng da bò thật 100% khóa kim loại chống gỉ tinh tế.", 390000, 0, 50, "https://images.unsplash.com/photo-1624222247344-550fb60583dc?w=600", "One Size", "Đen, Nâu", 4, false));
        list.add(new Product(21, "Kính Mắt Thời Trang Unisex Anti-UV", "Kính mát gọng kim loại chống tia UV400 bảo vệ mắt tối đa.", 550000, 0, 60, "https://images.unsplash.com/photo-1511499767150-a48a237f0083?w=600", "Freesize", "Đen, Vàng, Vàng Vàng", 4, false));
        list.add(new Product(22, "Đồng Hồ Kim Nam Dây Da Thụy Sỹ", "Đồng hồ nam máy Quartz chống nước dây da bò cao cấp lịch lãm.", 3500000, 0, 15, "https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?w=600", "One Size", "Đen, Nâu, Vàng", 4, true));
        list.add(new Product(23, "Túi Du Lịch Canvas Unisex Vintage", "Túi xách du lịch cỡ lớn vải canvas phối da mộc bụi bặm.", 1250000, 0, 25, "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=600", "Freesize", "Xanh Lá, Be, Nâu", 4, false));
        list.add(new Product(24, "Bộ Trang Sức Vòng Cổ Bạc Đính Đá", "Bộ dây chuyền và bông tai bạc 925 đính đá pha sáng lấp lánh.", 7200000, 0, 10, "https://images.unsplash.com/photo-1599643478518-a784e5dc4c8f?w=600", "One Size", "Trắng, Tím, Vàng", 4, true));

        for (Product p : list) {
            if (p.getCategoryId() == 1) p.setCategoryName("Áo Nam");
            else if (p.getCategoryId() == 2) p.setCategoryName("Quần Nam");
            else if (p.getCategoryId() == 3) p.setCategoryName("Thời Trang Nữ");
            else if (p.getCategoryId() == 4) p.setCategoryName("Phụ Kiện");
        }

        return list;
    }
}
