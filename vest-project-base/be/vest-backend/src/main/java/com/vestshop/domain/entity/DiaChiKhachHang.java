package com.vestshop.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name="dia_chi_khach_hang")
public class DiaChiKhachHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id", nullable=false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="id_khach_hang", nullable=false)
    private KhachHang khachHang;

    @Column(name="ten_nguoi_nhan", length=255)
    private String tenNguoiNhan;

    @Column(name="so_dien_thoai", length=20)
    private String soDienThoai;

    @Column(name="dia_chi_chi_tiet", length=255)
    private String diaChiChiTiet;

    @Column(name="phuong_xa", length=255)
    private String phuongXa;

    @Column(name="quan_huyen", length=255)
    private String quanHuyen;

    @Column(name="tinh_thanh", length=255)
    private String tinhThanh;

    @Column(name="quoc_gia", length=255)
    private String quocGia;

    @Column(name="la_mac_dinh", nullable=false)
    private Boolean laMacDinh;

    @Column(name="trang_thai", nullable=false)
    private Integer trangThai;

}
