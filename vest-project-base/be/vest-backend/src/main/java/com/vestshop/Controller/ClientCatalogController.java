package com.vestshop.Controller;

import com.vestshop.Entity.SanPham;
import com.vestshop.Repository.SanPhamRepository;
import com.vestshop.Service.SanPhamChiTietService;
import com.vestshop.Service.SanPhamService;
import com.vestshop.dto.response.SanPhamChiTietResponse;
import com.vestshop.dto.response.SanPhamResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/client")
@RequiredArgsConstructor
public class ClientCatalogController {

    private final SanPhamService sanPhamService;
    private final SanPhamRepository sanPhamRepository;
    private final SanPhamChiTietService sanPhamChiTietService;

    /**
     * HOME: lấy list sản phẩm mới (hiển thị "HÀNG MỚI VỀ")
     * GET /api/client/home?size=10
     */
    @GetMapping("/home")
    public ResponseEntity<Page<SanPhamResponse>> home(
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "ngayTao"));
        return ResponseEntity.ok(sanPhamService.getAll(pageable));
    }

    /**
     * SHOP/SEARCH: tìm kiếm + filter cơ bản (PUBLIC)
     * GET /api/client/products?q=...&page=0&size=12&minPrice=&maxPrice=&loaiId=&thuongHieuId=&fitId=
     *
     * Bạn đang thiết kế thôi nên logic này đủ dùng, sau sửa tiếp.
     */
    @GetMapping("/products")
    public ResponseEntity<Page<SanPhamResponse>> searchProducts(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long loaiId,
            @RequestParam(required = false) Long thuongHieuId,
            @RequestParam(required = false) Long fitId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @PageableDefault(sort = "ngayTao", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<SanPham> page = sanPhamRepository.searchClient(
                q, loaiId, thuongHieuId, fitId, minPrice, maxPrice, pageable
        );

        // map tạm sang SanPhamResponse để FE dùng thống nhất
        Page<SanPhamResponse> mapped = page.map(this::mapToClientResponse);
        return ResponseEntity.ok(mapped);
    }

    /**
     * PRODUCT DETAIL:
     * GET /api/client/products/{id}
     * Trả về: product + variants
     */
    @GetMapping("/products/{id}")
    public ResponseEntity<Map<String, Object>> productDetail(@PathVariable Long id) {
        SanPhamResponse product = sanPhamService.getById(id);
        List<SanPhamChiTietResponse> variants = sanPhamChiTietService.getByProductId(id);

        Map<String, Object> res = new HashMap<>();
        res.put("product", product);
        res.put("variants", variants);
        return ResponseEntity.ok(res);
    }

    // ===================== mapper tạm cho client =====================
    private SanPhamResponse mapToClientResponse(SanPham sp) {
        // Ở service bạn có mapper chi tiết rồi, nhưng private -> mình map đơn giản đủ dùng cho FE
        return SanPhamResponse.builder()
                .id(sp.getId())
                .maSanPham(sp.getMaSanPham())
                .tenSanPham(sp.getTenSanPham())
                .ngayTao(sp.getNgayTao())
                .ngayCapNhat(sp.getNgayCapNhat())
                .trangThai(sp.getTrangThai())
                .moTa(sp.getMoTa())
                .chatLieuId(sp.getChatLieu() != null ? sp.getChatLieu().getId() : null)
                .loaiSanPhamId(sp.getLoaiSanPham() != null ? sp.getLoaiSanPham().getId() : null)
                .thuongHieuId(sp.getThuongHieu() != null ? sp.getThuongHieu().getId() : null)
                .soKhuyId(sp.getSoKhuy() != null ? sp.getSoKhuy().getId() : null)
                .kieuTuiId(sp.getKieuTui() != null ? sp.getKieuTui().getId() : null)
                .veAoId(sp.getVeAo() != null ? sp.getVeAo().getId() : null)
                .xeTaId(sp.getXeTa() != null ? sp.getXeTa().getId() : null)
                .xuatXuId(sp.getXuatXu() != null ? sp.getXuatXu().getId() : null)
                .fitId(sp.getFit() != null ? sp.getFit().getId() : null)
                .tenLoaiSanPham(sp.getLoaiSanPham() != null ? sp.getLoaiSanPham().getTen() : null)
                .tenThuongHieu(sp.getThuongHieu() != null ? sp.getThuongHieu().getTen() : null)
                // đơn giản: nếu muốn giá min/max/ton kho chuẩn thì sau bạn cập nhật mapper
                .soLuongTon(0)
                .giaMin(BigDecimal.ZERO)
                .giaMax(BigDecimal.ZERO)
                .build();
    }
}