package com.vestshop.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PhieuGiamGiaCreateRequest {

    private String tenGiamGia;
    private Integer soLuong;
    private Boolean loaiGiam;
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;
    private String moTa;
    private LocalDate ngayTao;
    private BigDecimal giaTriPhanTram;
    private BigDecimal giaTriTienMat;
    private Boolean trangThai;
    private BigDecimal donHangToiThieu;
    private BigDecimal giaTriGiamToiDa;
    private Boolean loaiPhieu;
}
