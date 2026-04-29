package com.vestshop.Repository;

import com.vestshop.Entity.LichSuThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface LichSuThanhToanRepository extends JpaRepository<LichSuThanhToan, Long> {

    // (optional) giữ lại cũng được
    List<LichSuThanhToan> findAllByHoaDon_IdOrderByNgayThanhToanDesc(Long hoaDonId);
    boolean existsByHoaDon_Id(Long hoaDonId);
    @Query("""
    select lstt
    from LichSuThanhToan lstt
    left join fetch lstt.phuongThucThanhToan pttt
    where lstt.hoaDon.id = :hoaDonId
    order by lstt.ngayThanhToan desc
    """)
    List<LichSuThanhToan> findAllByHoaDonIdFetchPTTT(@Param("hoaDonId") Long hoaDonId);

    @Query("""
    select COALESCE(sum(lstt.soTien), 0)
    from LichSuThanhToan lstt
    join lstt.hoaDon h
    join h.nhanVien nv
    left join nv.quyenHan qh
    join lstt.phuongThucThanhToan p
    where lstt.ngayThanhToan >= :from
      and lstt.ngayThanhToan <= :to
      and lstt.trangThai = true
      and h.trangThai = true
      and (
          nv.id = :nhanVienId
          or upper(coalesce(qh.maQuyenHan, '')) in ('QH_ADMIN', 'ADMIN')
          or lower(coalesce(qh.tenQuyenHan, '')) like '%admin%'
      )
      and p.hinhThuc = 1
    """)
    BigDecimal sumTienMatByThanhToanRange(@Param("from") LocalDateTime from,
                                          @Param("to") LocalDateTime to,
                                          @Param("nhanVienId") Long nhanVienId);

    @Query("""
    select COALESCE(sum(lstt.soTien), 0)
    from LichSuThanhToan lstt
    join lstt.hoaDon h
    join h.nhanVien nv
    left join nv.quyenHan qh
    left join lstt.phuongThucThanhToan p
    where lstt.ngayThanhToan >= :from
      and lstt.ngayThanhToan <= :to
      and lstt.trangThai = true
      and h.trangThai = true
      and (
          nv.id = :nhanVienId
          or upper(coalesce(qh.maQuyenHan, '')) in ('QH_ADMIN', 'ADMIN')
          or lower(coalesce(qh.tenQuyenHan, '')) like '%admin%'
      )
      and (p is null or p.hinhThuc is null or p.hinhThuc <> 1)
""")
    BigDecimal sumKhacTienMatByThanhToanRange(@Param("from") LocalDateTime from,
                                              @Param("to") LocalDateTime to,
                                              @Param("nhanVienId") Long nhanVienId);
}