package com.vestshop.Controller;

import com.vestshop.Service.AISuggestService;
import com.vestshop.dto.AI.AISuggestRequest;
import com.vestshop.dto.AI.AISuggestResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AISuggestController {

    private final AISuggestService aiSuggestService;

    public AISuggestController(AISuggestService aiSuggestService) {
        this.aiSuggestService = aiSuggestService;
    }

    @PostMapping("/suggest")
    public AISuggestResponse suggest(@RequestBody AISuggestRequest request) {
        return aiSuggestService.suggestProducts(request.getMessage());
    }
}