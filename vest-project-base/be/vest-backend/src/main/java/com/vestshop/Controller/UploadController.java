package com.vestshop.Controller;

import com.vestshop.Service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UploadController {

    private final FileStorageService fileStorageService;

    @PostMapping(value = "/nhan-vien-avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadNhanVienAvatar(@RequestPart("file") MultipartFile file) {
        String url = fileStorageService.storeNhanVienAvatar(file);
        return ResponseEntity.ok(Map.of("url", url));
    }

    // ----------- ADD: upload khách hàng
    @PostMapping(value = "/khach-hang-avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadKhachHangAvatar(@RequestPart("file") MultipartFile file) {
        String url = fileStorageService.storeKhachHangAvatar(file);
        return ResponseEntity.ok(Map.of("url", url));
    }
}
