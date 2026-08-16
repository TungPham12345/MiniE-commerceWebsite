package context;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DBContext - Ket noi Co so du lieu SQL Server cho Du an Ban Hang Java Web
 */
public class DBContext {

    private static final String SERVER_NAME = "localhost";
    private static final String PORT_NUMBER = "1433";
    private static final String DB_NAME = "ban_hang_db";
    private static final String USER_NAME = "sa";
    private static final String PASSWORD = "123"; // Thay mat khau tai khoan sa cua ban neu co

    public static Connection getConnection() throws Exception {
        String url = "jdbc:sqlserver://" + SERVER_NAME + ":" + PORT_NUMBER 
                   + ";databaseName=" + DB_NAME 
                   + ";encrypt=false;trustServerCertificate=true;";
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        return DriverManager.getConnection(url, USER_NAME, PASSWORD);
    }

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
