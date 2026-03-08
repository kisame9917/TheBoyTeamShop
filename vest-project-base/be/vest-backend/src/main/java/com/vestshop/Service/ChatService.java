package com.vestshop.Service;

import com.vestshop.Entity.Conversation;
import com.vestshop.Entity.KhachHang;
import com.vestshop.Entity.Message;
import com.vestshop.dto.response.ChatMessageResponse;
import com.vestshop.dto.response.ChatSaveResult;
import com.vestshop.dto.response.ConversationSummaryResponse;

import java.util.List;

public interface ChatService {

     Conversation getOrCreateOpenConversation(KhachHang customer);

     Conversation getOrCreateOpenGuestConversation(String guestName);

     List<ChatSaveResult> saveMessage(Long conversationId, String senderType, String senderId, String content);

     List<Message> getRecentMessages(Long conversationId);

     List<ChatMessageResponse> getRecentMessageResponses(Long conversationId);

     List<ConversationSummaryResponse> getOpenConversationSummaries();
}