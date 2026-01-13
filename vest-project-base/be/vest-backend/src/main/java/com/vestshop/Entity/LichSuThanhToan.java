package com.vestshop.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name="lich_su_thanh_toan")
public class LichSuThanhToan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id", nullable=false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="id_hoa_don", nullable=false)
    private HoaDon hoaDon;

    @Column(name="ma_giao_dich", length=120)
    private String maGiaoDich;

    @Column(name="so_tien", nullable=false, precision=18, scale=2)
    private BigDecimal soTien;

    @Column(name="ngay_thanh_toan", nullable=false)
    private LocalDateTime ngayThanhToan;

    @Column(name="hinh_thuc_thanh_toan", length=255)
    private String hinhThucThanhToan;

    @Column(name="ghi_chu", length=500)
    private String ghiChu;

    @Column(name="trang_thai", nullable=false)
    private Boolean trangThai;

}
