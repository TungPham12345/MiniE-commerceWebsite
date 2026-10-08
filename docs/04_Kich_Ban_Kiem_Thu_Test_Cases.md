# TÀI LIỆU KỊCH BẢN KIỂM THỬ (TEST CASES DOCUMENT)
## HỆ THỐNG THƯƠNG MẠI ĐIỆN TỬ HIGHFASHION
**Phiên bản:** 1.0  
**Người thực hiện:** QA / Development Team  
**Môi trường kiểm thử:** Apache Tomcat 10.1, OpenJDK 17, Google Chrome / Microsoft Edge  

---

## 1. TỔNG QUAN KẾT QUẢ KIỂM THỬ (TEST SUMMARY)

| Tổng số Test Cases | Số lượng Passed | Số lượng Failed | Tỷ lệ thành công |
| :---: | :---: | :---: | :---: |
| **12** | **12** | **0** | **100%** |

---

## 2. DANH SÁCH TEST CASES CHI TIẾT

### [TC-01] Xem Trang chủ và tải danh sách sản phẩm nổi bật
- **Mục tiêu:** Kiểm tra trang chủ hiển thị đầy đủ banner, danh mục và sản phẩm.
- **Tiền điều kiện:** Server Tomcat đang hoạt động.
- **Các bước thực hiện:**
  1. Mở trình duyệt và truy cập `http://localhost:8080/ban_hang/home`.
  2. Quan sát phần Header, Banner chiến dịch, Danh mục và danh sách sản phẩm nổi bật.
- **Kết quả mong đợi:** Mã phản hồi HTTP 200, hiển thị đầy đủ thông tin sản phẩm, hình ảnh và giá niêm yết không bị vỡ giao diện.
- **Kết quả thực tế:** Trang web hiển thị sắc nét, banner và sản phẩm tải thành công.
- **Trạng thái:** **PASSED**

---

### [TC-02] Tìm kiếm sản phẩm theo từ khóa
- **Mục tiêu:** Kiểm tra chức năng tìm kiếm sản phẩm.
- **Các bước thực hiện:**
  1. Tại ô tìm kiếm trên thanh Navbar, nhập từ khóa `Jeans`.
  2. Nhấn nút kính lúp hoặc phím Enter.
- **Kết quả mong đợi:** Chuyển hướng tới `/products?keyword=Jeans`, hiển thị danh sách các sản phẩm quần Jeans chứa từ khóa tương ứng.
- **Kết quả thực tế:** Hiển thị chính xác các mẫu sản phẩm liên quan đến `Jeans`.
- **Trạng thái:** **PASSED**

---

### [TC-03] Lọc sản phẩm theo danh mục và khoảng giá
- **Mục tiêu:** Kiểm tra khả năng kết hợp các bộ lọc trong trang cửa hàng.
- **Các bước thực hiện:**
  1. Truy cập trang `http://localhost:8080/ban_hang/products`.
  2. Chọn danh mục "Áo Nam" (`categoryId=1`).
  3. Chọn khoảng giá "Dưới 500.000đ" (`priceRange=under_500k`).
- **Kết quả mong đợi:** Chỉ hiển thị các sản phẩm thuộc danh mục Áo Nam và có giá bán dưới 500.000đ.
- **Kết quả thực tế:** Dữ liệu lọc chính xác, không phát sinh lỗi.
- **Trạng thái:** **PASSED**

---

### [TC-04] Xem chi tiết sản phẩm và chọn thuộc tính (Size, Màu)
- **Mục tiêu:** Kiểm tra trang chi tiết hiển thị đầy đủ thông tin và cho phép chọn size/màu.
- **Các bước thực hiện:**
  1. Nhấp chọn 1 sản phẩm bất kỳ từ trang chủ (VD: Áo Sơ Mi Nam Oxford).
  2. Nhấp chọn Size `L` và Màu `Trắng`.
  3. Tăng số lượng lên `2`.
- **Kết quả mong đợi:** Giao diện phản hồi trực quan khi chọn size/màu, số lượng tăng lên 2.
- **Kết quả thực tế:** Hoạt động trơn tru.
- **Trạng thái:** **PASSED**

---

### [TC-05] Thêm sản phẩm vào giỏ hàng (Add to Cart)
- **Mục tiêu:** Kiểm tra cơ chế thêm món vào giỏ hàng và cập nhật huy hiệu số lượng.
- **Các bước thực hiện:**
  1. Tại trang chi tiết sản phẩm, nhấn nút **"Thêm Vào Giỏ Hàng"**.
- **Kết quả mong đợi:** Sản phẩm được thêm vào Session giỏ hàng, thông báo thành công xuất hiện, biểu tượng giỏ hàng trên thanh Navbar hiển thị số lượng tương ứng.
- **Kết quả thực tế:** Icon giỏ hàng tăng số lượng chính xác, Session giỏ hàng lưu trữ đầy đủ.
- **Trạng thái:** **PASSED**

---

### [TC-06] Điều chỉnh số lượng và xóa món hàng trong giỏ
- **Mục tiêu:** Kiểm tra các thao tác sửa đổi trong trang `/cart`.
- **Các bước thực hiện:**
  1. Truy cập `http://localhost:8080/ban_hang/cart`.
  2. Nhấp nút tăng/giảm số lượng của 1 món hàng.
  3. Nhấp biểu tượng thùng rác để xóa 1 món hàng.
- **Kết quả mong đợi:** Tổng tiền đơn hàng tự động tính toán lại tương ứng; món hàng bị xóa biến mất khỏi danh sách.
- **Kết quả thực tế:** Tổng tiền cập nhật chính xác từng đồng.
- **Trạng thái:** **PASSED**

---

### [TC-07] Đặt hàng thành công (Checkout COD)
- **Mục tiêu:** Kiểm tra luồng đặt hàng không cần đăng nhập (Guest Checkout).
- **Các bước thực hiện:**
  1. Nhấn nút "Tiến Hành Đặt Hàng" từ giỏ hàng.
  2. Điền thông tin: Họ tên = `Nguyễn Văn B`, SĐT = `0911223344`, Địa chỉ = `Cầu Giấy, Hà Nội`.
  3. Chọn phương thức thanh toán = "Thanh toán khi nhận hàng (COD)".
  4. Nhấn "Xác Nhận Đặt Hàng".
- **Kết quả mong đợi:** Hệ thống lưu đơn hàng, làm rỗng giỏ hàng và chuyển hướng sang trang `order-success.jsp`.
- **Kết quả thực tế:** Trang thông báo đặt hàng thành công hiển thị rõ mã đơn hàng và thông tin giao nhận.
- **Trạng thái:** **PASSED**

---

### [TC-08] Đăng nhập tài khoản Khách hàng thông thường
- **Mục tiêu:** Kiểm tra tính năng xác thực khách hàng.
- **Dữ liệu vào:** Username = `customer1`, Password = `123456`.
- **Các bước thực hiện:**
  1. Vào trang `http://localhost:8080/ban_hang/login`.
  2. Nhập thông tin và bấm "Đăng nhập".
- **Kết quả mong đợi:** Đăng nhập thành công, chuyển hướng về `/home`, Navbar hiển thị tên `Nguyễn Văn An`.
- **Kết quả thực tế:** Đăng nhập mượt mà, lưu thông tin vào Session.
- **Trạng thái:** **PASSED**

---

### [TC-09] Đăng nhập tài khoản Quản trị viên (Admin)
- **Mục tiêu:** Kiểm tra phân quyền chuyển hướng tới trang Quản trị.
- **Dữ liệu vào:** Username = `admin`, Password = `123456`.
- **Các bước thực hiện:**
  1. Vào trang đăng nhập, nhập tài khoản admin.
  2. Bấm "Đăng nhập".
- **Kết quả mong đợi:** Hệ thống nhận diện `role == 1`, tự động chuyển hướng tới `/admin/dashboard`.
- **Kết quả thực tế:** Trang Quản trị Dashboard hiển thị đầy đủ thống kê doanh thu và đơn hàng.
- **Trạng thái:** **PASSED**

---

### [TC-10] Chặn truy cập trái phép vào trang Quản trị (Security Check)
- **Mục tiêu:** Kiểm tra cơ chế bảo mật chống truy cập trái phép.
- **Các bước thực hiện:**
  1. Mở cửa sổ ẩn danh mới (chưa đăng nhập).
  2. Gõ trực tiếp URL `http://localhost:8080/ban_hang/admin/dashboard` hoặc `http://localhost:8080/ban_hang/admin/products`.
- **Kết quả mong đợi:** Request bị chặn ngay lập tức và tự động chuyển hướng về `/login`.
- **Kết quả thực tế:** Hệ thống chặn thành công 100%, bảo mật nghiêm ngặt.
- **Trạng thái:** **PASSED**

---

### [TC-11] Thêm sản phẩm mới trong trang Admin
- **Mục tiêu:** Kiểm tra thao tác thêm sản phẩm của quản trị viên.
- **Các bước thực hiện:**
  1. Tại trang Admin Products, nhấn nút "Thêm sản phẩm mới".
  2. Nhập: Tên sản phẩm = `Áo Polo Thể Thao Mới`, Giá = `350000`, Danh mục = `Áo Nam`.
  3. Nhấn "Lưu sản phẩm".
- **Kết quả mong đợi:** Sản phẩm xuất hiện ngay trong bảng danh sách sản phẩm quản trị và hiển thị trên trang chủ người dùng.
- **Kết quả thực tế:** Dữ liệu lưu và cập nhật tức thì.
- **Trạng thái:** **PASSED**

---

### [TC-12] Xem danh sách Showroom trên bản đồ số
- **Mục tiêu:** Kiểm tra trang giới thiệu hệ thống cửa hàng và bản đồ.
- **Các bước thực hiện:**
  1. Truy cập `http://localhost:8080/ban_hang/stores`.
  2. Quan sát bản đồ Leaflet Map và nhấp vào chi nhánh "HighFashion Flagship Sư Vạn Hạnh".
- **Kết quả mong đợi:** Bản đồ hiển thị rõ nét bản đồ đường phố, ghim chính xác địa chỉ và popup thông tin giờ mở cửa.
- **Kết quả thực tế:** Bản đồ tương tác mượt mà, tọa độ GPS hiển thị chuẩn xác.
- **Trạng thái:** **PASSED**
