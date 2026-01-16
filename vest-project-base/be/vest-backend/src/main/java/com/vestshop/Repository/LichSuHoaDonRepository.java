package com.vestshop.Repository;

import com.vestshop.Entity.LichSuHoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LichSuHoaDonRepository extends JpaRepository<LichSuHoaDon, Long> {
    List<LichSuHoaDon> findAllByHoaDon_IdOrderByThoiGianDesc(Long hoaDonId);
}
