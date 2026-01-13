package com.vestshop.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
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
@Table(name="khach_hang")
public class KhachHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id", nullable=false)
    private Long id;

    @Column(name="ma_khach_hang", nullable=false, length=80)
    private String maKhachHang;

    @Column(name="ten_khach_hang", nullable=false, length=255)
    private String tenKhachHang;

    @Column(name="so_dien_thoai", length=20)
    private String soDienThoai;

    @Column(name="tai_khoan", nullable=false, length=80)
    private String taiKhoan;

    @Column(name="mat_khau", nullable=false, length=255)
    private String matKhau;

    @Column(name="ngay_tao", nullable=false)
    private LocalDateTime ngayTao;

    @Column(name="ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;

    @Column(name="trang_thai", nullable=false)
    private Integer trangThai;

    @Column(name="email", length=255)
    private String email;

    @OneToMany(mappedBy = "khachHang", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<HoaDon> hoaDons = new ArrayList<>();

    @OneToMany(mappedBy = "khachHang", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<PhieuGiamGiaCaNhan> phieuGiamGiaCaNhans = new ArrayList<>();

    @OneToMany(mappedBy = "khachHang", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<DiaChiKhachHang> diaChiKhachHangs = new ArrayList<>();

}
