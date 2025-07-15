package entity;
import javax.persistence.*;

@Entity
@DiscriminatorValue("Courier")
public class Courier extends User {

    @Column
    private String workingRegion;

    private String status;

    // Getters and Setters
    public String getWorkingRegion() { return workingRegion; }
    public void setWorkingRegion(String workingRegion) { this.workingRegion = workingRegion; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Courier() {
        super();
        status = "Available";
    }
}
