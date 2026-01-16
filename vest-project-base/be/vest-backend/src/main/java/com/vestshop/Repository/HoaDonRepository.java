package com.vestshop.Repository;

import com.vestshop.Entity.HoaDon;
import org.springframework.data.jpa.repository.*;
import java.util.Optional;

public interface HoaDonRepository extends JpaRepository<HoaDon, Long>, JpaSpecificationExecutor<HoaDon> {
    Optional<HoaDon> findByMaHoaDon(String maHoaDon);
}
