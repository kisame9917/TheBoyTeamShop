package com.vestshop.Repository;

import com.vestshop.Entity.LichSuThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LichSuThanhToanRepository extends JpaRepository<LichSuThanhToan, Long> {

    // (optional) giữ lại cũng được
    List<LichSuThanhToan> findAllByHoaDon_IdOrderByNgayThanhToanDesc(Long hoaDonId);

    @Query("""
    select lstt
    from LichSuThanhToan lstt
    left join fetch lstt.phuongThucThanhToan pttt
    where lstt.hoaDon.id = :hoaDonId
    order by lstt.ngayThanhToan desc
""")
    List<LichSuThanhToan> findAllByHoaDonIdFetchPTTT(@Param("hoaDonId") Long hoaDonId);

}