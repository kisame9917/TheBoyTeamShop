package com.vestshop.Repository;

import com.vestshop.Entity.Conversation;
import com.vestshop.Entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ConversationRepo extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findFirstByCustomerAndStatusOrderByCreatedAtDesc(KhachHang customer, String status);

    @Query("""
        select c
        from Conversation c
        left join fetch c.customer
        where c.status = :status
        order by c.updatedAt desc
    """)
    List<Conversation> findByStatusOrderByUpdatedAtDesc(String status);
}