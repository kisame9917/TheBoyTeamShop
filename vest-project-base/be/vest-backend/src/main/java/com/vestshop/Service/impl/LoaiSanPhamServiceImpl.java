package com.vestshop.Service.impl;

import com.vestshop.Entity.LoaiSanPham;
import com.vestshop.Repository.LoaiSanPhamRepository;
import com.vestshop.Service.LoaiSanPhamService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoaiSanPhamServiceImpl implements LoaiSanPhamService {

    private final LoaiSanPhamRepository loaiSanPhamRepository;

    @Override
    public Page<LoaiSanPham> getAll(Pageable pageable) {
        return loaiSanPhamRepository.findAll(pageable);
    }

    @Override
    public List<LoaiSanPham> getAllList() {
        return loaiSanPhamRepository.findAll();
    }

    @Override
    public LoaiSanPham getById(Long id) {
        return loaiSanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loại sản phẩm không tồn tại"));
    }

    @Override
    public LoaiSanPham create(LoaiSanPham request) {
        return loaiSanPhamRepository.save(request);
    }

    @Override
    public LoaiSanPham update(Long id, LoaiSanPham request) {
        LoaiSanPham existing = getById(id);
        existing.setMa(request.getMa());
        existing.setTen(request.getTen());
        if (request.getMoTa() != null) {
            existing.setMoTa(request.getMoTa());
        }
        existing.setTrangThai(request.getTrangThai());
        return loaiSanPhamRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        loaiSanPhamRepository.deleteById(id);
    }
}
