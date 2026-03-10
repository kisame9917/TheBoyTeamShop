package com.vestshop.Controller;

import com.vestshop.Exception.ApiException;
import com.vestshop.Service.CloudinaryMediaStorageService;
import com.vestshop.Service.KhachHangChiTietService;
import com.vestshop.Service.KhachHangService;
import com.vestshop.dto.request.KhachHangRequest;
import com.vestshop.dto.request.KhachHangTrangThaiRequest;
import com.vestshop.dto.response.KhachHangChiTietResponse;
import com.vestshop.dto.response.KhachHangResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/api/khach-hang")
@CrossOrigin(
        origins = "*",
        allowedHeaders = "*",
        methods = {
                RequestMethod.GET,
                RequestMethod.POST,
                RequestMethod.PUT,
                RequestMethod.PATCH,
                RequestMethod.DELETE,
                RequestMethod.OPTIONS
        }
)
@RequiredArgsConstructor
public class KhachHangController {
    private final KhachHangChiTietService khachHangChiTietService;
    private final KhachHangService khachHangService;
    private final CloudinaryMediaStorageService mediaStorageService;

    // ========= LIST / DETAIL =========
    @GetMapping
    public List<KhachHangResponse> getAll() {
        return khachHangService.getAll();
    }

    @GetMapping("/{id}")
    public KhachHangResponse getById(@PathVariable Long id) {
        return khachHangService.getById(id);
    }

    // ========= NEXT CODE =========
    @GetMapping("/next-code")
    public Map<String, String> nextCode(@RequestParam(defaultValue = "KH") String prefix) {
        return Map.of("maKhachHang", khachHangService.getNextMaKhachHang(prefix));
    }

    // ========= CRUD =========
    @PostMapping
    public KhachHangResponse create(@RequestBody KhachHangRequest request) {
        return khachHangService.create(request);
    }

    @PutMapping("/{id}")
    public KhachHangResponse update(@PathVariable Long id, @RequestBody KhachHangRequest request) {
        return khachHangService.update(id, request);
    }

    // ========= SWITCH TRẠNG THÁI =========
    @PatchMapping("/{id}/trang-thai")
    public KhachHangResponse updateTrangThai(@PathVariable Long id, @RequestBody KhachHangTrangThaiRequest body) {
        if (body == null || body.getTrangThai() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Thiếu trangThai");
        }
        return khachHangService.updateTrangThai(id, body.getTrangThai());
    }

    // (Tuỳ chọn) Nếu FE lỡ gọi PUT
    @PutMapping("/{id}/trang-thai")
    public KhachHangResponse updateTrangThaiPut(@PathVariable Long id, @RequestBody KhachHangTrangThaiRequest body) {
        if (body == null || body.getTrangThai() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Thiếu trangThai");
        }
        return khachHangService.updateTrangThai(id, body.getTrangThai());
    }

    // ========= UPLOAD AVATAR =========
    @PostMapping("/upload-avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(mediaStorageService.uploadImage(file, "vestshop/customers/avatar"));
    }
    @GetMapping("/{id}/stats")
    public KhachHangChiTietResponse stats(@PathVariable Long id,
                                          @RequestParam(required = false) String month,
                                          @RequestParam(required = false) Boolean includeShip) {
        return khachHangChiTietService.getStats(id, month, includeShip);
    }
}
