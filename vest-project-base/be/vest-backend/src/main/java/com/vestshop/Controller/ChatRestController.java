package com.vestshop.Controller;

import com.vestshop.Entity.Conversation;
import com.vestshop.Entity.KhachHang;
import com.vestshop.Repository.KhachHangRepository;
import com.vestshop.Service.ChatService;
import com.vestshop.dto.response.ChatMessageResponse;
import com.vestshop.dto.response.ConversationCreateResponse;
import com.vestshop.dto.response.ConversationSummaryResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
    public ConversationCreateResponse createOrGet(
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String guestName
    ) {
        Conversation conversation;

        if (customerId != null) {
            KhachHang customer = khachHangRepository.findById(customerId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Khong tim thay khach hang voi id = " + customerId
                    ));

            conversation = chatService.getOrCreateOpenConversation(customer);
        } else {
            conversation = chatService.getOrCreateOpenGuestConversation(guestName);
        }

        return new ConversationCreateResponse(
                conversation.getId(),
                guestName != null && !guestName.isBlank() ? guestName : null,
                conversation.getStatus()
        );
    }

    @GetMapping("/conversations/open")
    public List<ConversationSummaryResponse> openConversations() {
        return chatService.getOpenConversationSummaries();
    }

    @GetMapping("/messages")
    public List<ChatMessageResponse> recent(@RequestParam Long conversationId) {
        return chatService.getRecentMessageResponses(conversationId);
    }
}