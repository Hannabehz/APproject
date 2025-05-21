package Entity;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name="orders")
public class Order {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name="user_id")
    private User customer;
    @ManyToOne
    @JoinColumn(name="deliveryMan_id")
    private DeliveryMan deliveryMan;
    private LocalDateTime orderedDateTime;
    @OneToMany(mappedBy = "order",cascade = CascadeType.ALL)
    private List<OrderItem> orderItems=new ArrayList<OrderItem>();
    private int totalPrice;
    public Order() {}
    public Order(User customer, Restaurant restaurant, DeliveryMan deliveryMan) {
        this.customer = customer;
        this.deliveryMan = deliveryMan;
        this.orderedDateTime = LocalDateTime.now();
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public User getCustomer() {
        return customer;
    }
    public void setCustomer(User customer) {
        this.customer = customer;
    }
    public DeliveryMan getDeliveryMan() {
        return deliveryMan;
    }
    public void setDeliveryMan(DeliveryMan deliveryMan) {
        this.deliveryMan = deliveryMan;
    }
    public LocalDateTime getOrderedDateTime() {
        return orderedDateTime;
    }
    public void setOrderedDateTime(LocalDateTime orderedDateTime) {
        this.orderedDateTime = orderedDateTime;
    }
    public List<OrderItem> getOrderItems() {
        return orderItems;
    }
    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }
    public int getTotalPrice() {
        return totalPrice;
    }
    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }
}
