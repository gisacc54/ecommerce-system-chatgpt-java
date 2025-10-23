package com.ecommerce.ecommerce.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @Column
    private Boolean confirmationSent = false;

    @Column(columnDefinition = "TEXT")
    private String shippingAddress;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems;

    @Column
    private LocalDateTime confirmationSentAt;  // ✅ add this field

    public LocalDateTime getConfirmationSentAt() {
        return confirmationSentAt;
    }

    public void setConfirmationSentAt(LocalDateTime confirmationSentAt) {
        this.confirmationSentAt = confirmationSentAt;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public enum Status {
        PENDING, PAID, CANCELLED
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Status getStatus() {
        return status;
    }



    public String getStatusStr() {
        if (status == Status.CANCELLED) {
            return "cancelled";
        } else if (status == Status.PAID) {
            return "paid";
        }
        return "pending";
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setStatusStr(String status) {
        if (status.equals(Status.CANCELLED)) {
            this.status = Status.CANCELLED;
        }else if (status.equals(Status.PAID)) {
            this.status = Status.PENDING;
        }else{
            this.status = Status.PENDING;
        }
    }

    public void setStatus(String status) {
        if (status.equals("paid")) {
            this.status = Status.PAID;
        }else if (status.equals("cancelled")) {
            this.status = Status.CANCELLED;
        }else{
            this.status = Status.PENDING;
        }
    }

    public Boolean getConfirmationSent() {
        return confirmationSent;
    }

    public void setConfirmationSent(Boolean confirmationSent) {
        this.confirmationSent = confirmationSent;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
