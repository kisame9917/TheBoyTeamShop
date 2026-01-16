package com.vestshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PhieuGiamGiaResponse {
    private Long id;
    private String maGiamGia;
    private String tenGiamGia;
    private Integer soLuong;
    private LocalDate ngay_bat_dau;
    private LocalDate ngay_ket_thuc;
    private Boolean trangThai;
    private Boolean loaiGiam;
    private BigDecimal giaTriPhanTram;
    private BigDecimal giaTriTienMat;
}
