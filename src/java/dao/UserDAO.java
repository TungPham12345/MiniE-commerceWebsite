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
// Import các lớp hỗ trợ đọc ghi file lưu trữ bộ nhớ dự phòng
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * UserDAO: Lớp truy xuất và thao tác dữ liệu người dùng (bảng 'users')
 * Chịu trách nhiệm: Đăng nhập, Đăng ký, Kiểm tra tài khoản tồn tại và ánh xạ dữ liệu
 * Hỗ trợ Cơ chế lưu trữ dự phòng (Mock Fallback Storage) khi CSDL chưa kết nối, đảm bảo đăng ký và đăng nhập hoạt động trơn tru.
 */
public class UserDAO {

    // Danh sách tài khoản người dùng trong bộ nhớ tạm (Thread-safe)
    private static final List<User> mockUsers = Collections.synchronizedList(new ArrayList<>());

    // Khối static: Nạp danh sách tài khoản ngay khi lớp UserDAO được tải vào JVM
    static {
        loadMockUsers();
    }

    /**
     * Nạp danh sách tài khoản mẫu từ file lưu trữ cục bộ hoặc danh sách mặc định
     */
    private static void loadMockUsers() {
        // Xóa trắng danh sách trước khi nạp
        mockUsers.clear();
        // 1. Thêm 2 tài khoản mẫu cốt lõi của hệ thống
        mockUsers.add(new User(1, "admin", "123456", "Quản Trị Viên HighFashion", "admin@fashion.com", "0988776655", "Hà Nội, Việt Nam", 1));
        mockUsers.add(new User(2, "customer1", "123456", "Nguyễn Văn An", "an.nguyen@gmail.com", "0912345678", "123 Nguyễn Trãi, Quận 5, TP.HCM", 0));

        // 2. Đọc thêm các tài khoản đã được người dùng đăng ký từ file lưu trữ
        File file = getStorageFile();
        if (file != null && file.exists()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    // Phân tách các trường theo dấu gạch đứng |
                    String[] parts = line.split("\\|", -1);
                    if (parts.length >= 8) {
                        try {
                            int id = Integer.parseInt(parts[0].trim());
                            String username = parts[1].trim();
                            String password = parts[2].trim();
                            String fullname = parts[3].trim();
                            String email = parts[4].trim();
                            String phone = parts[5].trim();
                            String address = parts[6].trim();
                            int role = Integer.parseInt(parts[7].trim());

                            // Kiểm tra tránh thêm trùng tên đăng nhập đã có
                            boolean exists = false;
                            for (User u : mockUsers) {
                                if (u.getUsername().equalsIgnoreCase(username)) {
                                    exists = true;
                                    break;
                                }
                            }
                            if (!exists) {
                                mockUsers.add(new User(id, username, password, fullname, email, phone, address, role));
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("⚠️ Không thể đọc file tài khoản lưu trữ: " + e.getMessage());
            }
        }
    }

    /**
     * Ghi toàn bộ danh sách tài khoản vào file cục bộ để duy trì dữ liệu qua các lần khởi động lại máy chủ
     */
    private static void saveMockUsers() {
        File file = getStorageFile();
        if (file == null) return;
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            synchronized (mockUsers) {
                for (User u : mockUsers) {
                    writer.write(u.getId() + "|" + 
                                 u.getUsername() + "|" + 
                                 u.getPassword() + "|" + 
                                 u.getFullname() + "|" + 
                                 u.getEmail() + "|" + 
                                 u.getPhone() + "|" + 
                                 u.getAddress() + "|" + 
                                 u.getRole());
                    writer.newLine();
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Không thể lưu file tài khoản: " + e.getMessage());
        }
    }

    /**
     * Xác định vị trí file lưu trữ tài khoản người dùng
     * @return File đối tượng trỏ tới file mock_users.txt
     */
    private static File getStorageFile() {
        String[] candidatePaths = {
            "c:/duan123/mock_users.txt",
            "mock_users.txt",
            "../mock_users.txt"
        };
        for (String path : candidatePaths) {
            try {
                File f = new File(path);
                if (f.exists()) return f;
            } catch (Exception ignored) {}
        }
        return new File("c:/duan123/mock_users.txt");
    }

    /**
     * Hàm login: Xác thực đăng nhập của người dùng qua username và password
     * @param username Tên đăng nhập
     * @param password Mật khẩu
     * @return Đối tượng User nếu đúng thông tin, ngược lại trả về null
     */
    public User login(String username, String password) {
        // Kiểm tra đầu vào hợp lệ
        if (username == null || password == null) return null;
        String trimmedUsername = username.trim();
        String trimmedPassword = password.trim();

        // 1. Thử xác thực với Cơ sở dữ liệu SQL Server nếu có kết nối
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            // Truyền tham số 1: username vào dấu ? đầu tiên
            ps.setString(1, trimmedUsername);
            // Truyền tham số 2: password vào dấu ? thứ hai (chống tấn công SQL Injection)
            ps.setString(2, trimmedPassword);
            // Thực thi truy vấn và nhận tập kết quả ResultSet
            try (ResultSet rs = ps.executeQuery()) {
                // Nếu có bản ghi trả về, tức là tài khoản và mật khẩu hoàn toàn chính xác
                if (rs.next()) {
                    return mapUser(rs); // Chuyển đổi dòng dữ liệu thành đối tượng User
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Lỗi kết nối CSDL khi đăng nhập, chuyển sang tài khoản bộ nhớ: " + e.getMessage());
        }

        // 2. CƠ CHẾ DỰ PHÒNG (FALLBACK MOCK DATA): Tìm kiếm trong danh sách mockUsers
        synchronized (mockUsers) {
            for (User u : mockUsers) {
                // So sánh tên đăng nhập (không phân biệt chữ hoa/thường) và mật khẩu chính xác
                if (u.getUsername().equalsIgnoreCase(trimmedUsername) && u.getPassword().equals(trimmedPassword)) {
                    return u;
                }
            }
        }
        return null; // Trả về null nếu thông tin đăng nhập không hợp lệ
    }

    /**
     * Hàm register: Đăng ký thêm một người dùng mới vào hệ thống
     * @param user Đối tượng người dùng chứa các thông tin cá nhân
     * @return true nếu thêm thành công, false nếu có lỗi hoặc tài khoản đã tồn tại
     */
    public boolean register(User user) {
        // Kiểm tra đầu vào hợp lệ
        if (user == null || user.getUsername() == null || user.getPassword() == null) {
            return false;
        }

        // Chuẩn hóa tên đăng nhập và mật khẩu
        String cleanUsername = user.getUsername().trim();
        user.setUsername(cleanUsername);

        // Kiểm tra xem tên đăng nhập đã được ai đăng ký trước đó chưa
        if (checkUsernameExists(cleanUsername)) {
            return false;
        }

        // 1. Thử thêm người dùng mới vào Cơ sở dữ liệu SQL Server nếu đang mở
        String sql = "INSERT INTO users (username, password, fullname, email, phone, address, role) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getFullname());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getPhone());
            ps.setString(6, user.getAddress());
            ps.setInt(7, user.getRole()); // role = 0: khách hàng
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("⚠️ Lỗi kết nối CSDL khi đăng ký, lưu vào bộ nhớ dự phòng: " + e.getMessage());
        }

        // 2. Luôn đồng bộ và lưu tài khoản mới vào danh sách mockUsers để người dùng đăng nhập được ngay
        synchronized (mockUsers) {
            int newId = mockUsers.size() + 1;
            user.setId(newId);
            mockUsers.add(user);
            saveMockUsers(); // Lưu ra file để duy trì lâu dài
        }
        return true;
    }

    /**
     * Hàm checkUsernameExists: Kiểm tra xem tên đăng nhập đã có người sử dụng chưa
     * @param username Tên đăng nhập cần kiểm tra
     * @return true nếu đã tồn tại, false nếu chưa có
     */
    public boolean checkUsernameExists(String username) {
        if (username == null) return false;
        String trimmed = username.trim();

        // 1. Kiểm tra trong Cơ sở dữ liệu SQL Server
        String sql = "SELECT id FROM users WHERE username = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trimmed);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return true;
            }
        } catch (Exception ignored) {
        }

        // 2. Kiểm tra trong danh sách mockUsers
        synchronized (mockUsers) {
            for (User u : mockUsers) {
                if (u.getUsername().equalsIgnoreCase(trimmed)) {
                    return true;
                }
            }
        }
        return false;
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
