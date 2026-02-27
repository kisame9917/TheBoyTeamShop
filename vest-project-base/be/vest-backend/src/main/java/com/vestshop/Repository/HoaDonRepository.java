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

    // ✅ Doanh thu trong range theo status int (trangThaiDon)
    @Query("SELECT COALESCE(SUM(h.tongTien), 0) " +
            "FROM HoaDon h " +
            "WHERE h.ngayTao BETWEEN :startDate AND :endDate " +
            "AND h.trangThaiDon = :status")
    BigDecimal sumDoanhThuInRange(@Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate,
                                  @Param("status") Integer status);

    // ✅ Top khách hàng theo chi tiêu trong range
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

    // ==============================
    // DOANH THU (JPQL + FUNCTION)
    // ==============================

    // Theo ngày trong tháng
    @Query("SELECT FUNCTION('day', h.ngayTao), COALESCE(SUM(h.tongTien), 0) " +
            "FROM HoaDon h " +
            "WHERE FUNCTION('month', h.ngayTao) = :month " +
            "AND FUNCTION('year', h.ngayTao) = :year " +
            "AND h.trangThaiDon = :status " +
            "GROUP BY FUNCTION('day', h.ngayTao) " +
            "ORDER BY FUNCTION('day', h.ngayTao)")
    List<Object[]> getDoanhThuNgayInMonth(@Param("month") int month,
                                          @Param("year") int year,
                                          @Param("status") Integer status);

    // Theo tháng trong năm
    @Query("SELECT FUNCTION('month', h.ngayTao), COALESCE(SUM(h.tongTien), 0) " +
            "FROM HoaDon h " +
            "WHERE FUNCTION('year', h.ngayTao) = :year " +
            "AND h.trangThaiDon = :status " +
            "GROUP BY FUNCTION('month', h.ngayTao) " +
            "ORDER BY FUNCTION('month', h.ngayTao)")
    List<Object[]> getDoanhThuThangInYear(@Param("year") int year,
                                          @Param("status") Integer status);

    // Theo tháng trong quý (startMonth..endMonth)
    @Query("SELECT FUNCTION('month', h.ngayTao), COALESCE(SUM(h.tongTien), 0) " +
            "FROM HoaDon h " +
            "WHERE FUNCTION('year', h.ngayTao) = :year " +
            "AND FUNCTION('month', h.ngayTao) BETWEEN :startMonth AND :endMonth " +
            "AND h.trangThaiDon = :status " +
            "GROUP BY FUNCTION('month', h.ngayTao) " +
            "ORDER BY FUNCTION('month', h.ngayTao)")
    List<Object[]> getDoanhThuThangInQuarter(@Param("year") int year,
                                             @Param("startMonth") int startMonth,
                                             @Param("endMonth") int endMonth,
                                             @Param("status") Integer status);

    // ==============================
    // ĐƠN HÀNG THEO TRẠNG THÁI
    // ==============================

    @Query("SELECT h.trangThaiDon, COUNT(h) " +
            "FROM HoaDon h " +
            "WHERE (:month IS NULL OR FUNCTION('month', h.ngayTao) = :month) " +
            "AND FUNCTION('year', h.ngayTao) = :year " +
            "GROUP BY h.trangThaiDon")
    List<Object[]> getThongKeDonHang(@Param("month") Integer month, @Param("year") int year);

    @Query("SELECT h.trangThaiDon, COUNT(h) " +
            "FROM HoaDon h " +
            "WHERE h.ngayTao BETWEEN :from AND :to " +
            "GROUP BY h.trangThaiDon")
    List<Object[]> countDonHangByTrangThaiInRange(@Param("from") LocalDateTime from,
                                                  @Param("to") LocalDateTime to);

    @Query("""
      select sum(
        case when :includeShip = true
             then (h.tongTienSauGiam + h.phiVanChuyen)
             else h.tongTienSauGiam
        end
      )
      from HoaDon h
      where h.khachHang.id = :khId
        and h.trangThai = true
        and h.trangThaiDon = 4
    """)
    BigDecimal totalSpent(@Param("khId") Long khId,
                          @Param("includeShip") boolean includeShip);

    @Query("""
      select count(h)
      from HoaDon h
      where h.khachHang.id = :khId
        and h.trangThai = true
        and h.trangThaiDon = 4
        and h.ngayTao >= :from and h.ngayTao < :to
    """)
    long monthlyOrderCount(@Param("khId") Long khId,
                           @Param("from") LocalDateTime from,
                           @Param("to") LocalDateTime to);

    @Query("""
      select sum(
        case when :includeShip = true
             then (h.tongTienSauGiam + h.phiVanChuyen)
             else h.tongTienSauGiam
        end
      )
      from HoaDon h
      where h.khachHang.id = :khId
        and h.trangThai = true
        and h.trangThaiDon = 4
        and h.ngayTao >= :from and h.ngayTao < :to
    """)
    BigDecimal monthlySpent(@Param("khId") Long khId,
                            @Param("from") LocalDateTime from,
                            @Param("to") LocalDateTime to,
                            @Param("includeShip") boolean includeShip);


    @Modifying
    @Query("""
        delete from HoaDon hd
        where hd.trangThaiDon = :status
          and hd.ngayTao < :cutoff
    """)
    int hardDeletePendingOlderThan(@Param("status") Integer status,
                                   @Param("cutoff") LocalDateTime cutoff);



}


