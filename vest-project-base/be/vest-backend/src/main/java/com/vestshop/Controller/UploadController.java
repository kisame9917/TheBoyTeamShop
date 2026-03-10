package com.vestshop.Controller;

import com.vestshop.Service.CloudinaryMediaStorageService;
import com.vestshop.dto.response.UploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UploadController {

    private final CloudinaryMediaStorageService mediaStorageService;

    @PostMapping(value = "/nhan-vien-avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> uploadNhanVienAvatar(@RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(mediaStorageService.uploadImage(file, "vestshop/staff/avatar"));
    }

    @PostMapping(value = "/khach-hang-avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> uploadKhachHangAvatar(@RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(mediaStorageService.uploadImage(file, "vestshop/customers/avatar"));
    }

    @PostMapping(value = "/san-pham-cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> uploadSanPhamCover(@RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(mediaStorageService.uploadImage(file, "vestshop/products/cover"));
    }

    @PostMapping(value = "/san-pham-chi-tiet-primary", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> uploadSanPhamChiTietPrimary(@RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(mediaStorageService.uploadImage(file, "vestshop/products/variants/primary"));
    }

    @PostMapping(value = "/san-pham-chi-tiet-gallery", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> uploadSanPhamChiTietGallery(@RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(mediaStorageService.uploadImage(file, "vestshop/products/variants/gallery"));
    }
}
