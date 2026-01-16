package com.vestshop.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LichSuThanhToanResponse {
    private Long id;
    private String maGiaoDich;
    private BigDecimal soTien;
    private LocalDateTime ngayThanhToan;
    private String hinhThucThanhToan;
    private String ghiChu;
}
