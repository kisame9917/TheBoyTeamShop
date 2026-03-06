package com.vestshop.Repository;

import com.vestshop.Entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ConversationRepo extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findFirstByCustomerIdAndStatusOrderByCreatedAtDesc(String customerId, String status);

    // ✅ Admin list: lấy các conversation OPEN, sort theo updatedAt mới nhất
    List<Conversation> findByStatusOrderByUpdatedAtDesc(String status);

    // ✅ bump updated_at mỗi khi có message mới
    @Transactional
    @Modifying
    @Query("update Conversation c set c.updatedAt = CURRENT_TIMESTAMP where c.id = :id")
    int touchUpdatedAt(@Param("id") Long id);
}