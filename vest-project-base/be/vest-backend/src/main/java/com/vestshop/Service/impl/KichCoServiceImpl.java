package com.vestshop.Service.impl;

import com.vestshop.Entity.KichCo;
import com.vestshop.Repository.KichCoRepository;
import com.vestshop.Service.KichCoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KichCoServiceImpl implements KichCoService {

    private final KichCoRepository kichCoRepository;

    @Override
    public Page<KichCo> getAll(Pageable pageable) {
        return kichCoRepository.findAll(pageable);
    }

    @Override
    public List<KichCo> getAllList() {
        return kichCoRepository.findAll();
    }

    @Override
    public KichCo getById(Long id) {
        return kichCoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kích cỡ không tồn tại"));
    }

    @Override
    public KichCo create(KichCo request) {
        return kichCoRepository.save(request);
    }

    @Override
    public KichCo update(Long id, KichCo request) {
        KichCo existing = getById(id);
        existing.setMa(request.getMa());
        existing.setSoSize(request.getSoSize());
        existing.setTrangThai(request.getTrangThai());
        return kichCoRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        kichCoRepository.deleteById(id);
    }
}
