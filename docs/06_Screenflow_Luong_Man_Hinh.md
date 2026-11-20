# Tài liệu Screen Flow — HighFashion

| Thuộc tính | Giá trị |
|---|---|
| Dự án | Website thương mại điện tử thời trang HighFashion |
| Phiên bản tài liệu | 1.0 |
| Ngày lập | 20/11/2026 |
| Phạm vi | Các màn hình, điều hướng, nhánh thành công, lỗi và phân quyền đang được triển khai |

## 1. Mục đích và quy ước

Tài liệu mô tả cách người dùng đi qua các màn hình của website theo luồng thực tế trong mã nguồn. Đây là đặc tả luồng màn hình hiện tại, không phải danh sách tính năng dự kiến.

- `{GET}`: mở màn hình hoặc đọc dữ liệu.
- `{POST}`: gửi biểu mẫu hoặc thực hiện thay đổi dữ liệu.
- `Forward`: máy chủ hiển thị JSP trong cùng request.
- `Redirect`: trình duyệt chuyển sang URL khác.
- `Modal`: hộp thoại nằm trong màn hình hiện tại, không phải route riêng.
- `Guest`: khách chưa đăng nhập; `Customer`: khách hàng; `Admin`: quản trị viên.
- Mọi URL bên dưới đều nằm dưới context path của ứng dụng, ví dụ `/ban_hang`.

## 2. Bản đồ màn hình và route

| Màn hình | Route / JSP | Quyền và cách vào |
|---|---|---|
| Trang chủ | `GET /home` → `index.jsp` | Công khai; route gốc cũng được ánh xạ tới trang chủ |
| Cửa hàng / danh sách sản phẩm | `GET /products` → `shop.jsp` | Công khai; tìm kiếm, lọc và sắp xếp qua query string |
| Chi tiết sản phẩm | `GET /product-detail?id=…` → `detail.jsp` | Công khai; `/detail` là alias cũ |
| Cửa hàng và bản đồ | `GET /stores` → `stores.jsp` | Công khai |
| Đăng nhập | `GET, POST /login` → `login.jsp` | Công khai |
| Đăng ký | `GET, POST /register` → `register.jsp` | Công khai |
| Quên mật khẩu | `GET, POST /forgot-password` → `forgot-password.jsp` | Công khai |
| Giỏ hàng | `GET, POST /cart` → `cart.jsp` | Chỉ khách hàng đã đăng nhập; admin bị chuyển về dashboard |
| Thanh toán | `GET, POST /checkout` → `checkout.jsp` | Chỉ khách hàng đã đăng nhập và giỏ không rỗng |
| Đặt hàng thành công | POST `/checkout` → `order-success.jsp` | Chỉ xuất hiện sau khi tạo đơn thành công |
| Đơn mua của tôi | `GET, POST /my-orders` → `my-orders.jsp` | Chỉ khách hàng; `/user/orders` là alias |
| Thông tin cá nhân | `GET, POST /account` → `account.jsp` | Mọi tài khoản đã đăng nhập |
| Dashboard quản trị | `GET /admin/dashboard` → `admin-dashboard.jsp` | Chỉ admin |
| Quản lý sản phẩm | `GET, POST /admin/products` → `admin-products.jsp` | Chỉ admin |
| Quản lý cửa hàng | `GET, POST /admin/stores` → `admin-stores.jsp` | Chỉ admin |
| Quản lý tài khoản | `GET, POST /admin/users` → `admin-users.jsp` | Chỉ admin |
| Đăng xuất | `GET, POST /logout` | Hủy session, sau đó về trang chủ |

Các JSP là màn hình trình bày; luồng bình thường truy cập qua route Servlet tương ứng. Những biểu mẫu quản trị dùng modal nhưng vẫn thuộc màn hình quản trị hiện tại.

## 3. Luồng điều hướng tổng quan

```mermaid
flowchart TD
    Entry["Mở website"] --> Home["Trang chủ /home"]
    Home --> Shop["Cửa hàng /products"]
    Home --> Stores["Cửa hàng và bản đồ /stores"]
    Home --> Login["Đăng nhập /login"]
    Home --> Register["Đăng ký /register"]
    Shop --> Detail["Chi tiết sản phẩm"]
    Detail --> Shop
    Detail --> CartAction{"Thêm giỏ / mua ngay"}
    CartAction -->|Chưa đăng nhập| Login
    CartAction -->|Đã đăng nhập, Customer| DetailAction{"Thao tác"}
    CartAction -->|Admin| Dashboard["Dashboard /admin/dashboard"]
    DetailAction -->|Thêm giỏ| Detail
    DetailAction -->|Mua ngay| Checkout["Thanh toán /checkout"]
    Detail -->|Mở biểu tượng giỏ| Cart["Giỏ hàng /cart"]
    Cart --> Checkout["Thanh toán /checkout"]
    Checkout --> Success["Đặt hàng thành công"]
    Success --> MyOrders["Đơn mua /my-orders"]
    Home --> MyOrders
    Home --> Account["Thông tin cá nhân /account"]
    Home --> AdminGate{"Vai trò"}
    AdminGate -->|Admin| Dashboard
    AdminGate -->|Customer| Shop
    Login -->|Đăng nhập thành công, Customer| Home
    Login -->|Đăng nhập thành công, Admin| Dashboard
    Home --> Logout["Đăng xuất /logout"]
    Dashboard --> Logout
    Logout --> Home

    subgraph Admin["Các màn hình quản trị — chỉ Admin"]
        Dashboard --> Products["Quản lý sản phẩm /admin/products"]
        Dashboard --> Branches["Quản lý cửa hàng /admin/stores"]
        Dashboard --> Users["Quản lý tài khoản /admin/users"]
        Products --> Dashboard
        Branches --> Dashboard
        Users --> Dashboard
    end
```

### Điều hướng theo phiên và vai trò

1. Người chưa đăng nhập có thể xem trang chủ, danh sách/chi tiết sản phẩm và cửa hàng.
2. Khi khách chưa đăng nhập truy cập giỏ hàng, thanh toán hoặc đơn mua, website lưu đích cần quay lại trong session và yêu cầu đăng nhập. Sau đăng nhập, khách được chuyển về đích đó.
3. Nếu admin đi vào giỏ hàng, thanh toán hoặc đơn mua cá nhân, ứng dụng đưa về dashboard quản trị thay vì cho thực hiện mua hàng.
4. Các route `/admin/*` kiểm tra session và quyền admin ở phía máy chủ. Người không đủ quyền được chuyển đến trang đăng nhập.
5. Đăng xuất hủy toàn bộ session hiện tại, bao gồm giỏ hàng gắn với session.

## 4. Luồng khám phá và mua hàng

```mermaid
flowchart TD
    Home["Trang chủ"] -->|Danh mục, sản phẩm nổi bật, tìm kiếm| Shop["Cửa hàng /products"]
    Shop --> Filters["Từ khóa, danh mục, sale, size, màu, giá, sắp xếp"]
    Filters --> Results{"Có sản phẩm?"}
    Results -->|Có| Detail["Chi tiết sản phẩm /product-detail?id=…"]
    Results -->|Không| ShopEmpty["Danh sách trống / xóa bộ lọc để xem lại"]
    ShopEmpty --> Shop
    Detail --> ValidProduct{"Sản phẩm tồn tại?"}
    ValidProduct -->|Không| Shop
    ValidProduct -->|Có| Choose["Chọn size, màu và số lượng"]
    Choose --> Add{"Thêm giỏ hoặc Mua ngay"}
    Add --> Logged{"Đã đăng nhập?"}
    Logged -->|Chưa| Login["Đăng nhập /login"]
    Login -->|Thất bại| Login
    Login -->|Customer thành công| ReturnTarget["Quay lại trang chi tiết đã lưu"]
    ReturnTarget --> Choose
    Logged -->|Admin| AdminInfo["Thông báo không hỗ trợ mua hàng"]
    AdminInfo --> Dashboard["Dashboard quản trị"]
    Logged -->|Customer| Stock{"Size còn đủ tồn kho?"}
    Stock -->|Không| DetailError["Báo hết hàng / số lượng khả dụng"]
    DetailError --> Choose
    Stock -->|Có| BuyType{"Chọn thao tác"}
    BuyType -->|Thêm vào giỏ| AddResult["Thêm thành công qua AJAX; ở lại chi tiết"]
    AddResult --> Cart["Mở giỏ /cart khi người dùng chọn"]
    BuyType -->|Mua ngay| Checkout["Thanh toán /checkout"]
    Cart --> Update{"Cập nhật, xóa món hoặc tiếp tục"}
    Update -->|Cập nhật số lượng| CartStock{"Số lượng mới không vượt tồn kho size?"}
    CartStock -->|Có| CartUpdated["Cập nhật giỏ và tổng tiền"]
    CartStock -->|Không| CartError["Báo tồn kho khả dụng, giữ giỏ"]
    CartUpdated --> Cart
    CartError --> Cart
    Update -->|Xóa món| Cart
    Update -->|Tiếp tục mua| Shop
    Update -->|Tiến hành đặt hàng| Empty{"Giỏ rỗng?"}
    Empty -->|Có| Cart
    Empty -->|Không| Checkout
    Checkout --> Recheck{"Session, vai trò và giỏ hợp lệ?"}
    Recheck -->|Không đăng nhập| Login
    Recheck -->|Admin| Dashboard
    Recheck -->|Giỏ rỗng| Cart
    Recheck -->|Hợp lệ| Submit["Nhập người nhận, địa chỉ, liên hệ, ghi chú, thanh toán"]
    Submit --> Create{"Tạo đơn và trừ tồn kho thành công?"}
    Create -->|Không| CheckoutError["Hiển thị lỗi, giữ luồng thanh toán"]
    CheckoutError --> Checkout
    Create -->|Có| OrderSuccess["Đặt hàng thành công /order-success.jsp"]
    OrderSuccess --> MyOrders["Đơn mua của tôi /my-orders"]
    OrderSuccess --> Shop
```

### Hành vi các màn hình mua sắm

- **Trang chủ:** mở sản phẩm nổi bật/mới, danh mục, liên kết cửa hàng, bản đồ và giỏ.
- **Danh sách:** nhận các tham số `keyword`, `categoryId`, `filter`, `sort`, `size`, `color`, `priceRange`. Kết quả có thể rỗng; người dùng có thể xóa bộ lọc để quay về danh sách.
- **Chi tiết:** ID sản phẩm không hợp lệ hoặc không tồn tại sẽ chuyển về `/products`. Người mua chọn biến thể size/màu và số lượng; thiếu tồn kho thì nhận thông báo và ở lại trang chi tiết.
- **Giỏ:** mặt hàng được phân biệt theo sản phẩm, size và màu; số lượng bị giới hạn bởi tồn kho theo size. Có thể cập nhật hoặc xóa mặt hàng trước khi thanh toán.
- **Thanh toán:** kiểm tra đăng nhập, vai trò và giỏ không rỗng. Tạo đơn thành công mới xóa giỏ; lỗi tạo đơn được báo và người dùng quay lại luồng thanh toán.
- Tồn kho được kiểm tra khi thêm/cập nhật giỏ và khi tạo đơn; đơn đã tạo giữ thông tin các mặt hàng cùng size/màu đã chọn.

## 5. Luồng tài khoản và xác thực

```mermaid
flowchart TD
    Guest["Khách / tài khoản"] --> Login["Đăng nhập /login"]
    Guest --> Register["Đăng ký /register"]
    Login --> LoginValid{"Thông tin đúng?"}
    LoginValid -->|Không| LoginError["Thông báo sai tên đăng nhập hoặc mật khẩu"]
    LoginError --> Login
    LoginValid -->|Có, Customer| CustomerHome["Trang chủ hoặc URL yêu cầu trước đăng nhập"]
    LoginValid -->|Có, Admin| AdminHome["Dashboard quản trị hoặc URL hợp lệ đã lưu"]
    Register --> RegisterForm["Nhập username, mật khẩu, xác nhận, họ tên, email, điện thoại, địa chỉ"]
    RegisterForm --> RegisterCheck{"Dữ liệu hợp lệ, username chưa tồn tại?"}
    RegisterCheck -->|Không| RegisterError["Báo lỗi trên form đăng ký"]
    RegisterError --> Register
    RegisterCheck -->|Có| RegisterSave{"Lưu tài khoản thành công?"}
    RegisterSave -->|Không| RegisterError
    RegisterSave -->|Có| LoginSuccessMsg["Thông báo đăng ký thành công trên trang đăng nhập"]
    LoginSuccessMsg --> Login
    Login --> Forgot["Quên mật khẩu /forgot-password"]
    Forgot --> SendCode["Nhập email và yêu cầu mã"]
    SendCode --> MailReady{"SMTP cấu hình và gửi được?"}
    MailReady -->|Không| MailError["Báo chưa cấu hình hoặc gửi email thất bại"]
    MailReady -->|Có| Verify["Nhập mã 6 số"]
    Verify --> CodeValid{"Mã đúng, còn hạn và chưa vượt số lần thử?"}
    CodeValid -->|Sai / hết hạn| VerifyError["Báo lỗi; tối đa 5 lần thử"]
    VerifyError --> Verify
    CodeValid -->|Đủ 5 lần sai hoặc hết hạn| SendCode
    CodeValid -->|Đúng| NewPassword["Nhập mật khẩu mới và xác nhận"]
    NewPassword --> ResetValid{"Mật khẩu hợp lệ, xác nhận khớp?"}
    ResetValid -->|Không| NewPassword
    ResetValid -->|Có| ResetSave{"Cập nhật mật khẩu thành công?"}
    ResetSave -->|Không| NewPassword
    ResetSave -->|Có| LoginReset["Thông báo thành công, chuyển đến đăng nhập"]
    LoginReset --> Login
    CustomerHome --> Account["Thông tin cá nhân /account"]
    AdminHome --> Account
    Account --> Profile{"Chọn cập nhật hồ sơ hay đổi mật khẩu"}
    Profile -->|Hồ sơ| ProfileForm["Sửa họ tên, email, điện thoại, địa chỉ"]
    ProfileForm --> ProfileSaved{"Dữ liệu hợp lệ và lưu được?"}
    ProfileSaved -->|Có| Account
    ProfileSaved -->|Không| AccountError["Thông báo lỗi"]
    AccountError --> Account
    Profile -->|Mật khẩu| PasswordForm["Mật khẩu hiện tại, mật khẩu mới, xác nhận"]
    PasswordForm --> PasswordSaved{"Mật khẩu hiện tại đúng và dữ liệu hợp lệ?"}
    PasswordSaved -->|Có| Account
    PasswordSaved -->|Không| AccountError
    CustomerHome --> Logout["Đăng xuất /logout"]
    AdminHome --> Logout
    Logout --> Home["Trang chủ, session bị hủy"]
```

### Quy tắc quên mật khẩu

- Email chưa đăng ký không được tiết lộ rõ ràng; giao diện dùng thông báo chung.
- Mã xác nhận có 6 chữ số, hiệu lực 10 phút; tối đa 5 lần nhập sai.
- Sau khi xác minh, thời gian đặt mật khẩu mới là 5 phút; yêu cầu gửi lại mã có thời gian chờ 60 giây.
- Cần cấu hình `GMAIL_SMTP_USERNAME` và `GMAIL_SMTP_APP_PASSWORD` cho tiến trình Tomcat để gửi thư.

## 6. Luồng đơn mua của khách hàng

```mermaid
flowchart TD
    Entry["Mở /my-orders"] --> Auth{"Customer đã đăng nhập?"}
    Auth -->|Chưa| Login["Chuyển đăng nhập, sau đó quay lại đơn mua"]
    Auth -->|Admin| AdminInfo["Thông báo quản lý đơn tại dashboard"]
    Auth -->|Có| Orders["Đơn mua của tôi"]
    Orders --> Tabs["Chọn Tất cả / Chờ xác nhận / Chờ lấy hàng / Chờ giao / Đã giao / Trả hàng / Đã hủy"]
    Tabs --> Search["Tìm theo mã đơn, tên khách, sản phẩm hoặc shop"]
    Search --> OrderCard["Danh sách đơn trong tab"]
    OrderCard --> Details["Mở modal chi tiết đơn"]
    OrderCard --> Action{"Thao tác khả dụng theo trạng thái"}
    Action -->|Đơn chờ xác nhận| Cancel["Hủy đơn"]
    Action -->|Đơn đang giao| Received["Xác nhận đã nhận"]
    Action -->|Đơn đã giao| Return["Yêu cầu trả hàng"]
    Action -->|Mua lại| Repurchase["Thêm các mặt hàng vào giỏ"]
    Cancel --> CancelResult{"Đơn còn được hủy?"}
    CancelResult -->|Có| Cancelled["Thông báo thành công, mở tab Đã hủy"]
    CancelResult -->|Không| ActionError["Thông báo thao tác không hợp lệ"]
    Received --> ReceivedResult{"Đơn đang ở trạng thái giao hàng?"}
    ReceivedResult -->|Có| Delivered["Thông báo thành công, mở tab Đã giao"]
    ReceivedResult -->|Không| ActionError
    Return --> ReturnResult{"Yêu cầu trả hàng được chấp nhận?"}
    ReturnResult -->|Có| Returned["Thông báo thành công, mở tab Trả hàng"]
    ReturnResult -->|Không| ActionError
    Repurchase --> Cart["Giỏ hàng /cart"]
    Cancelled --> Orders
    Delivered --> Orders
    Returned --> Orders
    ActionError --> Orders
```

Tab trạng thái được lọc theo nhóm trạng thái tương ứng: chờ xác nhận, chờ lấy hàng, chờ giao, đã giao, trả hàng/hoàn tiền và đã hủy. Các thao tác cập nhật trả về trang đơn mua kèm thông báo kết quả.

## 7. Luồng quản trị

```mermaid
flowchart TD
    AdminRoute["Mở /admin/*"] --> IsAdmin{"Có session admin?"}
    IsAdmin -->|Không| Login["Chuyển tới /login"]
    IsAdmin -->|Có| Dashboard["Dashboard quản trị"]
    Dashboard --> OrderAction{"Quản lý đơn hàng"}
    OrderAction -->|Mở chi tiết| OrderModal["Modal chi tiết đơn"]
    OrderAction -->|Cập nhật trạng thái| Confirm["Modal xác nhận"]
    OrderAction -->|Xóa một đơn| Confirm
    OrderAction -->|Xóa tất cả| Confirm
    Confirm --> OrderResult["Thông báo kết quả, tải lại dashboard"]
    Dashboard --> ProductPage["Quản lý sản phẩm"]
    ProductPage --> ProductAction{"Thêm / sửa / cập nhật tồn kho / xóa"}
    ProductAction --> ProductModal["Modal biểu mẫu"]
    ProductModal --> ProductValid{"Dữ liệu và ảnh hợp lệ?"}
    ProductValid -->|Không| ProductError["Thông báo lỗi và quay lại danh sách"]
    ProductValid -->|Có| ProductSaved["Lưu sản phẩm/tồn kho, báo kết quả"]
    ProductAction --> ProductDelete["Xác nhận xóa qua hộp thoại"]
    ProductDelete --> ProductSaved
    Dashboard --> StorePage["Quản lý cửa hàng"]
    StorePage --> StoreAction{"Thêm hoặc xóa chi nhánh"}
    StoreAction --> StoreForm["Modal thêm / xác nhận xóa"]
    StoreForm --> StoreSaved["Thực hiện thao tác, tải lại danh sách"]
    Dashboard --> UserPage["Quản lý tài khoản"]
    UserPage --> UserAction{"Tìm kiếm / sửa / xóa tài khoản"}
    UserAction --> UserSearch["Lọc theo username, họ tên, email, điện thoại"]
    UserAction --> UserModal["Modal sửa hoặc xác nhận xóa"]
    UserModal --> UserValid{"Dữ liệu / quyền hợp lệ?"}
    UserValid -->|Không| UserError["Thông báo lỗi"]
    UserValid -->|Có| UserSaved["Lưu thay đổi, tải lại danh sách"]
    ProductError --> ProductPage
    ProductSaved --> ProductPage
    StoreSaved --> StorePage
    UserSearch --> UserPage
    UserError --> UserPage
    UserSaved --> UserPage
```

### Chi tiết các phân hệ quản trị

#### Dashboard và xử lý đơn

- Dashboard tổng hợp số sản phẩm, số đơn, đơn chờ xác nhận và doanh thu; hiển thị bảng đơn gần đây.
- Trạng thái đơn quyết định các thao tác gợi ý: chờ xác nhận → xác nhận hoặc hủy; chờ lấy hàng → chuyển giao hoặc hủy; đang giao → xác nhận đã giao hoặc trả hàng; các trạng thái khác có thể khôi phục về chờ xác nhận theo giao diện.
- Xóa đơn hoặc xóa tất cả yêu cầu xác nhận trước khi thực hiện.
- Mọi thao tác trả thông báo thành công/lỗi và tải lại dashboard.

#### Sản phẩm và tồn kho

- Thêm/sửa sản phẩm trong modal; nhập thông tin, danh mục, size và số lượng tồn kho theo từng size.
- Có thể cập nhật tồn kho theo size từ modal sửa; xóa sản phẩm có hộp thoại xác nhận.
- Ảnh tải lên giới hạn 5 MB; chỉ nhận JPEG, PNG, GIF hoặc WebP đúng định dạng.
- Lỗi nhập liệu, ảnh hoặc lưu dữ liệu hiển thị thành thông báo trên trang quản lý.

#### Cửa hàng

- Danh sách cửa hàng có thao tác thêm và xóa theo giao diện hiện tại.
- Khi thêm, nhập tên, địa chỉ, thành phố, điện thoại, giờ mở cửa, tọa độ và trạng thái cửa hàng chính.
- Mã controller hiện có `add` và `delete`; tài liệu không mô tả chức năng sửa chi nhánh vì chưa thấy luồng cập nhật trong implementation hiện tại.

#### Tài khoản

- Tìm kiếm theo tên đăng nhập, họ tên, email và số điện thoại.
- Có thể sửa thông tin, quyền và tùy chọn đặt mật khẩu mới; có thể xóa tài khoản khác.
- Không thể tự xóa tài khoản đang đăng nhập hoặc tự hạ quyền quản trị; không thể xóa/hạ quyền admin cuối cùng.

## 8. Các luồng phụ và kết quả thay thế

| Tình huống | Kết quả màn hình / điều hướng |
|---|---|
| URL chi tiết không có sản phẩm tương ứng | Chuyển về `/products` |
| Thêm/cập nhật số lượng vượt tồn kho theo size | Báo số lượng còn khả dụng; giữ người dùng ở chi tiết hoặc giỏ |
| Gửi cart bằng AJAX nhưng chưa đăng nhập | Trả trạng thái yêu cầu đăng nhập cùng URL điều hướng |
| Admin truy cập giỏ, thanh toán hoặc đơn cá nhân | Thông báo và chuyển về dashboard |
| Giỏ rỗng khi vào thanh toán | Chuyển về `/cart` |
| Đăng ký sai dữ liệu, trùng tên đăng nhập hoặc lưu lỗi | Giữ form đăng ký và hiển thị lỗi |
| Đăng nhập sai | Giữ form đăng nhập và thông báo lỗi |
| Quên mật khẩu: SMTP chưa cấu hình/gửi lỗi | Giữ màn hình quên mật khẩu và hiển thị lỗi |
| Mã sai, hết hạn hoặc hết lượt | Cho thử lại trong giới hạn; hết lượt/hết hạn thì yêu cầu mã mới |
| Truy cập route quản trị không có quyền | Chuyển về `/login` |
| Lỗi CRUD quản trị | Thông báo toast và tải lại màn hình quản lý tương ứng |
| Đăng xuất | Hủy session, chuyển `/home` |

## 9. Phạm vi và lưu ý triển khai

- Trang cửa hàng hiện chọn chi nhánh chính để hiển thị; bản đồ và danh sách dùng dữ liệu chi nhánh do controller cung cấp.
- Hình thức thanh toán được ghi trong đơn theo dữ liệu form; controller dùng COD nếu không nhận được phương thức thanh toán. Không có luồng cổng thanh toán bên ngoài trong các route được rà soát.
- Tạo đơn thất bại không đi tới màn hình thành công; thông báo lỗi được hiển thị trong luồng checkout.
- Modal xác nhận là thành phần của trang, không phải màn hình/route riêng.
- Bảng trên mô tả luồng khách quan sát được. Các chi tiết dữ liệu, quy tắc nghiệp vụ và kiểm thử đầy đủ nằm trong [SRS](./01_SRS_Dac_Ta_Yeu_Cau_Phan_Mem.md), [Thiết kế kiến trúc](./02_Thiet_Ke_Kien_Truc_He_Thong.md) và [Test Cases](./04_Kich_Ban_Kiem_Thu_Test_Cases.md).
