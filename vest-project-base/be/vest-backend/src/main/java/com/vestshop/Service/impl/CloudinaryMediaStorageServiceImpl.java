package com.vestshop.Service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.vestshop.Config.CloudinaryProperties;
import com.vestshop.Entity.MediaAsset;
import com.vestshop.Exception.ApiException;
import com.vestshop.Repository.MediaAssetRepository;
import com.vestshop.Service.CloudinaryMediaStorageService;
import com.vestshop.dto.response.UploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CloudinaryMediaStorageServiceImpl implements CloudinaryMediaStorageService {

    private static final long MAX_SIZE_BYTES = 10L * 1024 * 1024;

    private final Cloudinary cloudinary;
    private final CloudinaryProperties cloudinaryProperties;
    private final MediaAssetRepository mediaAssetRepository;

    @Override
    @Transactional
    public UploadResponse uploadImage(MultipartFile file, String folder) {
        validateFile(file);
        String normalizedFolder = normalizeFolder(folder);

        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", normalizedFolder,
                            "resource_type", "image",
                            "overwrite", false,
                            "unique_filename", true
                    )
            );

            MediaAsset asset = mediaAssetRepository.save(MediaAsset.builder()
                    .provider("CLOUDINARY")
                    .resourceType(asString(result.get("resource_type"), "image"))
                    .assetId(asString(result.get("asset_id"), null))
                    .publicId(asString(result.get("public_id"), null))
                    .secureUrl(asString(result.get("secure_url"), null))
                    .version(asString(result.get("version"), null))
                    .format(asString(result.get("format"), null))
                    .width(asInteger(result.get("width")))
                    .height(asInteger(result.get("height")))
                    .bytes(asLong(result.get("bytes")))
                    .assetFolder(asString(result.get("asset_folder"), normalizedFolder))
                    .originalFilename(StringUtils.cleanPath(Objects.requireNonNullElse(file.getOriginalFilename(), "file")))
                    .displayName(StringUtils.cleanPath(Objects.requireNonNullElse(file.getOriginalFilename(), "file")))
                    .trangThai(Boolean.TRUE)
                    .build());

            return UploadResponse.builder()
                    .url(asset.getSecureUrl())
                    .mediaAssetId(asset.getId())
                    .publicId(asset.getPublicId())
                    .assetId(asset.getAssetId())
                    .format(asset.getFormat())
                    .width(asset.getWidth())
                    .height(asset.getHeight())
                    .bytes(asset.getBytes())
                    .assetFolder(asset.getAssetFolder())
                    .build();
        } catch (IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Upload Cloudinary thất bại: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public MediaAsset getRequired(Long id) {
        return mediaAssetRepository.findById(id)
                .filter(asset -> asset.getTrangThai() == null || asset.getTrangThai())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Không tìm thấy media asset ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public MediaAsset getOptional(Long id) {
        if (id == null) return null;
        return mediaAssetRepository.findById(id)
                .filter(asset -> asset.getTrangThai() == null || asset.getTrangThai())
                .orElse(null);
    }

    @Override
    public String resolveUrl(MediaAsset mediaAsset, String fallback) {
        if (mediaAsset != null && mediaAsset.getSecureUrl() != null && !mediaAsset.getSecureUrl().isBlank()) {
            return mediaAsset.getSecureUrl();
        }
        return fallback;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "File rỗng");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Dung lượng tối đa 10MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Chỉ chấp nhận file ảnh");
        }
    }

    private String normalizeFolder(String folder) {
        String base = cloudinaryProperties.getDefaultFolder();
        String target = (folder == null || folder.isBlank()) ? base : folder;
        return target.replace("\\", "/").replaceAll("/+", "/").replaceAll("/$", "");
    }

    private String asString(Object value, String defaultValue) {
        return value == null ? defaultValue : String.valueOf(value);
    }

    private Integer asInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.intValue();
        return Integer.parseInt(String.valueOf(value));
    }

    private Long asLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.longValue();
        return Long.parseLong(String.valueOf(value));
    }
}
