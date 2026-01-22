package com.vestshop.Service.impl;

import com.vestshop.Entity.Fit;
import com.vestshop.Repository.FitRepository;
import com.vestshop.Service.FitService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FitServiceImpl implements FitService {

    private final FitRepository fitRepository;

    @Override
    public Page<Fit> getAll(Pageable pageable) {
        return fitRepository.findAll(pageable);
    }

    @Override
    public List<Fit> getAllList() {
        return fitRepository.findAll();
    }

    @Override
    public Fit getById(Long id) {
        return fitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fit không tồn tại"));
    }

    @Override
    public Fit create(Fit request) {
        return fitRepository.save(request);
    }

    @Override
    public Fit update(Long id, Fit request) {
        Fit existing = getById(id);
        existing.setMa(request.getMa());
        existing.setTen(request.getTen());
        existing.setTrangThai(request.getTrangThai());
        return fitRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        fitRepository.deleteById(id);
    }
}
