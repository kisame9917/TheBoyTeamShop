package com.vestshop.Service.impl;

import com.vestshop.Entity.SoKhuy;
import com.vestshop.Repository.SoKhuyRepository;
import com.vestshop.Service.SoKhuyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SoKhuyServiceImpl implements SoKhuyService {

    private final SoKhuyRepository soKhuyRepository;

    @Override
    public Page<SoKhuy> getAll(Pageable pageable) {
        return soKhuyRepository.findAll(pageable);
    }

    @Override
    public List<SoKhuy> getAllList() {
        return soKhuyRepository.findAll();
    }

    @Override
    public SoKhuy getById(Long id) {
        return soKhuyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Số khuy không tồn tại"));
    }

    @Override
    public SoKhuy create(SoKhuy request) {
        return soKhuyRepository.save(request);
    }

    @Override
    public SoKhuy update(Long id, SoKhuy request) {
        SoKhuy existing = getById(id);
        existing.setMa(request.getMa());
        existing.setTen(request.getTen());
        existing.setTrangThai(request.getTrangThai());
        return soKhuyRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        soKhuyRepository.deleteById(id);
    }
}
