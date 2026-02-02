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
public class DoanhThuResponse {
    private String thoiGian; // VD: "Quý 1/2024"
    private BigDecimal doanhThu;
}
