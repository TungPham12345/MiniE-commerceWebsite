package model;

import java.io.Serializable;

/**
 * Lớp OrderDetail: Đại diện cho thực thể Chi tiết đơn hàng (bảng `order_details` trong CSDL)
 * Lưu trữ thông tin từng món hàng cụ thể thuộc về một đơn hàng:
 * - Mã đơn hàng liên kết (order_id)
 * - Sản phẩm mua, kích cỡ (size), màu sắc (color), số lượng mua, đơn giá và thành tiền
 */
public class OrderDetail implements Serializable {
    // Mã định danh dòng chi tiết đơn hàng (Khóa chính)
    private int id;
    // Mã đơn hàng cha mà chi tiết này thuộc về (Khóa ngoại trỏ đến bảng orders)
    private int orderId;
    // Mã sản phẩm được mua (Khóa ngoại trỏ đến bảng products)
    private int productId;
    // Tên sản phẩm tại thời điểm mua (dùng hiển thị tiện lợi)
    private String productName;
    // Link ảnh đại diện sản phẩm (dùng hiển thị danh sách đơn)
    private String productImage;
    // Đơn giá bán tại thời điểm đặt hàng
    private double price;
    // Số lượng sản phẩm khách đặt
    private int quantity;
    // Kích thước (size) của sản phẩm đã chọn
    private String size;
    // Màu sắc của sản phẩm đã chọn
    private String color;
    // Tổng thành tiền của món hàng này (price * quantity)
    private double totalMoney;

    /**
     * Hàm khởi tạo không tham số mặc định
     */
    public OrderDetail() {
    }

    /**
     * Hàm khởi tạo có đầy đủ tham số
     * @param id Mã chi tiết đơn
     * @param orderId Mã đơn hàng cha
     * @param productId Mã sản phẩm
     * @param price Đơn giá
     * @param quantity Số lượng mua
     * @param size Kích cỡ đã chọn
     * @param color Màu sắc đã chọn
     * @param totalMoney Tổng thành tiền của món này
     */
    public OrderDetail(int id, int orderId, int productId, double price, int quantity, String size, String color, double totalMoney) {
        this.id = id;
        this.orderId = orderId;
        this.productId = productId;
        this.price = price;
        this.quantity = quantity;
        this.size = size;
        this.color = color;
        this.totalMoney = totalMoney;
    }

    /**
     * Lấy mã chi tiết đơn hàng
     * @return int
     */
    public int getId() {
        return id;
    }

    /**
     * Gán mã chi tiết đơn hàng
     * @param id Mã chi tiết
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Lấy mã đơn hàng cha
     * @return int
     */
    public int getOrderId() {
        return orderId;
    }

    /**
     * Gán mã đơn hàng cha
     * @param orderId Mã đơn
     */
    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    /**
     * Lấy mã sản phẩm
     * @return int
     */
    public int getProductId() {
        return productId;
    }

    /**
     * Gán mã sản phẩm
     * @param productId Mã sản phẩm
     */
    public void setProductId(int productId) {
        this.productId = productId;
    }

    /**
     * Lấy tên sản phẩm
     * @return String
     */
    public String getProductName() {
        return productName;
    }

    /**
     * Gán tên sản phẩm
     * @param productName Tên sản phẩm
     */
    public void setProductName(String productName) {
        this.productName = productName;
    }

    /**
     * Lấy đường dẫn ảnh sản phẩm
     * @return String URL
     */
    public String getProductImage() {
        return productImage;
    }

    /**
     * Gán đường dẫn ảnh sản phẩm
     * @param productImage URL ảnh
     */
    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    /**
     * Lấy đơn giá bán
     * @return double
     */
    public double getPrice() {
        return price;
    }

    /**
     * Gán đơn giá bán
     * @param price Đơn giá
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * Lấy số lượng đặt mua
     * @return int
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Gán số lượng đặt mua
     * @param quantity Số lượng
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**
     * Lấy kích thước (size)
     * @return String
     */
    public String getSize() {
        return size;
    }

    /**
     * Gán kích thước (size)
     * @param size Kích cỡ
     */
    public void setSize(String size) {
        this.size = size;
    }

    /**
     * Lấy màu sắc sản phẩm
     * @return String
     */
    public String getColor() {
        return color;
    }

    /**
     * Gán màu sắc sản phẩm
     * @param color Tên màu
     */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     * Lấy tổng thành tiền
     * @return double
     */
    public double getTotalMoney() {
        return totalMoney;
    }

    /**
     * Gán tổng thành tiền
     * @param totalMoney Thành tiền
     */
    public void setTotalMoney(double totalMoney) {
        this.totalMoney = totalMoney;
    }
}
