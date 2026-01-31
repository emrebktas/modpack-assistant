package com.example.chatbotmc.dto;

import com.example.chatbotmc.entity.MessageRole;
import java.time.LocalDateTime;

public record ChatMessageDTO(
    Long id,
    String content,
    MessageRole role,
    LocalDateTime createdAt
) {}