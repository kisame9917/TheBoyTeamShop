package com.vestshop.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "media_asset")
public class MediaAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "provider", length = 30, nullable = false)
    private String provider;

    @Column(name = "resource_type", length = 20, nullable = false)
    private String resourceType;

    @Column(name = "asset_id", length = 100, nullable = false, unique = true)
    private String assetId;

    @Column(name = "public_id", length = 255, nullable = false, unique = true)
    private String publicId;

    @Column(name = "secure_url", length = 1000, nullable = false)
    private String secureUrl;

    @Column(name = "version", length = 50)
    private String version;

    @Column(name = "format", length = 20)
    private String format;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @Column(name = "bytes")
    private Long bytes;

    @Column(name = "asset_folder", length = 255)
    private String assetFolder;

    @Column(name = "original_filename", length = 255)
    private String originalFilename;

    @Column(name = "display_name", length = 255)
    private String displayName;

    @Column(name = "ngay_tao", nullable = false)
    private LocalDateTime ngayTao;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;

    @Column(name = "trang_thai", nullable = false)
    private Boolean trangThai;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.ngayTao = now;
        this.ngayCapNhat = now;
        if (this.trangThai == null) this.trangThai = Boolean.TRUE;
        if (this.provider == null || this.provider.isBlank()) this.provider = "CLOUDINARY";
        if (this.resourceType == null || this.resourceType.isBlank()) this.resourceType = "image";
    }

    @PreUpdate
    public void preUpdate() {
        this.ngayCapNhat = LocalDateTime.now();
    }
}
