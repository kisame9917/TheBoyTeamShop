package com.vestshop.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "message_product_suggestion")
@Getter
@Setter
public class MessageProductSuggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_id", nullable = false)
    private Long messageId;

    @Column(name = "san_pham_chi_tiet_id", nullable = false)
    private Long sanPhamChiTietId;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}