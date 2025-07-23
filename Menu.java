package entity;

import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.*;

@Entity(name = "Menu")
@Table(name = "menus",
        uniqueConstraints = @UniqueConstraint(columnNames = {"restaurant_id", "title"})
)
public class Menu {

    @Id
    @Column(columnDefinition = "BINARY(255)")
    @Type(type = "uuid-binary")
    private UUID id = UUID.randomUUID();

    @Column(nullable = false)
    private String title;

    @ManyToOne
    @JoinColumn(name = "restaurant_id", columnDefinition = "BINARY(16)", nullable = false)
    private Restaurant restaurant;

    @ManyToMany
    @JoinTable(
            name = "menu_items",
            joinColumns = @JoinColumn(name = "menu_id"),
            inverseJoinColumns = @JoinColumn(name = "food_id")
    )
    private List<Food> items = new ArrayList<>();

    public Menu() {}

    public Menu(String title, Restaurant restaurant) {
        this.title = title;
        this.restaurant = restaurant;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Restaurant getRestaurant() { return restaurant; }
    public void setRestaurant(Restaurant restaurant) { this.restaurant = restaurant; }

    public List<Food> getItems() { return items; }
    public void addItem(Food food) {
        if (!items.contains(food)) items.add(food);
    }
    public void removeItem(Food food) {
        items.remove(food);
    }
}