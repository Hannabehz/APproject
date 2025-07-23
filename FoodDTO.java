package dto;

import java.util.List;
import java.util.UUID;


public class FoodDTO {
    private String id;
    private String name;
    private String imageBase64;
    private String description;
    private String vendorId;
    private Integer price;
    private Integer supply;
    private Double rate;
    private List<String> categories;

    public FoodDTO() {}

    public FoodDTO(String id,String name, String imageBase64, String description, String vendorId,
                   Integer price, Integer supply, List<String> categories) {
        this.id = id;
        this.name = name;
        this.imageBase64 = imageBase64;
        this.description = description;
        this.vendorId = vendorId;
        this.price = price;
        this.supply = supply;
        this.categories = categories;
    }
    public FoodDTO(String itemId, String name, String imageBase64, String description, String vendorId, Integer price, Integer supply, Double rate, List<String> categories) {
        this.id = itemId;
        this.name = name;
        this.imageBase64 = imageBase64;
        this.description = description;
        this.vendorId = vendorId;
        this.price = price;
        this.supply = supply;
        this.rate = rate;
        this.categories = categories;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getImageBase64() { return imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getVendorId() { return vendorId; }
    public void setVendorId(String vendorId) { this.vendorId = vendorId; }
    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }
    public Integer getSupply() { return supply; }
    public void setSupply(Integer supply) { this.supply = supply; }
    public List<String> getCategories() { return categories; }
    public void setCategories(List<String> categories) { this.categories = categories; }
    public Double getRate() { return rate; }
    public void setRate(Double rate) { this.rate = rate; }
}
