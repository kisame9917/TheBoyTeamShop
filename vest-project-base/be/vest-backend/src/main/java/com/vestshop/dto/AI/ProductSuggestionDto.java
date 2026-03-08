package com.vestshop.dto.AI;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductSuggestionDto {
    private Long sanPhamChiTietId;
    private Long sanPhamId;

    private String maSanPham;
    private String maSanPhamChiTiet;

    private String tenSanPham;
    private String loaiSanPham;
    private String thuongHieu;
    private String fit;

    private String mauSac;
    private String kichCo;
    private String chatLieu;

    private BigDecimal donGia;
    private Integer soLuongTon;
    private String anh;
}