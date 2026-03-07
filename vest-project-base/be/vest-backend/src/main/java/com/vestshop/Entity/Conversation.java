package com.vestshop.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Data
@Entity
@Table(name = "conversations")
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private KhachHang customer;

    @Column(name = "guest_name", length = 255)
    private String guestName;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(name = "assigned_admin_id", length = 128)
    private String assignedAdminId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        var now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) status = "OPEN";
        if (customer == null && (guestName == null || guestName.isBlank())) {
            guestName = "Khách vãng lai";
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }
}