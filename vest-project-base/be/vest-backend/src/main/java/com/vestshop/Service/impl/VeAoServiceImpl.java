package com.vestshop.Service.impl;

import com.vestshop.Entity.VeAo;
import com.vestshop.Repository.VeAoRepository;
import com.vestshop.Service.VeAoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VeAoServiceImpl implements VeAoService {

    private final VeAoRepository veAoRepository;

    @Override
    public Page<VeAo> getAll(Pageable pageable) {
        return veAoRepository.findAll(pageable);
    }

    @Override
    public List<VeAo> getAllList() {
        return veAoRepository.findAll();
    }

    @Override
    public VeAo getById(Long id) {
        return veAoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ve áo không tồn tại"));
    }

    @Override
    public VeAo create(VeAo request) {
        return veAoRepository.save(request);
    }

    @Override
    public VeAo update(Long id, VeAo request) {
        VeAo existing = getById(id);
        existing.setMa(request.getMa());
        existing.setTen(request.getTen());
        existing.setTrangThai(request.getTrangThai());
        return veAoRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        veAoRepository.deleteById(id);
    }
}
