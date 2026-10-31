package model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

/**
 * Lớp Order: Đại diện cho thực thể Đơn hàng trong hệ thống (tương ứng với bảng `orders` trong CSDL)
 * Chứa toàn bộ thông tin về đơn hàng: thông tin người nhận, địa chỉ giao, hình thức thanh toán,
 * trạng thái xử lý đơn, tổng tiền thanh toán và danh sách chi tiết các món hàng.
 */
public class Order implements Serializable {
    // Mã định danh đơn hàng (Khóa chính tự tăng trong CSDL)
    private int id;
    // Mã người dùng đặt hàng (User ID), có thể là null nếu khách vãng lai đặt không cần đăng nhập
    private Integer userId;
    // Họ và tên người nhận hàng
    private String customerName;
    // Địa chỉ email người nhận để gửi hóa đơn điện tử
    private String email;
    // Số điện thoại liên hệ giao hàng
    private String phone;
    // Địa chỉ chi tiết nhận hàng (số nhà, đường, phường/xã, quận/huyện, tỉnh/thành)
    private String address;
    // Ghi chú của khách hàng (ví dụ: giao giờ hành chính, gọi trước khi đến)
    private String note;
    // Tổng số tiền của toàn bộ đơn hàng (VNĐ)
    private double totalMoney;
    // Phương thức thanh toán (ví dụ: "COD" - Thanh toán khi nhận hàng, "VNPAY", "BANKING")
    private String paymentMethod;
    // Trạng thái đơn hàng: PENDING (Chờ duyệt), PROCESSING (Đang đóng gói), SHIPPED (Đang giao), DELIVERED (Đã giao), CANCELLED (Đã hủy)
    private String status;
    // Thời điểm khách hàng tạo đơn hàng
    private Timestamp createdAt;
    // Danh sách chi tiết các mặt hàng nằm trong đơn hàng này
    private List<OrderDetail> orderDetails;

    /**
     * Hàm khởi tạo mặc định (No-argument constructor)
     */
    public Order() {
    }

    /**
     * Hàm khởi tạo đầy đủ tham số cho đơn hàng
     * @param id Mã đơn hàng
     * @param userId Mã người dùng đặt (có thể null)
     * @param customerName Họ tên người nhận
     * @param email Email nhận thông báo
     * @param phone Số điện thoại nhận
     * @param address Địa chỉ giao hàng
     * @param note Ghi chú đơn hàng
     * @param totalMoney Tổng số tiền
     * @param paymentMethod Phương thức thanh toán
     * @param status Trạng thái xử lý
     * @param createdAt Thời gian tạo đơn
     */
    public Order(int id, Integer userId, String customerName, String email, String phone, String address, 
                 String note, double totalMoney, String paymentMethod, String status, Timestamp createdAt) {
        this.id = id;
        this.userId = userId;
        this.customerName = customerName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.note = note;
        this.totalMoney = totalMoney;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.createdAt = createdAt;
    }

    /**
     * Lấy mã đơn hàng
     * @return int
     */
    public int getId() {
        return id;
    }

    /**
     * Gán mã đơn hàng
     * @param id Mã đơn
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Lấy ID tài khoản người đặt hàng
     * @return Integer (hoặc null)
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     * Gán ID tài khoản người đặt
     * @param userId ID tài khoản
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    /**
     * Lấy họ tên người nhận
     * @return String
     */
    public String getCustomerName() {
        return customerName;
    }

    /**
     * Gán họ tên người nhận
     * @param customerName Tên người nhận
     */
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    /**
     * Lấy email khách hàng
     * @return String
     */
    public String getEmail() {
        return email;
    }

    /**
     * Gán email khách hàng
     * @param email Chuỗi email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Lấy số điện thoại người nhận
     * @return String
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Gán số điện thoại người nhận
     * @param phone Số điện thoại
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Lấy địa chỉ giao hàng
     * @return String
     */
    public String getAddress() {
        return address;
    }

    /**
     * Gán địa chỉ giao hàng
     * @param address Địa chỉ
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Lấy ghi chú đơn hàng
     * @return String
     */
    public String getNote() {
        return note;
    }

    /**
     * Gán ghi chú đơn hàng
     * @param note Chuỗi ghi chú
     */
    public void setNote(String note) {
        this.note = note;
    }

    /**
     * Lấy tổng tiền đơn hàng
     * @return double
     */
    public double getTotalMoney() {
        return totalMoney;
    }

    /**
     * Gán tổng tiền đơn hàng
     * @param totalMoney Số tiền
     */
    public void setTotalMoney(double totalMoney) {
        this.totalMoney = totalMoney;
    }

    /**
     * Lấy phương thức thanh toán
     * @return String (COD, VNPAY...)
     */
    public String getPaymentMethod() {
        return paymentMethod;
    }

    /**
     * Gán phương thức thanh toán
     * @param paymentMethod Phương thức
     */
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    /**
     * Lấy trạng thái đơn hàng
     * @return String (PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED)
     */
    public String getStatus() {
        return status;
    }

    /**
     * Gán trạng thái đơn hàng
     * @param status Trạng thái mới
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Lấy thời điểm tạo đơn hàng
     * @return Timestamp
     */
    public Timestamp getCreatedAt() {
        return createdAt;
    }

    /**
     * Gán thời điểm tạo đơn hàng
     * @param createdAt Thời gian
     */
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Lấy danh sách chi tiết các mặt hàng trong đơn
     * @return List<OrderDetail>
     */
    public List<OrderDetail> getOrderDetails() {
        return orderDetails;
    }

    /**
     * Gán danh sách chi tiết các mặt hàng vào đơn
     * @param orderDetails Danh sách chi tiết
     */
    public void setOrderDetails(List<OrderDetail> orderDetails) {
        this.orderDetails = orderDetails;
    }

    /**
     * Lấy định dạng tiền tệ Việt Nam (VNĐ)
     * @return Chuỗi số tiền đã format (ví dụ: 1.030.000 đ)
     */
    public String getFormattedTotalMoney() {
        return String.format("%,.0f đ", totalMoney).replace(',', '.');
    }

    /**
     * Lấy tổng số lượng sản phẩm có trong đơn hàng
     * @return Số lượng sản phẩm
     */
    public int getTotalItemCount() {
        if (orderDetails == null || orderDetails.isEmpty()) {
            return 1;
        }
        int total = 0;
        for (OrderDetail d : orderDetails) {
            total += d.getQuantity();
        }
        return total > 0 ? total : 1;
    }

    /**
     * Lấy văn bản trạng thái hiển thị bằng Tiếng Việt
     * @return Chuỗi trạng thái thân thiện với người dùng
     */
    public String getStatusVietnamese() {
        if (status == null) return "Chờ giao hàng";
        switch (status.toUpperCase()) {
            case "DELIVERED":
            case "COMPLETED":
                return "Hoàn thành";
            case "SHIPPING":
                return "Vận chuyển";
            case "PENDING":
                return "Chờ giao hàng";
            case "CONFIRMED":
                return "Chờ giao hàng";
            case "PROCESSING":
                return "Chờ giao hàng";
            case "UNPAID":
                return "Chờ thanh toán";
            case "CANCELLED":
                return "Đã hủy";
            case "REFUNDED":
                return "Trả hàng/Hoàn tiền";
            default:
                return status;
        }
    }

    /**
     * Lấy biểu tượng Bootstrap Icon tương ứng với trạng thái
     * @return Tên class icon Bootstrap
     */
    public String getStatusIcon() {
        if (status == null) return "bi-clock-history";
        switch (status.toUpperCase()) {
            case "DELIVERED":
            case "COMPLETED":
                return "bi-check-circle-fill";
            case "SHIPPING":
                return "bi-truck";
            case "PENDING":
            case "CONFIRMED":
            case "PROCESSING":
                return "bi-hourglass-split";
            case "UNPAID":
                return "bi-credit-card-2-front";
            case "CANCELLED":
                return "bi-x-circle-fill";
            case "REFUNDED":
                return "bi-arrow-counterclockwise";
            default:
                return "bi-info-circle";
        }
    }

    /**
     * Lấy class màu sắc CSS tương ứng cho Badge trạng thái
     * @return Class màu Bootstrap
     */
    public String getStatusBadgeClass() {
        if (status == null) return "bg-warning-subtle text-warning-emphasis";
        switch (status.toUpperCase()) {
            case "DELIVERED":
            case "COMPLETED":
                return "bg-success-subtle text-success border border-success-subtle";
            case "SHIPPING":
                return "bg-primary-subtle text-primary border border-primary-subtle";
            case "PENDING":
            case "CONFIRMED":
            case "PROCESSING":
                return "bg-warning-subtle text-warning-emphasis border border-warning-subtle";
            case "UNPAID":
                return "bg-info-subtle text-info-emphasis border border-info-subtle";
            case "CANCELLED":
                return "bg-danger-subtle text-danger border border-danger-subtle";
            case "REFUNDED":
                return "bg-secondary-subtle text-secondary border border-secondary-subtle";
            default:
                return "bg-light text-dark border";
        }
    }
}
