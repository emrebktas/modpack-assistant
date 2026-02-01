package com.example.chatbotmc.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;

@Service
public class EmailService {
    
    @Value("${sendgrid.api-key}")
    private String sendGridApiKey;
    
    @Value("${sendgrid.from-email:noreply@yourdomain.com}")
    private String fromEmail;
    
    @Value("${sendgrid.from-name:Modpack Assistant}")
    private String fromName;
    
    @Value("${app.backend.url:http://localhost:8080}")
    private String backendUrl;
    
    @Value("${admin.email}")
    private String adminEmail;
    
    public void sendAdminApprovalEmail(String username, String email, String approvalToken) {
        try {
            Email from = new Email(fromEmail, fromName);
            Email to = new Email(adminEmail);
            String subject = "New User Registration Approval Required - " + username;
            
            String approveUrl = backendUrl + "/api/auth/approve-user?token=" + approvalToken + "&action=approve";
            String rejectUrl = backendUrl + "/api/auth/approve-user?token=" + approvalToken + "&action=reject";
            
            String htmlContent = buildAdminApprovalEmail(username, email, approveUrl, rejectUrl);
            Content content = new Content("text/html", htmlContent);
            
            Mail mail = new Mail(from, subject, to, content);
            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();
            
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            
            Response response = sg.api(request);
            
            if (response.getStatusCode() >= 400) {
                throw new RuntimeException("SendGrid error: " + response.getStatusCode() + " - " + response.getBody());
            }
        } catch (IOException e) {
            throw new RuntimeException("Email could not be sent: " + e.getMessage(), e);
        }
    }
    
    public void sendUserApprovalNotification(String userEmail, String username, boolean approved) {
        try {
            Email from = new Email(fromEmail, fromName);
            Email to = new Email(userEmail);
            
            String subject;
            String htmlContent;
            
            if (approved) {
                subject = "Your Account Has Been Approved!";
                htmlContent = buildUserApprovedEmail(username);
            } else {
                subject = "Registration Status Update";
                htmlContent = buildUserRejectedEmail(username);
            }
            
            Content content = new Content("text/html", htmlContent);
            Mail mail = new Mail(from, subject, to, content);
            
            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();
            
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            
            Response response = sg.api(request);
            
            if (response.getStatusCode() >= 400) {
                throw new RuntimeException("SendGrid error: " + response.getStatusCode() + " - " + response.getBody());
            }
        } catch (IOException e) {
            throw new RuntimeException("Email could not be sent: " + e.getMessage(), e);
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
