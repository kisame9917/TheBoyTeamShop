package com.vestshop.Service;

import com.vestshop.Entity.Conversation;
import com.vestshop.Entity.Message;
import com.vestshop.dto.response.ConversationSummaryResponse;

import java.util.List;

public interface ChatService {

     Conversation getOrCreateOpenConversation(String customerId);

     Message saveMessage(Long conversationId, String senderType, String senderId, String content);

     // Admin list: các conversation OPEN (kèm last message preview)
     List<ConversationSummaryResponse> getOpenConversationSummaries();

     // History: 50 message gần nhất
     List<Message> getRecentMessages(Long conversationId);
}