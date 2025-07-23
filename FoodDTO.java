package model;

import java.util.List;
import java.util.UUID;



public class FoodDTO {
    private UUID itemId;
    private String name;
    private String imageBase64;
    private String description;
    private UUID vendorId; // تغییر از String به UUID
    private Integer price;
    private Integer supply;
    private Double rate;
    private List<String> categories;

    public FoodDTO() {}

    public FoodDTO(UUID itemId, String name, String imageBase64, String description, UUID vendorId,
                   Integer price, Integer supply, List<String> categories) {
        this.itemId = itemId;
        this.name = name;
        this.imageBase64 = imageBase64;
        this.description = description;
        this.vendorId = vendorId;
        this.price = price;
        this.supply = supply;
        this.categories = categories;
    }
    public FoodDTO(UUID itemId, String name, String imageBase64, String description, UUID vendorId, Integer price, Integer supply, Double rate, List<String> categories) {
        this.itemId = itemId;
        this.name = name;
        this.imageBase64 = imageBase64;
        this.description = description;
        this.vendorId = vendorId;
        this.price = price;
        this.supply = supply;
        this.rate = rate;
        this.categories = categories;
    }
    public UUID getItemId() { return itemId; }
    public void setItemId(UUID itemId) { this.itemId = itemId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getImageBase64() { return imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public UUID getVendorId() { return vendorId; } // تغییر به UUID
    public void setVendorId(UUID vendorId) { this.vendorId = vendorId; } // تغییر به UUID
    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }
    public Integer getSupply() { return supply; }
    public void setSupply(Integer supply) { this.supply = supply; }
    public List<String> getCategories() { return categories; }
    public void setCategories(List<String> categories) { this.categories = categories; }
    public Double getRate() { return rate; }
    public void setRate(Double rate) { this.rate = rate; }
}