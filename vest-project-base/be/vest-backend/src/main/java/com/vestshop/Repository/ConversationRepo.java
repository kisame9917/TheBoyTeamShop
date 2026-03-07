package com.vestshop.Repository;

import com.vestshop.Entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConversationRepo extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findFirstByCustomerIdAndStatusOrderByCreatedAtDesc(String customerId, String status);

    List<Conversation> findByStatusOrderByUpdatedAtDesc(String status);
}