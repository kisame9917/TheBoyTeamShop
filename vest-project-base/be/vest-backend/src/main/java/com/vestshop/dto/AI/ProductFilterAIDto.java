package com.vestshop.dto.AI;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductFilterAIDto {
    private String keyword;          // từ khóa chung
    private String loaiSanPham;      // vest, áo sơ mi, quần âu...
    private String mauSac;           // đen, trắng, xanh...
    private String kichCo;           // S, M, L, XL...
    private String fit;              // slimfit, regular...
    private String chatLieu;         // cotton, wool...
    private BigDecimal priceMin;
    private BigDecimal priceMax;
}
