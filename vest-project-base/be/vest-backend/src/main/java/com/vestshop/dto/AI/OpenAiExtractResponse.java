package com.vestshop.dto.AI;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OpenAiExtractResponse {
    private String loaiSanPham;
    private String mauSac;
    private String kichCo;
    private BigDecimal priceMin;
    private BigDecimal priceMax;
    private String fit;
    private String chatLieu;
    private String occasion;
    private String reply;
    private List<String> fallbackSuggestions;
}