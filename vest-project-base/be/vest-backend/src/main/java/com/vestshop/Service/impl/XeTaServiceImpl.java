package com.vestshop.Service.impl;

import com.vestshop.Entity.XeTa;
import com.vestshop.Repository.XeTaRepository;
import com.vestshop.Service.XeTaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class XeTaServiceImpl implements XeTaService {

    private final XeTaRepository xeTaRepository;

    @Override
    public Page<XeTa> getAll(Pageable pageable) {
        return xeTaRepository.findAll(pageable);
    }

    @Override
    public List<XeTa> getAllList() {
        return xeTaRepository.findAll();
    }

    @Override
    public XeTa getById(Long id) {
        return xeTaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Xẻ tà không tồn tại"));
    }

    @Override
    public XeTa create(XeTa request) {
        return xeTaRepository.save(request);
    }

    @Override
    public XeTa update(Long id, XeTa request) {
        XeTa existing = getById(id);
        existing.setMa(request.getMa());
        existing.setTen(request.getTen());
        existing.setTrangThai(request.getTrangThai());
        return xeTaRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        xeTaRepository.deleteById(id);
    }
}
