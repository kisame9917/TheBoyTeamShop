package com.vestshop.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface PhieuGiamGiaCaNhanProjection {
    Long getId();
    String getMaKhachHang();
    String getTenKhachHang();
    String getSoDienThoai();
    String getEmail();
    LocalDate getNgaySinh();

    Long getSoDonThangHienTai();
    BigDecimal getTongTienDaTieu();
}
