package com.vestshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationSummaryResponse {
    private Long conversationId;
    private String customerName;
    private String lastMessage;
    private Instant lastAt;
    private Instant updatedAt;
}