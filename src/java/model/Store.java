package model;

import java.io.Serializable;

/**
 * Lớp Store: Đại diện cho thực thể Cửa hàng / Chi nhánh phân phối (bảng `stores` trong CSDL)
 * Chứa thông tin về địa chỉ, số điện thoại, giờ phục vụ, tọa độ GPS (kinh độ, vĩ độ)
 * để hiển thị bản đồ trực quan cho khách hàng tìm đến mua sắm trực tiếp.
 */
public class Store implements Serializable {
    // Mã định danh chi nhánh cửa hàng (Khóa chính)
    private int id;
    // Tên chi nhánh (ví dụ: "HighFashion Flagship Sư Vạn Hạnh")
    private String name;
    // Địa chỉ chi tiết của cửa hàng
    private String address;
    // Tên thành phố / Tỉnh (ví dụ: TP.HCM, Hà Nội, Đà Nẵng)
    private String city;
    // Số điện thoại đường dây nóng của cửa hàng
    private String phone;
    // Giờ mở cửa hoạt động (ví dụ: "08:30 - 22:00")
    private String hours;
    // Tọa độ vĩ độ (Latitude) để hiển thị trên bản đồ số
    private double latitude;
    // Tọa độ kinh độ (Longitude) để hiển thị trên bản đồ số
    private double longitude;
    // Cờ đánh dấu cửa hàng Flagship/Chi nhánh chính
    private boolean isMain;

    /**
     * Hàm khởi tạo không đối số mặc định
     */
    public Store() {
    }

    /**
     * Hàm khởi tạo có đầy đủ tham số
     * @param id Mã cửa hàng
     * @param name Tên cửa hàng
     * @param address Địa chỉ chi tiết
     * @param city Khu vực thành phố
     * @param phone Số điện thoại liên hệ
     * @param hours Giờ mở cửa
     * @param latitude Vĩ độ
     * @param longitude Kinh độ
     * @param isMain Cờ chi nhánh chính
     */
    public Store(int id, String name, String address, String city, String phone, String hours, double latitude, double longitude, boolean isMain) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.city = city;
        this.phone = phone;
        this.hours = hours;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isMain = isMain;
    }

    /**
     * Lấy mã cửa hàng
     * @return int
     */
    public int getId() {
        return id;
    }

    /**
     * Gán mã cửa hàng
     * @param id Mã cửa hàng
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Lấy tên cửa hàng
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Gán tên cửa hàng
     * @param name Tên mới
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Lấy địa chỉ cửa hàng
     * @return String
     */
    public String getAddress() {
        return address;
    }

    /**
     * Gán địa chỉ cửa hàng
     * @param address Địa chỉ
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Lấy thành phố của cửa hàng
     * @return String
     */
    public String getCity() {
        return city;
    }

    /**
     * Gán thành phố của cửa hàng
     * @param city Tên thành phố
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Lấy số điện thoại cửa hàng
     * @return String
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Gán số điện thoại cửa hàng
     * @param phone Số điện thoại
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Lấy giờ hoạt động của cửa hàng
     * @return String
     */
    public String getHours() {
        return hours;
    }

    /**
     * Gán giờ hoạt động của cửa hàng
     * @param hours Chuỗi giờ mở cửa
     */
    public void setHours(String hours) {
        this.hours = hours;
    }

    /**
     * Lấy tọa độ vĩ độ (latitude)
     * @return double
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Gán tọa độ vĩ độ
     * @param latitude Vĩ độ
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * Lấy tọa độ kinh độ (longitude)
     * @return double
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Gán tọa độ kinh độ
     * @param longitude Kinh độ
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * Kiểm tra đây có phải là cửa hàng chính / Flagship không
     * @return true nếu là chi nhánh chính, ngược lại false
     */
    public boolean isIsMain() {
        return isMain;
    }

    /**
     * Thiết lập cờ chi nhánh chính
     * @param isMain true hoặc false
     */
    public void setIsMain(boolean isMain) {
        this.isMain = isMain;
    }
}
