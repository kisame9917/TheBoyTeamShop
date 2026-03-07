package com.vestshop.Controller;

import com.vestshop.Entity.Conversation;
import com.vestshop.Entity.KhachHang;
import com.vestshop.Entity.Message;
import com.vestshop.Repository.KhachHangRepository;
import com.vestshop.Service.ChatService;
import com.vestshop.dto.response.ConversationSummaryResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatRestController {

    private final ChatService chatService;
    private final KhachHangRepository khachHangRepository;

    public ChatRestController(ChatService chatService, KhachHangRepository khachHangRepository) {
        this.chatService = chatService;
        this.khachHangRepository = khachHangRepository;
    }

    @PostMapping("/conversation")
    public Conversation createOrGet(
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String guestName
    ) {
        if (customerId != null) {
            KhachHang customer = khachHangRepository.findById(customerId)
                    .orElseThrow(() -> new RuntimeException("Khong tim thay khach hang voi id = " + customerId));

            return chatService.getOrCreateOpenConversation(customer);
        }

        return chatService.getOrCreateOpenGuestConversation(guestName);
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