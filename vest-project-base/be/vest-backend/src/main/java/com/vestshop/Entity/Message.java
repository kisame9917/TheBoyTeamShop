package com.vestshop.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Data
@Entity
@Table(name="messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="conversation_id", nullable=false)
    private Long conversationId;

    @Column(name="sender_type", nullable=false, length=16)
    private String senderType; // CLIENT / ADMIN

    @Column(name="sender_id", length=64)
    private String senderId;

    @Column(nullable=false, length=2000)
    private String content;

    @Column(name="created_at", nullable=false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = Instant.now();
    }

    // getters/setters
}