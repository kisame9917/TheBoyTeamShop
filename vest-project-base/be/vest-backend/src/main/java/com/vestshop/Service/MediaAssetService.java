package com.vestshop.Service;

import com.vestshop.Entity.MediaAsset;
import org.springframework.web.multipart.MultipartFile;

public interface MediaAssetService {
    MediaAsset uploadImage(MultipartFile file, String assetFolder, String displayName);
    MediaAsset getRequired(Long id);
    MediaAsset findBySecureUrl(String secureUrl);
    String resolveUrl(Long mediaAssetId, String fallbackUrl);
}
