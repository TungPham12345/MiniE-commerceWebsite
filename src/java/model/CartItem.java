package model;

import java.io.Serializable;

/**
 * Lớp CartItem: Đại diện cho một mặt hàng nằm trong Giỏ hàng của người dùng
 * Implement Serializable để có thể lưu trữ trong HttpSession một cách an toàn.
 * Bao gồm thông tin: Sản phẩm, số lượng, kích thước (size), màu sắc đã chọn và tính tổng giá trị mặt hàng.
 */
public class CartItem implements Serializable {
    // Đối tượng sản phẩm tương ứng trong giỏ hàng
    private Product product;
    // Số lượng sản phẩm khách muốn đặt
    private int quantity;
    // Kích thước (size) khách đã chọn (ví dụ: S, M, L, XL, 39, 40...)
    private String size;
    // Màu sắc khách đã chọn (ví dụ: Trắng, Đen, Xanh...)
    private String color;

    /**
     * Hàm khởi tạo không tham số (Default Constructor)
     */
    public CartItem() {
    }

    /**
     * Hàm khởi tạo có tham số đầy đủ
     * @param product Đối tượng sản phẩm
     * @param quantity Số lượng mua
     * @param size Kích thước đã chọn
     * @param color Màu sắc đã chọn
     */
    public CartItem(Product product, int quantity, String size, String color) {
        this.product = product;
        this.quantity = quantity;
        this.size = size;
        this.color = color;
    }

    /**
     * Lấy đối tượng sản phẩm trong giỏ hàng
     * @return Product
     */
    public Product getProduct() {
        return product;
    }

    /**
     * Gán đối tượng sản phẩm vào giỏ hàng
     * @param product Sản phẩm cần gán
     */
    public void setProduct(Product product) {
        this.product = product;
    }

    /**
     * Lấy số lượng sản phẩm
     * @return Số lượng nguyên
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Cập nhật số lượng sản phẩm
     * @param quantity Số lượng mới
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**
     * Lấy kích thước (size) đã chọn
     * @return Chuỗi tên size
     */
    public String getSize() {
        return size;
    }

    /**
     * Thiết lập kích thước (size)
     * @param size Chuỗi tên size
     */
    public void setSize(String size) {
        this.size = size;
    }

    /**
     * Lấy màu sắc đã chọn
     * @return Chuỗi tên màu
     */
    public String getColor() {
        return color;
    }

    /**
     * Thiết lập màu sắc
     * @param color Chuỗi tên màu
     */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     * Tính tổng thành tiền cho mặt hàng này (Đơn giá thực tế * Số lượng)
     * Đơn giá thực tế được lấy từ `product.getEffectivePrice()` (tự động ưu tiên giá khuyến mãi nếu có)
     * @return Tổng thành tiền dạng double
     */
    public double getTotalPrice() {
        if (product == null) return 0; // Nếu không có sản phẩm thì thành tiền bằng 0
        return product.getEffectivePrice() * quantity; // Tính tổng tiền
    }
}
