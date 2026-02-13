package com.vestshop.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "phien_ca")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PhienCa {

    // 1 = ĐANG MỞ, 2 = ĐÃ ĐÓNG, 0 = HỦY
    public static final int TRANG_THAI_DANG_MO = 1;
    public static final int TRANG_THAI_DA_DONG = 2;
    public static final int TRANG_THAI_HUY = 0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_phien", length = 80)
    private String maPhien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nhan_vien", nullable = false)
    private NhanVien nhanVien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ca_lam_viec")
    private CaLamViec caLamViec;

    @Column(name = "ngay_lam_viec", nullable = false)
    private LocalDate ngayLamViec;

    @Column(name = "thoi_gian_mo", nullable = false)
    private LocalDateTime thoiGianMo;

    @Column(name = "thoi_gian_dong")
    private LocalDateTime thoiGianDong;

    @Column(name = "tien_mat_dau_ca", nullable = false, precision = 18, scale = 2)
    private BigDecimal tienMatDauCa;

    // NEW: tiền tài khoản đầu ca (để validate khớp ca trước)
    @Column(name = "tien_tai_khoan_dau_ca", nullable = false, precision = 18, scale = 2)
    private BigDecimal tienTaiKhoanDauCa;

    @Column(name = "tien_mat_thuc_te", precision = 18, scale = 2)
    private BigDecimal tienMatThucTe;

    // NEW: tiền tài khoản thực tế khi đóng ca
    @Column(name = "tien_tai_khoan_thuc_te", precision = 18, scale = 2)
    private BigDecimal tienTaiKhoanThucTe;

    @Column(name = "doanh_thu_tien_mat", precision = 18, scale = 2)
    private BigDecimal doanhThuTienMat;

    @Column(name = "doanh_thu_ck_the", precision = 18, scale = 2)
    private BigDecimal doanhThuCkThe;

    @Column(name = "tong_doanh_thu", precision = 18, scale = 2)
    private BigDecimal tongDoanhThu;

    @Column(name = "tien_mat_ly_thuyet", precision = 18, scale = 2)
    private BigDecimal tienMatLyThuyet;

    @Column(name = "chenh_lech", precision = 18, scale = 2)
    private BigDecimal chenhLech;

    @Column(name = "ghi_chu", length = 500)
    private String ghiChu;

    @Column(name = "trang_thai", nullable = false)
    private Integer trangThai;

    @Column(name = "ngay_tao", nullable = false)
    private LocalDateTime ngayTao;

    @PrePersist
    protected void onCreate() {
        if (ngayTao == null) {
            ngayTao = LocalDateTime.now();
        }
        if (tienMatDauCa == null) {
            tienMatDauCa = BigDecimal.ZERO;
        }
        if (tienTaiKhoanDauCa == null) {
            tienTaiKhoanDauCa = BigDecimal.ZERO;
        }
        if (trangThai == null) {
            trangThai = TRANG_THAI_DANG_MO;
        }
    }
}
