package com.vestshop.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GiaoDichThanhToanResponse {
    private Long id;

    private Long idPhuongThucThanhToan;
    private String tenPhuongThucThanhToan;

    private BigDecimal soTien;

    private String maGiaoDich;
    private String maYeuCau;
    private String maGiaoDichNgoai;
    private String maThamChieu;

    private String duLieuQr;
    private LocalDateTime thoiHan;

    private LocalDateTime thoiGianTao;
    private LocalDateTime thoiGianCapNhat;

    private String ghiChu;
}
