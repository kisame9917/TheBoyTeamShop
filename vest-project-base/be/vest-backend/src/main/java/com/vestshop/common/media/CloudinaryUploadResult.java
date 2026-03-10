package com.vestshop.common.media;

public record CloudinaryUploadResult(
        String assetId,
        String publicId,
        String secureUrl,
        String resourceType,
        String format,
        Integer width,
        Integer height,
        Long bytes,
        String assetFolder,
        String originalFilename,
        String displayName,
        String version
) {
}
