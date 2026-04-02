package com.vestshop.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RefundConfirmRequest {
    private BigDecimal soTienHoan;
    private String ghiChu;
    private String maGiaoDichHoan;
}