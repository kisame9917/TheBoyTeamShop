package com.vestshop.Entity;

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
@Table(name="phieu_giam_gia")
public class PhieuGiamGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id", nullable=false)
    private Long id;

    @Column(name="ma_giam_gia", nullable=false, length=80)
    private String maGiamGia;

    @Column(name="ten_giam_gia", nullable=false, length=255)
    private String tenGiamGia;

    @Column(name="so_luong", nullable=false)
    private Integer soLuong;

    @Column(name="loai_giam")
    private Boolean loaiGiam;

    @Column(name="ngay_bat_dau")
    private LocalDateTime ngayBatDau;

    @Column(name="ngay_ket_thuc")
    private LocalDateTime ngayKetThuc;

    @Column(name="mo_ta", length=500)
    private String moTa;

    @Column(name="ngay_tao", nullable=false)
    private LocalDateTime ngayTao;

    @Column(name="ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;

    @Column(name="gia_tri_phan_tram", precision=5, scale=2)
    private BigDecimal giaTriPhanTram;

    @Column(name="gia_tri_tien_mat", precision=18, scale=2)
    private BigDecimal giaTriTienMat;

    @Column(name="trang_thai", nullable=false)
    private Boolean trangThai;

    @OneToMany(mappedBy = "phieuGiamGia", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<HoaDon> hoaDons = new ArrayList<>();

    @OneToMany(mappedBy = "phieuGiamGia", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<PhieuGiamGiaCaNhan> phieuGiamGiaCaNhans = new ArrayList<>();

}
