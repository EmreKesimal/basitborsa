package com.basitborsa.controller;

import com.basitborsa.dto.ai.*;
import com.basitborsa.service.AiService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/chart-story")
    public ResponseEntity<ChartStoryResponse> chartStory(@Valid @RequestBody ChartStoryRequest request) {
        return ResponseEntity.ok(aiService.chartStory(request));
    }

    @PostMapping("/explain-event")
    public ResponseEntity<AiExplanationDto> explainEvent(@Valid @RequestBody ExplainEventRequest request) {
        return ResponseEntity.ok(aiService.explainEvent(request));
    }

    @PostMapping("/explain-term")
    public ResponseEntity<AiExplanationDto> explainTerm(@Valid @RequestBody ExplainTermRequest request) {
        return ResponseEntity.ok(aiService.explainTerm(request));
    }

    @PostMapping("/explain-stock")
    public ResponseEntity<AiExplanationDto> explainStock(@Valid @RequestBody ExplainStockRequest request) {
        return ResponseEntity.ok(aiService.explainStock(request));
    }
}
