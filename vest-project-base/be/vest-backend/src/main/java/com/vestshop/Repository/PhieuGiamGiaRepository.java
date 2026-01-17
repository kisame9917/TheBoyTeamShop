package com.vestshop.Repository;

import com.vestshop.Entity.PhieuGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhieuGiamGiaRepository extends JpaRepository<PhieuGiamGia, Long> {
    boolean existsByMaGiamGia(String maGiamGia);

}
