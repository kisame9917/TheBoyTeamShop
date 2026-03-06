package com.vestshop.Repository;

import com.vestshop.Entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessageRepo extends JpaRepository<Message, Long> {

    // ✅ history cho client/admin
    List<Message> findTop50ByConversationIdOrderByCreatedAtAsc(Long conversationId);

    // ✅ lấy tin nhắn mới nhất để làm preview cho admin list
    Optional<Message> findTop1ByConversationIdOrderByCreatedAtDesc(Long conversationId);
}