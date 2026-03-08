package com.vestshop.dto.response;

import com.vestshop.dto.AI.ProductSuggestionDto;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ChatMessageResponse {
    private Long id;
    private Long conversationId;
    private String senderType;
    private String senderId;
    private String content;
    private Instant createdAt;

    private List<ProductSuggestionDto> products;
}