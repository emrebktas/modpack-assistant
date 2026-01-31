package com.example.chatbotmc.dto;

public record ChatResponse(
    String response,
    Long conversationId,
    Long messageId
) {}