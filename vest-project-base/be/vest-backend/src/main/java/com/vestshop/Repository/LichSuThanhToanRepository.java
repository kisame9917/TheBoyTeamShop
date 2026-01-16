package com.vestshop.Repository;

import com.vestshop.Entity.LichSuThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LichSuThanhToanRepository extends JpaRepository<LichSuThanhToan, Long> {
    List<LichSuThanhToan> findAllByHoaDon_IdOrderByNgayThanhToanDesc(Long hoaDonId);
}
