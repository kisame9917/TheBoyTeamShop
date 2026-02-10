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
public class PhieuGiamGiaCaNhanResponse {
    private Long idPggCaNhan;
    private LocalDate ngaySinh;
    private Long soDonThangHienTai;
    private BigDecimal tongTienDaTieu;
    private Long idKhachHang;
    private String maKhachHang;
    private String tenKhachHang;
    private String soDienThoai;
    private String email;

    private String maPhieuGiamGiaCaNhan;
    private LocalDateTime ngayNhan;
    private Boolean daSuDung;
}
