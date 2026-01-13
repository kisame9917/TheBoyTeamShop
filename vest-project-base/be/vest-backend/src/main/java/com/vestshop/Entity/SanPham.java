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
@Table(name="san_pham")
public class SanPham {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id", nullable=false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="id_chat_lieu", nullable=false)
    private ChatLieu chatLieu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="id_loai_san_pham", nullable=false)
    private LoaiSanPham loaiSanPham;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="id_thuong_hieu", nullable=false)
    private ThuongHieu thuongHieu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="id_so_khuy", nullable=false)
    private SoKhuy soKhuy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="id_kieu_tui", nullable=false)
    private KieuTui kieuTui;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="id_ve_ao", nullable=false)
    private VeAo veAo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="id_xe_ta", nullable=false)
    private XeTa xeTa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="id_xuat_xu", nullable=false)
    private XuatXu xuatXu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="id_fit", nullable=false)
    private Fit fit;

    @Column(name="ma_san_pham", nullable=false, length=80)
    private String maSanPham;

    @Column(name="ten_san_pham", nullable=false, length=255)
    private String tenSanPham;

    @Column(name="ngay_tao", nullable=false)
    private LocalDateTime ngayTao;

    @Column(name="ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;

    @Column(name="trang_thai", nullable=false)
    private Boolean trangThai;

    @OneToMany(mappedBy = "sanPham", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<SanPhamChiTiet> sanPhamChiTiets = new ArrayList<>();

}
