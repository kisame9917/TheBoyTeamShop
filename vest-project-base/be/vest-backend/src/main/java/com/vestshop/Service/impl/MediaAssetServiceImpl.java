package com.vestshop.Service.impl;

import com.vestshop.Entity.MediaAsset;
import com.vestshop.Repository.MediaAssetRepository;
import com.vestshop.Service.CloudinaryMediaStorageService;
import com.vestshop.Service.MediaAssetService;
import com.vestshop.dto.response.UploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MediaAssetServiceImpl implements MediaAssetService {

    private final CloudinaryMediaStorageService cloudinaryMediaStorageService;
    private final MediaAssetRepository mediaAssetRepository;

    @Override
    @Transactional
    public MediaAsset uploadImage(MultipartFile file, String assetFolder, String displayName) {
        UploadResponse uploadResponse = cloudinaryMediaStorageService.uploadImage(file, assetFolder);
        MediaAsset mediaAsset = cloudinaryMediaStorageService.getRequired(uploadResponse.getMediaAssetId());

        if (StringUtils.hasText(displayName)) {
            mediaAsset.setDisplayName(displayName.trim());
            mediaAsset = mediaAssetRepository.save(mediaAsset);
        }

        return mediaAsset;
    }

    @Override
    @Transactional(readOnly = true)
    public MediaAsset getRequired(Long id) {
        return cloudinaryMediaStorageService.getRequired(id);
    }

    @Override
    @Transactional(readOnly = true)
    public MediaAsset findBySecureUrl(String secureUrl) {
        if (!StringUtils.hasText(secureUrl)) {
            return null;
        }
        return mediaAssetRepository.findBySecureUrl(secureUrl.trim()).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public String resolveUrl(Long mediaAssetId, String fallbackUrl) {
        MediaAsset mediaAsset = mediaAssetId == null ? null : getRequired(mediaAssetId);
        if (mediaAsset != null && StringUtils.hasText(mediaAsset.getSecureUrl())) {
            return mediaAsset.getSecureUrl();
        }
        return fallbackUrl;
    }
}
