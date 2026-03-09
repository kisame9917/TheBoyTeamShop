package com.vestshop.dto.AI;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OpenAiExtractResponse {
    private String intent; // greeting, handoff, product_search

    private String loaiSanPham;
    private String mauSac;
    private String kichCo;
    private BigDecimal priceMin;
    private BigDecimal priceMax;
    private String fit;
    private String chatLieu;
}