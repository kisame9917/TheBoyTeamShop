package com.vestshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SanPhamThongKeResponse {
    private Long idSanPham;
    private String tenSanPham;
    private Long soLuongDaBan;
    private Long soLuongTonKho;
}
