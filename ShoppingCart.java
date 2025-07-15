package entity;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;



@Entity
@Table(name="shopping_carts")
public class ShoppingCart {
    @Id
    private UUID id = UUID.randomUUID();

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(
            name = "shopping_carts_order_items",
            joinColumns = @JoinColumn(name = "shopping_cart_id"),
            inverseJoinColumns = @JoinColumn(name = "order_item_id")
    )
    private List<OrderItem> orderItems = new ArrayList<>();

    private int totalPrice;

    public ShoppingCart() {}

    public ShoppingCart(User user) {

    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public List<OrderItem> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItem> orderItems) { this.orderItems = orderItems; }

    public int getTotalPrice() { return totalPrice; }
    public void setTotalPrice(int totalPrice) { this.totalPrice = totalPrice; }

}
