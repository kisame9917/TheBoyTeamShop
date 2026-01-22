package com.vestshop.Repository;

import com.vestshop.Entity.SanPhamChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SanPhamChiTietRepository extends JpaRepository<SanPhamChiTiet, Long> {
    @org.springframework.data.jpa.repository.Query("SELECT s FROM SanPhamChiTiet s JOIN FETCH s.kichCo JOIN FETCH s.mauSac WHERE s.sanPham.id = :sanPhamId")
    java.util.List<SanPhamChiTiet> findBySanPhamId(@org.springframework.data.repository.query.Param("sanPhamId") Long sanPhamId);
}
