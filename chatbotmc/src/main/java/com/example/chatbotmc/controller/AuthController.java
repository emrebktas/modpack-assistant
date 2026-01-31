package com.example.chatbotmc.controller;

import com.example.chatbotmc.dto.*;
import com.example.chatbotmc.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final AuthService authService;
    
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
    
    @GetMapping("/approve-user")
    public ResponseEntity<String> approveUser(@RequestParam String token, @RequestParam String action) {
        String message = authService.approveUser(token, action);
        
        // Return HTML page with result
        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>User Approval</title>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        min-height: 100vh;
                        margin: 0;
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                    }
                    .container {
                        background: white;
                        padding: 40px;
                        border-radius: 10px;
                        box-shadow: 0 10px 40px rgba(0,0,0,0.2);
                        text-align: center;
                        max-width: 500px;
                    }
                    .icon {
                        font-size: 64px;
                        margin-bottom: 20px;
                    }
                    .success { color: #4CAF50; }
                    .reject { color: #f44336; }
                    h1 { color: #333; margin-bottom: 10px; }
                    p { color: #666; line-height: 1.6; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="icon %s">%s</div>
                    <h1>%s</h1>
                    <p>%s</p>
                </div>
            </body>
            </html>
            """.formatted(
                action.equals("approve") ? "success" : "reject",
                action.equals("approve") ? "✓" : "✗",
                action.equals("approve") ? "User Approved!" : "User Rejected",
                message
            );
        
        return ResponseEntity.ok()
            .header("Content-Type", "text/html; charset=UTF-8")
            .body(html);
    }
}