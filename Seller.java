package Entity;
import jakarta.persistence.*;

import java.util.ArrayList;

@Entity
@DiscriminatorValue("SELLER")
public class Seller extends User{
    ArrayList<Restaurant> sellerRestaurants;
    public Seller() {
        super();
        this.sellerRestaurants = new ArrayList<Restaurant>();
    }
}
