# 🛍️ HighFashion - Website Thương Mại Điện Tử Thời Trang

> Hệ thống website thương mại điện tử chuyên nghiệp bán hàng thời trang (HighFashion), xây dựng trên nền tảng **Java Web (Jakarta EE 10 / Servlet 6.0 / JSP)** và máy chủ **Apache Tomcat 10.1**.

---

## 📌 Mục Lục
- [Giới Thiệu Dự Án](#-giới-thiệu-dự-án)
- [Tính Năng Chính](#-tính-năng-chính)
- [Công Nghệ Sử Dụng (Tech Stack)](#-công-nghệ-sử-dụng-tech-stack)
- [Cấu Trúc Thư Mục](#-cấu-trúc-thư-mục)
- [Hướng Dẫn Khởi Chạy Nhanh](#-hướng-dẫn-khởi-chạy-nhanh)
- [Tài Khoản Đăng Nhập Kiểm Thử](#-tài-khoản-đăng-nhập-kiểm-thử)
- [Cấu Hình Cơ Sở Dữ Liệu](#-cấu-hình-cơ-sở-dữ-liệu)
- [Lệnh Quản Trị Server (CLI / Terminal)](#-lệnh-quản-trị-server-cli--terminal)
- [Khắc Phục Sự Cố (Troubleshooting)](#-khắc-phục-sự-cố-troubleshooting)

---

## 📖 Giới Thiệu Dự Án

**HighFashion** là nền tảng thương mại điện tử thời trang trực tuyến với giao diện hiện đại, tối ưu trải nghiệm người dùng từ duyệt sản phẩm, mua sắm đến quản trị chuỗi cửa hàng, quản lý kho bãi và giao vận.

---

## ✨ Tính Năng Chính

### 1. Phân hệ Khách hàng (Customer & Guest)
- **Trang chủ & Banner:** Giới thiệu bộ sưu tập mới, sản phẩm bán chạy, ưu đãi nổi bật.
- **Duyệt & Tìm kiếm sản phẩm:** Tìm theo từ khóa, lọc theo danh mục, khoảng giá, sắp xếp theo giá/độ mới.
- **Chi tiết sản phẩm:** Biến thể màu sắc, kích thước, ảnh phóng to, thông tin chất liệu.
- **Giỏ hàng & Đặt hàng (Checkout):** Thêm/sửa/xóa sản phẩm trong giỏ, thanh toán khi nhận hàng (COD).
- **Quản lý tài khoản cá nhân:** Đăng ký, đăng nhập, quên mật khẩu, cập nhật hồ sơ cá nhân và theo dõi đơn hàng (`/my-orders`).
- **Hệ thống cửa hàng (Showroom / Store List):** Tra cứu địa chỉ chuỗi showroom thời trang trên toàn quốc.

### 2. Phân hệ Quản trị viên (Admin)
- **Dashboard tổng quan:** Biểu đồ doanh thu, số lượng đơn hàng, người dùng mới.
- **Quản lý sản phẩm (CRUD):** Thêm, sửa, xóa, tải ảnh, phân loại danh mục sản phẩm.
- **Quản lý người dùng & phân quyền:** Danh sách tài khoản khách hàng, nhân viên, phân quyền truy cập.
- **Quản lý cửa hàng (Store Management):** Cập nhật mạng lưới chi nhánh cửa hàng.

### 3. Phân hệ Vận hành & Hậu cần (Logistics & Warehouse)
- **Quản lý kho hàng (Warehouse):** Kiểm soát tồn kho theo từng mẫu mã.
- **Điều phối & Giao vận (Logistics / Shipper / Hub):** Theo dõi trạng thái đơn hàng và quá trình giao nhận hàng hóa.

---

## 🛠️ Công Nghệ Sử Dụng (Tech Stack)

| Thành phần | Công nghệ / Phiên bản |
|------------|------------------------|
| **Ngôn ngữ** | Java 17 LTS |
| **Nền tảng Web** | Jakarta EE 10 (Servlet 6.0, JSP 3.1, JSTL) |
| **Máy chủ Web** | Apache Tomcat 10.1.60 (tích hợp sẵn, port 10000) |
| **Cơ sở dữ liệu** | Microsoft SQL Server (hoặc tự động dùng Mock Fallback) |
| **Frontend** | HTML5, CSS3, JavaScript, Bootstrap, FontAwesome |
| **Kiến trúc** | MVC (Model - View - Controller), DAO Pattern |

---

## 📂 Cấu Trúc Thư Mục

```plaintext
MiniE-commerceWebsite/
├── src/java/                # Mã nguồn Java (Controller, DAO, Model, Service, Context)
├── web/                     # Giao diện JSP, CSS, JS, hình ảnh, WEB-INF, web.xml
├── lib/                     # Thư viện phụ thuộc (.jar: JDBC, Jakarta Servlet, JSTL, ...)
├── scripts/
│   └── server.ps1           # Script PowerShell điều khiển máy chủ thông minh
├── run-website.bat          # File click đúp để khởi động website
├── stop-website.bat         # File click đúp để dừng máy chủ
├── database.sql             # Kịch bản khởi tạo Cơ sở dữ liệu SQL Server
└── build.xml                # Cấu hình Ant build
```

---

## 🚀 Hướng Dẫn Khởi Chạy Nhanh

Bạn có thể chạy dự án bằng **1 trong 3 cách** dưới đây:

### Cách 1: Nhấp đúp chuột (Đơn giản nhất)
1. Nhấp đúp chuột vào file 👉 **`run-website.bat`**
2. Server sẽ tự động biên dịch code, khởi động Tomcat và tự mở trình duyệt tại:  
   🔗 **http://localhost:10000/ban_hang/home**
3. Khi muốn dừng, chỉ cần nhấp đúp file 👉 **`stop-website.bat`**.

---

### Cách 2: Chạy trực tiếp từ Antigravity IDE / VS Code
1. Mở bất kỳ file `.jsp` hoặc `.java` nào trong trình soạn thảo.
2. Nhấn nút **Run Java** ở góc trên bên phải thanh công cụ (hoặc chạy file `RunWebsite.java`).
3. Hệ thống sẽ tự động kích hoạt tiến trình biên dịch và bật server.

---

### Cách 3: Chạy bằng dòng lệnh Terminal (PowerShell)
```powershell
# Khởi động server và mở trình duyệt:
powershell -ExecutionPolicy Bypass -File "scripts\server.ps1" -Action start

# Chạy server ở chế độ xem log trực tiếp (Console):
powershell -ExecutionPolicy Bypass -File "scripts\server.ps1" -Action run

# Biên dịch lại mã nguồn Java:
powershell -ExecutionPolicy Bypass -File "scripts\server.ps1" -Action build

# Dừng server:
powershell -ExecutionPolicy Bypass -File "scripts\server.ps1" -Action stop

# Khởi động lại server:
powershell -ExecutionPolicy Bypass -File "scripts\server.ps1" -Action restart
```

---

## 👥 Tài Khoản Đăng Nhập Kiểm Thử

| Vai trò | Tên đăng nhập (Username) | Mật khẩu (Password) | Chức năng chính |
|---------|--------------------------|---------------------|-----------------|
| **Quản trị viên (Admin)** | `admin` | `123456` | Toàn quyền Dashboard, Quản lý sản phẩm, Quản lý đơn hàng, Thống kê |
| **Khách hàng 1** | `customer1` | `123456` | Mua hàng, Giỏ hàng, Đặt hàng, Xem lịch sử đơn |
| **Khách hàng 2** | `testacc` | `mypass123` | Tài khoản khách hàng phụ |
| **Khách hàng 3** | `pc` | `123` | Tài khoản kiểm thử nhanh |

---

## 🗄️ Cấu Hình Cơ Sở Dữ Liệu

- **Chế độ Mock Fallback (Mặc định):** Tự động kích hoạt khi chưa có SQL Server, website vẫn hoạt động bình thường với dữ liệu trong `../mock_users.txt`.
- **Microsoft SQL Server:**
  - File tạo DB: `database.sql`
  - Kết nối mặc định trong `DBContext.java`: `localhost:1433`, Database: `ban_hang_db`, User: `sa`, Password: `123`.

---

## 🔧 Khắc Phục Sự Cố

- **Cổng 10000 bị chiếm dụng:** Chạy lệnh `powershell -ExecutionPolicy Bypass -File "scripts\server.ps1" -Action stop`.
- **Sửa file Java không đổi:** Chạy lệnh build `powershell -ExecutionPolicy Bypass -File "scripts\server.ps1" -Action build`.
- **Giao diện không cập nhật:** Nhấn **Ctrl + F5** để xóa cache trình duyệt.
