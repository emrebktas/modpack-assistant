package com.example.chatbotmc.dto;

import java.time.LocalDateTime;

public record ConversationDTO(
    Long id,
    String title,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    int messageCount
) {}