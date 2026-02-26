package com.vestshop.Repository;

import com.vestshop.Entity.SanPham;
import com.vestshop.dto.response.SanPhamThongKeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SanPhamRepository extends JpaRepository<SanPham, Long> {

    // ✅ Sửa: hd.trangThaiDon = :status
    @Query("SELECT new com.vestshop.dto.response.SanPhamThongKeResponse(" +
            "sp.id, sp.tenSanPham, 0L, SUM(spct.soLuongTon)) " +
            "FROM SanPham sp " +
            "JOIN sp.sanPhamChiTiets spct " +
            "WHERE sp.id NOT IN (" +
            "   SELECT DISTINCT sp2.id FROM HoaDonChiTiet hdct " +
            "   JOIN hdct.sanPhamChiTiet spct2 " +
            "   JOIN spct2.sanPham sp2 " +
            "   JOIN hdct.hoaDon hd " +
            "   WHERE hd.ngayTao BETWEEN :startDate AND :endDate " +
            "   AND hd.trangThaiDon = :status" +
            ") " +
            "GROUP BY sp.id, sp.tenSanPham")
    List<SanPhamThongKeResponse> findSlowMovingProducts(@Param("startDate") LocalDateTime startDate,
                                                        @Param("endDate") LocalDateTime endDate,
                                                        @Param("status") Integer status);

    /**
     * ✅ API client: search/filter sản phẩm (đơn giản, sau bạn nâng cấp thêm)
     */
    @Query(
            value = """
                SELECT sp
                FROM SanPham sp
                WHERE
                    (:q IS NULL OR :q = '' OR LOWER(sp.tenSanPham) LIKE LOWER(CONCAT('%', :q, '%')))
                    AND (:loaiId IS NULL OR sp.loaiSanPham.id = :loaiId)
                    AND (:thuongHieuId IS NULL OR sp.thuongHieu.id = :thuongHieuId)
                    AND (:fitId IS NULL OR sp.fit.id = :fitId)
                    AND (:minPrice IS NULL OR EXISTS (
                        SELECT 1 FROM SanPhamChiTiet spct
                        WHERE spct.sanPham.id = sp.id AND spct.donGia >= :minPrice
                    ))
                    AND (:maxPrice IS NULL OR EXISTS (
                        SELECT 1 FROM SanPhamChiTiet spct
                        WHERE spct.sanPham.id = sp.id AND spct.donGia <= :maxPrice
                    ))
            """,
            countQuery = """
                SELECT COUNT(sp)
                FROM SanPham sp
                WHERE
                    (:q IS NULL OR :q = '' OR LOWER(sp.tenSanPham) LIKE LOWER(CONCAT('%', :q, '%')))
                    AND (:loaiId IS NULL OR sp.loaiSanPham.id = :loaiId)
                    AND (:thuongHieuId IS NULL OR sp.thuongHieu.id = :thuongHieuId)
                    AND (:fitId IS NULL OR sp.fit.id = :fitId)
                    AND (:minPrice IS NULL OR EXISTS (
                        SELECT 1 FROM SanPhamChiTiet spct
                        WHERE spct.sanPham.id = sp.id AND spct.donGia >= :minPrice
                    ))
                    AND (:maxPrice IS NULL OR EXISTS (
                        SELECT 1 FROM SanPhamChiTiet spct
                        WHERE spct.sanPham.id = sp.id AND spct.donGia <= :maxPrice
                    ))
            """
    )
    Page<SanPham> searchClient(
            @Param("q") String q,
            @Param("loaiId") Long loaiId,
            @Param("thuongHieuId") Long thuongHieuId,
            @Param("fitId") Long fitId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );
}