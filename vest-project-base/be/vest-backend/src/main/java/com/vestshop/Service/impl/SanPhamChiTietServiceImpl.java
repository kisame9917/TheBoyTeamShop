package com.vestshop.Service.impl;

import com.vestshop.Entity.SanPham;
import com.vestshop.Entity.SanPhamChiTiet;
import com.vestshop.Repository.KichCoRepository;
import com.vestshop.Repository.MauSacRepository;
import com.vestshop.Repository.SanPhamChiTietRepository;
import com.vestshop.Repository.SanPhamRepository;
import com.vestshop.Service.SanPhamChiTietService;
import com.vestshop.dto.request.SanPhamChiTietRequest;
import com.vestshop.dto.response.SanPhamChiTietResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SanPhamChiTietServiceImpl implements SanPhamChiTietService {

    private final SanPhamChiTietRepository repository;
    private final SanPhamRepository sanPhamRepository;
    private final KichCoRepository kichCoRepository;
    private final MauSacRepository mauSacRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<SanPhamChiTietResponse> getAll(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SanPhamChiTietResponse> getByProductId(Long productId) {
        return repository.findBySanPhamId(productId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SanPhamChiTietResponse create(SanPhamChiTietRequest request) {
        SanPham sanPham = sanPhamRepository.findById(request.getIdSanPham())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Generate SKU Code: SP + ProductId + SizeId + ColorId + Random/Timestamp? 
        // Simple Logic: SP{id}-{Size}-{Color}
        String sku = "SKU" + System.currentTimeMillis(); 

        SanPhamChiTiet entity = SanPhamChiTiet.builder()
                .sanPham(sanPham)
                .kichCo(kichCoRepository.findById(request.getIdKichCo()).orElseThrow(() -> new RuntimeException("Size not found")))
                .mauSac(mauSacRepository.findById(request.getIdMauSac()).orElseThrow(() -> new RuntimeException("Color not found")))
                .soLuongTon(request.getSoLuongTon())
                .donGia(request.getDonGia())
                .ghiChu(request.getGhiChu())
                .trangThai(request.getTrangThai())
                .maSanPhamChiTiet(sku)
                .anh(request.getAnh())
                .ngayTao(LocalDateTime.now())
                .ngayCapNhat(LocalDateTime.now())
                .build();

        return mapToResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public SanPhamChiTietResponse update(Long id, SanPhamChiTietRequest request) {
        SanPhamChiTiet entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Detail not found"));

        entity.setKichCo(kichCoRepository.findById(request.getIdKichCo()).orElseThrow(() -> new RuntimeException("Size not found")));
        entity.setMauSac(mauSacRepository.findById(request.getIdMauSac()).orElseThrow(() -> new RuntimeException("Color not found")));
        entity.setSoLuongTon(request.getSoLuongTon());
        entity.setDonGia(request.getDonGia());
        entity.setGhiChu(request.getGhiChu());
        entity.setTrangThai(request.getTrangThai());
        // Initial image is null or handle via separate upload, assuming request might have it if added to request DTO
        // For now, keeping it basic, will update Request DTO next.
        entity.setAnh(request.getAnh()); // Assuming 'anh' field is added to SanPhamChiTietRequest
        entity.setNgayCapNhat(LocalDateTime.now());

        return mapToResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private SanPhamChiTietResponse mapToResponse(SanPhamChiTiet entity) {
        return SanPhamChiTietResponse.builder()
                .id(entity.getId())
                .idSanPham(entity.getSanPham().getId())
                .tenSanPham(entity.getSanPham().getTenSanPham())
                .idKichCo(entity.getKichCo().getId())
                .tenKichCo(entity.getKichCo().getSoSize())
                .idMauSac(entity.getMauSac().getId())
                .tenMauSac(entity.getMauSac().getTen())
                .maSanPhamChiTiet(entity.getMaSanPhamChiTiet())
                .soLuongTon(entity.getSoLuongTon())
                .donGia(entity.getDonGia())
                .ghiChu(entity.getGhiChu())
                .trangThai(entity.getTrangThai())
                .anh(entity.getAnh())
                .build();
    }
}
