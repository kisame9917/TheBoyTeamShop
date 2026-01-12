package com.vestshop.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name="giao_dich_thanh_toan")
public class GiaoDichThanhToan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id", nullable=false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="id_phuong_thuc_thanh_toan", nullable=false)
    private PhuongThucThanhToan phuongThucThanhToan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="id_hoa_don", nullable=false)
    private HoaDon hoaDon;

    @Column(name="so_tien", nullable=false, precision=18, scale=2)
    private BigDecimal soTien;

    @Column(name="ma_giao_dich", length=120)
    private String maGiaoDich;

    @Column(name="ma_yeu_cau", length=120)
    private String maYeuCau;

    @Column(name="ma_giao_dich_ngoai", length=120)
    private String maGiaoDichNgoai;

    @Column(name="ma_tham_chieu", length=120)
    private String maThamChieu;

    @Column(name="du_lieu_qr", columnDefinition="NVARCHAR(MAX)")
    private String duLieuQr;

    @Column(name="thoi_han")
    private LocalDateTime thoiHan;

    @Column(name="du_lieu_phan_hoi", columnDefinition="NVARCHAR(MAX)")
    private String duLieuPhanHoi;

    @Column(name="thoi_gian_tao", nullable=false)
    private LocalDateTime thoiGianTao;

    @Column(name="thoi_gian_cap_nhat")
    private LocalDateTime thoiGianCapNhat;

    @Column(name="ghi_chu", length=500)
    private String ghiChu;

    @Column(name="trang_thai", nullable=false)
    private Integer trangThai;

}
