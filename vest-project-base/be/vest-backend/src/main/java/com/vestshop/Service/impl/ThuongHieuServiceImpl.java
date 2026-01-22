package com.vestshop.Service.impl;

import com.vestshop.Entity.ThuongHieu;
import com.vestshop.Repository.ThuongHieuRepository;
import com.vestshop.Service.ThuongHieuService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ThuongHieuServiceImpl implements ThuongHieuService {

    private final ThuongHieuRepository thuongHieuRepository;

    @Override
    public Page<ThuongHieu> getAll(Pageable pageable) {
        return thuongHieuRepository.findAll(pageable);
    }

    @Override
    public List<ThuongHieu> getAllList() {
        return thuongHieuRepository.findAll();
    }

    @Override
    public ThuongHieu getById(Long id) {
        return thuongHieuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Thương hiệu không tồn tại"));
    }

    @Override
    public ThuongHieu create(ThuongHieu request) {
        return thuongHieuRepository.save(request);
    }

    @Override
    public ThuongHieu update(Long id, ThuongHieu request) {
        ThuongHieu existing = getById(id);
        existing.setMa(request.getMa());
        existing.setTen(request.getTen());
        existing.setTrangThai(request.getTrangThai());
        return thuongHieuRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        thuongHieuRepository.deleteById(id);
    }
}
