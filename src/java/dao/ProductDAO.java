package dao;

import context.DBContext;
import model.Product;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp ProductDAO: Đối tượng truy xuất dữ liệu sản phẩm (Data Access Object)
 * Chịu trách nhiệm thực hiện tất cả các thao tác với bảng `products` trong cơ sở dữ liệu:
 * - Lấy danh sách sản phẩm (tất cả, nổi bật, theo danh mục, tìm kiếm theo tên)
 * - Lấy thông tin chi tiết một sản phẩm theo ID
 * - Thêm mới, cập nhật, xóa sản phẩm (dành cho Admin)
 * - Cung cấp dữ liệu mẫu (Mock data) khi cơ sở dữ liệu chưa sẵn sàng hoặc gặp lỗi kết nối
 */
public class ProductDAO {

    /**
     * Lấy toàn bộ danh sách sản phẩm trong hệ thống kèm tên danh mục tương ứng
     * Sắp xếp theo ID giảm dần (sản phẩm mới nhất lên đầu)
     * @return Danh sách các sản phẩm (List<Product>)
     */
    public List<Product> getAllProducts() {
        // Khởi tạo danh sách kết quả rỗng
        List<Product> list = new ArrayList<>();
        // Câu lệnh SQL: Kết hợp bảng products (p) với categories (c) qua khóa ngoại category_id
        String sql = "SELECT p.*, c.name AS category_name FROM products p " +
                     "LEFT JOIN categories c ON p.category_id = c.id ORDER BY p.id DESC";
        
        // Quản lý kết nối và tài nguyên tự động bằng try-with-resources
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            // Duyệt từng dòng kết quả trả về từ database
            while (rs.next()) {
                Product p = mapProduct(rs); // Chuyển đổi dòng dữ liệu ResultSet thành đối tượng Product
                list.add(p);                 // Thêm vào danh sách trả về
            }
        } catch (Exception e) {
            // Ghi nhận lỗi và thông báo chuyển sang sử dụng dữ liệu mẫu dự phòng
            System.err.println("⚠️ DB Query failed/not connected, loading Mock Products: " + e.getMessage());
        }

        // Nếu danh sách rỗng (database chưa kết nối hoặc chưa có dữ liệu), nạp danh sách mẫu
        if (list.isEmpty()) {
            return getMockProducts();
        }
        return list;
    }

    /**
     * Lấy danh sách các sản phẩm nổi bật (is_featured = 1) để hiển thị ở trang chủ
     * @return Danh sách sản phẩm nổi bật
     */
    public List<Product> getFeaturedProducts() {
        List<Product> list = new ArrayList<>();
        // Câu lệnh SQL lọc các sản phẩm có cờ is_featured = 1
        String sql = "SELECT p.*, c.name AS category_name FROM products p " +
                     "LEFT JOIN categories c ON p.category_id = c.id WHERE p.is_featured = 1 ORDER BY p.id DESC";
        
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            // Đọc kết quả và ánh xạ vào danh sách
            while (rs.next()) {
                list.add(mapProduct(rs));
            }
        } catch (Exception e) {
            System.err.println("⚠️ DB Query failed: " + e.getMessage());
        }

        // Dự phòng: Nếu database không có, lọc từ danh sách mock data
        if (list.isEmpty()) {
            List<Product> featured = new ArrayList<>();
            for (Product p : getMockProducts()) {
                if (p.isIsFeatured()) featured.add(p); // Lọc sản phẩm có cờ isFeatured = true
            }
            return featured;
        }
        return list;
    }

    /**
     * Lấy danh sách sản phẩm thuộc một danh mục cụ thể
     * @param catId Mã danh mục (category_id)
     * @return Danh sách sản phẩm thuộc danh mục đó
     */
    public List<Product> getProductsByCategoryId(int catId) {
        List<Product> list = new ArrayList<>();
        // Câu lệnh SQL lọc theo category_id sử dụng tham số ?
        String sql = "SELECT p.*, c.name AS category_name FROM products p " +
                     "LEFT JOIN categories c ON p.category_id = c.id WHERE p.category_id = ? ORDER BY p.id DESC";
        
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            // Gán giá trị mã danh mục vào tham số thứ 1
            ps.setInt(1, catId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapProduct(rs)); // Thêm sản phẩm tìm thấy vào danh sách
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ DB Query failed: " + e.getMessage());
        }

        // Dự phòng: Nếu database rỗng, lọc từ dữ liệu mẫu
        if (list.isEmpty()) {
            List<Product> catList = new ArrayList<>();
            for (Product p : getMockProducts()) {
                if (p.getCategoryId() == catId) catList.add(p);
            }
            return catList.isEmpty() ? getMockProducts() : catList;
        }
        return list;
    }

    /**
     * Tìm kiếm sản phẩm theo từ khóa tên sản phẩm (không phân biệt chữ hoa/thường)
     * @param keyword Từ khóa tìm kiếm do người dùng nhập
     * @return Danh sách sản phẩm có tên chứa từ khóa
     */
    public List<Product> searchProductsByName(String keyword) {
        List<Product> list = new ArrayList<>();
        // Sử dụng toán tử LIKE để tìm kiếm tương đối trong SQL
        String sql = "SELECT p.*, c.name AS category_name FROM products p " +
                     "LEFT JOIN categories c ON p.category_id = c.id WHERE p.name LIKE ? ORDER BY p.id DESC";
        
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            // Đặt chuỗi tìm kiếm dạng %keyword%
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapProduct(rs));
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ DB Query failed: " + e.getMessage());
        }

        // Dự phòng: Tìm kiếm trên danh sách mẫu nếu kết nối database lỗi
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

    /**
     * Lấy chi tiết thông tin một sản phẩm theo ID
     * @param id Mã định danh sản phẩm cần tìm
     * @return Đối tượng Product tương ứng hoặc sản phẩm đầu tiên nếu không thấy
     */
    public Product getProductById(int id) {
        // Truy vấn sản phẩm theo ID kèm theo tên danh mục
        String sql = "SELECT p.*, c.name AS category_name FROM products p " +
                     "LEFT JOIN categories c ON p.category_id = c.id WHERE p.id = ?";
        
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            // Truyền giá trị ID vào câu lệnh SQL
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                // Nếu tìm thấy bản ghi, chuyển đổi thành Product và trả về ngay
                if (rs.next()) {
                    return mapProduct(rs);
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ DB Query failed: " + e.getMessage());
        }

        // Tìm trong dữ liệu mẫu nếu database không phản hồi
        for (Product p : getMockProducts()) {
            if (p.getId() == id) return p;
        }
        return getMockProducts().get(0);
    }

    /**
     * Thêm mới một sản phẩm vào cơ sở dữ liệu (Dành cho Quản trị viên)
     * @param p Đối tượng Product chứa thông tin cần thêm
     * @return true nếu thêm thành công (>0 dòng bị tác động), ngược lại false
     */
    public boolean insertProduct(Product p) {
        String sql = "INSERT INTO products (name, description, price, sale_price, quantity, image, size, color, category_id, is_featured) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            // Gán lần lượt các thuộc tính vào dấu hỏi chấm tương ứng
            ps.setString(1, p.getName());          // Tên sản phẩm
            ps.setString(2, p.getDescription());   // Mô tả chi tiết
            ps.setDouble(3, p.getPrice());         // Giá gốc
            ps.setDouble(4, p.getSalePrice());     // Giá khuyến mãi
            ps.setInt(5, p.getQuantity());         // Số lượng tồn kho
            ps.setString(6, p.getImage());         // Link ảnh đại diện
            ps.setString(7, p.getSize());          // Danh sách kích thước (size)
            ps.setString(8, p.getColor());         // Danh sách màu sắc
            ps.setInt(9, p.getCategoryId());       // Mã danh mục
            ps.setBoolean(10, p.isIsFeatured());   // Cờ nổi bật (true/false)
            return ps.executeUpdate() > 0;         // Trả về true nếu thực thi ghi vào database thành công
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Cập nhật thông tin của một sản phẩm đã có trong cơ sở dữ liệu
     * @param p Đối tượng Product mang thông tin mới nhất kèm ID cần cập nhật
     * @return true nếu cập nhật thành công, ngược lại false
     */
    public boolean updateProduct(Product p) {
        String sql = "UPDATE products SET name=?, description=?, price=?, sale_price=?, quantity=?, image=?, size=?, color=?, category_id=?, is_featured=? WHERE id=?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            // Gán các giá trị mới cần cập nhật
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
            ps.setInt(11, p.getId());              // ID sản phẩm cần sửa (điều kiện WHERE)
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Xóa sản phẩm khỏi cơ sở dữ liệu dựa trên mã ID
     * @param id Mã sản phẩm cần xóa
     * @return true nếu xóa thành công, ngược lại false
     */
    public boolean deleteProduct(int id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id); // Gán ID cần xóa
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Hàm phụ trợ ánh xạ (map) một dòng dữ liệu từ ResultSet sang đối tượng Product
     * @param rs Con trỏ ResultSet đang trỏ tới bản ghi hiện tại
     * @return Đối tượng Product đã được điền đầy đủ dữ liệu
     * @throws Exception Ngoại lệ nếu có lỗi khi đọc các cột SQL
     */
    private Product mapProduct(ResultSet rs) throws Exception {
        Product p = new Product();
        p.setId(rs.getInt("id"));                             // Mã định danh
        p.setName(rs.getString("name"));                      // Tên sản phẩm
        p.setDescription(rs.getString("description"));        // Mô tả chi tiết
        p.setPrice(rs.getDouble("price"));                    // Giá niêm yết
        p.setSalePrice(rs.getDouble("sale_price"));          // Giá ưu đãi
        p.setQuantity(rs.getInt("quantity"));                 // Số lượng trong kho
        p.setImage(rs.getString("image"));                    // URL ảnh đại diện
        p.setSize(rs.getString("size"));                      // Các kích thước
        p.setColor(rs.getString("color"));                    // Các màu sắc
        p.setCategoryId(rs.getInt("category_id"));            // Khóa ngoại danh mục
        p.setCategoryName(rs.getString("category_name"));     // Tên danh mục (lấy từ JOIN)
        p.setIsFeatured(rs.getBoolean("is_featured"));        // Cờ sản phẩm nổi bật
        p.setCreatedAt(rs.getTimestamp("created_at"));        // Thời gian tạo
        return p;
    }

    /**
     * Khởi tạo danh sách 24 sản phẩm mẫu chất lượng cao chia theo 4 danh mục
     * Dùng dự phòng khi cơ sở dữ liệu chưa tạo hoặc chưa kết nối
     * @return Danh sách sản phẩm mẫu đầy đủ thông tin
     */
    public List<Product> getMockProducts() {
        List<Product> list = new ArrayList<>();
        
        // Danh mục 1: Áo Nam (Chỉ sản phẩm 1 có giảm giá SALE, các sản phẩm còn lại salePrice = 0)
        list.add(new Product(1, "Áo Sơ Mi Nam Oxford Premium", "Áo sơ mi oxford cotton cao cấp thoáng mát, form dáng Slimfit trẻ trung.", 450000, 380000, 50, "https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=600", "35, 36, 37, 38, 39, 40, S, M, L, XL", "Trắng, Xanh Lam, Đen", 1, true));
        list.add(new Product(2, "Áo Thun Polo Nam Basic Cotton", "Áo thun polo cá sấu 100% cotton co giãn 4 chiều, thấm hút mồ hôi tốt.", 290000, 0, 80, "https://images.unsplash.com/photo-1625910513413-4ec2b1418f4a?w=600", "35, 36, 37, 38, M, L, XL, XXL", "Đen, Trắng, Xanh Navy, Ghi", 1, true));
        list.add(new Product(3, "Áo Khoác Denim Jacket Classic", "Áo khoác jeans bò cao cấp bụi bặm phong cách streetwear cá tính.", 650000, 0, 30, "https://images.unsplash.com/photo-1576995853123-5a10305d93c0?w=600", "38, 39, 40, M, L, XL", "Xanh Navy, Đen", 1, true));
        list.add(new Product(4, "Áo Leather Biker Jacket Nam", "Áo khoác da cừu cao cấp phong cách Biker thời thượng đẳng cấp.", 3200000, 0, 15, "https://images.unsplash.com/photo-1551028719-00167b16eac5?w=600", "S, M, L, XL", "Đen, Nâu", 1, true));
        list.add(new Product(5, "Áo Măng Tô Dạ Nam High-End", "Áo măng tô dạ dáng dài phong cách quý ông Châu Âu siêu ấm áp.", 4500000, 0, 10, "https://images.unsplash.com/photo-1544441893-675973e31985?w=600", "M, L, XL, XXL", "Be, Ghi, Đen", 1, false));
        list.add(new Product(6, "Áo Hoodie Oversize Unisex Streetwear", "Áo hoodie nỉ bông form rộng năng động trẻ trung cá tính.", 480000, 390000, 60, "https://images.unsplash.com/photo-1556905055-8f358a7a47b2?w=600", "XS, S, M, L, XL", "Cam, Xanh Lá, Hồng, Vàng", 1, true));

        // Danh mục 2: Quần Nam (Tất cả sản phẩm bán đúng giá gốc, salePrice = 0)
        list.add(new Product(7, "Quần Jeans Nam Slimfit Co Giãn", "Quần jeans nam chất liệu denim co giãn thoải mái, màu xanh trẻ trung.", 520000, 0, 45, "https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=600", "S, M, L, XL", "Xanh Lam, Đen", 2, true));
        list.add(new Product(8, "Quần Tây Nam Hàn Quốc Uniqlo", "Quần tây nam form ống đứng chuẩn phong cách Hàn Quốc chống nhăn.", 480000, 0, 60, "https://images.unsplash.com/photo-1479064555552-3ef4979f8908?w=600", "S, M, L, XL", "Đen, Xám, Kem", 2, false));
        list.add(new Product(9, "Quần Short Cargo Nam Pocket", "Quần short túi hộp cá tính chất liệu kaki dày dặn siêu bền.", 390000, 0, 50, "https://images.unsplash.com/photo-1591195853828-11db59a44f6b?w=600", "M, L, XL", "Be, Xanh Lá, Đen", 2, false));
        list.add(new Product(10, "Quần Jogger Nam Fleece Thể Thao", "Quần jogger thun nỉ co giãn đi chơi và tập luyện thể thao.", 490000, 0, 40, "https://images.unsplash.com/photo-1552902865-b72c031ac5ea?w=600", "S, M, L, XL", "Ghi, Đen, Xanh Navy", 2, false));
        list.add(new Product(11, "Quần Trouser Da Cừu Luxury", "Quần da cừu hàng hiệu may thủ công tinh tế dành cho doanh nhân.", 1950000, 0, 12, "https://images.unsplash.com/photo-1624378439575-d8705ad7ae80?w=600", "M, L, XL", "Đen, Nâu", 2, true));
        list.add(new Product(12, "Quần Vải Linen Nam Mùa Hè", "Quần đũi thoáng mát phong cách dạo biển lịch lãm.", 680000, 0, 35, "https://images.unsplash.com/photo-1507679799987-c73779587ccf?w=600", "S, M, L", "Trắng, Kem, Be", 2, false));

        // Danh mục 3: Thời Trang Nữ (Chỉ sản phẩm 13 có giảm giá SALE)
        list.add(new Product(13, "Đầm Xòe Nữ Vintage Dáng Dài", "Đầm nữ phong cách vintage nhẹ nhàng nữ tính, chất lụa satin mềm mịn.", 580000, 490000, 25, "https://images.unsplash.com/photo-1539109136881-3be0616acf4b?w=600", "XS, S, M, L", "Trắng, Hồng, Kem", 3, true));
        list.add(new Product(14, "Áo Blazer Nữ Form Rộng Style Hàn", "Áo blazer nữ thanh lịch hiện đại, phù hợp đi làm và dạo phố.", 720000, 0, 20, "https://images.unsplash.com/photo-1584273143981-41c073dfe8f8?w=600", "S, M, L", "Be, Ghi, Đen", 3, true));
        list.add(new Product(15, "Váy Dạ Hội Sparkle Sequins Elegant", "Váy dạ hội đính kim tuyến lấp lánh đi tiệc sang trọng kiêu sa.", 2800000, 0, 15, "https://images.unsplash.com/photo-1566174053879-31528523f8ae?w=600", "XS, S, M", "Đỏ, Vàng, Đen", 3, true));
        list.add(new Product(16, "Váy Lụa Tơ Tằm Haute Couture Nữ", "Thiết kế váy lụa tơ tằm may đo thủ công độc bản quyến rũ.", 5500000, 0, 8, "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=600", "S, M", "Đỏ, Trắng, Tím", 3, false));
        list.add(new Product(17, "Áo Croptop Nữ Dệt Kim Năng Động", "Áo croptop len dệt kim ôm body khoe đường cong quyến rũ.", 220000, 0, 70, "https://images.unsplash.com/photo-1503342217505-b0a15ec3261c?w=600", "XS, S, M", "Vàng, Cam, Xanh Lam", 3, false));
        list.add(new Product(18, "Set Trang Phục Vest Nữ Công Sở Luxe", "Set áo vest và quần tây nữ sang trọng chuẩn style doanh nhân thành đạt.", 2100000, 0, 18, "https://images.unsplash.com/photo-1487222477894-8943e31ef7b2?w=600", "S, M, L", "Trắng, Đen, Be", 3, true));

        // Danh mục 4: Phụ Kiện Thời Trang (Chỉ sản phẩm 19 có giảm giá SALE)
        list.add(new Product(19, "Túi Xách Da Nữ Crossbody Luxe", "Túi xách da PU cao cấp khóa mạ vàng sang trọng ngăn chứa rộng rãi.", 390000, 320000, 40, "https://images.unsplash.com/photo-1584917865442-de89df76afd3?w=600", "Freesize", "Đen, Nâu, Trắng", 4, true));
        list.add(new Product(20, "Thắt Lưng Da Bò Nam Mặt Kim Loại", "Thắt lưng da bò thật 100% khóa kim loại chống gỉ tinh tế.", 390000, 0, 50, "https://images.unsplash.com/photo-1624222247344-550fb60583dc?w=600", "One Size", "Đen, Nâu", 4, false));
        list.add(new Product(21, "Kính Mắt Thời Trang Unisex Anti-UV", "Kính mát gọng kim loại chống tia UV400 bảo vệ mắt tối đa.", 550000, 0, 60, "https://images.unsplash.com/photo-1511499767150-a48a237f0083?w=600", "Freesize", "Đen, Vàng, Vàng Vàng", 4, false));
        list.add(new Product(22, "Đồng Hồ Kim Nam Dây Da Thụy Sỹ", "Đồng hồ nam máy Quartz chống nước dây da bò cao cấp lịch lãm.", 3500000, 0, 15, "https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?w=600", "One Size", "Đen, Nâu, Vàng", 4, true));
        list.add(new Product(23, "Túi Du Lịch Canvas Unisex Vintage", "Túi xách du lịch cỡ lớn vải canvas phối da mộc bụi bặm.", 1250000, 0, 25, "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=600", "Freesize", "Xanh Lá, Be, Nâu", 4, false));
        list.add(new Product(24, "Bộ Trang Sức Vòng Cổ Bạc Đính Đá", "Bộ dây chuyền và bông tai bạc 925 đính đá pha sáng lấp lánh.", 7200000, 0, 10, "https://images.unsplash.com/photo-1599643478518-a784e5dc4c8f?w=600", "One Size", "Trắng, Tím, Vàng", 4, true));

        // Thiết lập tên danh mục tương ứng cho dữ liệu mẫu
        for (Product p : list) {
            if (p.getCategoryId() == 1) p.setCategoryName("Áo Nam");
            else if (p.getCategoryId() == 2) p.setCategoryName("Quần Nam");
            else if (p.getCategoryId() == 3) p.setCategoryName("Thời Trang Nữ");
            else if (p.getCategoryId() == 4) p.setCategoryName("Phụ Kiện");
        }

        return list;
    }
}
