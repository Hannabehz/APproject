package Entity;
import jakarta.persistence.*;
@Entity
@DiscriminatorValue("RESTAURANT")
public class restaurantProfile extends Profile{
}
