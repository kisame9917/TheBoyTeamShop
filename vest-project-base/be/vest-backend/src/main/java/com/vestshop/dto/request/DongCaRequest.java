package com.vestshop.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DongCaRequest {
    private BigDecimal tienMatThucTe;

    /**
     * NEW: tiền chuyển khoản/thẻ thực tế khi đóng ca
     */
    private BigDecimal tienTaiKhoanThucTe;

    private String ghiChu;
}
