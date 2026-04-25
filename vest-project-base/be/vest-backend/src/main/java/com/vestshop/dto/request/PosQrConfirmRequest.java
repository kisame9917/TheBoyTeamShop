package com.vestshop.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PosQrConfirmRequest {
    private String requestCode;
    private String maGiaoDich;
    private BigDecimal soTien;
    private String ghiChu;
    private String paymentGateway;
}