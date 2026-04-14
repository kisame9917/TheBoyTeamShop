package com.vestshop.Repository;

import com.vestshop.Entity.SanPhamChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface SanPhamChiTietRepository extends JpaRepository<SanPhamChiTiet, Long> {
    @org.springframework.data.jpa.repository.Query(
            "SELECT s FROM SanPhamChiTiet s " +
                    "JOIN FETCH s.sanPham " +
                    "JOIN FETCH s.kichCo " +
                    "JOIN FETCH s.mauSac " +
                    "WHERE s.sanPham.id = :sanPhamId"
    )
    java.util.List<SanPhamChiTiet> findBySanPhamId(
            @org.springframework.data.repository.query.Param("sanPhamId") Long sanPhamId
    );

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(MAX(s.donGia), 0) FROM SanPhamChiTiet s")
    BigDecimal findMaxDonGia();

    @Modifying
    @Query("UPDATE SanPhamChiTiet s SET s.soLuongTon = s.soLuongTon - :qty WHERE s.id = :id AND s.soLuongTon >= :qty")
    int decreaseStock(@Param("id") Long id, @Param("qty") Integer qty);

    @Modifying
    @Query("UPDATE SanPhamChiTiet s SET s.soLuongTon = s.soLuongTon + :qty WHERE s.id = :id")
    int increaseStock(@Param("id") Long id, @Param("qty") Integer qty);

    @Query("""
    SELECT s FROM SanPhamChiTiet s
    JOIN FETCH s.sanPham sp
    JOIN FETCH s.kichCo kc
    JOIN FETCH s.mauSac ms
    JOIN FETCH sp.loaiSanPham lsp
    JOIN FETCH sp.thuongHieu th
    JOIN FETCH sp.fit f
    WHERE s.trangThai = true
      AND s.soLuongTon > 0
      AND sp.trangThai = true
""")
    java.util.List<SanPhamChiTiet> findAllAvailableForAI();
    @Query("""
    SELECT s FROM SanPhamChiTiet s
    JOIN FETCH s.sanPham sp
    JOIN FETCH sp.loaiSanPham
    JOIN FETCH s.kichCo
    JOIN FETCH s.mauSac
    WHERE s.id IN :ids
""")
    List<SanPhamChiTiet> findByIdIn(@Param("ids") List<Long> ids);

    Optional<SanPhamChiTiet> findByMaSanPhamChiTietIgnoreCase(String maSanPhamChiTiet);
}
