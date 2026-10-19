package model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Lớp Product: Đại diện cho thực thể Sản phẩm trong cửa hàng trực tuyến (bảng `products` trong CSDL)
 * Chứa đầy đủ các thuộc tính của một sản phẩm thời trang: tên, mô tả, giá gốc, giá sale,
 * số lượng kho, hình ảnh, kích thước, màu sắc, danh mục và cờ nổi bật.
 */
public class Product implements Serializable {
    // Mã sản phẩm (Khóa chính tự tăng trong CSDL)
    private int id;
    // Tên đầy đủ của sản phẩm thời trang
    private String name;
    // Mô tả chi tiết về sản phẩm, chất liệu, hướng dẫn sử dụng
    private String description;
    // Giá niêm yết ban đầu (VNĐ)
    private double price;
    // Giá bán khuyến mãi (nếu có sale, ngược lại bằng 0)
    private double salePrice;
    // Số lượng tồn kho hiện tại
    private int quantity;
    // Đường dẫn ảnh đại diện (URL ảnh Unsplash hoặc ảnh cục bộ)
    private String image;
    // Danh sách kích cỡ có sẵn (ví dụ: "S, M, L, XL" hoặc "39, 40, 41")
    private String size;
    // Danh sách màu sắc có sẵn (ví dụ: "Trắng, Đen, Xanh")
    private String color;
    // Mã danh mục (Khóa ngoại trỏ đến bảng categories)
    private int categoryId;
    // Tên danh mục (lấy từ phép JOIN với bảng categories để tiện hiển thị trên UI)
    private String categoryName;
    // Cờ đánh dấu sản phẩm nổi bật (true: hiển thị ở mục Hot / Nổi bật ở trang chủ)
    private boolean isFeatured;
    // Thời điểm sản phẩm được tạo / thêm vào hệ thống
    private Timestamp createdAt;

    /**
     * Hàm khởi tạo không tham số mặc định
     */
    public Product() {
    }

    /**
     * Hàm khởi tạo có tham số tiện ích
     * @param id Mã sản phẩm
     * @param name Tên sản phẩm
     * @param description Mô tả
     * @param price Giá gốc
     * @param salePrice Giá khuyến mãi
     * @param quantity Số lượng tồn
     * @param image Link ảnh
     * @param size Chuỗi kích thước
     * @param color Chuỗi màu sắc
     * @param categoryId Mã danh mục
     * @param isFeatured Cờ nổi bật
     */
    public Product(int id, String name, String description, double price, double salePrice, int quantity, 
                   String image, String size, String color, int categoryId, boolean isFeatured) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.salePrice = salePrice;
        this.quantity = quantity;
        this.image = image;
        this.size = size;
        this.color = color;
        this.categoryId = categoryId;
        this.isFeatured = isFeatured;
    }

    /**
     * Lấy mã sản phẩm
     * @return int
     */
    public int getId() {
        return id;
    }

    /**
     * Gán mã sản phẩm
     * @param id Mã sản phẩm
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Lấy tên sản phẩm
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Gán tên sản phẩm
     * @param name Tên mới
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Lấy mô tả sản phẩm
     * @return String
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gán mô tả sản phẩm
     * @param description Nội dung mô tả
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Lấy giá niêm yết gốc của sản phẩm
     * @return double
     */
    public double getPrice() {
        return price;
    }

    /**
     * Gán giá niêm yết gốc
     * @param price Số tiền
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * Lấy giá khuyến mãi của sản phẩm (nếu bằng 0 tức không có khuyến mãi)
     * @return double
     */
    public double getSalePrice() {
        return salePrice;
    }

    /**
     * Gán giá khuyến mãi cho sản phẩm
     * @param salePrice Số tiền khuyến mãi
     */
    public void setSalePrice(double salePrice) {
        this.salePrice = salePrice;
    }

    /**
     * Lấy giá bán thực tế tại thời điểm hiện tại:
     * Nếu có giá khuyến mãi (salePrice > 0) thì áp dụng giá khuyến mãi, ngược lại áp dụng giá gốc (price)
     * @return Giá bán thực tế dạng double
     */
    public double getEffectivePrice() {
        return (salePrice > 0) ? salePrice : price;
    }

    /**
     * Lấy số lượng hàng còn trong kho
     * @return int
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Gán số lượng hàng trong kho
     * @param quantity Số lượng mới
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**
     * Lấy đường dẫn ảnh sản phẩm
     * @return String URL
     */
    public String getImage() {
        return image;
    }

    /**
     * Gán đường dẫn ảnh sản phẩm
     * @param image Link ảnh
     */
    public void setImage(String image) {
        this.image = image;
    }

    /**
     * Lấy chuỗi danh sách kích thước (size)
     * @return String
     */
    public String getSize() {
        return size;
    }

    /**
     * Gán chuỗi kích thước
     * @param size Chuỗi kích thước phân cách bởi dấu phẩy
     */
    public void setSize(String size) {
        this.size = size;
    }

    /**
     * Lấy chuỗi danh sách màu sắc
     * @return String
     */
    public String getColor() {
        return color;
    }

    /**
     * Gán chuỗi danh sách màu sắc
     * @param color Chuỗi màu sắc
     */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     * Lấy mã danh mục
     * @return int
     */
    public int getCategoryId() {
        return categoryId;
    }

    /**
     * Gán mã danh mục
     * @param categoryId Mã danh mục
     */
    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    /**
     * Lấy tên danh mục
     * @return String
     */
    public String getCategoryName() {
        return categoryName;
    }

    /**
     * Gán tên danh mục
     * @param categoryName Tên danh mục
     */
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    /**
     * Kiểm tra sản phẩm có phải sản phẩm nổi bật không
     * @return true nếu nổi bật, ngược lại false
     */
    public boolean isIsFeatured() {
        return isFeatured;
    }

    /**
     * Thiết lập cờ nổi bật cho sản phẩm
     * @param isFeatured true hoặc false
     */
    public void setIsFeatured(boolean isFeatured) {
        this.isFeatured = isFeatured;
    }

    /**
     * Lấy thời điểm tạo sản phẩm
     * @return Timestamp
     */
    public Timestamp getCreatedAt() {
        return createdAt;
    }

    /**
     * Gán thời điểm tạo sản phẩm
     * @param createdAt Thời gian
     */
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
