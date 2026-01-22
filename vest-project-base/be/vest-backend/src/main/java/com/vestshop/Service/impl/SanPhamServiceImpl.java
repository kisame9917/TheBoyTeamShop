package com.vestshop.Service.impl;

import com.vestshop.Entity.*;
import com.vestshop.Repository.*;
import com.vestshop.Service.SanPhamService;
import com.vestshop.dto.request.SanPhamRequest;
import com.vestshop.dto.response.SanPhamResponse;
import com.vestshop.dto.request.SanPhamChiTietRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SanPhamServiceImpl implements SanPhamService {

    private final SanPhamRepository sanPhamRepository;
    private final ChatLieuRepository chatLieuRepository;
    private final LoaiSanPhamRepository loaiSanPhamRepository;
    private final ThuongHieuRepository thuongHieuRepository;
    private final SoKhuyRepository soKhuyRepository;
    private final KieuTuiRepository kieuTuiRepository;
    private final VeAoRepository veAoRepository;
    private final XeTaRepository xeTaRepository;
    private final XuatXuRepository xuatXuRepository;
    private final FitRepository fitRepository;
    private final MauSacRepository mauSacRepository;
    private final KichCoRepository kichCoRepository;
    private final SanPhamChiTietRepository sanPhamChiTietRepository;

    @Override
    @Transactional
    public SanPhamResponse create(SanPhamRequest request) {
        SanPham sanPham = mapToEntity(request);
        sanPham.setNgayTao(LocalDateTime.now());
        sanPham.setNgayCapNhat(LocalDateTime.now());
        SanPham saved = sanPhamRepository.save(sanPham);

        List<SanPhamChiTiet> details = new ArrayList<>();

        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            for (SanPhamChiTietRequest variantRequest : request.getVariants()) {
                MauSac mauSac = mauSacRepository.findById(variantRequest.getIdMauSac())
                        .orElseThrow(() -> new RuntimeException("MauSac not found: " + variantRequest.getIdMauSac()));
                KichCo kichCo = kichCoRepository.findById(variantRequest.getIdKichCo())
                        .orElseThrow(() -> new RuntimeException("KichCo not found: " + variantRequest.getIdKichCo()));

                SanPhamChiTiet chiTiet = SanPhamChiTiet.builder()
                        .sanPham(saved)
                        .mauSac(mauSac)
                        .kichCo(kichCo)
                        .donGia(variantRequest.getDonGia())
                        .soLuongTon(variantRequest.getSoLuongTon())
                        .maSanPhamChiTiet(saved.getMaSanPham() + "-" + mauSac.getId() + "-" + kichCo.getId()) // Simple SKU logic
                        .anh(variantRequest.getAnh())
                        .chatLieu(variantRequest.getChatLieu())
                        .trangThai(true)
                        .ngayTao(LocalDateTime.now())
                        .ngayCapNhat(LocalDateTime.now())
                        .build();
                details.add(chiTiet);
            }
        } else if (request.getMauSacId() != null && request.getKichCoId() != null) {
            // Legacy single variant logic
            MauSac mauSac = mauSacRepository.findById(request.getMauSacId())
                    .orElseThrow(() -> new RuntimeException("MauSac not found"));
            KichCo kichCo = kichCoRepository.findById(request.getKichCoId())
                    .orElseThrow(() -> new RuntimeException("KichCo not found"));

            SanPhamChiTiet chiTiet = SanPhamChiTiet.builder()
                    .sanPham(saved)
                    .mauSac(mauSac)
                    .kichCo(kichCo)
                    .donGia(request.getDonGia())
                    .soLuongTon(request.getSoLuongTon())
                    .maSanPhamChiTiet(saved.getMaSanPham() + "-" + mauSac.getId() + "-" + kichCo.getId())
                    .trangThai(true)
                    .ngayTao(LocalDateTime.now())
                    .ngayCapNhat(LocalDateTime.now())
                    .build();
            details.add(chiTiet);
        }

        if (!details.isEmpty()) {
            sanPhamChiTietRepository.saveAll(details);
            saved.setSanPhamChiTiets(details);
        }

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SanPhamResponse> getAll(Pageable pageable) {
        return sanPhamRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public SanPhamResponse getById(Long id) {
        SanPham sanPham = sanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SanPham not found with id: " + id));
        return mapToResponse(sanPham);
    }

    @Override
    @Transactional
    public SanPhamResponse update(Long id, SanPhamRequest request) {
        SanPham sanPham = sanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SanPham not found with id: " + id));
        
        updateEntity(sanPham, request);
        sanPham.setNgayCapNhat(LocalDateTime.now());
        
        SanPham updated = sanPhamRepository.save(sanPham);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!sanPhamRepository.existsById(id)) {
            throw new RuntimeException("SanPham not found with id: " + id);
        }
        sanPhamRepository.deleteById(id);
    }

    private SanPham mapToEntity(SanPhamRequest request) {
        return SanPham.builder()
                .maSanPham(request.getMaSanPham())
                .tenSanPham(request.getTenSanPham())
                .trangThai(request.getTrangThai())
                .moTa(request.getMoTa())
                .moTa(request.getMoTa())
                .chatLieu(getChatLieuOrDefault(request.getChatLieuId()))
                .loaiSanPham(loaiSanPhamRepository.findById(request.getLoaiSanPhamId()).orElseThrow(() -> new RuntimeException("LoaiSanPham not found")))
                .thuongHieu(thuongHieuRepository.findById(request.getThuongHieuId()).orElseThrow(() -> new RuntimeException("ThuongHieu not found")))
                .soKhuy(soKhuyRepository.findById(request.getSoKhuyId()).orElseThrow(() -> new RuntimeException("SoKhuy not found")))
                .kieuTui(kieuTuiRepository.findById(request.getKieuTuiId()).orElseThrow(() -> new RuntimeException("KieuTui not found")))
                .veAo(veAoRepository.findById(request.getVeAoId()).orElseThrow(() -> new RuntimeException("VeAo not found")))
                .xeTa(xeTaRepository.findById(request.getXeTaId()).orElseThrow(() -> new RuntimeException("XeTa not found")))
                .xuatXu(xuatXuRepository.findById(request.getXuatXuId()).orElseThrow(() -> new RuntimeException("XuatXu not found")))
                .fit(fitRepository.findById(request.getFitId()).orElseThrow(() -> new RuntimeException("Fit not found")))
                .anh(request.getAnh())
                .build();
    }

    private void updateEntity(SanPham sanPham, SanPhamRequest request) {
        sanPham.setMaSanPham(request.getMaSanPham());
        sanPham.setTenSanPham(request.getTenSanPham());
        sanPham.setTrangThai(request.getTrangThai());
        sanPham.setTrangThai(request.getTrangThai());
        sanPham.setMoTa(request.getMoTa());
        sanPham.setAnh(request.getAnh());
        
        if (!sanPham.getChatLieu().getId().equals(request.getChatLieuId())) {
            sanPham.setChatLieu(chatLieuRepository.findById(request.getChatLieuId()).orElseThrow(() -> new RuntimeException("ChatLieu not found")));
        }
        if (!sanPham.getLoaiSanPham().getId().equals(request.getLoaiSanPhamId())) {
            sanPham.setLoaiSanPham(loaiSanPhamRepository.findById(request.getLoaiSanPhamId()).orElseThrow(() -> new RuntimeException("LoaiSanPham not found")));
        }
        if (!sanPham.getThuongHieu().getId().equals(request.getThuongHieuId())) {
            sanPham.setThuongHieu(thuongHieuRepository.findById(request.getThuongHieuId()).orElseThrow(() -> new RuntimeException("ThuongHieu not found")));
        }
        if (!sanPham.getSoKhuy().getId().equals(request.getSoKhuyId())) {
            sanPham.setSoKhuy(soKhuyRepository.findById(request.getSoKhuyId()).orElseThrow(() -> new RuntimeException("SoKhuy not found")));
        }
        if (!sanPham.getKieuTui().getId().equals(request.getKieuTuiId())) {
            sanPham.setKieuTui(kieuTuiRepository.findById(request.getKieuTuiId()).orElseThrow(() -> new RuntimeException("KieuTui not found")));
        }
        if (!sanPham.getVeAo().getId().equals(request.getVeAoId())) {
            sanPham.setVeAo(veAoRepository.findById(request.getVeAoId()).orElseThrow(() -> new RuntimeException("VeAo not found")));
        }
        if (!sanPham.getXeTa().getId().equals(request.getXeTaId())) {
            sanPham.setXeTa(xeTaRepository.findById(request.getXeTaId()).orElseThrow(() -> new RuntimeException("XeTa not found")));
        }
        if (!sanPham.getXuatXu().getId().equals(request.getXuatXuId())) {
            sanPham.setXuatXu(xuatXuRepository.findById(request.getXuatXuId()).orElseThrow(() -> new RuntimeException("XuatXu not found")));
        }
        if (!sanPham.getFit().getId().equals(request.getFitId())) {
            sanPham.setFit(fitRepository.findById(request.getFitId()).orElseThrow(() -> new RuntimeException("Fit not found")));
        }
    }

    private SanPhamResponse mapToResponse(SanPham sanPham) {
        return SanPhamResponse.builder()
                .id(sanPham.getId())
                .maSanPham(sanPham.getMaSanPham())
                .tenSanPham(sanPham.getTenSanPham())
                .ngayTao(sanPham.getNgayTao())
                .ngayCapNhat(sanPham.getNgayCapNhat())
                .trangThai(sanPham.getTrangThai())
                .moTa(sanPham.getMoTa())
                .moTa(sanPham.getMoTa())
                .chatLieuId(sanPham.getChatLieu() != null ? sanPham.getChatLieu().getId() : null)
                .loaiSanPhamId(sanPham.getLoaiSanPham().getId())
                .thuongHieuId(sanPham.getThuongHieu().getId())
                .soKhuyId(sanPham.getSoKhuy().getId())
                .kieuTuiId(sanPham.getKieuTui().getId())
                .veAoId(sanPham.getVeAo().getId())
                .xeTaId(sanPham.getXeTa().getId())
                .xuatXuId(sanPham.getXuatXu().getId())
                .fitId(sanPham.getFit().getId())
                .tenLoaiSanPham(sanPham.getLoaiSanPham().getTen())
                .tenThuongHieu(sanPham.getThuongHieu().getTen())
                // Calculation logic for derived fields
                .soLuongTon(calculateSoLuongTon(sanPham))
                .giaMin(calculateGiaMin(sanPham))
                .giaMax(calculateGiaMax(sanPham))
                .build();
    }

    private Integer calculateSoLuongTon(SanPham sanPham) {
        if (sanPham.getSanPhamChiTiets() == null || sanPham.getSanPhamChiTiets().isEmpty()) {
            return 0;
        }
        return sanPham.getSanPhamChiTiets().stream()
                .filter(ct -> ct.getTrangThai() != null && ct.getTrangThai()) // Only count Active variants
                .mapToInt(com.vestshop.Entity.SanPhamChiTiet::getSoLuongTon)
                .sum();
    }

    private java.math.BigDecimal calculateGiaMin(SanPham sanPham) {
        if (sanPham.getSanPhamChiTiets() == null || sanPham.getSanPhamChiTiets().isEmpty()) {
            return java.math.BigDecimal.ZERO;
        }
        return sanPham.getSanPhamChiTiets().stream()
                .map(com.vestshop.Entity.SanPhamChiTiet::getDonGia)
                .min(java.util.Comparator.naturalOrder())
                .orElse(java.math.BigDecimal.ZERO);
    }

    private java.math.BigDecimal calculateGiaMax(SanPham sanPham) {
        if (sanPham.getSanPhamChiTiets() == null || sanPham.getSanPhamChiTiets().isEmpty()) {
            return java.math.BigDecimal.ZERO;
        }
        return sanPham.getSanPhamChiTiets().stream()
                .map(com.vestshop.Entity.SanPhamChiTiet::getDonGia)
                .max(java.util.Comparator.naturalOrder())
                .orElse(java.math.BigDecimal.ZERO);
    }
    private ChatLieu getChatLieuOrDefault(Long id) {
        if (id != null) {
            return chatLieuRepository.findById(id).orElseGet(this::getDefaultChatLieu);
        }
        return getDefaultChatLieu();
    }

    private ChatLieu getDefaultChatLieu() {
        return chatLieuRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("He thong can it nhat 1 Chat Lieu de hoat dong (Legacy DB Constraint)."));
    }
}
