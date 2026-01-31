package com.example.chatbotmc.service;

import com.example.chatbotmc.dto.AuthResponse;
import com.example.chatbotmc.dto.LoginRequest;
import com.example.chatbotmc.dto.RegisterRequest;
import com.example.chatbotmc.entity.User;
import com.example.chatbotmc.entity.Role;
import com.example.chatbotmc.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    
    public AuthService(UserRepository userRepository, 
                      PasswordEncoder passwordEncoder,
                      JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }
    
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new RuntimeException("Kullanıcı adı zaten kullanılıyor");
        }
        
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email zaten kayıtlı");
        }
        
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);
        
        userRepository.save(user);
        
        String token = jwtService.generateToken(user);
        
        return new AuthResponse(
            token, 
            user.getUsername(), 
            user.getEmail(),
            user.getRole().name()
        );
    }
    
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
            .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Şifre hatalı");
        }
        
        String token = jwtService.generateToken(user);
        
        return new AuthResponse(
            token,
            user.getUsername(),
            user.getEmail(),
            user.getRole().name()
        );
    }
}