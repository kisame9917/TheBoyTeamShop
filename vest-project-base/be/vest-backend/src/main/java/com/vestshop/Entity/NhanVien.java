package com.vestshop.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDate;
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
@Table(name="nhan_vien")
public class NhanVien {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id", nullable=false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="id_quyen_han", nullable=false)
    private QuyenHan quyenHan;

    @Column(name="ma_nhan_vien", nullable=false, length=80)
    private String maNhanVien;

    @Column(name="ten_nhan_vien", nullable=false, length=255)
    private String tenNhanVien;

    @Column(name="so_dien_thoai", length=20)
    private String soDienThoai;

    @Column(name="cccd", length=20)
    private String cccd;

    @Column(name="email", length=255)
    private String email;

    @Column(name="tai_khoan", nullable=false, length=80)
    private String taiKhoan;

    @Column(name="mat_khau", nullable=false, length=255)
    private String matKhau;

    @Column(name="ngay_sinh")
    private LocalDate ngaySinh;

    @Column(name="gioi_tinh")
    private Boolean gioiTinh;

    @Column(name="ngay_tao", nullable=false)
    private LocalDateTime ngayTao;

    @Column(name="nguoi_cap_nhat", length=255)
    private String nguoiCapNhat;

    @Column(name="ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;

    @Column(name="dia_chi", length=255)
    private String diaChi;

    @Column(name="trang_thai", nullable=false)
    private Boolean trangThai;

    @OneToMany(mappedBy = "nhanVien", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<HoaDon> hoaDons = new ArrayList<>();

}
