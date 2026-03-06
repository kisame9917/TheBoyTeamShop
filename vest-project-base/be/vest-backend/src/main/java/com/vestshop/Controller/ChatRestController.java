package com.vestshop.Controller;

import com.vestshop.Entity.Conversation;
import com.vestshop.Entity.Message;
import com.vestshop.Service.ChatService;
import com.vestshop.dto.response.ConversationSummaryResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatRestController {

    private final ChatService chatService;

    public ChatRestController(ChatService chatService) {
        this.chatService = chatService;
    }

    // Client gọi khi mở widget để lấy conversationId
    @PostMapping("/conversation")
    public Conversation createOrGet(@RequestParam String customerId) {
        return chatService.getOrCreateOpenConversation(customerId);
    }
    @GetMapping("/conversations/open")
    public List<ConversationSummaryResponse> openConversations() {
        return chatService.getOpenConversationSummaries();
    }
    @GetMapping("/messages")
    public List<Message> recent(@RequestParam Long conversationId) {
        return chatService.getRecentMessages(conversationId);
    }
}