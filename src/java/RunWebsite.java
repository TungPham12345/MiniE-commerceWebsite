import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * RunWebsite: Lớp khởi động toàn bộ Website Bán Hàng HighFashion.
 * Giúp bạn có thể bấm trực tiếp nút "Run Java" (hình tam giác ở góc trên bên phải)
 * để khởi động website và tự động mở trình duyệt mà không cần nhớ phím tắt!
 */
public class RunWebsite {

    public static void main(String[] args) {
        System.out.println("========================================================");
        System.out.println("🚀 ĐANG KHỞI ĐỘNG WEBSITE BÁN HÀNG HIGHFASHION...");
        System.out.println("========================================================");
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "powershell", "-NoProfile", "-ExecutionPolicy", "Bypass",
                "-File", "d:\\duan111\\ban_hang\\scripts\\server.ps1", "-Action", "start"
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }
            process.waitFor();
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi khởi động website: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
