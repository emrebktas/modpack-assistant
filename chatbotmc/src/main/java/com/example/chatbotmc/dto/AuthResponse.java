package com.example.chatbotmc.dto;

public record AuthResponse(
    String token,
    String username,
    String email,
    String role
) {}