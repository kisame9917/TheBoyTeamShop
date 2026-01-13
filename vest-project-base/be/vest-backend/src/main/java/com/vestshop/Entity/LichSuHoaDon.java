package com.vestshop.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name="lich_su_hoa_don")
public class LichSuHoaDon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id", nullable=false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="id_hoa_don", nullable=false)
    private HoaDon hoaDon;

    @Column(name="ghi_chu", length=500)
    private String ghiChu;

    @Column(name="thoi_gian", nullable=false)
    private LocalDateTime thoiGian;

    @Column(name="hanh_dong", length=255)
    private String hanhDong;

    @Column(name="trang_thai", nullable=false)
    private Integer trangThai;

}
