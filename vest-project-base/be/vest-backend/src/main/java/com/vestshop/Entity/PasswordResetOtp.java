package com.vestshop.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
@Entity
@Table(name = "password_reset_otp")
public class PasswordResetOtp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nhan_vien_id", nullable = false)
    private Long nhanVienId;

    @Column(nullable = false)
    private String email;

    @Column(name = "otp_hash", nullable = false)
    private String otpHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private Boolean used = false;

    @Column(nullable = false)
    private Integer attempts = 0;

    @Column(name = "last_sent_at")
    private LocalDateTime lastSentAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
