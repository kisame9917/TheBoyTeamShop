package com.vestshop.Controller;

import com.vestshop.Service.CaLamViecService;
import com.vestshop.common.ApiResponse;
import com.vestshop.dto.request.CaLamViecRequest;
import com.vestshop.dto.request.LichLamViecRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/ca-lam-viec") // Đổi path từ /api/shifts -> /api/ca-lam-viec
@CrossOrigin("*")
public class CaLamViecController {

    @Autowired
    private CaLamViecService caLamViecService;

    // ============= API CA MẪU (TEMPLATES) =============

    @GetMapping("/mau") // GET /api/ca-lam-viec/mau
    public ApiResponse<?> getAllCaLamViec() {
        return ApiResponse.ok(caLamViecService.getAllCaLamViec());
    }

    @PostMapping("/mau")
    public ApiResponse<?> createCaLamViec(@RequestBody CaLamViecRequest req) {
        try {
            return ApiResponse.ok(caLamViecService.createCaLamViec(req));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/mau/{id}")
    public ApiResponse<?> updateCaLamViec(@PathVariable Long id, @RequestBody CaLamViecRequest req) {
        return ApiResponse.ok(caLamViecService.updateCaLamViec(id, req));
    }

    // ============= API LỊCH LÀM VIỆC (SCHEDULE) =============

    // GET /api/ca-lam-viec/lich?from=2026-02-01&to=2026-02-28
    @GetMapping("/lich")
    public ApiResponse<?> getLichLamViec(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        if (from == null) from = LocalDate.now().withDayOfMonth(1);
        if (to == null) to = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        return ApiResponse.ok(caLamViecService.getLichLamViec(from, to));
    }

    @PostMapping("/lich")
    public ApiResponse<?> taoLichLamViec(@RequestBody LichLamViecRequest req) {
        try {
            return ApiResponse.ok(caLamViecService.taoLichLamViec(req));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage()); // Trả lỗi nếu trùng lịch
        }
    }

    @DeleteMapping("/lich/{id}")
    public ApiResponse<?> xoaLichLamViec(@PathVariable Long id) {
        caLamViecService.xoaLichLamViec(id);
        return ApiResponse.ok("Đã xóa lịch làm việc");
    }

    @GetMapping("/lich-ca-nhan")
    public ApiResponse<?> getLichCaNhan(
            @RequestParam Long idNhanVien,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        if (from == null) from = LocalDate.now().withDayOfMonth(1);
        if (to == null) to = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        return ApiResponse.ok(caLamViecService.getLichCaNhan(idNhanVien, from, to));
    }

    @PutMapping("/lich/{id}")
    public ApiResponse<?> updateLichLamViec(@PathVariable Long id, @RequestBody LichLamViecRequest req) {
        try {
            return ApiResponse.ok(caLamViecService.updateLichLamViec(id, req));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}