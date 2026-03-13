package com.vestshop.Repository;

import com.vestshop.Entity.HoaDonChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.vestshop.dto.response.SanPhamThongKeResponse;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;

@Repository
public interface HoaDonChiTietRepository extends JpaRepository<HoaDonChiTiet, Long> {
    List<HoaDonChiTiet> findAllByHoaDon_Id(Long hoaDonId);

    // ✅ Sửa: hd.trangThaiDon = :status
    // Vẫn giữ nguyên spct.soLuongTon (nếu đúng tên cột tồn kho của bạn)
    @Query("SELECT new com.vestshop.dto.response.SanPhamThongKeResponse(" +
            "sp.id, sp.tenSanPham, SUM(hdct.soLuong), SUM(spct.soLuongTon)) " +
            "FROM HoaDonChiTiet hdct " +
            "JOIN hdct.sanPhamChiTiet spct " +
            "JOIN spct.sanPham sp " +
            "JOIN hdct.hoaDon hd " +
            "WHERE hd.ngayTao BETWEEN :startDate AND :endDate " +
            "AND hd.trangThaiDon = :status " +
            "GROUP BY sp.id, sp.tenSanPham " +
            "ORDER BY SUM(hdct.soLuong) DESC")
    List<SanPhamThongKeResponse> findTopSellingProducts(@Param("startDate") LocalDateTime startDate,
                                                        @Param("endDate") LocalDateTime endDate,
                                                        @Param("status") Integer status);

    // ==============================
    // TỔNG QUAN ĐẦU TRANG
    // ==============================
    @Query("SELECT COALESCE(SUM(hdct.soLuong), 0) " +
            "FROM HoaDonChiTiet hdct " +
            "JOIN hdct.hoaDon hd " +
            "WHERE hd.ngayTao BETWEEN :startDate AND :endDate " +
            "AND hd.trangThaiDon = :status")
    Long sumSoldQuantityInRange(@Param("startDate") LocalDateTime startDate,
                                @Param("endDate") LocalDateTime endDate,
                                @Param("status") Integer status);

    @Modifying
    @Query("delete from HoaDonChiTiet c where c.hoaDon.id = :hoaDonId")
    void deleteByHoaDonId(@Param("hoaDonId") Long hoaDonId);

    @Query("select c from HoaDonChiTiet c where c.hoaDon.id in :hoaDonIds")
    List<HoaDonChiTiet> findAllByHoaDonIds(@Param("hoaDonIds") List<Long> hoaDonIds);

    @Modifying
    @Query("delete from HoaDonChiTiet c where c.hoaDon.id in :hoaDonIds")
    void deleteAllByHoaDonIds(@Param("hoaDonIds") List<Long> hoaDonIds);
}