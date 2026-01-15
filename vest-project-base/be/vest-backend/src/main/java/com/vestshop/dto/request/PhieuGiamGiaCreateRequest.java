package com.vestshop.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PhieuGiamGiaCreateRequest {
    private String maGiamGia;
    private String tenGiamGia;
    private Integer soLuong;
    private Boolean loaiGiam;
    private LocalDateTime ngayBatDau;
    private LocalDateTime ngayKetThuc;
    private String moTa;
    private LocalDateTime ngayTao;
    private BigDecimal giaTriPhanTram;
    private BigDecimal giaTriTienMat;
    private Boolean trangThai;

}
