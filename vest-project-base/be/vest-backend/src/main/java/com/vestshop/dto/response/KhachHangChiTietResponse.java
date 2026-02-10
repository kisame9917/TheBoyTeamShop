package com.vestshop.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class KhachHangChiTietResponse {
    private Long customerId;
    private String month;
    private long monthlyOrderCount;
    private BigDecimal monthlySpent;
    private BigDecimal totalSpent;
    private LocalDate ngaySinh;
}
