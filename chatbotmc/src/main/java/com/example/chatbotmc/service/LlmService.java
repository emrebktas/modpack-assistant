package com.example.chatbotmc.service;

import com.example.chatbotmc.dto.ChatResponse;
import com.example.chatbotmc.entity.Conversation;
import com.example.chatbotmc.entity.MessageRole;
import com.example.chatbotmc.llm.LlmClient;
import com.example.chatbotmc.prompt.PromptBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LlmService {

    private final LlmClient llmClient;
    private final ConversationService conversationService;

    public LlmService(LlmClient llmClient, ConversationService conversationService) {
        this.llmClient = llmClient;
        this.conversationService = conversationService;
    }

    public String chat(String userInput) {
        String prompt = PromptBuilder.minecraftPrompt(userInput);
        return llmClient.generate(prompt);
    }

    @Transactional
    public ChatResponse chatWithHistory(Long userId, String userInput, Long conversationId) {
        // Create new conversation if none provided
        if (conversationId == null) {
            String title = conversationService.generateConversationTitle(userInput);
            Conversation newConversation = conversationService.createConversation(userId, title);
            conversationId = newConversation.getId();
        }
        
        // Save user message
        conversationService.saveMessage(conversationId, userId, userInput, MessageRole.USER);
        
        // Generate AI response
        String prompt = PromptBuilder.minecraftPrompt(userInput);
        String aiResponse = llmClient.generate(prompt);
        
        // Save AI message
        var savedMessage = conversationService.saveMessage(conversationId, userId, aiResponse, MessageRole.ASSISTANT);
        
        return new ChatResponse(aiResponse, conversationId, savedMessage.getId());
    }
}
