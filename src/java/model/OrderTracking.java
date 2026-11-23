package model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/**
 * Lớp OrderTracking: Đại diện cho nhật ký hành trình đơn hàng chi tiết (bảng `order_tracking`)
 */
public class OrderTracking implements Serializable {
    private int id;
    private int orderId;
    private String status;
    private String statusName;
    private Integer actorId;
    private String actorName;
    private Integer actorRole;
    private String location;
    private String note;
    private Timestamp createdAt;

    public OrderTracking() {
    }

    public OrderTracking(int id, int orderId, String status, String statusName, Integer actorId, String actorName, Integer actorRole, String location, String note, Timestamp createdAt) {
        this.id = id;
        this.orderId = orderId;
        this.status = status;
        this.statusName = statusName;
        this.actorId = actorId;
        this.actorName = actorName;
        this.actorRole = actorRole;
        this.location = location;
        this.note = note;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public Integer getActorId() {
        return actorId;
    }

    public void setActorId(Integer actorId) {
        this.actorId = actorId;
    }

    public String getActorName() {
        return actorName;
    }

    public void setActorName(String actorName) {
        this.actorName = actorName;
    }

    public Integer getActorRole() {
        return actorRole;
    }

    public void setActorRole(Integer actorRole) {
        this.actorRole = actorRole;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getFormattedTime() {
        if (createdAt == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
        return sdf.format(createdAt);
    }
}
