package com.vestshop.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "ca_lam_viec")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CaLamViec {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ten_ca", nullable = false)
    private String tenCa;

    @Column(name = "gio_bat_dau", nullable = false)
    private LocalTime gioBatDau;

    @Column(name = "gio_ket_thuc", nullable = false)
    private LocalTime gioKetThuc;

    @Column(name = "mo_ta")
    private String moTa;

    @Column(name = "trang_thai")
    private Integer trangThai; // 1: Active, 0: Inactive

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @PrePersist
    protected void onCreate() {
        ngayTao = LocalDateTime.now();
    }
}