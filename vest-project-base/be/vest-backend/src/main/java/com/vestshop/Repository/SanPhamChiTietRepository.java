package com.vestshop.Repository;

import com.vestshop.Entity.SanPhamChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;

public interface SanPhamChiTietRepository extends JpaRepository<SanPhamChiTiet, Long> {
    @org.springframework.data.jpa.repository.Query("SELECT s FROM SanPhamChiTiet s JOIN FETCH s.kichCo JOIN FETCH s.mauSac WHERE s.sanPham.id = :sanPhamId")
    java.util.List<SanPhamChiTiet> findBySanPhamId(@org.springframework.data.repository.query.Param("sanPhamId") Long sanPhamId);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(MAX(s.donGia), 0) FROM SanPhamChiTiet s")
    BigDecimal findMaxDonGia();
}
