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
@Table(name="hoa_don")
public class HoaDon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id", nullable=false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="id_khach_hang", nullable=true)
    private KhachHang khachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="id_phieu_giam_gia", nullable=true)
    private PhieuGiamGia phieuGiamGia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="id_nhan_vien", nullable=true)
    private NhanVien nhanVien;

    @Column(name="ma_hoa_don", nullable=false, length=80)
    private String maHoaDon;

    @Column(name="loai_don")
    private Boolean loaiDon;

    @Column(name="phi_van_chuyen", nullable=false, precision=18, scale=2)
    private BigDecimal phiVanChuyen;

    @Column(name="tong_tien", nullable=false, precision=18, scale=2)
    private BigDecimal tongTien;

    @Column(name="tong_tien_giam", nullable=false, precision=18, scale=2)
    private BigDecimal tongTienGiam;

    @Column(name="tong_tien_sau_giam", nullable=false, precision=18, scale=2)
    private BigDecimal tongTienSauGiam;

    @Column(name="ten_khach_hang", length=255)
    private String tenKhachHang;

    @Column(name="so_dien_thoai", length=20)
    private String soDienThoai;

    @Column(name="dia_chi_khach_hang", length=255)
    private String diaChiKhachHang;

    @Column(name="email_khach_hang", length=255)
    private String emailKhachHang;

    @Column(name="qr_code", columnDefinition="NVARCHAR(MAX)")
    private String qrCode;

    @Column(name="ghi_chu", length=500)
    private String ghiChu;

    @Column(name="ngay_tao", nullable=false)
    private LocalDateTime ngayTao;

    @Column(name="nguoi_tao", length=255)
    private String nguoiTao;

    @Column(name="ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;

    @Column(name="nguoi_cap_nhat", length=255)
    private String nguoiCapNhat;

    @Column(name="trang_thai", nullable=false)
    private Boolean trangThai;

    @OneToMany(mappedBy = "hoaDon", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<LichSuHoaDon> lichSuHoaDons = new ArrayList<>();

    @OneToMany(mappedBy = "hoaDon", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<HoaDonChiTiet> hoaDonChiTiets = new ArrayList<>();

    @OneToMany(mappedBy = "hoaDon", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<LichSuThanhToan> lichSuThanhToans = new ArrayList<>();

    @OneToMany(mappedBy = "hoaDon", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<GiaoDichThanhToan> giaoDichThanhToans = new ArrayList<>();

}
