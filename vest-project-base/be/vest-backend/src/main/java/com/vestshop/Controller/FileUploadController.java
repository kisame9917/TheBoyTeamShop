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
public class FileUploadController {

    private final CloudinaryMediaStorageService mediaStorageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> uploadFile(@RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(mediaStorageService.uploadImage(file, "vestshop/products/variants"));
    }
}