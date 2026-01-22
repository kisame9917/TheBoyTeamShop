package com.vestshop.Service.impl;

import com.vestshop.Entity.KieuTui;
import com.vestshop.Repository.KieuTuiRepository;
import com.vestshop.Service.KieuTuiService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KieuTuiServiceImpl implements KieuTuiService {

    private final KieuTuiRepository kieuTuiRepository;

    @Override
    public Page<KieuTui> getAll(Pageable pageable) {
        return kieuTuiRepository.findAll(pageable);
    }

    @Override
    public List<KieuTui> getAllList() {
        return kieuTuiRepository.findAll();
    }

    @Override
    public KieuTui getById(Long id) {
        return kieuTuiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kiểu túi không tồn tại"));
    }

    @Override
    public KieuTui create(KieuTui request) {
        return kieuTuiRepository.save(request);
    }

    @Override
    public KieuTui update(Long id, KieuTui request) {
        KieuTui existing = getById(id);
        existing.setMa(request.getMa());
        existing.setTen(request.getTen());
        existing.setTrangThai(request.getTrangThai());
        return kieuTuiRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        kieuTuiRepository.deleteById(id);
    }
}
