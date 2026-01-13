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
@Table(name="hoa_don_chi_tiet")
public class HoaDonChiTiet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id", nullable=false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="id_hoa_don", nullable=false)
    private HoaDon hoaDon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="id_san_pham_chi_tiet", nullable=false)
    private SanPhamChiTiet sanPhamChiTiet;

    @Column(name="so_luong", nullable=false)
    private Integer soLuong;

    @Column(name="ngay_tao", nullable=false)
    private LocalDateTime ngayTao;

    @Column(name="ngay_sua")
    private LocalDateTime ngaySua;

    @Column(name="nguoi_tao", length=255)
    private String nguoiTao;

    @Column(name="nguoi_sua", length=255)
    private String nguoiSua;

    @Column(name="trang_thai", nullable=false)
    private Integer trangThai;

}
