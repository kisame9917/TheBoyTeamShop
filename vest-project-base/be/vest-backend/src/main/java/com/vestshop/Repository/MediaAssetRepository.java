package com.vestshop.Repository;

import com.vestshop.Entity.MediaAsset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MediaAssetRepository extends JpaRepository<MediaAsset, Long> {
    Optional<MediaAsset> findByPublicId(String publicId);
    Optional<MediaAsset> findByAssetId(String assetId);
    Optional<MediaAsset> findBySecureUrl(String secureUrl);
}
