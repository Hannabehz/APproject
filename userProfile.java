package Entity;
import jakarta.persistence.*;
@Entity
@DiscriminatorValue("USER")
public class userProfile extends Profile{

}
