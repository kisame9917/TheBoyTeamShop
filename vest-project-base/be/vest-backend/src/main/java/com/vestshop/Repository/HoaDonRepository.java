package com.vestshop.Repository;

import com.vestshop.Entity.HoaDon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HoaDonRepository extends JpaRepository<HoaDon, Long> {

    boolean existsByMaHoaDon(String maHoaDon);

    Page<HoaDon> findByTrangThai(Boolean trangThai, Pageable pageable);
}
