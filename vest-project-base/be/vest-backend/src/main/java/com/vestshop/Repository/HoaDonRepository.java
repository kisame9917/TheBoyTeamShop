package com.vestshop.Repository;

import com.vestshop.Entity.HoaDon;
import org.springframework.data.jpa.repository.*;
import java.util.Optional;

import com.vestshop.dto.response.TopKhachHangResponse; // Import DTO vừa tạo
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Long>, JpaSpecificationExecutor<HoaDon> {
    Optional<HoaDon> findByMaHoaDon(String maHoaDon);

    // ✅ Sửa: h.trangThaiDon = :status
    @Query("SELECT COALESCE(SUM(h.tongTien), 0) FROM HoaDon h " +
            "WHERE h.ngayTao BETWEEN :startDate AND :endDate " +
            "AND h.trangThaiDon = :status")
    BigDecimal sumDoanhThuInRange(@Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate,
                                  @Param("status") Integer status);

    // ✅ Sửa: h.trangThaiDon = :status
    @Query("SELECT new com.vestshop.dto.response.TopKhachHangResponse(" +
            "kh.id, kh.tenKhachHang, kh.soDienThoai, COUNT(h), SUM(h.tongTien)) " +
            "FROM HoaDon h JOIN h.khachHang kh " +
            "WHERE h.ngayTao BETWEEN :startDate AND :endDate " +
            "AND h.trangThaiDon = :status " +
            "GROUP BY kh.id, kh.tenKhachHang, kh.soDienThoai " +
            "ORDER BY SUM(h.tongTien) DESC")
    List<TopKhachHangResponse> findTopKhachHang(@Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate,
                                                @Param("status") Integer status);
}
