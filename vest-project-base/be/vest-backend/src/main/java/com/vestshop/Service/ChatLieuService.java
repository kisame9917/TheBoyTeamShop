package com.vestshop.Service;

import com.vestshop.Entity.ChatLieu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ChatLieuService {
    Page<ChatLieu> getAll(Pageable pageable);
    List<ChatLieu> getAllList();
    ChatLieu getById(Long id);
    ChatLieu create(ChatLieu request);
    ChatLieu update(Long id, ChatLieu request);
    void delete(Long id);
}
