package com.example.chatbotmc.service;

import com.example.chatbotmc.dto.ChatMessageDTO;
import com.example.chatbotmc.dto.ConversationDTO;
import com.example.chatbotmc.entity.*;
import com.example.chatbotmc.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConversationService {
    
    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    
    public ConversationService(ConversationRepository conversationRepository,
                              ChatMessageRepository chatMessageRepository,
                              UserRepository userRepository) {
        this.conversationRepository = conversationRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
    }
    
    @Transactional
    public Conversation createConversation(Long userId, String title) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Conversation conversation = new Conversation();
        conversation.setUser(user);
        conversation.setTitle(title != null && !title.trim().isEmpty() ? title : "New Conversation");
        
        return conversationRepository.save(conversation);
    }
    
    @Transactional(readOnly = true)
    public List<ConversationDTO> getUserConversations(Long userId) {
        return conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId)
            .stream()
            .map(conv -> new ConversationDTO(
                conv.getId(),
                conv.getTitle(),
                conv.getCreatedAt(),
                conv.getUpdatedAt(),
                conv.getMessages().size()
            ))
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getConversationMessages(Long conversationId, Long userId) {
        // Verify user has access to this conversation
        conversationRepository.findByIdAndUserId(conversationId, userId)
            .orElseThrow(() -> new RuntimeException("Conversation not found"));
        
        return chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId)
            .stream()
            .map(msg -> new ChatMessageDTO(
                msg.getId(),
                msg.getContent(),
                msg.getRole(),
                msg.getCreatedAt()
            ))
            .collect(Collectors.toList());
    }
    
    @Transactional
    public ChatMessage saveMessage(Long conversationId, Long userId, String content, MessageRole role) {
        Conversation conversation = conversationRepository.findByIdAndUserId(conversationId, userId)
            .orElseThrow(() -> new RuntimeException("Conversation not found"));
        
        ChatMessage message = new ChatMessage();
        message.setConversation(conversation);
        message.setContent(content);
        message.setRole(role);
        
        ChatMessage savedMessage = chatMessageRepository.save(message);
        
        // Update conversation's updatedAt timestamp
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);
        
        return savedMessage;
    }
    
    @Transactional(readOnly = true)
    public Conversation getConversation(Long conversationId, Long userId) {
        return conversationRepository.findByIdAndUserId(conversationId, userId)
            .orElseThrow(() -> new RuntimeException("Conversation not found"));
    }
    
    @Transactional
    public void deleteConversation(Long conversationId, Long userId) {
        Conversation conversation = conversationRepository.findByIdAndUserId(conversationId, userId)
            .orElseThrow(() -> new RuntimeException("Conversation not found"));
        conversationRepository.delete(conversation);
    }
    
    @Transactional
    public void updateConversationTitle(Long conversationId, Long userId, String title) {
        Conversation conversation = conversationRepository.findByIdAndUserId(conversationId, userId)
            .orElseThrow(() -> new RuntimeException("Conversation not found"));
        conversation.setTitle(title);
        conversationRepository.save(conversation);
    }
    
    @Transactional
    public String generateConversationTitle(String firstMessage) {
        // Generate a title from the first message (max 50 chars)
        if (firstMessage == null || firstMessage.trim().isEmpty()) {
            return "New Conversation";
        }
        String cleaned = firstMessage.trim().replaceAll("\\s+", " ");
        return cleaned.length() > 50 ? cleaned.substring(0, 47) + "..." : cleaned;
    }
}
