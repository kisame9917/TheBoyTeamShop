package com.vestshop.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhienCaResponse {
    private Long id;
    private String maPhien;

    private Long idNhanVien;
    private String maNhanVien;
    private String tenNhanVien;
    private String anhDaiDienNhanVien;

    private Long idCaLamViec;
    private String tenCa;
    private LocalTime gioBatDau;
    private LocalTime gioKetThuc;

    private LocalDate ngayLamViec;
    private LocalDateTime thoiGianMo;
    private LocalDateTime thoiGianDong;

    private BigDecimal tienMatDauCa;
    private BigDecimal tienTaiKhoanDauCa;

    private BigDecimal tienMatThucTe;
    private BigDecimal tienTaiKhoanThucTe;

    private BigDecimal doanhThuTienMat;
    private BigDecimal doanhThuCkThe;
    private BigDecimal tongDoanhThu;

    private BigDecimal tienMatLyThuyet;
    private BigDecimal tienTaiKhoanLyThuyet;

    /**
     * Chênh lệch theo tiền mặt (giữ để tương thích UI cũ)
     */
    private BigDecimal chenhLech;

    /**
     * NEW: tổng lý thuyết / tổng thực tế / chênh lệch tổng (tiền mặt + chuyển khoản)
     */
    private BigDecimal tongLyThuyet;
    private BigDecimal tongThucTe;
    private BigDecimal chenhLechTong;

    private String ghiChu;
    private Integer trangThai;
}
