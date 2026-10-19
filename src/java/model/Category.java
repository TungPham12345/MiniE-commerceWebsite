package model;

import java.io.Serializable;

/**
 * Lớp Category: Mô hình dữ liệu đại diện cho một Danh mục sản phẩm (bảng categories)
 * Implement Serializable để có thể truyền dữ liệu qua Session hoặc Cache
 * Ví dụ các danh mục: Áo Nam, Quần Nam, Thời Trang Nữ, Phụ Kiện Thời Trang
 */
public class Category implements Serializable {
    // Mã định danh danh mục (Khóa chính tự tăng trong CSDL)
    private int id;
    // Tên hiển thị của danh mục (ví dụ: "Áo Nam")
    private String name;
    // Mô tả ngắn gọn về danh mục sản phẩm
    private String description;
    // Tên class icon Bootstrap Icons hiển thị trên giao diện (ví dụ: "bi-person-badge")
    private String icon;

    /**
     * Hàm khởi tạo không đối số mặc định
     */
    public Category() {
    }

    /**
     * Hàm khởi tạo có đầy đủ tham số
     * @param id Mã danh mục
     * @param name Tên danh mục
     * @param description Mô tả danh mục
     * @param icon Tên icon hiển thị
     */
    public Category(int id, String name, String description, String icon) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.icon = icon;
    }

    /**
     * Lấy mã định danh danh mục
     * @return int
     */
    public int getId() {
        return id;
    }

    /**
     * Gán mã định danh danh mục
     * @param id Mã danh mục
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Lấy tên danh mục
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Gán tên danh mục
     * @param name Tên mới
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Lấy mô tả danh mục
     * @return String
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gán mô tả danh mục
     * @param description Mô tả mới
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Lấy tên class icon Bootstrap
     * @return String (ví dụ: "bi-gem")
     */
    public String getIcon() {
        return icon;
    }

    /**
     * Gán icon mới cho danh mục
     * @param icon Chuỗi class icon
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }
}
