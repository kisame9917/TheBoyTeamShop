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
    private String customerId;
    private String lastMessage; // lấy từ messages (tin mới nhất)
    private Instant lastAt;     // createdAt của tin mới nhất
    private Instant updatedAt;  // từ conversations.updated_at
}