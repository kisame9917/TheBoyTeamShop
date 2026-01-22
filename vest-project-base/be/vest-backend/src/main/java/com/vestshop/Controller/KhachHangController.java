package com.vestshop.Controller;

import com.vestshop.Exception.ApiException;
import com.vestshop.Service.KhachHangService;
import com.vestshop.dto.request.KhachHangRequest;
import com.vestshop.dto.request.KhachHangTrangThaiRequest;
import com.vestshop.dto.request.KhachHangTrangThaiRequest;
import com.vestshop.dto.response.KhachHangResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/khach-hang")
@RequiredArgsConstructor
@CrossOrigin // nếu bạn đã có CORS global thì có thể bỏ
public class KhachHangController {

    private final KhachHangService khachHangService;

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

    private static final String UPLOAD_DIR = "D:/2_DATN/vest-project-base/uploads/khachhang";

    @PostMapping("/upload-avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "File rỗng");
            }

            Files.createDirectories(Paths.get(UPLOAD_DIR));

            String original = file.getOriginalFilename();
            String ext = "";
            if (StringUtils.hasText(original) && original.contains(".")) {
                ext = original.substring(original.lastIndexOf('.'));
            }

            String name = System.currentTimeMillis() + "-" + UUID.randomUUID() + ext;
            Path target = Paths.get(UPLOAD_DIR).resolve(name).normalize();

            file.transferTo(target.toFile());

            String url = "/uploads/khachhang/" + name;
            return ResponseEntity.ok(Map.of("url", url));
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Upload ảnh thất bại: " + e.getMessage());
        }
    }
}
