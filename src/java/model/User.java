package model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Lớp User: Đại diện cho thực thể Tài khoản người dùng trong hệ thống (bảng `users` trong CSDL)
 * Phân quyền hệ thống:
 * - role = 0: Khách hàng (Customer) - có thể xem hàng, thêm giỏ hàng, đặt hàng, xem lịch sử đơn
 * - role = 1: Quản trị viên (Admin) - có toàn quyền quản trị sản phẩm, đơn hàng, cửa hàng, thống kê doanh thu
 */
public class User implements Serializable {
    // Mã định danh người dùng (Khóa chính tự tăng trong CSDL)
    private int id;
    // Tên đăng nhập (Username duy nhất)
    private String username;
    // Mật khẩu tài khoản (đã băm hoặc chuỗi mật khẩu)
    private String password;
    // Họ và tên đầy đủ của người dùng
    private String fullname;
    // Địa chỉ email (duy nhất trong hệ thống)
    private String email;
    // Số điện thoại liên hệ
    private String phone;
    // Địa chỉ thường trú hoặc địa chỉ nhận hàng mặc định
    private String address;
    // Vai trò tài khoản: 0 = Khách hàng (Customer), 1 = Quản trị viên (Admin)
    private int role;
    // Thời điểm đăng ký tài khoản
    private Timestamp createdAt;

    /**
     * Hàm khởi tạo không tham số mặc định
     */
    public User() {
    }

    /**
     * Hàm khởi tạo có tham số tiện ích
     * @param id Mã tài khoản
     * @param username Tên đăng nhập
     * @param password Mật khẩu
     * @param fullname Họ tên đầy đủ
     * @param email Email liên hệ
     * @param phone Số điện thoại
     * @param address Địa chỉ
     * @param role Vai trò phân quyền (0 hoặc 1)
     */
    public User(int id, String username, String password, String fullname, String email, String phone, String address, int role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullname = fullname;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.role = role;
    }

    /**
     * Lấy mã định danh tài khoản
     * @return int
     */
    public int getId() {
        return id;
    }

    /**
     * Gán mã tài khoản
     * @param id Mã tài khoản
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Lấy tên đăng nhập
     * @return String
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gán tên đăng nhập
     * @param username Tên đăng nhập
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Lấy mật khẩu
     * @return String
     */
    public String getPassword() {
        return password;
    }

    /**
     * Gán mật khẩu
     * @param password Mật khẩu mới
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Lấy họ tên đầy đủ
     * @return String
     */
    public String getFullname() {
        return fullname;
    }

    /**
     * Gán họ tên đầy đủ
     * @param fullname Họ tên mới
     */
    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    /**
     * Lấy email người dùng
     * @return String
     */
    public String getEmail() {
        return email;
    }

    /**
     * Gán email người dùng
     * @param email Chuỗi email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Lấy số điện thoại
     * @return String
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Gán số điện thoại
     * @param phone Chuỗi số điện thoại
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Lấy địa chỉ người dùng
     * @return String
     */
    public String getAddress() {
        return address;
    }

    /**
     * Gán địa chỉ người dùng
     * @param address Địa chỉ mới
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Lấy giá trị vai trò (0: Customer, 1: Admin)
     * @return int
     */
    public int getRole() {
        return role;
    }

    /**
     * Gán giá trị vai trò
     * @param role 0 hoặc 1
     */
    public void setRole(int role) {
        this.role = role;
    }

    /**
     * Kiểm tra người dùng có phải là Quản trị viên (Admin) hay không
     * @return true nếu role == 1, ngược lại false
     */
    public boolean isAdmin() {
        return this.role == 1;
    }

    /**
     * Lấy thời điểm tạo tài khoản
     * @return Timestamp
     */
    public Timestamp getCreatedAt() {
        return createdAt;
    }

    /**
     * Gán thời điểm tạo tài khoản
     * @param createdAt Thời gian
     */
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
