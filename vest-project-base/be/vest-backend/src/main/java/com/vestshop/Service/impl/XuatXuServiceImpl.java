package com.vestshop.Service.impl;

import com.vestshop.Entity.XuatXu;
import com.vestshop.Repository.XuatXuRepository;
import com.vestshop.Service.XuatXuService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class XuatXuServiceImpl implements XuatXuService {

    private final XuatXuRepository xuatXuRepository;

    @Override
    public Page<XuatXu> getAll(Pageable pageable) {
        return xuatXuRepository.findAll(pageable);
    }

    @Override
    public List<XuatXu> getAllList() {
        return xuatXuRepository.findAll();
    }

    @Override
    public XuatXu getById(Long id) {
        return xuatXuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Xuất xứ không tồn tại"));
    }

    @Override
    public XuatXu create(XuatXu request) {
        return xuatXuRepository.save(request);
    }

    @Override
    public XuatXu update(Long id, XuatXu request) {
        XuatXu existing = getById(id);
        existing.setMa(request.getMa());
        existing.setTen(request.getTen());
        existing.setTrangThai(request.getTrangThai());
        return xuatXuRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        xuatXuRepository.deleteById(id);
    }
}
