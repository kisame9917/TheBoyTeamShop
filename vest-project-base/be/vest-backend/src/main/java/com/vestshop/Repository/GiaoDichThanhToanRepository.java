package com.vestshop.Repository;

import com.vestshop.Entity.GiaoDichThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GiaoDichThanhToanRepository extends JpaRepository<GiaoDichThanhToan, Long> {
    List<GiaoDichThanhToan> findAllByHoaDon_IdOrderByThoiGianTaoDesc(Long hoaDonId);
}
