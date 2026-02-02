package com.vestshop.Repository;

import com.vestshop.Entity.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import com.vestshop.dto.response.SanPhamThongKeResponse;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
            "SELECT DISTINCT sp2.id FROM HoaDonChiTiet hdct " +
            "JOIN hdct.sanPhamChiTiet spct2 " +
            "JOIN spct2.sanPham sp2 " +
            "JOIN hdct.hoaDon hd " +
            "WHERE hd.ngayTao BETWEEN :startDate AND :endDate " +
            "AND hd.trangThaiDon = :status" +
            ") " +
            "GROUP BY sp.id, sp.tenSanPham")
    List<SanPhamThongKeResponse> findSlowMovingProducts(@Param("startDate") LocalDateTime startDate,
                                                        @Param("endDate") LocalDateTime endDate,
                                                        @Param("status") Integer status);
}
