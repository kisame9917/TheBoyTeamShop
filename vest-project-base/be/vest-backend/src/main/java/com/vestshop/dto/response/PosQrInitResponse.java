package com.vestshop.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PosQrInitResponse {
    private Long hoaDonId;
    private String maHoaDon;
    private String requestCode;
    private BigDecimal amount;
    private String paymentUrl;
    private LocalDateTime expiresAt;
}
