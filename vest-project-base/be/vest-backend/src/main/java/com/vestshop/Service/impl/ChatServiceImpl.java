package com.vestshop.Service.impl;

import com.vestshop.Chatbot.RuleBasedChatbotService;
import com.vestshop.Entity.Conversation;
import com.vestshop.Entity.KhachHang;
import com.vestshop.Entity.Message;
import com.vestshop.Repository.ConversationRepo;
import com.vestshop.Repository.MessageRepo;
import com.vestshop.Service.ChatService;
import com.vestshop.dto.response.ConversationSummaryResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {

    private final ConversationRepo conversationRepo;
    private final MessageRepo messageRepo;
    private final RuleBasedChatbotService ruleBasedChatbotService;

    public ChatServiceImpl(
            ConversationRepo conversationRepo,
            MessageRepo messageRepo,
            RuleBasedChatbotService ruleBasedChatbotService
    ) {
        this.conversationRepo = conversationRepo;
        this.messageRepo = messageRepo;
        this.ruleBasedChatbotService = ruleBasedChatbotService;
    }

    @Override
    public Conversation getOrCreateOpenConversation(KhachHang customer) {
        return conversationRepo
                .findFirstByCustomerAndStatusOrderByCreatedAtDesc(customer, "OPEN")
                .orElseGet(() -> {
                    Conversation cv = new Conversation();
                    cv.setCustomer(customer);
                    cv.setGuestName(null);
                    cv.setStatus("OPEN");
                    return conversationRepo.save(cv);
                });
    }

    @Override
    public Conversation getOrCreateOpenGuestConversation(String guestName) {
        Conversation cv = new Conversation();
        cv.setCustomer(null);
        cv.setGuestName(
                guestName != null && !guestName.isBlank()
                        ? guestName.trim()
                        : "Khách vãng lai"
        );
        cv.setStatus("OPEN");
        return conversationRepo.save(cv);
    }

    @Override
    public Message saveMessage(Long conversationId, String senderType, String senderId, String content) {
        Message msg = new Message();
        msg.setConversationId(conversationId);
        msg.setSenderType(senderType);
        msg.setSenderId(senderId);
        msg.setContent(content);

        Message saved = messageRepo.save(msg);

        Conversation conversation = conversationRepo.findById(conversationId).orElseThrow();
        conversation.setUpdatedAt(saved.getCreatedAt());
        conversationRepo.save(conversation);

        if ("CLIENT".equalsIgnoreCase(senderType)) {
            String botReply = ruleBasedChatbotService.findBestReply(content);

            if (botReply != null && !botReply.isBlank()) {
                Message botMsg = new Message();
                botMsg.setConversationId(conversationId);
                botMsg.setSenderType("BOT");
                botMsg.setSenderId("RULE_BASED_BOT");
                botMsg.setContent(botReply);

                Message botSaved = messageRepo.save(botMsg);

                conversation.setUpdatedAt(botSaved.getCreatedAt());
                conversationRepo.save(conversation);
            }
        }

        return saved;
    }

    @Override
    public List<Message> getRecentMessages(Long conversationId) {
        return messageRepo.findTop50ByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    @Override
    public List<ConversationSummaryResponse> getOpenConversationSummaries() {
        var cvs = conversationRepo.findByStatusOrderByUpdatedAtDesc("OPEN");

        return cvs.stream().map(cv -> {
            var last = messageRepo.findTop1ByConversationIdOrderByCreatedAtDesc(cv.getId()).orElse(null);

            return ConversationSummaryResponse.builder()
                    .conversationId(cv.getId())
                    .customerName(resolveCustomerName(cv))
                    .lastMessage(last != null ? last.getContent() : "")
                    .lastAt(last != null ? last.getCreatedAt() : null)
                    .updatedAt(cv.getUpdatedAt())
                    .build();
        }).toList();
    }

    private String resolveCustomerName(Conversation cv) {
        if (cv.getCustomer() != null
                && cv.getCustomer().getTenKhachHang() != null
                && !cv.getCustomer().getTenKhachHang().isBlank()) {
            return cv.getCustomer().getTenKhachHang();
        }

        if (cv.getGuestName() != null && !cv.getGuestName().isBlank()) {
            return cv.getGuestName();
        }

        return "Khách vãng lai";
    }
}