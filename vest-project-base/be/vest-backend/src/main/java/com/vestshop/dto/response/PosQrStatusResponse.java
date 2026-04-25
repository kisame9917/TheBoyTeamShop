package com.vestshop.dto.response;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class PosQrStatusResponse {
    private Long hoaDonId;
    private String maHoaDon;
    private String requestCode;
    private boolean pending;
    private boolean paid;
    private String maGiaoDich;
    private BigDecimal soTien;
    private LocalDateTime expiresAt;
}