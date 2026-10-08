# TÀI LIỆU THIẾT KẾ KIẾN TRÚC & HỆ THỐNG
## SYSTEM ARCHITECTURE & TECHNICAL DESIGN DOCUMENT (SAD)
### DỰ ÁN: WEBSITE THƯƠNG MẠI ĐIỆN TỬ THỜI TRANG HIGHFASHION
**Phiên bản:** 1.0  
**Chuẩn công nghệ:** MVC Model 2, Jakarta EE 10, Java Servlet 6.0, Apache Tomcat 10.1, MSSQL JDBC 13.2  

---

## 1. TỔNG QUAN KIẾN TRÚC (ARCHITECTURAL OVERVIEW)

Hệ thống được thiết kế hoàn toàn theo mô hình kinh điển chuẩn công nghiệp **MVC Model 2 (Model - View - Controller)** cho các ứng dụng Java Web doanh nghiệp. Kiến trúc này phân tách rõ ràng giữa giao diện hiển thị, logic điều khiển nghiệp vụ và tầng lưu trữ dữ liệu.

```mermaid
graph TD
    User["Khách hàng / Quản trị viên (Browser)"] -->|1. HTTP Request (GET/POST)| Tomcat["Apache Tomcat 10.1 Web Container"]
    
    subgraph Controller_Layer ["TẦNG ĐIỀU KHIỂN (CONTROLLERS)"]
        Tomcat -->|2. Route URL| Servlet["Java Servlet (@WebServlet)<br/>HomeController, ProductListController,<br/>CartController, AdminDashboard..."]
    end
    
    subgraph Model_Layer ["TẦNG DỮ LIỆU & DAO (MODELS & DAOs)"]
        Servlet -->|3. Call Methods| DAO["Data Access Objects (DAO)<br/>ProductDAO, UserDAO, OrderDAO..."]
        DAO -->|4. Get Connection| DBContext["context.DBContext<br/>(JDBC Connection Manager)"]
        DAO -->|Map Data| Entities["POJO Entities<br/>Product, User, Order, Category..."]
    end
    
    subgraph Data_Storage ["TẦNG LƯU TRỮ (STORAGE)"]
        DBContext -->|5. SQL Query| MSSQL[("Microsoft SQL Server<br/>(ban_hang_db)")]
        DAO -.->|Fallback if DB Offline| MockData["In-Memory Mock Fallback<br/>(24 Sản phẩm, Danh mục, Auth)"]
    end
    
    subgraph View_Layer ["TẦNG GIAO DIỆN (VIEWS)"]
        Servlet -->|6. Set Attributes & Forward| JSP["JavaServer Pages (JSP)<br/>index.jsp, shop.jsp, detail.jsp,<br/>admin-dashboard.jsp..."]
        JSP -->|Includes| Fragments["JSP Fragments<br/>header.jsp, navbar.jsp, footer.jsp"]
    end
    
    JSP -->|7. Render HTML/CSS/JS| Tomcat
    Tomcat -->|8. HTTP Response (HTML5)| User
```

---

## 2. PHÂN TẦNG CHI TIẾT (LAYERED ARCHITECTURE)

### 2.1. Tầng Trình diễn (View Layer - JSP)
- **Công nghệ:** Jakarta Server Pages (JSP), Bootstrap 5, Bootstrap Icons, Leaflet OpenStreetMap.
- **Vai trò:** Nhận dữ liệu được chuyển giao từ Controller thông qua `request.getAttribute(...)` và render ra mã HTML5 trả về cho trình duyệt.
- **Module dùng chung (Reusability):**
  - `web/includes/header.jsp`: Thư viện CSS, meta tags, tiêu đề trang.
  - `web/includes/navbar.jsp`: Thanh điều hướng, tìm kiếm nhanh, số lượng giỏ hàng thời gian thực.
  - `web/includes/footer.jsp`: Thông tin liên hệ, bản quyền, chính sách đổi trả, mạng xã hội.

### 2.2. Tầng Điều khiển (Controller Layer - Servlets)
Tập hợp các Servlet kế thừa từ `jakarta.servlet.http.HttpServlet`. Nhận và xử lý các phương thức HTTP `doGet()` và `doPost()`.

| Tên Controller | URL Pattern | Phương thức | Chức năng nghiệp vụ |
| :--- | :--- | :--- | :--- |
| **HomeController** | `/home`, `/` | `GET` | Tải banner, danh mục, sản phẩm nổi bật & mới nhất |
| **ProductListController** | `/products` | `GET` | Tìm kiếm, lọc theo giá, size, màu sắc, sắp xếp & phân trang |
| **ProductDetailController** | `/product-detail`| `GET` | Xem chi tiết 1 sản phẩm, chọn size, màu, sản phẩm tương tự |
| **CartController** | `/cart` | `GET`, `POST` | Xem giỏ hàng, thêm sản phẩm, cập nhật số lượng, xóa món |
| **CheckoutController** | `/checkout` | `GET`, `POST` | Xác nhận đơn hàng, lưu thông tin nhận hàng, thanh toán COD |
| **LoginController** | `/login` | `GET`, `POST` | Xác thực đăng nhập, điều hướng theo quyền hạn (Admin / User) |
| **RegisterController** | `/register` | `GET`, `POST` | Kiểm tra tính hợp lệ và đăng ký tài khoản khách hàng mới |
| **LogoutController** | `/logout` | `GET` | Xóa phiên đăng nhập `session.invalidate()` và chuyển hướng |
| **StoreListController** | `/stores` | `GET` | Hiển thị chuỗi showroom trên bản đồ tương tác Leaflet Map |
| **AdminDashboardController**| `/admin/dashboard`| `GET` | Thống kê doanh thu, đơn hàng, biểu đồ tổng quan cho Admin |
| **AdminProductController** | `/admin/products` | `GET`, `POST` | CRUD sản phẩm (Xem danh sách, Thêm, Sửa, Xóa sản phẩm) |
| **AdminStoreController** | `/admin/stores` | `GET`, `POST` | CRUD showroom (Quản lý chuỗi chi nhánh và tọa độ GPS) |

### 2.3. Tầng Truy xuất Dữ liệu (Data Access Object - DAO Layer)
Đóng gói toàn bộ các câu lệnh SQL nhằm che giấu chi tiết cấu trúc Database với tầng nghiệp vụ:
- `ProductDAO`: Các hàm `getAllProducts()`, `getFeaturedProducts()`, `getProductsByCategoryId()`, `searchProductsByName()`, `insertProduct()`, `updateProduct()`, `deleteProduct()`.
- `CategoryDAO`: Các hàm `getAllCategories()`, `getCategoryById()`.
- `UserDAO`: Các hàm `login()`, `register()`, `checkUsernameExists()`.
- `OrderDAO`: Các hàm `createOrder()`, `getAllOrders()`, `getOrderById()`, `updateOrderStatus()`.
- `StoreDAO`: Các hàm `getAllStores()`, `getStoreById()`, `insertStore()`, `updateStore()`, `deleteStore()`.

### 2.4. Tầng Thực thể (Entity Model Layer)
Chứa các đối tượng thuần túy (Plain Old Java Objects - POJO) đại diện cho các bảng dữ liệu:
- `Product`: `id`, `name`, `description`, `price`, `salePrice`, `quantity`, `image`, `size`, `color`, `categoryId`, `isFeatured`.
- `Category`: `id`, `name`, `description`, `icon`.
- `User`: `id`, `username`, `password`, `fullname`, `email`, `phone`, `address`, `role`.
- `Order`: `id`, `userId`, `customerName`, `email`, `phone`, `address`, `note`, `totalMoney`, `paymentMethod`, `status`, `createdAt`.
- `OrderDetail`: `id`, `orderId`, `productId`, `price`, `quantity`, `size`, `color`, `totalMoney`.
- `CartItem`: `product`, `quantity`, `selectedSize`, `selectedColor`, `totalPrice`.
- `Store`: `id`, `name`, `address`, `city`, `phone`, `hours`, `latitude`, `longitude`, `isMain`.

---

## 3. THIẾT KẾ BẢO MẬT & PHÂN QUYỀN (SECURITY DESIGN)

### 3.1. Phân quyền truy cập (Access Control)
Hệ thống sử dụng cơ chế bảo mật theo phiên (Session-based Authentication):
- Khi đăng nhập thành công, đối tượng `User` được lưu vào `session.setAttribute("user", user)`.
- Mọi Servlet thuộc phạm vi Quản trị (`/admin/*`) đều thực hiện tiền kiểm tra:
```java
HttpSession session = request.getSession();
User user = (User) session.getAttribute("user");
if (user == null || !user.isAdmin()) { // user.getRole() != 1
    response.sendRedirect(request.getContextPath() + "/login");
    return;
}
```
Nếu người dùng chưa đăng nhập hoặc không có cờ Admin (`role != 1`), request bị chặn ngay lập tức và chuyển hướng về trang đăng nhập.

### 3.2. Bảo vệ dữ liệu & Phòng chống tấn công (Security Hardening)
1. **Chống SQL Injection:** 100% câu truy vấn tầng DAO đều dùng `java.sql.PreparedStatement` với parameter bindings `?`, ngăn chặn hoàn toàn việc chèn lệnh SQL độc hại.
2. **Session Hijacking Mitigation:** Cookie phiên `JSESSIONID` được Tomcat tự động cấp cờ `HttpOnly`, ngăn chặn các script mã độc client đọc trộm cookie.
3. **Encoding dữ liệu:** Thiết lập mã hóa toàn diện UTF-8 ở cả bộ lọc Servlet (`request.setCharacterEncoding("UTF-8")`) và phản hồi (`response.setContentType("text/html;charset=UTF-8")`), đảm bảo hiển thị tiếng Việt có dấu chuẩn xác 100%.

---

## 4. CƠ CHẾ SẴN SÀNG CAO (DUAL-MODE RESILIENCE)

Một điểm đặc sắc trong thiết kế của hệ thống là **Kiến trúc Tự phục hồi (Self-Healing Fallback Architecture)**:
- Tầng DAO được thiết kế với khối `try-catch` bao bọc các thao tác gọi `DBContext.getConnection()`.
- Trong trường hợp cơ sở dữ liệu Microsoft SQL Server chưa được khởi chạy hoặc cổng 1433 không thể truy cập, DAO tự động chuyển sang chế độ **In-Memory Mock Repository**.
- Nhờ cơ chế này, hệ thống đảm bảo **không bao giờ bị crash hoặc hiển thị trang lỗi 500 trắng bệch**, luôn đảm bảo trang web phục vụ tốt cho mục đích thuyết trình, demo hoặc kiểm thử giao diện.
