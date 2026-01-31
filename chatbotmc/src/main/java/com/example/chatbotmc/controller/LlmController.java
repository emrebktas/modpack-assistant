package com.example.chatbotmc.controller;

import com.example.chatbotmc.dto.ChatRequest;
import com.example.chatbotmc.dto.ChatResponse;
import com.example.chatbotmc.service.JwtService;
import com.example.chatbotmc.service.LlmService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/llm")
public class LlmController {

    private final LlmService llmService;
    private final JwtService jwtService;

    public LlmController(LlmService llmService, JwtService jwtService) {
        this.llmService = llmService;
        this.jwtService = jwtService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(
            @RequestBody ChatRequest request,
            @RequestHeader("Authorization") String authHeader) {
        
        // Extract userId from JWT token
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        Long userId = jwtService.extractUserId(token);
        
        ChatResponse response = llmService.chatWithHistory(userId, request.prompt(), request.conversationId());
        return ResponseEntity.ok(response);
    }
}
