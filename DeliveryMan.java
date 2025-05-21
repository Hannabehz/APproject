package Entity;
import jakarta.persistence.*;
@Entity
@DiscriminatorValue("DELIVERYMEN")
public class DeliveryMan extends User{
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private boolean active;
}
