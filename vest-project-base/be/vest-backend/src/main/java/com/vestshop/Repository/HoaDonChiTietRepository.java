package com.vestshop.Repository;

import com.vestshop.Entity.HoaDonChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface HoaDonChiTietRepository extends JpaRepository<HoaDonChiTiet, Long> {
    List<HoaDonChiTiet> findAllByHoaDon_Id(Long hoaDonId);

    @Modifying
    @Query("delete from HoaDonChiTiet ct where ct.hoaDon.id = :hoaDonId")
    void deleteByHoaDonId(Long hoaDonId);
}
