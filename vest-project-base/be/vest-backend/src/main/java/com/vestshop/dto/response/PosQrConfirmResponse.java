package com.vestshop.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PosQrConfirmResponse {
    private Long hoaDonId;
    private String maHoaDon;
    private String requestCode;
    private String maGiaoDich;
    private BigDecimal soTien;
    private boolean paid;
    private String message;
}
