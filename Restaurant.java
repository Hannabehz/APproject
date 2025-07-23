package entity;
import org.hibernate.annotations.Type;

import javax.persistence.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name="restaurants")
public class Restaurant {
    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)") // مشخص کردن نوع باینری
    @Type(type = "org.hibernate.type.UUIDBinaryType") // استفاده از نوع باینری برای UUID
    private UUID id ;
    private String name;
    private String address;
    private String phone;
    private String email;
    @Column(name="rate",nullable=true)
    private int rate;
    private String category;

    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @OneToMany(mappedBy="restaurant",cascade=CascadeType.ALL)
    private List<Food> foods=new ArrayList<>();

    @ManyToMany(mappedBy = "favorites")
    private List<User> favoritedBy = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Menu> menus = new ArrayList<>();

    @Column(length = 100000)
    private String logoBase64;

    @Column
    private int taxFee;

    @Column
    private int additionalFee;

    public Restaurant(String name, String address) {
        this.name = name;
        this.address = address;
    }
    public Restaurant() {}
    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }
    public void setSeller(User seller) {
        this.seller = seller;
    }
    public User getSeller() {
        return seller;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public List<Food> getFoods() {
        return foods;
    }
    public void setFoods(List<Food> foods) {
        this.foods = foods;
    }
    public int getRate() {
        return rate;
    }
    public void setRate(int rate) {
        this.rate = rate;
    }
    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public List<User> getFavoritedBy() {
        return favoritedBy;
    }
    public void setFavoritedBy(List<User> favoritedBy) {
        this.favoritedBy = favoritedBy;
    }
    public List<Menu> getMenus() {
        return menus;
    }
    public void setMenus(List<Menu> menus) {
        this.menus = menus;
    }
    public String getLogoBase64() {
        return logoBase64;
    }
    public void setLogoBase64(String logoBase64) {
        this.logoBase64 = logoBase64;
    }
    public int getTaxFee() {
        return taxFee;
    }
    public void setTaxFee(int taxFee) {
        this.taxFee = taxFee;
    }
    public int getAdditionalFee() {
        return additionalFee;
    }
    public void setAdditionalFee(int additionalFee) {
        this.additionalFee = additionalFee;
    }
}

