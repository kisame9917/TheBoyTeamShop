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
@Table(name="san_pham_chi_tiet")
public class SanPhamChiTiet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id", nullable=false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="id_kich_co", nullable=false)
    private KichCo kichCo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="id_mau_sac", nullable=false)
    private MauSac mauSac;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="id_san_pham", nullable=false)
    private SanPham sanPham;

    @Column(name="ma_san_pham_chi_tiet", nullable=false, length=80)
    private String maSanPhamChiTiet;

    @Column(name="so_luong_ton", nullable=false)
    private Integer soLuongTon;

    @Column(name="ghi_chu", length=500)
    private String ghiChu;

    @Column(name="don_gia", nullable=false, precision=18, scale=2)
    private BigDecimal donGia;

    @Column(name="ngay_tao", nullable=false)
    private LocalDateTime ngayTao;

    @Column(name="ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;

    @Column(name="trang_thai", nullable=false)
    private Boolean trangThai;

    @Column(name = "anh_dai_dien")
    private String anh;

    @Column(name = "chat_lieu")
    private String chatLieu;

    @OneToMany(mappedBy = "sanPhamChiTiet", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<HoaDonChiTiet> hoaDonChiTiets = new ArrayList<>();

    @OneToMany(mappedBy = "sanPhamChiTiet", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<AnhChiTietSanPham> anhChiTietSanPhams = new ArrayList<>();

}
