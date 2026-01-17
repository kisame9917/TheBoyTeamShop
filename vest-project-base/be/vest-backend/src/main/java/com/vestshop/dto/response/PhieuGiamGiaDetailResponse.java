package com.vestshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PhieuGiamGiaDetailResponse {
    private Long id;
    private String maGiamGia;
    private String tenGiamGia;
    private Integer soLuong;

    private Boolean loaiGiam;
    private BigDecimal giaTriPhanTram;
    private BigDecimal giaTriTienMat;

    private String moTa;

    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;

    private LocalDate ngayTao;
    private LocalDate ngayCapNhat;

    private Boolean trangThai;
    private BigDecimal donHangToiThieu;
    private BigDecimal giaTriGiamToiDa;
    private Boolean loaiPhieu;
}
