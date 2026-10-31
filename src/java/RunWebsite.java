import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/**
 * RunWebsite: Lớp khởi động toàn bộ Website Bán Hàng HighFashion.
 * Giúp bạn có thể bấm trực tiếp nút "Run Java" (hình tam giác ở góc trên bên phải)
 * để khởi động website và tự động mở trình duyệt mà không cần nhớ phím tắt!
 */
public class RunWebsite {

    public static void main(String[] args) {
        try {
            System.setOut(new java.io.PrintStream(System.out, true, "UTF-8"));
            System.setErr(new java.io.PrintStream(System.err, true, "UTF-8"));
        } catch (Exception ignored) {}

        System.out.println("========================================================");
        System.out.println("🚀 ĐANG KHỞI ĐỘNG WEBSITE BÁN HÀNG HIGHFASHION...");
        System.out.println("========================================================");
        try {
            // Tự động tìm file scripts/server.ps1 linh hoạt theo thư mục hiện tại
            File scriptFile = findServerScript();

            if (scriptFile == null || !scriptFile.exists()) {
                System.err.println("❌ Không tìm thấy file scripts/server.ps1!");
                System.err.println("👉 Vui lòng đảm bảo thư mục dự án có chứa 'ban_hang/scripts/server.ps1' hoặc 'scripts/server.ps1'.");
                return;
            }

            System.out.println("📂 Đang thực thi: " + scriptFile.getAbsolutePath());

            ProcessBuilder pb = new ProcessBuilder(
                "powershell", "-NoProfile", "-ExecutionPolicy", "Bypass",
                "-File", scriptFile.getAbsolutePath(), "-Action", "start"
            );
            // Thiết lập thư mục làm việc là thư mục chứa dự án ban_hang
            File projectDir = scriptFile.getParentFile() != null ? scriptFile.getParentFile().getParentFile() : null;
            if (projectDir != null && projectDir.exists()) {
                pb.directory(projectDir);
            }
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("========================================================");
                System.out.println("✅ Khởi động website thành công!");
                System.out.println("========================================================");
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi khởi động website: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Tự động dò tìm file server.ps1 ở các vị trí khả dĩ trong thư mục dự án
     */
    private static File findServerScript() {
        String[] relativePaths = {
            "ban_hang/scripts/server.ps1",
            "scripts/server.ps1",
            "../ban_hang/scripts/server.ps1",
            "../scripts/server.ps1",
            "MiniE-commerceWebsite/scripts/server.ps1",
            "../MiniE-commerceWebsite/scripts/server.ps1"
        };

        for (String rel : relativePaths) {
            File f = new File(rel);
            if (f.exists()) {
                return f.getAbsoluteFile();
            }
        }

        // 1. Dò từ vị trí class đang chạy (RunWebsite.class)
        try {
            java.net.URL location = RunWebsite.class.getProtectionDomain().getCodeSource().getLocation();
            if (location != null) {
                File codeLoc = new File(location.toURI());
                File dir = codeLoc.isDirectory() ? codeLoc : codeLoc.getParentFile();
                for (int i = 0; i < 6 && dir != null; i++) {
                    File s1 = new File(dir, "scripts/server.ps1");
                    if (s1.exists()) return s1.getAbsoluteFile();
                    File s2 = new File(dir, "ban_hang/scripts/server.ps1");
                    if (s2.exists()) return s2.getAbsoluteFile();
                    dir = dir.getParentFile();
                }
            }
        } catch (Exception ignored) {}

        // 2. Dò ngược lên các thư mục cha từ thư mục làm việc hiện tại (user.dir)
        File current = new File(System.getProperty("user.dir"));
        for (int i = 0; i < 5 && current != null; i++) {
            File test0 = new File(current, "ban_hang/scripts/server.ps1");
            if (test0.exists()) return test0.getAbsoluteFile();

            File test1 = new File(current, "scripts/server.ps1");
            if (test1.exists()) return test1.getAbsoluteFile();

            File test2 = new File(current, "MiniE-commerceWebsite/scripts/server.ps1");
            if (test2.exists()) return test2.getAbsoluteFile();

            current = current.getParentFile();
        }

        return null;
    }
}
