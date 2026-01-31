// RegisterRequest.java
package com.example.chatbotmc.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "Kullanıcı adı boş olamaz")
    @Size(min = 3, max = 20, message = "Kullanıcı adı 3-20 karakter arası olmalı")
    String username,
    
    @NotBlank(message = "Email boş olamaz")
    @Email(message = "Geçerli bir email giriniz")
    String email,
    
    @NotBlank(message = "Şifre boş olamaz")
    @Size(min = 6, message = "Şifre en az 6 karakter olmalı")
    String password
) {}