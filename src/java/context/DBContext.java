// Khai báo package context chứa các lớp kết nối hạ tầng CSDL
package context;

// Import các lớp JDBC chuẩn của Java để quản lý kết nối CSDL
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DBContext: Lớp quản lý kết nối Cơ sở dữ liệu Microsoft SQL Server
 * Cung cấp phương thức tĩnh getConnection() để các lớp DAO dùng chung
 */
public class DBContext {

    // Địa chỉ máy chủ CSDL (localhost: máy tính cục bộ)
    private static final String SERVER_NAME = "localhost";
    // Cổng dịch vụ mặc định của Microsoft SQL Server là 1433
    private static final String PORT_NUMBER = "1433";
    // Tên Cơ sở dữ liệu chính của dự án
    private static final String DB_NAME = "ban_hang_db";
    // Tên tài khoản quản trị hệ quản trị CSDL SQL Server (mặc định là 'sa')
    private static final String USER_NAME = "sa";
    // Mật khẩu tài khoản 'sa' (có thể thay đổi tương ứng với cấu hình máy bạn)
    private static final String PASSWORD = "123";

    /**
     * Phương thức getConnection: Tạo và mở một kết nối (Connection) tới SQL Server
     * @return Connection đối tượng kết nối CSDL
     * @throws Exception nếu kết nối thất bại hoặc sai thông tin đăng nhập
     */
    public static Connection getConnection() throws Exception {
        // Xây dựng chuỗi kết nối JDBC URL chuẩn cho Microsoft SQL Server
        // encrypt=false: không bắt buộc SSL nội bộ; trustServerCertificate=true: tin cậy chứng chỉ tự cấp
        String url = "jdbc:sqlserver://" + SERVER_NAME + ":" + PORT_NUMBER 
                   + ";databaseName=" + DB_NAME 
                   + ";encrypt=false;trustServerCertificate=true;";

        // Nạp Driver JDBC của Microsoft SQL Server vào bộ nhớ máy ảo JVM
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

        // Gọi DriverManager để thiết lập và trả về kết nối thực tế
        return DriverManager.getConnection(url, USER_NAME, PASSWORD);
    }

    /**
     * Hàm main: Dùng để chạy thử nghiệm độc lập kiểm tra kết nối CSDL
     */
    public static void main(String[] args) {
        try (Connection con = getConnection()) {
            if (con != null) {
                System.out.println("✅ KẾT NỐI DATABASE SQL SERVER THÀNH CÔNG!");
            }
        } catch (Exception ex) {
            System.err.println("❌ LỖI KẾT NỐI DATABASE: " + ex.getMessage());
            Logger.getLogger(DBContext.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
