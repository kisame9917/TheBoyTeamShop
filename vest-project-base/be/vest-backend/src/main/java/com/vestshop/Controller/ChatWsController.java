package com.vestshop.Controller;

import com.vestshop.Entity.Message;
import com.vestshop.Service.ChatService;
import com.vestshop.dto.request.ChatSendRequest;
import com.vestshop.dto.response.ChatMessageResponse;
import com.vestshop.dto.response.ChatSaveResult;
import jakarta.validation.Valid;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class ChatWsController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatWsController(ChatService chatService, SimpMessagingTemplate template) {
        this.chatService = chatService;
        this.messagingTemplate = template;
    }

    @MessageMapping("/chat.send")
    public void send(@Valid ChatSendRequest req) {
        List<ChatSaveResult> results = chatService.saveMessage(
                req.getConversationId(),
                req.getSenderType(),
                req.getSenderId(),
                req.getContent()
        );

        for (ChatSaveResult item : results) {
            ChatMessageResponse res = toResponse(item);
            broadcast(req.getConversationId(), res);
        }
    }

    private ChatMessageResponse toResponse(ChatSaveResult item) {
        Message message = item.getMessage();

        ChatMessageResponse res = new ChatMessageResponse();
        res.setId(message.getId());
        res.setConversationId(message.getConversationId());
        res.setSenderType(message.getSenderType());
        res.setSenderId(message.getSenderId());
        res.setContent(message.getContent());
        res.setCreatedAt(message.getCreatedAt());
        res.setProducts(item.getProducts());
        return res;
    }

    private void broadcast(Long conversationId, ChatMessageResponse res) {
        messagingTemplate.convertAndSend(
                "/topic/conversations/" + conversationId,
                res
        );

        messagingTemplate.convertAndSend(
                "/topic/admin/conversations",
                res
        );
    }
}