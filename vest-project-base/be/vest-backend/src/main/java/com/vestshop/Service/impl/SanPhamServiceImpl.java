package com.vestshop.Service.impl;

import com.vestshop.Entity.*;
import com.vestshop.Repository.*;
import com.vestshop.Service.CloudinaryMediaStorageService;
import com.vestshop.Service.SanPhamService;
import com.vestshop.dto.request.SanPhamChiTietRequest;
import com.vestshop.dto.request.SanPhamRequest;
import com.vestshop.dto.response.SanPhamResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private final AnhChiTietSanPhamRepository anhChiTietSanPhamRepository;
    private final CloudinaryMediaStorageService mediaStorageService;

    @Override
    @Transactional
    public SanPhamResponse create(SanPhamRequest request) {
        SanPham sanPham = mapToEntity(request);
        LocalDateTime now = LocalDateTime.now();
        sanPham.setNgayTao(now);
        sanPham.setNgayCapNhat(now);
        SanPham saved = sanPhamRepository.save(sanPham);

        List<SanPhamChiTiet> details = new ArrayList<>();

        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            for (SanPhamChiTietRequest variantRequest : request.getVariants()) {
                details.add(buildVariant(saved, variantRequest));
            }
        } else if (request.getMauSacId() != null && request.getKichCoId() != null) {
            SanPhamChiTietRequest variantRequest = SanPhamChiTietRequest.builder()
                    .idSanPham(saved.getId())
                    .idMauSac(request.getMauSacId())
                    .idKichCo(request.getKichCoId())
                    .soLuongTon(request.getSoLuongTon())
                    .donGia(request.getDonGia())
                    .trangThai(Boolean.TRUE)
                    .build();
            details.add(buildVariant(saved, variantRequest));
        }

        if (!details.isEmpty()) {
            List<SanPhamChiTiet> persisted = sanPhamChiTietRepository.saveAll(details);
            saved.setSanPhamChiTiets(persisted);
            for (int i = 0; i < persisted.size(); i++) {
                syncVariantGallery(persisted.get(i), request.getVariants() != null && request.getVariants().size() > i ? request.getVariants().get(i) : null);
            }
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
        MediaAsset mediaCover = mediaStorageService.getOptional(request.getMediaCoverId());
        String resolvedCoverUrl = mediaStorageService.resolveUrl(mediaCover, request.getAnh());
        return SanPham.builder()
                .maSanPham(request.getMaSanPham())
                .tenSanPham(request.getTenSanPham())
                .trangThai(request.getTrangThai())
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
                .mediaCover(mediaCover)
                .anh(resolvedCoverUrl)
                .build();
    }

    private void updateEntity(SanPham sanPham, SanPhamRequest request) {
        sanPham.setMaSanPham(request.getMaSanPham());
        sanPham.setTenSanPham(request.getTenSanPham());
        sanPham.setTrangThai(request.getTrangThai());
        sanPham.setMoTa(request.getMoTa());

        MediaAsset mediaCover = mediaStorageService.getOptional(request.getMediaCoverId());
        sanPham.setMediaCover(mediaCover);
        if (request.getAnh() != null || mediaCover != null) {
            sanPham.setAnh(mediaStorageService.resolveUrl(mediaCover, request.getAnh()));
        }

        if (request.getChatLieuId() != null && (sanPham.getChatLieu() == null || !sanPham.getChatLieu().getId().equals(request.getChatLieuId()))) {
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

    private SanPhamChiTiet buildVariant(SanPham sanPham, SanPhamChiTietRequest request) {
        MauSac mauSac = mauSacRepository.findById(request.getIdMauSac())
                .orElseThrow(() -> new RuntimeException("MauSac not found: " + request.getIdMauSac()));
        KichCo kichCo = kichCoRepository.findById(request.getIdKichCo())
                .orElseThrow(() -> new RuntimeException("KichCo not found: " + request.getIdKichCo()));

        MediaAsset mediaPrimary = mediaStorageService.getOptional(request.getMediaPrimaryId());
        String imageUrl = mediaStorageService.resolveUrl(mediaPrimary, request.getAnh());

        return SanPhamChiTiet.builder()
                .sanPham(sanPham)
                .mauSac(mauSac)
                .kichCo(kichCo)
                .donGia(request.getDonGia())
                .soLuongTon(request.getSoLuongTon())
                .maSanPhamChiTiet(sanPham.getMaSanPham() + "-" + mauSac.getId() + "-" + kichCo.getId())
                .anh(imageUrl)
                .mediaPrimary(mediaPrimary)
                .chatLieu(request.getChatLieu())
                .ghiChu(request.getGhiChu())
                .trangThai(request.getTrangThai() != null ? request.getTrangThai() : Boolean.TRUE)
                .ngayTao(LocalDateTime.now())
                .ngayCapNhat(LocalDateTime.now())
                .build();
    }

    private void syncVariantGallery(SanPhamChiTiet entity, SanPhamChiTietRequest request) {
        if (request == null || request.getGalleryMediaIds() == null || request.getGalleryMediaIds().isEmpty()) {
            return;
        }

        int order = 1;
        for (Long mediaId : request.getGalleryMediaIds()) {
            MediaAsset media = mediaStorageService.getOptional(mediaId);
            if (media == null) continue;
            AnhChiTietSanPham gallery = AnhChiTietSanPham.builder()
                    .sanPhamChiTiet(entity)
                    .ma("IMG-" + entity.getId() + "-" + order)
                    .ten(media.getSecureUrl())
                    .mediaAsset(media)
                    .thuTuHienThi(order++)
                    .trangThai(Boolean.TRUE)
                    .build();
            anhChiTietSanPhamRepository.save(gallery);
        }
    }

    private SanPhamResponse mapToResponse(SanPham sanPham) {
        String imageUrl = mediaStorageService.resolveUrl(sanPham.getMediaCover(), sanPham.getAnh());
        return SanPhamResponse.builder()
                .id(sanPham.getId())
                .maSanPham(sanPham.getMaSanPham())
                .tenSanPham(sanPham.getTenSanPham())
                .anh(imageUrl)
                .imageUrl(imageUrl)
                .mediaCoverId(sanPham.getMediaCover() != null ? sanPham.getMediaCover().getId() : null)
                .ngayTao(sanPham.getNgayTao())
                .ngayCapNhat(sanPham.getNgayCapNhat())
                .trangThai(sanPham.getTrangThai())
                .moTa(sanPham.getMoTa())
                .chatLieuId(sanPham.getChatLieu() != null ? sanPham.getChatLieu().getId() : null)
                .loaiSanPhamId(sanPham.getLoaiSanPham() != null ? sanPham.getLoaiSanPham().getId() : null)
                .thuongHieuId(sanPham.getThuongHieu() != null ? sanPham.getThuongHieu().getId() : null)
                .soKhuyId(sanPham.getSoKhuy() != null ? sanPham.getSoKhuy().getId() : null)
                .kieuTuiId(sanPham.getKieuTui() != null ? sanPham.getKieuTui().getId() : null)
                .veAoId(sanPham.getVeAo() != null ? sanPham.getVeAo().getId() : null)
                .xeTaId(sanPham.getXeTa() != null ? sanPham.getXeTa().getId() : null)
                .xuatXuId(sanPham.getXuatXu() != null ? sanPham.getXuatXu().getId() : null)
                .fitId(sanPham.getFit() != null ? sanPham.getFit().getId() : null)
                .tenLoaiSanPham(sanPham.getLoaiSanPham() != null ? sanPham.getLoaiSanPham().getTen() : null)
                .tenThuongHieu(sanPham.getThuongHieu() != null ? sanPham.getThuongHieu().getTen() : null)
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
                .filter(ct -> ct.getTrangThai() != null && ct.getTrangThai())
                .mapToInt(SanPhamChiTiet::getSoLuongTon)
                .sum();
    }

    private BigDecimal calculateGiaMin(SanPham sanPham) {
        if (sanPham.getSanPhamChiTiets() == null || sanPham.getSanPhamChiTiets().isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sanPham.getSanPhamChiTiets().stream()
                .map(SanPhamChiTiet::getDonGia)
                .min(java.util.Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal calculateGiaMax(SanPham sanPham) {
        if (sanPham.getSanPhamChiTiets() == null || sanPham.getSanPhamChiTiets().isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sanPham.getSanPhamChiTiets().stream()
                .map(SanPhamChiTiet::getDonGia)
                .max(java.util.Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
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

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getGiaMaxDb() {
        return sanPhamChiTietRepository.findMaxDonGia();
    }
}
