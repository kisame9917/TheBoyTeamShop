package com.vestshop.Controller;

import com.vestshop.Entity.Message;
import com.vestshop.Service.ChatService;
import com.vestshop.dto.request.ChatSendRequest;
import com.vestshop.dto.response.ChatMessageResponse;
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
        Message saved = chatService.saveMessage(
                req.getConversationId(),
                req.getSenderType(),
                req.getSenderId(),
                req.getContent()
        );

        // broadcast message user/admin vừa gửi
        ChatMessageResponse res = toResponse(saved);
        broadcast(req.getConversationId(), res);

        // nếu client gửi, có thể bot đã auto reply trong saveMessage()
        if ("CLIENT".equalsIgnoreCase(req.getSenderType())) {
            List<Message> messages = chatService.getRecentMessages(req.getConversationId());

            if (!messages.isEmpty()) {
                Message lastMessage = messages.get(messages.size() - 1);

                // nếu message cuối là BOT và không trùng với message vừa gửi thì broadcast thêm bot
                if ("BOT".equalsIgnoreCase(lastMessage.getSenderType())
                        && !lastMessage.getId().equals(saved.getId())) {

                    ChatMessageResponse botRes = toResponse(lastMessage);
                    broadcast(req.getConversationId(), botRes);
                }
            }
        }
    }

    private ChatMessageResponse toResponse(Message message) {
        ChatMessageResponse res = new ChatMessageResponse();
        res.setId(message.getId());
        res.setConversationId(message.getConversationId());
        res.setSenderType(message.getSenderType());
        res.setSenderId(message.getSenderId());
        res.setContent(message.getContent());
        res.setCreatedAt(message.getCreatedAt());
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