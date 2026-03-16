package com.vestshop.Service.impl;

import com.vestshop.Entity.AnhChiTietSanPham;
import com.vestshop.Entity.MediaAsset;
import com.vestshop.Entity.SanPham;
import com.vestshop.Entity.SanPhamChiTiet;
import com.vestshop.Repository.AnhChiTietSanPhamRepository;
import com.vestshop.Repository.KichCoRepository;
import com.vestshop.Repository.MauSacRepository;
import com.vestshop.Repository.SanPhamChiTietRepository;
import com.vestshop.Repository.SanPhamRepository;
import com.vestshop.Service.CloudinaryMediaStorageService;
import com.vestshop.Service.SanPhamChiTietService;
import com.vestshop.dto.request.SanPhamChiTietRequest;
import com.vestshop.dto.response.SanPhamChiTietResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.vestshop.Service.NotificationRealtimeService;
import com.vestshop.dto.response.NotificationEventResponse;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SanPhamChiTietServiceImpl implements SanPhamChiTietService {

    private final SanPhamChiTietRepository repository;
    private final SanPhamRepository sanPhamRepository;
    private final KichCoRepository kichCoRepository;
    private final MauSacRepository mauSacRepository;
    private final AnhChiTietSanPhamRepository anhChiTietSanPhamRepository;
    private final CloudinaryMediaStorageService mediaStorageService;
    private final NotificationRealtimeService notificationRealtimeService;
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

        String sku = "SKU" + System.currentTimeMillis();
        MediaAsset mediaPrimary = mediaStorageService.getOptional(request.getMediaPrimaryId());
        String imageUrl = mediaStorageService.resolveUrl(mediaPrimary, request.getAnh());

        SanPhamChiTiet entity = SanPhamChiTiet.builder()
                .sanPham(sanPham)
                .kichCo(kichCoRepository.findById(request.getIdKichCo()).orElseThrow(() -> new RuntimeException("Size not found")))
                .mauSac(mauSacRepository.findById(request.getIdMauSac()).orElseThrow(() -> new RuntimeException("Color not found")))
                .soLuongTon(request.getSoLuongTon())
                .donGia(request.getDonGia())
                .ghiChu(request.getGhiChu())
                .trangThai(request.getTrangThai() != null ? request.getTrangThai() : Boolean.TRUE)
                .maSanPhamChiTiet(sku)
                .anh(imageUrl)
                .mediaPrimary(mediaPrimary)
                .chatLieu(request.getChatLieu())
                .ngayTao(LocalDateTime.now())
                .ngayCapNhat(LocalDateTime.now())
                .build();

        SanPhamChiTiet saved = repository.save(entity);
        replaceGallery(saved, request.getGalleryMediaIds());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public SanPhamChiTietResponse decreaseStock(Long id, Integer qty) {
        int q = (qty == null || qty <= 0) ? 1 : qty;

        SanPhamChiTiet beforeEntity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ko tìm thấy chi tiết sản phẩm"));
        Integer beforeQty = beforeEntity.getSoLuongTon();

        int updated = repository.decreaseStock(id, q);
        if (updated == 0) {
            throw new IllegalArgumentException("Không đủ tồn kho");
        }

        SanPhamChiTiet entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ko tìm thấy chi tiết sản phẩm"));

        pushOutOfStockNotificationIfNeeded(beforeQty, entity);

        return mapToResponse(entity);
    }

    @Override
    @Transactional
    public SanPhamChiTietResponse increaseStock(Long id, Integer qty) {
        int q = (qty == null || qty <= 0) ? 1 : qty;

        int updated = repository.increaseStock(id, q);
        if (updated == 0) throw new IllegalArgumentException("Detail not found");

        SanPhamChiTiet entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Detail not found"));

        return mapToResponse(entity);
    }

    @Override
    @Transactional
    public SanPhamChiTietResponse update(Long id, SanPhamChiTietRequest request) {
        SanPhamChiTiet entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Detail not found"));

        Integer beforeQty = entity.getSoLuongTon();

        entity.setKichCo(kichCoRepository.findById(request.getIdKichCo()).orElseThrow(() -> new RuntimeException("Size not found")));
        entity.setMauSac(mauSacRepository.findById(request.getIdMauSac()).orElseThrow(() -> new RuntimeException("Color not found")));
        entity.setSoLuongTon(request.getSoLuongTon());
        entity.setDonGia(request.getDonGia());
        entity.setGhiChu(request.getGhiChu());
        entity.setTrangThai(request.getTrangThai());
        entity.setChatLieu(request.getChatLieu());

        MediaAsset mediaPrimary = mediaStorageService.getOptional(request.getMediaPrimaryId());
        entity.setMediaPrimary(mediaPrimary);
        if (request.getAnh() != null || mediaPrimary != null) {
            entity.setAnh(mediaStorageService.resolveUrl(mediaPrimary, request.getAnh()));
        }
        entity.setNgayCapNhat(LocalDateTime.now());

        SanPhamChiTiet saved = repository.save(entity);
        if (request.getGalleryMediaIds() != null) {
            replaceGallery(saved, request.getGalleryMediaIds());
        }

        pushOutOfStockNotificationIfNeeded(beforeQty, saved);

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void replaceGallery(SanPhamChiTiet entity, List<Long> galleryMediaIds) {
        if (galleryMediaIds == null) return;

        List<AnhChiTietSanPham> existing = anhChiTietSanPhamRepository.findAllBySanPhamChiTiet_IdOrderByThuTuHienThiAscIdAsc(entity.getId());
        if (!existing.isEmpty()) {
            anhChiTietSanPhamRepository.deleteAll(existing);
        }

        int order = 1;
        for (Long mediaId : galleryMediaIds) {
            MediaAsset media = mediaStorageService.getOptional(mediaId);
            if (media == null) continue;
            AnhChiTietSanPham row = AnhChiTietSanPham.builder()
                    .sanPhamChiTiet(entity)
                    .ma("IMG-" + entity.getId() + "-" + order)
                    .ten(media.getSecureUrl())
                    .mediaAsset(media)
                    .thuTuHienThi(order++)
                    .trangThai(Boolean.TRUE)
                    .build();
            anhChiTietSanPhamRepository.save(row);
        }
    }

    private SanPhamChiTietResponse mapToResponse(SanPhamChiTiet entity) {
        List<String> gallery = new ArrayList<>();
        List<AnhChiTietSanPham> galleryRows = anhChiTietSanPhamRepository.findAllBySanPhamChiTiet_IdAndTrangThaiTrue(entity.getId());
        for (AnhChiTietSanPham row : galleryRows) {
            String url = row.getMediaAsset() != null && row.getMediaAsset().getSecureUrl() != null
                    ? row.getMediaAsset().getSecureUrl()
                    : row.getTen();
            if (url != null && !url.isBlank()) gallery.add(url);
        }

        String imageUrl = mediaStorageService.resolveUrl(entity.getMediaPrimary(), entity.getAnh());
        if ((imageUrl == null || imageUrl.isBlank()) && !gallery.isEmpty()) {
            imageUrl = gallery.get(0);
        }

        return SanPhamChiTietResponse.builder()
                .id(entity.getId())
                .idSanPham(entity.getSanPham().getId())
                .maSanPham(entity.getSanPham().getMaSanPham())
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
                .anh(imageUrl)
                .imageUrl(imageUrl)
                .mediaPrimaryId(entity.getMediaPrimary() != null ? entity.getMediaPrimary().getId() : null)
                .gallery(gallery)
                .build();
    }
    private void pushOutOfStockNotificationIfNeeded(Integer beforeQty, SanPhamChiTiet entity) {
        int before = beforeQty == null ? 0 : beforeQty;
        int after = entity.getSoLuongTon() == null ? 0 : entity.getSoLuongTon();

        // chỉ bắn khi từ còn hàng -> hết hàng
        if (before > 0 && after == 0) {
            String tenSanPham = entity.getSanPham() != null ? entity.getSanPham().getTenSanPham() : "Sản phẩm";
            String mau = entity.getMauSac() != null ? entity.getMauSac().getTen() : "";
            String size = entity.getKichCo() != null ? entity.getKichCo().getSoSize() : "";

            String suffix = "";
            if (!mau.isBlank() || !size.isBlank()) {
                suffix = " (" + mau + (mau.isBlank() || size.isBlank() ? "" : " / ") + size + ")";
            }

            notificationRealtimeService.pushToRole(
                    "ADMIN",
                    NotificationEventResponse.builder()
                            .id(String.valueOf(System.currentTimeMillis()))
                            .title("Sản phẩm " + entity.getMaSanPhamChiTiet() + " đã hết hàng")
                            .time("Vừa xong")
                            .link("/products")
                            .type("PRODUCT_OUT_OF_STOCK")
                            .createdAt(LocalDateTime.now().toString())
                            .build()
            );
        }
    }
}
