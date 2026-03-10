package com.vestshop.Service;

import com.vestshop.Entity.MediaAsset;
import com.vestshop.dto.response.UploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryMediaStorageService {
    UploadResponse uploadImage(MultipartFile file, String folder);
    MediaAsset getRequired(Long id);
    MediaAsset getOptional(Long id);
    String resolveUrl(MediaAsset mediaAsset, String fallback);
}
