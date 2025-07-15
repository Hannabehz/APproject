package entity;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_type", discriminatorType = DiscriminatorType.STRING)
@Table(name="users")
public class User {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(nullable = false)
    private String fullName;

    @Column(unique = true, nullable = false)
    private String phone;

    @Column
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private String address;
    @Column
    private String profileImageBase64;

    @Embedded
    private BankInfo bankInfo;


    @OneToOne(cascade = CascadeType.ALL, mappedBy = "user")
    private ShoppingCart shoppingCart;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "user")
    private UserWallet userWallet;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();
    @ManyToMany
    @JoinTable(
            name = "favorites",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "restaurant_id")
    )
    private List<Restaurant> favorites = new ArrayList<>();
    public User() {}
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public String getProfileImageBase64() {
        return profileImageBase64;
    }
    public void setProfileImageBase64(String profileImageBase64) {
        this.profileImageBase64 = profileImageBase64;
    }
    public BankInfo getBankInfo() {
        return bankInfo;
    }
    public void setBankInfo(BankInfo bankInfo) {
        this.bankInfo = bankInfo;
    }

    public UserWallet getUserWallet() {
        return userWallet;
    }
    public void setUserWallet(UserWallet userWallet) {
        this.userWallet = userWallet;
    }
    public ShoppingCart getShoppingCart() {
        return shoppingCart;
    }
    public void setShoppingCart(ShoppingCart shoppingCart) {
        this.shoppingCart = shoppingCart;
    }
    public List<Order> getOrders() {
        return orders;
    }
    public void addOrder(Order order) {
        orders.add(order);
    }
    public void setFavorites(List<Restaurant> favorites) {
        this.favorites = favorites;
    }
    public List<Restaurant> getFavorites() {
        return favorites;
    }

    // Helper methods for managing favorites
    public void addFavorite(Restaurant restaurant) {
        if (!favorites.contains(restaurant)) {
            favorites.add(restaurant);
            restaurant.getFavoritedBy().add(this);
        }
    }

    public void removeFavorite(Restaurant restaurant) {
        if (favorites.contains(restaurant)) {
            favorites.remove(restaurant);
            restaurant.getFavoritedBy().remove(this);
        }
    }
    @Override
    public String toString() {
        return "User{id=" + id + ", phone=" + phone + ", fullName=" + fullName + "}";
    }
}
