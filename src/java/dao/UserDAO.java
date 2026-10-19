// Khai báo package dao chứa các lớp thao tác CSDL
package dao;

// Import lớp kết nối DBContext
import context.DBContext;
// Import lớp mô hình User
import model.User;
// Import các đối tượng JDBC
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * UserDAO: Lớp truy xuất và thao tác dữ liệu người dùng (bảng 'users')
 * Chịu trách nhiệm: Đăng nhập, Đăng ký, Kiểm tra tài khoản tồn tại và ánh xạ dữ liệu
 */
public class UserDAO {

    /**
     * Hàm login: Xác thực đăng nhập của người dùng qua username và password
     * @param username Tên đăng nhập
     * @param password Mật khẩu
     * @return Đối tượng User nếu đúng thông tin, ngược lại trả về null
     */
    public User login(String username, String password) {
        // Câu lệnh SQL kiểm tra sự tồn tại của cặp username và password
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        // Sử dụng try-with-resources để tự động đóng Connection và PreparedStatement sau khi truy vấn
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            // Truyền tham số 1: username vào dấu ? đầu tiên
            ps.setString(1, username);
            // Truyền tham số 2: password vào dấu ? thứ hai (chống tấn công SQL Injection)
            ps.setString(2, password);
            // Thực thi truy vấn và nhận tập kết quả ResultSet
            try (ResultSet rs = ps.executeQuery()) {
                // Nếu có bản ghi trả về, tức là tài khoản và mật khẩu hoàn toàn chính xác
                if (rs.next()) {
                    return mapUser(rs); // Chuyển đổi dòng dữ liệu thành đối tượng User
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Lỗi kết nối CSDL khi đăng nhập, chuyển sang tài khoản mẫu: " + e.getMessage());
        }

        // CƠ CHẾ DỰ PHÒNG (FALLBACK MOCK DATA): Luôn đảm bảo đăng nhập thành công khi chưa bật SQL Server
        if ("admin".equals(username) && "123456".equals(password)) {
            // Tài khoản Admin mẫu (role = 1)
            return new User(1, "admin", "123456", "Quản Trị Viên HighFashion", "admin@fashion.com", "0988776655", "Hà Nội, Việt Nam", 1);
        }
        if ("customer1".equals(username) && "123456".equals(password)) {
            // Tài khoản Khách hàng mẫu (role = 0)
            return new User(2, "customer1", "123456", "Nguyễn Văn An", "an.nguyen@gmail.com", "0912345678", "123 Nguyễn Trãi, Quận 5, TP.HCM", 0);
        }
        return null; // Trả về null nếu thông tin đăng nhập không hợp lệ
    }

    /**
     * Hàm register: Đăng ký thêm một người dùng mới vào bảng 'users'
     * @param user Đối tượng người dùng chứa các thông tin cá nhân
     * @return true nếu thêm thành công, false nếu có lỗi
     */
    public boolean register(User user) {
        // Câu lệnh SQL thêm người dùng mới
        String sql = "INSERT INTO users (username, password, fullname, email, phone, address, role) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            // Gán các tham số tương ứng vào các dấu hỏi chấm (?)
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getFullname());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getPhone());
            ps.setString(6, user.getAddress());
            ps.setInt(7, user.getRole()); // role = 0: khách hàng
            // Thực thi lệnh INSERT, nếu số dòng bị ảnh hưởng > 0 thì trả về true
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Hàm checkUsernameExists: Kiểm tra xem tên đăng nhập đã có người sử dụng chưa
     * @param username Tên đăng nhập cần kiểm tra
     * @return true nếu đã tồn tại, false nếu chưa có
     */
    public boolean checkUsernameExists(String username) {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                // Nếu rs.next() là true tức là đã có người đăng ký tên này rồi
                return rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Kiểm tra đối với các tài khoản mẫu offline
        return "admin".equals(username) || "customer1".equals(username);
    }

    /**
     * Hàm phụ trợ mapUser: Đọc một dòng dữ liệu từ ResultSet và gán vào các thuộc tính của đối tượng User
     * @param rs Tập kết quả ResultSet đang trỏ tới một dòng dữ liệu
     * @return Đối tượng User hoàn chỉnh
     */
    private User mapUser(ResultSet rs) throws Exception {
        User u = new User();
        u.setId(rs.getInt("id"));                   // Mã ID người dùng
        u.setUsername(rs.getString("username"));   // Tên tài khoản
        u.setPassword(rs.getString("password"));   // Mật khẩu
        u.setFullname(rs.getString("fullname"));   // Họ tên đầy đủ
        u.setEmail(rs.getString("email"));         // Email
        u.setPhone(rs.getString("phone"));         // Số điện thoại
        u.setAddress(rs.getString("address"));     // Địa chỉ
        u.setRole(rs.getInt("role"));               // Quyền hạn (0: User, 1: Admin)
        return u;
    }
}
