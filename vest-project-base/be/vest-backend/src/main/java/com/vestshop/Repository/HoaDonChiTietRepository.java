package com.vestshop.Repository;

import com.vestshop.Entity.HoaDonChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HoaDonChiTietRepository extends JpaRepository<HoaDonChiTiet, Long> {
    List<HoaDonChiTiet> findAllByHoaDon_Id(Long hoaDonId);
}
