package entity;

import org.hibernate.annotations.Type;

import java.util.UUID;
import javax.persistence.*;
@Entity
@Table(name="user_wallets")
public class UserWallet {

    @Id
    @Column(columnDefinition = "BINARY(16)")
    @Type(type = "uuid-binary")
    private UUID id=UUID.randomUUID();

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true, columnDefinition = "BINARY(16)")
    @Type(type = "uuid-binary")
    private User user;

    @Column(nullable = false)
    private double balance;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

}