package com.vestshop.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoaDonListResponse {
    private Long id;
    private String maHoaDon;
    private Boolean trangThai;
    private Integer trangThaiDon;
    private String tenTrangThaiDon;

    private Boolean loaiDon;
    private BigDecimal tongTienSauGiam;

    private String tenKhachHang;
    private String soDienThoai;
    private String maNhanVien;
    private String tenNhanVien;
    private String tenChucVu;

    private LocalDateTime ngayTao;
}