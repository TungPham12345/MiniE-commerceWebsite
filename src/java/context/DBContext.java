// Khai báo package context chứa các lớp kết nối hạ tầng CSDL
package context;

// Import các lớp JDBC chuẩn của Java để quản lý kết nối CSDL
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
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

    private static boolean driverLoaded = false;

    private static synchronized void ensureDriverLoaded() {
        if (driverLoaded) return;
        try {
            // Nạp Driver JDBC của Microsoft SQL Server vào bộ nhớ máy ảo JVM
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            driverLoaded = true;
        } catch (ClassNotFoundException e) {
            // Trường hợp chạy đơn tệp hoặc môi trường terminal chưa truyền tham số -cp
            loadDriverFromLib();
        }
    }

    private static void loadDriverFromLib() {
        String[] possiblePaths = {
            "lib/mssql-jdbc-13.2.0.jre11.jar",
            "ban_hang/lib/mssql-jdbc-13.2.0.jre11.jar",
            "../ban_hang/lib/mssql-jdbc-13.2.0.jre11.jar",
            "d:/duan111/ban_hang/lib/mssql-jdbc-13.2.0.jre11.jar"
        };
        File foundJar = null;
        for (String path : possiblePaths) {
            File f = new File(path);
            if (f.exists()) {
                foundJar = f;
                break;
            }
        }
        if (foundJar != null) {
            try {
                URLClassLoader loader = new URLClassLoader(new URL[]{ foundJar.toURI().toURL() }, DBContext.class.getClassLoader());
                Class<?> driverClass = Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver", true, loader);
                final Driver driverInstance = (Driver) driverClass.getDeclaredConstructor().newInstance();
                DriverManager.registerDriver(new Driver() {
                    @Override
                    public Connection connect(String url, Properties info) throws SQLException {
                        return driverInstance.connect(url, info);
                    }
                    @Override
                    public boolean acceptsURL(String url) throws SQLException {
                        return driverInstance.acceptsURL(url);
                    }
                    @Override
                    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
                        return driverInstance.getPropertyInfo(url, info);
                    }
                    @Override
                    public int getMajorVersion() {
                        return driverInstance.getMajorVersion();
                    }
                    @Override
                    public int getMinorVersion() {
                        return driverInstance.getMinorVersion();
                    }
                    @Override
                    public boolean jdbcCompliant() {
                        return driverInstance.jdbcCompliant();
                    }
                    @Override
                    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
                        return driverInstance.getParentLogger();
                    }
                });
                driverLoaded = true;
            } catch (Exception ex) {
                System.err.println("⚠️ Không thể nạp driver tự động từ " + foundJar.getPath() + ": " + ex.getMessage());
            }
        }
    }

    /**
     * Phương thức getConnection: Tạo và mở một kết nối (Connection) tới SQL Server
     * @return Connection đối tượng kết nối CSDL
     * @throws Exception nếu kết nối thất bại hoặc sai thông tin đăng nhập
     */
    public static Connection getConnection() throws Exception {
        // Đảm bảo driver JDBC đã được nạp
        ensureDriverLoaded();

        // Xây dựng chuỗi kết nối JDBC URL chuẩn cho Microsoft SQL Server
        // encrypt=false: không bắt buộc SSL nội bộ; trustServerCertificate=true: tin cậy chứng chỉ tự cấp
        String url = "jdbc:sqlserver://" + SERVER_NAME + ":" + PORT_NUMBER 
                   + ";databaseName=" + DB_NAME 
                   + ";encrypt=false;trustServerCertificate=true;";

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
