package com.vestshop.Service;

import com.vestshop.Entity.SanPhamChiTiet;
import com.vestshop.dto.AI.AISuggestResponse;
import com.vestshop.dto.AI.ProductSuggestionDto;

import java.util.List;

public interface AISuggestService {
    ProductSuggestionDto toDto(SanPhamChiTiet spct);
    List<ProductSuggestionDto> toDtoList(List<SanPhamChiTiet> list);
    AISuggestResponse suggestProducts(String message);
}