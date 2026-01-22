package com.vestshop.Service.impl;

import com.vestshop.Entity.ChatLieu;
import com.vestshop.Repository.ChatLieuRepository;
import com.vestshop.Service.ChatLieuService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatLieuServiceImpl implements ChatLieuService {

    private final ChatLieuRepository chatLieuRepository;

    @Override
    public Page<ChatLieu> getAll(Pageable pageable) {
        return chatLieuRepository.findAll(pageable);
    }

    @Override
    public List<ChatLieu> getAllList() {
        return chatLieuRepository.findAll();
    }

    @Override
    public ChatLieu getById(Long id) {
        return chatLieuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chất liệu không tồn tại"));
    }

    @Override
    public ChatLieu create(ChatLieu request) {
        return chatLieuRepository.save(request);
    }

    @Override
    public ChatLieu update(Long id, ChatLieu request) {
        ChatLieu existing = getById(id);
        existing.setMa(request.getMa());
        existing.setTen(request.getTen());
        existing.setTrangThai(request.getTrangThai());
        return chatLieuRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        chatLieuRepository.deleteById(id);
    }
}
