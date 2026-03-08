package com.vestshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationCreateResponse {
    private Long id;
    private String customerName;
    private String status;
}