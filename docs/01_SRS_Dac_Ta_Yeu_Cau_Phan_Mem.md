# TÀI LIỆU ĐẶC TẢ YÊU CẦU PHẦN MỀM (SRS)
## SOFTWARE REQUIREMENTS SPECIFICATION
### DỰ ÁN: HỆ THỐNG WEBSITE THƯƠNG MẠI ĐIỆN TỬ THỜI TRANG HIGHFASHION (BAN_HANG)
**Tiêu chuẩn áp dụng:** IEEE Std 830-1998  
**Phiên bản:** 1.0  
**Ngày lập:** Tháng 10/2026  
**Ngôn ngữ triển khai:** Java Web (Jakarta EE 10 / Servlet 6.0 / JSP), Apache Tomcat 10.1, Microsoft SQL Server  

---

## MỤC LỤC
1. [GIỚI THIỆU (INTRODUCTION)](#1-giới-thiệu-introduction)
   - 1.1. Mục đích tài liệu
   - 1.2. Phạm vi dự án
   - 1.3. Định nghĩa và từ viết tắt
   - 1.4. Tài liệu tham khảo
2. [TỔNG QUAN HỆ THỐNG (OVERALL DESCRIPTION)](#2-tổng-quan-hệ-thống-overall-description)
   - 2.1. Phối cảnh hệ thống
   - 2.2. Các tác nhân người dùng (Actors)
   - 2.3. Môi trường vận hành
   - 2.4. Ràng buộc thiết kế và triển khai
3. [YÊU CẦU CHỨC NĂNG (FUNCTIONAL REQUIREMENTS)](#3-yêu-cầu-chức-năng-functional-requirements)
   - 3.1. Phân hệ Khách vãng lai & Người dùng (Customer)
   - 3.2. Phân hệ Quản trị viên (Admin)
   - 3.3. Ma trận Use Case chi tiết
4. [YÊU CẦU PHI CHỨC NĂNG (NON-FUNCTIONAL REQUIREMENTS)](#4-yêu-cầu-phi-chức-năng-non-functional-requirements)
   - 4.1. Hiệu năng (Performance)
   - 4.2. An toàn & Bảo mật (Security)
   - 4.3. Tính khả dụng và tương thích (Usability & Compatibility)
   - 4.4. Tính sẵn sàng & Độ tin cậy (Reliability & Availability)
5. [QUY TẮC NGHIỆP VỤ (BUSINESS RULES)](#5-quy-tắc-nghiệp-vụ-business-rules)

---

## 1. GIỚI THIỆU (INTRODUCTION)

### 1.1. Mục đích tài liệu
Tài liệu Đặc tả Yêu cầu Phần mềm (SRS) này mô tả chi tiết các yêu cầu chức năng, yêu cầu phi chức năng, quy tắc nghiệp vụ và giao diện người dùng của hệ thống Website Thương mại Điện tử Thời trang **HighFashion**. Tài liệu đóng vai trò là cơ sở kỹ thuật để:
- Đội ngũ phát triển (Developers) xây dựng và hoàn thiện tính năng.
- Đội ngũ kiểm thử (Testers) xây dựng kịch bản kiểm thử (Test Cases).
- Giảng viên / Khách hàng / Hội đồng đánh giá nghiệm thu sản phẩm phần mềm.

### 1.2. Phạm vi dự án
**HighFashion** là hệ thống bán lẻ trực tuyến chuyên biệt về quần áo, váy đầm và phụ kiện thời trang cao cấp. Hệ thống giải quyết các bài toán:
- Cho phép khách hàng tìm kiếm, chọn lọc sản phẩm theo đa tiêu chí (danh mục, khoảng giá, size, màu sắc, từ khóa).
- Cho phép mua sắm trực tuyến với giỏ hàng linh hoạt, đặt hàng giao hàng tận nơi (COD).
- Tra cứu danh sách hệ thống showroom, cửa hàng bán lẻ tích hợp bản đồ số tương tác (Interactive Map).
- Cung cấp trang quản trị tập trung (Admin Dashboard) giúp chủ cửa hàng quản lý doanh thu, trạng thái đơn hàng, danh mục sản phẩm và chuỗi cửa hàng.

### 1.3. Định nghĩa và từ viết tắt
| Thuật ngữ | Diễn giải |
| :--- | :--- |
| **SRS** | Software Requirements Specification (Đặc tả yêu cầu phần mềm) |
| **MVC** | Model - View - Controller (Mô hình kiến trúc phần mềm) |
| **Servlet** | Công nghệ phía máy chủ Java dùng để tiếp nhận và xử lý HTTP Request |
| **JSP** | JavaServer Pages (Công nghệ tạo giao diện Web động bằng Java) |
| **DAO** | Data Access Object (Mẫu thiết kế truy xuất dữ liệu) |
| **COD** | Cash On Delivery (Thanh toán khi nhận hàng) |
| **CRUD** | Create, Read, Update, Delete (Bốn thao tác dữ liệu cơ bản) |
| **Session** | Phiên làm việc của người dùng trên Web browser |

---

## 2. TỔNG QUAN HỆ THỐNG (OVERALL DESCRIPTION)

### 2.1. Phối cảnh hệ thống
Website HighFashion được phát triển theo mô hình ứng dụng web đa tầng (Multi-tier Architecture):
- **Tầng Client (Giao diện):** HTML5, CSS3, Bootstrap 5, Bootstrap Icons, JavaScript Vanilla, Leaflet Map API.
- **Tầng Ứng dụng (Application Server):** Java EE / Jakarta EE Servlet 6.0 chạy trên nền Apache Tomcat 10.1.
- **Tầng Dữ liệu (Database Server):** Microsoft SQL Server (kèm cơ chế Mock Fallback độc lập khi offline).

### 2.2. Các tác nhân người dùng (Actors)
Hệ thống phân quyền rõ ràng thành 3 nhóm đối tượng:
1. **Khách vãng lai (Guest):** Người dùng chưa đăng nhập. Có thể xem danh mục, tìm kiếm sản phẩm, xem chi tiết, chọn size/màu, thêm vào giỏ hàng và tiến hành đặt hàng.
2. **Khách hàng có tài khoản (Registered Customer):** Người dùng đã đăng ký tài khoản (Role = 0). Có đầy đủ quyền của Guest, đồng thời hệ thống tự động điền thông tin cá nhân khi đặt hàng, lưu giỏ hàng theo Session.
3. **Quản trị viên (Admin):** Người quản lý hệ thống (Role = 1). Có quyền truy cập bảng điều khiển Admin, quản trị sản phẩm, quản trị đơn hàng, quản lý hệ thống showroom cửa hàng.

### 2.3. Môi trường vận hành
- **Phía máy chủ:** HĐH Windows / Linux, OpenJDK 17 LTS trở lên, Apache Tomcat 10.1 trở lên, SQL Server 2017 trở lên.
- **Phía máy khách:** Trình duyệt hiện đại bất kỳ (Google Chrome, Microsoft Edge, Mozilla Firefox, Safari) hỗ trợ HTML5 và JavaScript.

---

## 3. YÊU CẦU CHỨC NĂNG (FUNCTIONAL REQUIREMENTS)

### 3.1. Phân hệ Khách vãng lai & Người dùng (Customer)

#### [FR-01] Xem Trang chủ (Home Page)
- **Mã Controller:** `HomeController` (URL: `/home`, `/`)
- **Mô tả:**
  - Hiển thị Banner quảng bá chiến dịch thời trang mới nhất.
  - Danh sách danh mục nổi bật (Áo Nam, Quần Nam, Thời Trang Nữ, Phụ Kiện).
  - Danh sách sản phẩm Hot / Bán chạy (`featuredProducts`).
  - Danh sách sản phẩm mới về (`latestProducts`).
  - Lối tắt truy cập nhanh tới Cửa hàng, Bản đồ chi nhánh, Giỏ hàng và Đăng nhập.

#### [FR-02] Danh mục sản phẩm & Bộ lọc thông minh (Product Catalog & Filters)
- **Mã Controller:** `ProductListController` (URL: `/products`)
- **Mô tả:**
  - **Lọc theo Danh mục:** Lọc sản phẩm theo `categoryId`.
  - **Tìm kiếm từ khóa:** Tìm kiếm theo tên sản phẩm (`keyword`).
  - **Lọc theo Nhãn đặc biệt:** Lọc sản phẩm bán chạy (`filter=bestseller`), sản phẩm giảm giá (`filter=sale`).
  - **Lọc theo Khoảng giá:**
    - Dưới 500.000đ (`priceRange=under_500k`)
    - Từ 500.000đ - 1.000.000đ (`priceRange=500k_1m`)
    - Từ 1.000.000đ - 2.000.000đ (`priceRange=1m_2m`)
    - Từ 2.000.000đ - 3.000.000đ (`priceRange=2m_3m`)
    - Trên 3.000.000đ (`priceRange=above_3m`)
  - **Lọc theo Kích cỡ (Size):** S, M, L, XL, XXL...
  - **Lọc theo Màu sắc (Color):** Trắng, Đen, Xanh, Đỏ, Be, Xám...
  - **Sắp xếp (Sorting):** Giá tăng dần (`price_asc`), Giá giảm dần (`price_desc`), Sản phẩm mới nhất (`new`), Nổi bật (`featured`).

#### [FR-03] Xem chi tiết sản phẩm (Product Detail)
- **Mã Controller:** `ProductDetailController` (URL: `/product-detail?id={id}`)
- **Mô tả:**
  - Hiển thị hình ảnh độ phân giải cao, tên sản phẩm, giá gốc, giá khuyến mãi (% tiết kiệm).
  - Lựa chọn Size và Màu sắc trực quan qua các nút chọn radio/button.
  - Bộ chọn số lượng mua (Quantity Selector với nút tăng/giảm).
  - Mô tả chi tiết chất liệu, form dáng và chính sách bảo hành, đổi trả.
  - Danh sách sản phẩm tương tự cùng chuyên mục (Related Products).

#### [FR-04] Quản lý Giỏ hàng (Shopping Cart)
- **Mã Controller:** `CartController` (URL: `/cart`)
- **Mô tả:**
  - **Thêm vào giỏ (`action=add`):** Lưu trữ trong `HttpSession`, gồm ID, Size, Màu, Số lượng, Đơn giá. Nếu sản phẩm cùng size/màu đã có thì tự động cộng dồn số lượng.
  - **Cập nhật số lượng (`action=update`):** Tăng/giảm trực tiếp số lượng từng sản phẩm.
  - **Xóa sản phẩm (`action=remove`):** Xóa 1 mặt hàng khỏi giỏ.
  - **Xóa trắng giỏ (`action=clear`):** Làm rỗng toàn bộ giỏ hàng.
  - Hiển thị tổng tiền tạm tính, phí vận chuyển và tổng tiền cần thanh toán.

#### [FR-05] Đặt hàng & Thanh toán (Checkout)
- **Mã Controller:** `CheckoutController` (URL: `/checkout`)
- **Mô tả:**
  - Form nhập thông tin nhận hàng: Họ và tên, Email, Số điện thoại, Địa chỉ nhận hàng chi tiết, Ghi chú đơn hàng.
  - Chọn hình thức thanh toán (COD - Trả tiền khi nhận hàng).
  - Hệ thống tự động ghi nhận vào bảng `orders` và chi tiết các món vào bảng `order_details`.
  - Tự động làm rỗng giỏ hàng sau khi đặt thành công và điều hướng sang trang `order-success.jsp`.

#### [FR-06] Hệ thống Cửa hàng & Bản đồ (Store Locator & Interactive Map)
- **Mã Controller:** `StoreListController` (URL: `/stores`)
- **Mô tả:**
  - Hiển thị danh sách chuỗi cửa hàng tại các thành phố lớn (Hà Nội, TP.HCM, Đà Nẵng).
  - Tích hợp bản đồ số Leaflet / OpenStreetMap hiển thị trực quan các điểm đánh dấu (Markers) với tọa độ thực tế (`latitude`, `longitude`).
  - Nhấp vào từng cửa hàng trên danh sách để bản đồ tự động cuộn và zoom tới tọa độ cửa hàng đó.

#### [FR-07] Đăng nhập & Đăng ký (Authentication)
- **Mã Controller:** `LoginController` (`/login`), `RegisterController` (`/register`), `LogoutController` (`/logout`)
- **Mô tả:**
  - **Đăng ký:** Kiểm tra trùng tên tài khoản (`checkUsernameExists`), xác nhận mật khẩu, lưu thông tin người dùng mới.
  - **Đăng nhập:** Xác thực tên đăng nhập và mật khẩu qua DBContext hoặc Mock Auth:
    - Nếu là Admin (`role = 1`): Chuyển hướng tới trang quản trị `/admin/dashboard`.
    - Nếu là Khách hàng (`role = 0`): Chuyển hướng về trang chủ `/home`.
  - **Đăng xuất:** Hủy phiên (`session.invalidate()`), chuyển về trang chủ.

---

### 3.2. Phân hệ Quản trị viên (Admin)

#### [FR-08] Thống kê & Bảng điều khiển (Admin Dashboard)
- **Mã Controller:** `AdminDashboardController` (URL: `/admin/dashboard`)
- **Kiểm tra quyền:** Chỉ người dùng có `role == 1` mới được truy cập, nếu không sẽ bị chuyển hướng về trang `/login`.
- **Mô tả:**
  - Thống kê Tổng doanh thu từ các đơn hàng thành công.
  - Thống kê Số lượng đơn hàng đang chờ duyệt (Pending Orders).
  - Thống kê Tổng số sản phẩm hiện có trong kho.
  - Bảng danh sách các đơn hàng gần đây với trạng thái chi tiết.

#### [FR-09] Quản lý Sản phẩm (Admin Product Management)
- **Mã Controller:** `AdminProductController` (URL: `/admin/products`)
- **Mô tả:**
  - **Xem danh sách:** Hiển thị toàn bộ sản phẩm kèm ảnh đại diện, danh mục, giá, tồn kho.
  - **Thêm sản phẩm mới (`action=add`):** Nhập tên, mô tả, giá gốc, giá sale, số lượng, URL ảnh, size, màu, danh mục, cờ nổi bật (`isFeatured`).
  - **Cập nhật sản phẩm (`action=edit`):** Chỉnh sửa thông tin sản phẩm đã chọn.
  - **Xóa sản phẩm (`action=delete`):** Xóa sản phẩm khỏi hệ thống (cascade xóa chi tiết liên quan).

#### [FR-10] Quản lý Hệ thống Chi nhánh (Admin Store Management)
- **Mã Controller:** `AdminStoreController` (URL: `/admin/stores`)
- **Mô tả:**
  - Xem danh sách chi nhánh và trạng thái cơ sở chính (`is_main`).
  - Thêm, sửa, xóa showroom với tọa độ địa lý (Kinh độ/Vĩ độ), số điện thoại và giờ mở cửa.

---

## 4. YÊU CẦU PHI CHỨC NĂNG (NON-FUNCTIONAL REQUIREMENTS)

### 4.1. Hiệu năng (Performance)
- **Thời gian phản hồi:** Thời gian tải trang trung bình dưới 1.5 giây trong điều kiện mạng tiêu chuẩn.
- **Tối ưu hóa Database:** Truy vấn có chỉ mục (Index) trên các khóa ngoại và trường tìm kiếm (`username`, `category_id`, `is_featured`).
- **Xử lý đồng thời:** Hỗ trợ tối thiểu 50-100 phiên làm việc đồng thời trên máy chủ Apache Tomcat chuẩn.

### 4.2. An toàn & Bảo mật (Security)
- **Bảo vệ trang Quản trị:** Mọi request tới `/admin/*` đều qua cơ chế kiểm tra Session và Role chặt chẽ (`if (user == null || !user.isAdmin()) response.sendRedirect(...)`).
- **Phòng chống SQL Injection:** Toàn bộ tầng DAO sử dụng 100% `PreparedStatement` với các tham số truyền qua placeholder `?`, triệt tiêu rủi ro tiêm mã độc SQL.
- **Mã hóa URL:** Chống can thiệp dữ liệu trái phép qua tham số URL.

### 4.3. Tính khả dụng và tương thích (Usability & Compatibility)
- **Thiết kế Responsive (RWD):** Giao diện tương thích hoàn hảo trên mọi kích thước màn hình: Desktop (1920x1080), Tablet (iPad 768px), Smartphone (iPhone/Android 375px - 414px).
- **Trải nghiệm người dùng (UX):** Thiết kế thẩm mỹ sang trọng chuẩn xu hướng thời trang cao cấp (tông màu Dark/Neutral, Typography hiện đại, Card bóng đổ nhẹ nhàng, nút bấm bo góc mềm mại).

### 4.4. Tính sẵn sàng & Dự phòng (High Availability & Resilience)
- **Cơ chế Dual-Mode (Mock Fallback thông minh):**
  - Khi có kết nối SQL Server: Hệ thống lưu trữ và đồng bộ vào DB chính xác.
  - Khi không có SQL Server (hoặc SQL Server chưa bật): Tầng DAO tự động kích hoạt chế độ Fallback Mock Data, cung cấp đầy đủ 24 sản phẩm mẫu, danh mục, tài khoản đăng nhập admin/khách hàng mà không bao giờ báo lỗi `NullPointerException` hay sập hệ thống (Zero-Crash Guarantee).

---

## 5. QUY TẮC NGHIỆP VỤ (BUSINESS RULES)

1. **Quy tắc tính giá hiệu dụng (`Effective Price`):**
   - Nếu `sale_price > 0`: Giá bán tính theo `sale_price`.
   - Nếu `sale_price == 0`: Giá bán tính theo `price`.
2. **Quy tắc giỏ hàng:**
   - Cùng một sản phẩm nhưng khác Size hoặc khác Màu sắc sẽ được tính là 2 mặt hàng độc lập trong giỏ hàng.
   - Nếu cùng ID, cùng Size và cùng Màu: Số lượng sẽ được tự động cộng dồn.
3. **Quy tắc phân quyền:**
   - Role = 1: Quản trị viên (Toàn quyền).
   - Role = 0: Khách hàng tiêu chuẩn.
4. **Quy tắc đặt hàng:**
   - Khách vãng lai vẫn được phép đặt hàng mà không bắt buộc phải đăng nhập trước (Guest Checkout).
   - Khách hàng đã đăng nhập sẽ tự động gắn kết `user_id` vào đơn hàng để phục vụ tra cứu lịch sử đơn hàng.
