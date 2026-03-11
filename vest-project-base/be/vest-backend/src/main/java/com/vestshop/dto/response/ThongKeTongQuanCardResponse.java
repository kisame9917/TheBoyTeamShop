package com.vestshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ThongKeTongQuanCardResponse {
    private String nhan;
    private BigDecimal doanhThu;
    private Long sanPhamDaBan;
    private Long donHang;
    private Long hoanThanh;
    private Long huy;
    private Long xuLy;
}