package model;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;
import java.util.UUID;

public class Order {
    @SerializedName("order_id")
    private UUID id;

    @SerializedName("delivery_address")
    private String deliveryAddress;

    @SerializedName("restaurant")
    private Restaurant restaurant;

    @SerializedName("pay_price")
    private int payPrice;

    @SerializedName("created_at")
    private LocalDateTime createdAt;

    @SerializedName("status")
    private String status;

    @SerializedName("delivery_status")
    private String deliveryStatus;

    // سازنده پیش‌فرض برای Gson
    public Order() {
    }

    // سازنده با پارامترها
    public Order(UUID id, String deliveryAddress, Restaurant restaurant, int payPrice, LocalDateTime createdAt, String status, String deliveryStatus) {
        this.id = id;
        this.deliveryAddress = deliveryAddress;
        this.restaurant = restaurant;
        this.payPrice = payPrice;
        this.createdAt = createdAt;
        this.status = status;
        this.deliveryStatus = deliveryStatus;
    }

    // Getters و Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public String getRestaurantName() {
        return restaurant != null ? restaurant.getName() : "";
    }

    public int getPayPrice() {
        return payPrice;
    }

    public void setPayPrice(int payPrice) {
        this.payPrice = payPrice;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }
}