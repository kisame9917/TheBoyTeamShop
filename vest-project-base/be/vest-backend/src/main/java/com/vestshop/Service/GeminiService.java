package com.vestshop.Service;

import com.vestshop.dto.AI.OpenAiExtractResponse;

public interface GeminiService {
    OpenAiExtractResponse extractFilters(String userMessage);
    OpenAiExtractResponse extractFilters(String userMessage, String previousContext);
}