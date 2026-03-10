package com.vestshop.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadResponse {
    private String url;
    private Long mediaAssetId;
    private String publicId;
    private String assetId;
    private String format;
    private Integer width;
    private Integer height;
    private Long bytes;
    private String assetFolder;
}
