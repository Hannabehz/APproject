package entity;

import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @Column(columnDefinition = "BINARY(16)")
    @Type(type = "uuid-binary")
    private UUID id=UUID.randomUUID();

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, columnDefinition = "BINARY(16)")
    @Type(type = "uuid-binary")
    private User user;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = true)
    private Order order; // برای تراکنش‌های پرداخت سفارش (اختیاری)

    @Column(name = "amount")
    private double amount;

    @Column(name = "type")
    private String type; // مثلاً "top-up" یا "payment"

    @Column(name = "method")
    private String method; // مثلاً "online", "card", "wallet", "paywall"

    @Column(name = "status")
    private String status; // مثلاً "successful", "failed"

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Transaction() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}