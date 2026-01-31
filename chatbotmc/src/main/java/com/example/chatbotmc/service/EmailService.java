package com.example.chatbotmc.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${app.backend.url:http://localhost:8080}")
    private String backendUrl;
    
    @Value("${admin.email}")
    private String adminEmail;
    
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    
    public void sendAdminApprovalEmail(String username, String email, String approvalToken) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(adminEmail);
            helper.setSubject("New User Registration Approval Required - " + username);
            
            String approveUrl = backendUrl + "/api/auth/approve-user?token=" + approvalToken + "&action=approve";
            String rejectUrl = backendUrl + "/api/auth/approve-user?token=" + approvalToken + "&action=reject";
            
            String htmlContent = buildAdminApprovalEmail(username, email, approveUrl, rejectUrl);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Email gönderilemedi: " + e.getMessage());
        }
    }
    
    public void sendUserApprovalNotification(String userEmail, String username, boolean approved) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(userEmail);
            
            if (approved) {
                helper.setSubject("Your Account Has Been Approved!");
                String htmlContent = buildUserApprovedEmail(username);
                helper.setText(htmlContent, true);
            } else {
                helper.setSubject("Registration Status Update");
                String htmlContent = buildUserRejectedEmail(username);
                helper.setText(htmlContent, true);
            }
            
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Email gönderilemedi: " + e.getMessage());
        }
    }
    
    private String buildAdminApprovalEmail(String username, String email, String approveUrl, String rejectUrl) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #2196F3; color: white; padding: 20px; text-align: center; }
                    .content { background-color: #f9f9f9; padding: 30px; border-radius: 5px; }
                    .user-info { background-color: #fff; padding: 15px; border-left: 4px solid #2196F3; margin: 20px 0; }
                    .button { 
                        display: inline-block; 
                        padding: 12px 30px; 
                        color: white; 
                        text-decoration: none; 
                        border-radius: 5px; 
                        margin: 10px 5px;
                    }
                    .approve { background-color: #4CAF50; }
                    .reject { background-color: #f44336; }
                    .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>⚠️ New User Registration</h1>
                    </div>
                    <div class="content">
                        <h2>Admin Approval Required</h2>
                        <p>A new user has registered and is waiting for your approval:</p>
                        
                        <div class="user-info">
                            <p><strong>Username:</strong> %s</p>
                            <p><strong>Email:</strong> %s</p>
                            <p><strong>Registration Date:</strong> %s</p>
                        </div>
                        
                        <p>Please review and take action:</p>
                        
                        <div style="text-align: center;">
                            <a href="%s" class="button approve">✓ Approve User</a>
                            <a href="%s" class="button reject">✗ Reject User</a>
                        </div>
                        
                        <p style="margin-top: 30px; font-size: 12px; color: #666;">
                            Note: Approved users will be able to log in and use the chatbot application.
                            Rejected users will be notified and their account will remain inactive.
                        </p>
                    </div>
                    <div class="footer">
                        <p>© 2026 Chatbot Application - Admin Panel</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(username, email, java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), approveUrl, rejectUrl);
    }
    
    private String buildUserApprovedEmail(String username) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }
                    .content { background-color: #f9f9f9; padding: 30px; border-radius: 5px; }
                    .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>✓ Account Approved!</h1>
                    </div>
                    <div class="content">
                        <h2>Hello %s,</h2>
                        <p>Great news! Your account has been approved by our administrator.</p>
                        <p>You can now log in and start using the chatbot application.</p>
                        <p><strong>What's next?</strong></p>
                        <ul>
                            <li>Visit the application</li>
                            <li>Log in with your credentials</li>
                            <li>Start chatting with our AI assistant</li>
                        </ul>
                        <p>Thank you for joining us!</p>
                    </div>
                    <div class="footer">
                        <p>© 2026 Chatbot Application. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(username);
    }
    
    private String buildUserRejectedEmail(String username) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #f44336; color: white; padding: 20px; text-align: center; }
                    .content { background-color: #f9f9f9; padding: 30px; border-radius: 5px; }
                    .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Registration Status</h1>
                    </div>
                    <div class="content">
                        <h2>Hello %s,</h2>
                        <p>Thank you for your interest in our chatbot application.</p>
                        <p>Unfortunately, your registration request could not be approved at this time.</p>
                        <p>If you believe this is an error, please contact our support team.</p>
                    </div>
                    <div class="footer">
                        <p>© 2026 Chatbot Application. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(username);
    }
}
