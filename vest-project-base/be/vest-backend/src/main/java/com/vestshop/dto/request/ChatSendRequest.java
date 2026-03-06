package com.vestshop.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatSendRequest {
    @NotNull
    private Long conversationId;

    @NotBlank
    private String senderType; // CLIENT / ADMIN

    private String senderId;

    @NotBlank
    private String content;


}