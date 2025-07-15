package entity;

import java.util.UUID;
import javax.persistence.*;
@Entity
@Table(name="user_wallets")
public class UserWallet {

    @Id
    private UUID id = UUID.randomUUID();

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
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