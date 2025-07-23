package model;
import com.google.gson.annotations.SerializedName;


import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class OrderResponseDTO {
    @SerializedName("order_id")
    private UUID orderId;
    @SerializedName("buyer_id")
    private UUID buyerId;
    @SerializedName("vendor_id")
    private UUID vendorId;
    @SerializedName("restaurant_name")
    private String restaurantName;
    @SerializedName("delivery_address")
    private String deliveryAddress;
    @SerializedName("status")
    private String status;
    @SerializedName("deliveryStatus")
    private String deliveryStatus;
    @SerializedName("pay_price")
    private double payPrice;
    @SerializedName("created_at")
    private LocalDateTime createdAt;
    @SerializedName("items")
    private List<OrderItemDTO> items;

    public OrderResponseDTO() {
    }

    // Getters و Setters
    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public UUID getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(UUID buyerId) {
        this.buyerId = buyerId;
    }

    public UUID getVendorId() {
        return vendorId;
    }

    public void setVendorId(UUID vendorId) {
        this.vendorId = vendorId;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getPayPrice() {
        return payPrice;
    }

    public void setPayPrice(double payPrice) {
        this.payPrice = payPrice;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<OrderItemDTO> getItems() {
        return items;
    }

    public void setItems(List<OrderItemDTO> items) {
        this.items = items;
    }
    public String getDeliveryStatus() {
        return deliveryStatus;
    }
    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }
}