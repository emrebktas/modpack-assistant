package com.example.chatbotmc.dto;

public record ChatRequest(
    String prompt,
    Long conversationId  // Optional: null for new conversation
) {}