package com.vestshop.dto.response;

import com.vestshop.Entity.Message;
import com.vestshop.dto.AI.ProductSuggestionDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatSaveResult {
    private Message message;
    private List<ProductSuggestionDto> products;
}