package com.vestshop.Repository;

import com.vestshop.Entity.MessageProductSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageProductSuggestionRepo extends JpaRepository<MessageProductSuggestion, Long> {
    List<MessageProductSuggestion> findByMessageId(Long messageId);
    List<MessageProductSuggestion> findByMessageIdIn(List<Long> messageIds);
}