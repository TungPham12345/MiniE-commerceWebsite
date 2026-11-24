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

    public enum AccountMutationResult {
        SUCCESS,
        NOT_FOUND,
        LAST_ADMIN,
        ERROR
    }

    public enum ProfileMutationResult {
        SUCCESS,
        INVALID_PASSWORD,
        ERROR
    }

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
        mockUsers.clear();
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

                            if (role != 0 && role != 1) continue;
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
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("⚠️ Không thể đọc file tài khoản lưu trữ: " + e.getMessage());
            }
        }
        if (mockUsers.isEmpty()) {
            mockUsers.add(new User(1, "admin", "123456", "Quản Trị Viên HighFashion", "admin@fashion.com", "0988776655", "Hà Nội, Việt Nam", 1));
            mockUsers.add(new User(2, "khachhang", "123456", "Nguyễn Văn An", "an.nguyen@gmail.com", "0912345678", "123 Nguyễn Trãi, Quận 5, TP.HCM", 0));
            mockUsers.add(new User(1004, "khoshop", "123456", "Nhân Viên Kho Shop", "kho@fashion.com", "0901234001", "Tổng kho HighFashion Tân Bình, TP.HCM", 2));
            mockUsers.add(new User(1005, "vanchuyen", "123456", "Điều Phối ĐVVC Express", "logistics@express.vn", "0901234002", "Bưu cục Trung tâm Vận Chuyển Express", 3));
            mockUsers.add(new User(1006, "buucuc", "123456", "Thủ Kho Bưu Cục Phát (Hub Q5)", "hubq5@express.vn", "0901234003", "Bưu cục phát Quận 5, TP.HCM", 4));
            mockUsers.add(new User(1007, "giaohang", "123456", "Shipper Nguyễn Văn Giao", "shipper@express.vn", "0901234004", "Đội giao hàng nhanh Quận 5", 5));
        }
    }

    /**
     * Ghi toàn bộ danh sách tài khoản vào file cục bộ để duy trì dữ liệu qua các lần khởi động lại máy chủ
     */
    private static boolean saveMockUsers() {
        File file = getStorageFile();
        if (file == null) return false;
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            System.err.println("⚠️ Không thể tạo thư mục lưu tài khoản: " + parent.getPath());
            return false;
        }
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
            return true;
        } catch (Exception e) {
            System.err.println("⚠️ Không thể lưu file tài khoản: " + e.getMessage());
            return false;
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

        Connection connection;
        try {
            connection = DBContext.getConnection();
        } catch (Exception e) {
            System.err.println("⚠️ Không thể kết nối CSDL khi đăng nhập, dùng tài khoản bộ nhớ: " + e.getMessage());
            return loginFromMockUsers(trimmedUsername, trimmedPassword);
        }

        // Nếu tài khoản có trong CSDL, mật khẩu trong CSDL là nguồn xác thực duy nhất.
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = connection;
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trimmedUsername);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    return trimmedPassword.equals(storedPassword) ? mapUser(rs) : null;
                }
            }
            return loginFromMockUsers(trimmedUsername, trimmedPassword);
        } catch (Exception e) {
            System.err.println("⚠️ Lỗi truy vấn tài khoản khi đăng nhập: " + e.getMessage());
            return null;
        }
    }

    private User loginFromMockUsers(String username, String password) {
        synchronized (mockUsers) {
            for (User u : mockUsers) {
                if (u.getUsername().equalsIgnoreCase(username) && u.getPassword().equals(password)) {
                    return u;
                }
            }
        }
        return null;
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
            int newId = 1;
            for (User existing : mockUsers) {
                newId = Math.max(newId, existing.getId() + 1);
            }
            user.setId(newId);
            mockUsers.add(user);
            saveMockUsers(); // Lưu ra file để duy trì lâu dài
        }
        return true;
    }

    public List<User> getAllUsers() {
        Connection connection;
        try {
            connection = DBContext.getConnection();
        } catch (Exception e) {
            System.err.println("⚠️ Không thể kết nối CSDL khi tải tài khoản, dùng dữ liệu dự phòng: " + e.getMessage());
            return getMockUsers();
        }

        String sql = "SELECT id, username, fullname, email, phone, address, role FROM users ORDER BY id DESC";
        List<User> users = new ArrayList<>();
        try (Connection conn = connection;
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setFullname(rs.getString("fullname"));
                user.setEmail(rs.getString("email"));
                user.setPhone(rs.getString("phone"));
                user.setAddress(rs.getString("address"));
                user.setRole(rs.getInt("role"));
                users.add(user);
            }
            return users;
        } catch (Exception e) {
            throw new IllegalStateException("Không thể đọc danh sách tài khoản từ CSDL.", e);
        }
    }

    public User getAccountById(int userId) {
        Connection connection;
        try {
            connection = DBContext.getConnection();
        } catch (Exception e) {
            System.err.println("⚠️ Không thể kết nối CSDL khi tải hồ sơ, dùng dữ liệu dự phòng: " + e.getMessage());
            synchronized (mockUsers) {
                int index = findMockUserIndex(userId);
                return index < 0 ? null : copyUser(mockUsers.get(index), false);
            }
        }

        String sql = "SELECT id, username, fullname, email, phone, address, role FROM users WHERE id = ?";
        try (Connection conn = connection;
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setFullname(rs.getString("fullname"));
                user.setEmail(rs.getString("email"));
                user.setPhone(rs.getString("phone"));
                user.setAddress(rs.getString("address"));
                user.setRole(rs.getInt("role"));
                return user;
            }
        } catch (Exception e) {
            throw new IllegalStateException("Không thể đọc thông tin tài khoản từ CSDL.", e);
        }
    }

    public User findAccountByEmail(String email) {
        if (email == null || email.trim().isEmpty()) return null;
        String normalizedEmail = email.trim();

        Connection connection;
        try {
            connection = DBContext.getConnection();
        } catch (Exception e) {
            System.err.println("⚠️ Không thể kết nối CSDL khi tìm tài khoản theo email, dùng dữ liệu dự phòng: " + e.getMessage());
            User matchedUser = null;
            synchronized (mockUsers) {
                for (User user : mockUsers) {
                    if (user.getEmail() != null && user.getEmail().equalsIgnoreCase(normalizedEmail)) {
                        if (matchedUser != null) return null;
                        matchedUser = copyUser(user, false);
                    }
                }
            }
            return matchedUser;
        }

        String sql = "SELECT TOP 2 id, username, fullname, email, phone, address, role "
                + "FROM users WHERE LOWER(email) = LOWER(?) ORDER BY id";
        try (Connection conn = connection;
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalizedEmail);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setFullname(rs.getString("fullname"));
                user.setEmail(rs.getString("email"));
                user.setPhone(rs.getString("phone"));
                user.setAddress(rs.getString("address"));
                user.setRole(rs.getInt("role"));
                return rs.next() ? null : user;
            }
        } catch (Exception e) {
            throw new IllegalStateException("Không thể tìm tài khoản theo email trong CSDL.", e);
        }
    }

    public ProfileMutationResult resetPassword(int userId, String newPassword) {
        if (newPassword == null) return ProfileMutationResult.ERROR;

        Connection connection;
        try {
            connection = DBContext.getConnection();
        } catch (Exception e) {
            System.err.println("⚠️ Không thể kết nối CSDL khi đặt lại mật khẩu, dùng dữ liệu dự phòng: " + e.getMessage());
            return resetMockPassword(userId, newPassword);
        }

        try (Connection conn = connection;
             PreparedStatement ps = conn.prepareStatement("UPDATE users SET password = ? WHERE id = ?")) {
            ps.setString(1, newPassword);
            ps.setInt(2, userId);
            return ps.executeUpdate() == 1 ? ProfileMutationResult.SUCCESS : ProfileMutationResult.ERROR;
        } catch (Exception e) {
            System.err.println("⚠️ Không thể đặt lại mật khẩu tài khoản: " + e.getMessage());
            return ProfileMutationResult.ERROR;
        }
    }

    private ProfileMutationResult resetMockPassword(int userId, String newPassword) {
        synchronized (mockUsers) {
            int index = findMockUserIndex(userId);
            if (index < 0) return ProfileMutationResult.ERROR;
            User existing = mockUsers.get(index);
            String originalPassword = existing.getPassword();
            existing.setPassword(newPassword);
            if (!saveMockUsers()) {
                existing.setPassword(originalPassword);
                return ProfileMutationResult.ERROR;
            }
            return ProfileMutationResult.SUCCESS;
        }
    }

    public ProfileMutationResult updateProfile(User profile) {
        if (profile == null) return ProfileMutationResult.ERROR;

        Connection connection;
        try {
            connection = DBContext.getConnection();
        } catch (Exception e) {
            System.err.println("⚠️ Không thể kết nối CSDL khi cập nhật hồ sơ, dùng dữ liệu dự phòng: " + e.getMessage());
            return updateMockProfile(profile);
        }

        String sql = "UPDATE users SET fullname = ?, email = ?, phone = ?, address = ? WHERE id = ?";
        try (Connection conn = connection;
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, profile.getFullname());
            ps.setString(2, profile.getEmail());
            ps.setString(3, profile.getPhone());
            ps.setString(4, profile.getAddress());
            ps.setInt(5, profile.getId());
            return ps.executeUpdate() == 1 ? ProfileMutationResult.SUCCESS : ProfileMutationResult.ERROR;
        } catch (Exception e) {
            System.err.println("⚠️ Không thể cập nhật hồ sơ tài khoản: " + e.getMessage());
            return ProfileMutationResult.ERROR;
        }
    }

    public ProfileMutationResult changePassword(int userId, String currentPassword, String newPassword) {
        if (currentPassword == null || newPassword == null) return ProfileMutationResult.ERROR;

        Connection connection;
        try {
            connection = DBContext.getConnection();
        } catch (Exception e) {
            System.err.println("⚠️ Không thể kết nối CSDL khi đổi mật khẩu, dùng dữ liệu dự phòng: " + e.getMessage());
            return changeMockPassword(userId, currentPassword, newPassword);
        }

        String sql = "UPDATE users SET password = ? WHERE id = ? AND password = ?";
        try (Connection conn = connection;
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setInt(2, userId);
            ps.setString(3, currentPassword);
            return ps.executeUpdate() == 1
                    ? ProfileMutationResult.SUCCESS
                    : ProfileMutationResult.INVALID_PASSWORD;
        } catch (Exception e) {
            System.err.println("⚠️ Không thể đổi mật khẩu tài khoản: " + e.getMessage());
            return ProfileMutationResult.ERROR;
        }
    }

    private ProfileMutationResult updateMockProfile(User profile) {
        synchronized (mockUsers) {
            int index = findMockUserIndex(profile.getId());
            if (index < 0) return ProfileMutationResult.ERROR;
            User existing = mockUsers.get(index);
            User original = copyUser(existing, true);
            existing.setFullname(profile.getFullname());
            existing.setEmail(profile.getEmail());
            existing.setPhone(profile.getPhone());
            existing.setAddress(profile.getAddress());
            if (!saveMockUsers()) {
                mockUsers.set(index, original);
                return ProfileMutationResult.ERROR;
            }
            return ProfileMutationResult.SUCCESS;
        }
    }

    private ProfileMutationResult changeMockPassword(int userId, String currentPassword, String newPassword) {
        synchronized (mockUsers) {
            int index = findMockUserIndex(userId);
            if (index < 0 || !currentPassword.equals(mockUsers.get(index).getPassword())) {
                return ProfileMutationResult.INVALID_PASSWORD;
            }
            User existing = mockUsers.get(index);
            String originalPassword = existing.getPassword();
            existing.setPassword(newPassword);
            if (!saveMockUsers()) {
                existing.setPassword(originalPassword);
                return ProfileMutationResult.ERROR;
            }
            return ProfileMutationResult.SUCCESS;
        }
    }

    public AccountMutationResult updateManagedAccount(User updatedUser, String newPassword) {
        if (updatedUser == null || (updatedUser.getRole() != 0 && updatedUser.getRole() != 1)) {
            return AccountMutationResult.ERROR;
        }

        Connection connection;
        try {
            connection = DBContext.getConnection();
        } catch (Exception e) {
            System.err.println("⚠️ Không thể kết nối CSDL khi cập nhật tài khoản, dùng dữ liệu dự phòng: " + e.getMessage());
            return updateMockAccount(updatedUser, newPassword);
        }

        try (Connection conn = connection) {
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            conn.setAutoCommit(false);
            try {
                int currentRole = getAccountRoleForUpdate(conn, updatedUser.getId());
                if (currentRole < 0) {
                    conn.rollback();
                    return AccountMutationResult.NOT_FOUND;
                }
                if (currentRole == 1 && updatedUser.getRole() != 1 && countAdministrators(conn) <= 1) {
                    conn.rollback();
                    return AccountMutationResult.LAST_ADMIN;
                }

                String sql = newPassword == null
                        ? "UPDATE users SET fullname = ?, email = ?, phone = ?, address = ?, role = ? WHERE id = ?"
                        : "UPDATE users SET fullname = ?, email = ?, phone = ?, address = ?, role = ?, password = ? WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, updatedUser.getFullname());
                    ps.setString(2, updatedUser.getEmail());
                    ps.setString(3, updatedUser.getPhone());
                    ps.setString(4, updatedUser.getAddress());
                    ps.setInt(5, updatedUser.getRole());
                    if (newPassword == null) {
                        ps.setInt(6, updatedUser.getId());
                    } else {
                        ps.setString(6, newPassword);
                        ps.setInt(7, updatedUser.getId());
                    }
                    if (ps.executeUpdate() != 1) {
                        conn.rollback();
                        return AccountMutationResult.NOT_FOUND;
                    }
                }
                conn.commit();
                return AccountMutationResult.SUCCESS;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (Exception e) {
            System.err.println("⚠️ Không thể cập nhật tài khoản: " + e.getMessage());
            return AccountMutationResult.ERROR;
        }
    }

    public AccountMutationResult deleteAccount(int userId) {
        Connection connection;
        try {
            connection = DBContext.getConnection();
        } catch (Exception e) {
            System.err.println("⚠️ Không thể kết nối CSDL khi xóa tài khoản, dùng dữ liệu dự phòng: " + e.getMessage());
            return deleteMockAccount(userId);
        }

        try (Connection conn = connection) {
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            conn.setAutoCommit(false);
            try {
                int currentRole = getAccountRoleForUpdate(conn, userId);
                if (currentRole < 0) {
                    conn.rollback();
                    return AccountMutationResult.NOT_FOUND;
                }
                if (currentRole == 1 && countAdministrators(conn) <= 1) {
                    conn.rollback();
                    return AccountMutationResult.LAST_ADMIN;
                }

                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
                    ps.setInt(1, userId);
                    if (ps.executeUpdate() != 1) {
                        conn.rollback();
                        return AccountMutationResult.NOT_FOUND;
                    }
                }
                conn.commit();
                return AccountMutationResult.SUCCESS;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (Exception e) {
            System.err.println("⚠️ Không thể xóa tài khoản: " + e.getMessage());
            return AccountMutationResult.ERROR;
        }
    }

    private int getAccountRoleForUpdate(Connection connection, int userId) throws Exception {
        String sql = "SELECT role FROM users WITH (UPDLOCK, HOLDLOCK) WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("role") : -1;
            }
        }
    }

    private int countAdministrators(Connection connection) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT COUNT(*) FROM users WITH (UPDLOCK, HOLDLOCK) WHERE role = 1");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private List<User> getMockUsers() {
        List<User> users = new ArrayList<>();
        synchronized (mockUsers) {
            for (User user : mockUsers) {
                users.add(copyUser(user, false));
            }
        }
        users.sort((first, second) -> Integer.compare(second.getId(), first.getId()));
        return users;
    }

    private AccountMutationResult updateMockAccount(User updatedUser, String newPassword) {
        synchronized (mockUsers) {
            int index = findMockUserIndex(updatedUser.getId());
            if (index < 0) return AccountMutationResult.NOT_FOUND;

            User existing = mockUsers.get(index);
            if (existing.getRole() == 1 && updatedUser.getRole() != 1 && countMockAdministrators() <= 1) {
                return AccountMutationResult.LAST_ADMIN;
            }

            User original = copyUser(existing, true);
            existing.setFullname(updatedUser.getFullname());
            existing.setEmail(updatedUser.getEmail());
            existing.setPhone(updatedUser.getPhone());
            existing.setAddress(updatedUser.getAddress());
            existing.setRole(updatedUser.getRole());
            if (newPassword != null) existing.setPassword(newPassword);
            if (!saveMockUsers()) {
                mockUsers.set(index, original);
                return AccountMutationResult.ERROR;
            }
            return AccountMutationResult.SUCCESS;
        }
    }

    private AccountMutationResult deleteMockAccount(int userId) {
        synchronized (mockUsers) {
            int index = findMockUserIndex(userId);
            if (index < 0) return AccountMutationResult.NOT_FOUND;
            User removed = mockUsers.get(index);
            if (removed.getRole() == 1 && countMockAdministrators() <= 1) {
                return AccountMutationResult.LAST_ADMIN;
            }
            mockUsers.remove(index);
            if (!saveMockUsers()) {
                mockUsers.add(index, removed);
                return AccountMutationResult.ERROR;
            }
            return AccountMutationResult.SUCCESS;
        }
    }

    private int findMockUserIndex(int userId) {
        for (int index = 0; index < mockUsers.size(); index++) {
            if (mockUsers.get(index).getId() == userId) return index;
        }
        return -1;
    }

    private int countMockAdministrators() {
        int count = 0;
        for (User user : mockUsers) {
            if (user.isAdmin()) count++;
        }
        return count;
    }

    private User copyUser(User source, boolean includePassword) {
        User copy = new User();
        copy.setId(source.getId());
        copy.setUsername(source.getUsername());
        copy.setPassword(includePassword ? source.getPassword() : null);
        copy.setFullname(source.getFullname());
        copy.setEmail(source.getEmail());
        copy.setPhone(source.getPhone());
        copy.setAddress(source.getAddress());
        copy.setRole(source.getRole());
        copy.setCreatedAt(source.getCreatedAt());
        return copy;
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
