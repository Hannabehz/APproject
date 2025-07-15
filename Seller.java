package entity;
import javax.persistence.Entity;
import javax.persistence.DiscriminatorValue;
@Entity
@DiscriminatorValue("Courier")
public class Seller extends User{
    //methods and fields related to seller and restaurant
}
