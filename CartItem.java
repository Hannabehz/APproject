package model;

import com.google.gson.annotations.SerializedName;

public class CartItem {
    @SerializedName("item_id")
    private String itemId;
    @SerializedName("vendor_id")
    private String vendorId;
    @SerializedName("name")
    private String name;
    @SerializedName("description")
    private String description;
    @SerializedName("price")
    private double price;
    @SerializedName("quantity")
    private int quantity;
    @SerializedName("categories")
    private String categories;
    @SerializedName("supply")
    private int supply;
    @SerializedName("image_base64")
    private String imageBase64;

    // سازنده پیش‌فرض برای Gson
    public CartItem() {
    }

    public CartItem(String itemId, String vendorId, String name, String description, double price, int quantity, String categories, int supply, String imageBase64) {
        this.itemId = itemId;
        this.vendorId = vendorId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.categories = categories;
        this.supply = supply;
        this.imageBase64 = imageBase64;
    }

    // Getters و Setters
    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getVendorId() {
        return vendorId;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getCategories() {
        return categories;
    }

    public void setCategories(String categories) {
        this.categories = categories;
    }

    public int getSupply() {
        return supply;
    }

    public void setSupply(int supply) {
        this.supply = supply;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }
}