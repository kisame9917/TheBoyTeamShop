package com.vestshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SanPhamChiTietResponse {
    private Long id;
    private Long idSanPham;
    private String tenSanPham;
    private String anh;
    private Long idKichCo;
    private String tenKichCo;
    private Long idMauSac;
    private String tenMauSac;
    
    private String maSanPhamChiTiet;
    private Integer soLuongTon;
    private BigDecimal donGia;
    private String ghiChu;
    private Boolean trangThai;
}
