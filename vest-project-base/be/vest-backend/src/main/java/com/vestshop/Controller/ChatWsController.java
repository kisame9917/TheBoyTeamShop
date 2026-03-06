package com.vestshop.Controller;

import com.vestshop.Entity.Message;
import com.vestshop.Service.ChatService;
import com.vestshop.dto.request.ChatSendRequest;
import com.vestshop.dto.response.ChatMessageResponse;
import jakarta.validation.Valid;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

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
        Message saved = chatService.saveMessage(
                req.getConversationId(),
                req.getSenderType(),
                req.getSenderId(),
                req.getContent()
        );

        ChatMessageResponse res = new ChatMessageResponse();
        res.setId(saved.getId());
        res.setConversationId(saved.getConversationId());
        res.setSenderType(saved.getSenderType());
        res.setSenderId(saved.getSenderId());
        res.setContent(saved.getContent());
        res.setCreatedAt(saved.getCreatedAt());

        // 1) Bắn vào room theo conversationId (client/admin đang mở room này sẽ nhận)
        messagingTemplate.convertAndSend(
                "/topic/conversations/" + req.getConversationId(),
                res
        );

        // 2) Bắn thêm 1 kênh tổng cho admin để admin luôn nhận được tin mới
        // => admin không cần nhập Conversation ID vẫn thấy tin + biết conversationId để hiện lên list
        messagingTemplate.convertAndSend(
                "/topic/admin/conversations",
                res
        );
    }
}