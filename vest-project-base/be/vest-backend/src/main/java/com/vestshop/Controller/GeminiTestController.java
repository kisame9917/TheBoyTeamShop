package com.vestshop.Controller;

import com.vestshop.Service.GeminiService;
import com.vestshop.dto.AI.OpenAiExtractResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gemini")
public class GeminiTestController {

    private final GeminiService geminiService;

    public GeminiTestController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @GetMapping("/test")
    public OpenAiExtractResponse test(@RequestParam String message) {
        return geminiService.extractFilters(message);
    }
}