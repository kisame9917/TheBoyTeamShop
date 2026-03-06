package com.vestshop.Repository;

import com.vestshop.Entity.GiaoDichThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface GiaoDichThanhToanRepository extends JpaRepository<GiaoDichThanhToan, Long> {

    List<GiaoDichThanhToan> findAllByHoaDon_IdOrderByThoiGianTaoDesc(Long hoaDonId);

    Optional<GiaoDichThanhToan> findFirstByHoaDon_IdOrderByIdDesc(Long hoaDonId);

    /**
     * Tính doanh thu TIỀN MẶT theo thời gian tạo hóa đơn (hoa_don.ngay_tao)
     * - Chỉ tính các hóa đơn trạng thái hoàn thành (trangThaiDon = status)
     * - Chỉ tính giao dịch còn hiệu lực (g.trangThai = true)
     */
    @Query("""
        select COALESCE(sum(g.soTien), 0)
        from GiaoDichThanhToan g
        join g.hoaDon h
        join g.phuongThucThanhToan p
        where h.ngayTao between :from and :to
          and h.trangThaiDon = :status
          and g.trangThai = true
          and p.hinhThuc = 1
    """)
    BigDecimal sumTienMatByHoaDonCreatedRange(@Param("from") LocalDateTime from,
                                              @Param("to") LocalDateTime to,
                                              @Param("status") Integer status);

    /**
     * Tính doanh thu CK/THẺ (hoặc các hình thức không phải tiền mặt) theo thời gian tạo hóa đơn
     */
    @Query("""
        select COALESCE(sum(g.soTien), 0)
        from GiaoDichThanhToan g
        join g.hoaDon h
        join g.phuongThucThanhToan p
        where h.ngayTao between :from and :to
          and h.trangThaiDon = :status
          and g.trangThai = true
          and (p.hinhThuc is null or p.hinhThuc <> 1)
    """)
    BigDecimal sumKhacTienMatByHoaDonCreatedRange(@Param("from") LocalDateTime from,
                                                  @Param("to") LocalDateTime to,
                                                  @Param("status") Integer status);
}