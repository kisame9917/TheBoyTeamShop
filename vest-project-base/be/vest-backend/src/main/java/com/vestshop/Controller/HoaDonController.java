package com.vestshop.Controller;

import com.vestshop.Service.HoaDonService;
import com.vestshop.dto.request.*;
import com.vestshop.dto.response.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/hoa-don")
@RequiredArgsConstructor
public class HoaDonController {

    private final HoaDonService hoaDonService;

    @GetMapping
    public ResponseEntity<Page<HoaDonListResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer trangThaiDon,
            @RequestParam(required = false) String phanLoai,
            @RequestParam(required = false) Boolean loaiDon,

            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,

            @RequestParam(required = false) BigDecimal minTotal,
            @RequestParam(required = false) BigDecimal maxTotal,
            @RequestParam(required = false) Boolean hasVoucher,
            @RequestParam(required = false) Long idNhanVien,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ngayTao") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Sort sort = "asc".equalsIgnoreCase(sortDir)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        LocalDateTime fromDT = (from == null) ? null : from.atStartOfDay();
        LocalDateTime toDT = (to == null) ? null : to.plusDays(1).atStartOfDay();

        return ResponseEntity.ok(
                hoaDonService.search(
                        keyword, trangThaiDon, phanLoai, loaiDon,
                        fromDT, toDT, minTotal, maxTotal, hasVoucher, idNhanVien, active, pageable
                )
        );
    }
    @PostMapping("/draft/{id}/cancel")
    public ResponseEntity<Void> cancelDraft(@PathVariable Long id,
                                            @RequestBody(required = false) CancelDraftRequest req) {
        hoaDonService.cancelDraft(id, req);
        return ResponseEntity.noContent().build();
    }

//    @PostMapping("/pos")
//    public ResponseEntity<HoaDonDetailResponse> createPos(@Valid @RequestBody BanHangRequest req) {
//        return ResponseEntity.ok(hoaDonService.createPos(req));
//    }

    @GetMapping("/{id}")
    public ResponseEntity<HoaDonDetailResponse> detail(@PathVariable Long id) {
        return ResponseEntity.ok(hoaDonService.getDetailById(id));
    }

    @GetMapping("/by-ma/{maHoaDon}")
    public ResponseEntity<HoaDonDetailResponse> detailByMa(@PathVariable String maHoaDon) {
        return ResponseEntity.ok(hoaDonService.getDetailByMaHoaDon(maHoaDon));
    }

    @GetMapping("/{id}/lich-su")
    public ResponseEntity<List<LichSuHoaDonResponse>> lichSu(@PathVariable Long id) {
        return ResponseEntity.ok(hoaDonService.getLichSuHoaDon(id));
    }

    @GetMapping("/{id}/thanh-toan")
    public ResponseEntity<List<LichSuThanhToanResponse>> lichSuThanhToan(@PathVariable Long id) {
        return ResponseEntity.ok(hoaDonService.getLichSuThanhToan(id));
    }

    @GetMapping("/{id}/giao-dich")
    public ResponseEntity<List<GiaoDichThanhToanResponse>> giaoDich(@PathVariable Long id) {
        return ResponseEntity.ok(hoaDonService.getGiaoDichThanhToan(id));
    }

    @PatchMapping("/{id}/trang-thai")
    public ResponseEntity<HoaDonDetailResponse> changeStatus(@PathVariable Long id, @RequestBody HoaDonChangeStatusRequest req) {
        return ResponseEntity.ok(hoaDonService.changeStatus(id, req));
    }

    @PatchMapping("/{id}/hoan-hang")
    public ResponseEntity<HoaDonDetailResponse> hoanHang(@PathVariable Long id, @RequestBody HoaDonReturnRequest req) {
        return ResponseEntity.ok(hoaDonService.hoanHang(id, req));
    }
    @PostMapping("/taohoadon")
    public ResponseEntity<TaohoadonResponse> createDraft(@RequestBody(required = false) TaoHoaDonChoXacNhanRequest req) {
        return ResponseEntity.ok(hoaDonService.createDraft(req));
    }
    @PostMapping("/draft/{id}/checkout")
    public ResponseEntity<?> checkoutDraft(@PathVariable("id") Long id,
                                           @RequestBody BanHangRequest req) {
        return ResponseEntity.ok(hoaDonService.checkoutDraft(id, req));
    }
    @GetMapping("/drafts/pos-active")
    public ResponseEntity<List<HoaDonDetailResponse>> getPosDrafts() {
        return ResponseEntity.ok(hoaDonService.getPosDrafts());
    }

    @PostMapping("/draft/{id}/sync-pos")
    public ResponseEntity<HoaDonDetailResponse> syncPosDraft(
            @PathVariable Long id,
            @RequestBody PosDraftSyncRequest req
    ) {
        return ResponseEntity.ok(hoaDonService.syncPosDraft(id, req));
    }

    @PostMapping("/{id}/xac-nhan-hoan-tien")
    public ResponseEntity<HoaDonDetailResponse> confirmRefund(@PathVariable Long id,
                                                              @RequestBody RefundConfirmRequest request) {
        return ResponseEntity.ok(hoaDonService.confirmRefund(id, request));
    }
    @PostMapping("/draft/{id}/generate-qr")
    public ResponseEntity<HoaDonDetailResponse> generateQr(@PathVariable Long id) {
        return ResponseEntity.ok(hoaDonService.generatePosQr(id));
    }
}
