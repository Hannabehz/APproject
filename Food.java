package entity;
import org.hibernate.annotations.Type;

import javax.persistence.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name="food")
public class Food {
    @Id
    @Column(columnDefinition = "BINARY(16)")
    @Type(type = "uuid-binary")
    private UUID id=UUID.randomUUID();
    @Column(nullable = false)
    private String name;

    @Lob
    private String imageBase64;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int supply;
    @Column(columnDefinition = "BINARY(16)")
    @Type(type = "uuid-binary")
    private UUID vendorId;
    private double rate;
    @ElementCollection
    @CollectionTable(name = "food_categories", joinColumns = @JoinColumn(name = "food_id"))
    @Column(name = "category")
    private List<String> categories;
    @ManyToOne
    @JoinColumn(name = "restaurant_id", columnDefinition = "BINARY(16)")
    private Restaurant restaurant;
    public Food(){}
    public Food(UUID id, String name, String imageBase64, String description, UUID vendorId, Integer price, Integer supply, List<String> categories) {
        this.id = id;
        this.name = name;
        this.imageBase64 = imageBase64;
        this.description = description;
        this.vendorId = vendorId;
        this.price = price;
        this.supply = supply;
        this.categories = categories;
    }

    public Food(String name, double price, int quantity, Restaurant restaurant) {
        this.name = name;
        this.price =(int) price;
        this.restaurant = restaurant;
        //this.quantity = quantity;
    }

    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UUID getVendorId() {
        return vendorId;
    }

    public void setVendorId(UUID vendorId) {
        this.vendorId = vendorId;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Integer getSupply() {
        return supply;
    }

    public void setSupply(Integer supply) {
        this.supply = supply;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }
    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public void setCategory(String category) {
        categories.add(category);
    }
    public double getRate() {
        return rate;
    }
    public void setRate(double rate) {
        this.rate = rate;
    }
}

