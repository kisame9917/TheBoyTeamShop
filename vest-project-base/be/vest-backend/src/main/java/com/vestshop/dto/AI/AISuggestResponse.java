package com.vestshop.dto.AI;

import lombok.Data;

import java.util.List;
@Data
public class AISuggestResponse {
    private String reply;
    private List<ProductSuggestionDto> products;
}

