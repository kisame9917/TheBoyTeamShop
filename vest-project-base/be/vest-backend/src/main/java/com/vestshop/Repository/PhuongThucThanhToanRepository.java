package com.vestshop.Repository;

import com.vestshop.Entity.PhuongThucThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PhuongThucThanhToanRepository extends JpaRepository<PhuongThucThanhToan, Long> {

    Optional<PhuongThucThanhToan> findFirstByHinhThucAndTrangThaiTrue(Integer hinhThuc);

    Optional<PhuongThucThanhToan> findFirstByMaPhuongThucThanhToanIgnoreCaseAndTrangThaiTrue(String ma);
}