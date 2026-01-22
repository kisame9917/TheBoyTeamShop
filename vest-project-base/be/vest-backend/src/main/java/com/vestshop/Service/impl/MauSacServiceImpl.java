package com.vestshop.Service.impl;

import com.vestshop.Entity.MauSac;
import com.vestshop.Repository.MauSacRepository;
import com.vestshop.Service.MauSacService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MauSacServiceImpl implements MauSacService {

    private final MauSacRepository mauSacRepository;

    @Override
    public Page<MauSac> getAll(Pageable pageable) {
        return mauSacRepository.findAll(pageable);
    }

    @Override
    public List<MauSac> getAllList() {
        return mauSacRepository.findAll();
    }

    @Override
    public MauSac getById(Long id) {
        return mauSacRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Màu sắc không tồn tại"));
    }

    @Override
    public MauSac create(MauSac request) {
        return mauSacRepository.save(request);
    }

    @Override
    public MauSac update(Long id, MauSac request) {
        MauSac existing = getById(id);
        existing.setMa(request.getMa());
        existing.setTen(request.getTen());
        existing.setTrangThai(request.getTrangThai());
        return mauSacRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        mauSacRepository.deleteById(id);
    }
}
