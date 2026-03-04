package com.vestshop.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LichSuThanhToanResponse {
    private Long id;
    private String maGiaoDich;
    private BigDecimal soTien;
    private LocalDateTime ngayThanhToan;

    // ✅ DB: lich_su_thanh_toan.id_phuong_thuc_thanh_toan
    private Long idPhuongThucThanhToan;

    // ✅ DB: phuong_thuc_thanh_toan.ten_phuong_thuc_thanh_toan
    private String tenPhuongThucThanhToan;

    // ✅ DB: phuong_thuc_thanh_toan.hinh_thuc (1/2)
    private Integer hinhThuc;

    private String ghiChu;
}