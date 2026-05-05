package com.vestshop.Repository;

import com.vestshop.Entity.LichSuHoaDon;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LichSuHoaDonRepository extends JpaRepository<LichSuHoaDon, Long> {
    @EntityGraph(attributePaths = "nhanVien")
    List<LichSuHoaDon> findAllByHoaDon_IdOrderByThoiGianDesc(Long hoaDonId);

    Optional<LichSuHoaDon> findTopByHoaDon_IdAndTrangThaiTrueOrderByThoiGianDesc(Long hoaDonId);
}
