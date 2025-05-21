package Entity;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="foods")
public class Food {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;
    private String name;
    private double price;
    private int quantity;
    private int rate;
    @ManyToOne
    @JoinColumn(name="restaurant_id")
    private Restaurant restaurant;
    @OneToMany(mappedBy="food",cascade=CascadeType.ALL)
    private List<Comment> comments=new ArrayList<>();
    public Food(){}
    public Food(String name, double price, int quantity, Restaurant restaurant) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.restaurant = restaurant;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
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
    public Restaurant getRestaurant() {
        return restaurant;
    }
    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }
    public List<Comment> getComments() {
        return comments;
    }
    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
    public int getRate() {
        return rate;
    }
    public void setRate(int rate) {
        this.rate = rate;
    }
}
