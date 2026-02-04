package com.vestshop.Repository;

import com.vestshop.Entity.DiaChiKhachHang;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DiaChiKhachHangRepository extends JpaRepository<DiaChiKhachHang, Long> {

    Optional<DiaChiKhachHang> findFirstByKhachHangIdAndLaMacDinhTrueOrderByIdDesc(Long khachHangId);

    List<DiaChiKhachHang> findByKhachHang_IdOrderByIdDesc(Long khachHangId);

    List<DiaChiKhachHang> findByKhachHang_IdAndTrangThaiTrueOrderByLaMacDinhDescIdDesc(Long khachHangId);

    long countByKhachHang_IdAndTrangThaiTrue(Long khachHangId);

    Optional<DiaChiKhachHang> findByIdAndKhachHang_Id(Long id, Long khachHangId);

    List<DiaChiKhachHang> findByKhachHangIdOrderByLaMacDinhDescIdDesc(Long khachHangId);

    Optional<DiaChiKhachHang> findByIdAndKhachHangId(Long id, Long khachHangId);

    List<DiaChiKhachHang> findByKhachHangIdAndTrangThaiTrueOrderByLaMacDinhDescIdDesc(Long khachHangId);

    Optional<DiaChiKhachHang> findByIdAndKhachHangIdAndTrangThaiTrue(Long id, Long khachHangId);

    Optional<DiaChiKhachHang> findFirstByKhachHangIdAndTrangThaiTrueOrderByIdDesc(Long khachHangId);

    @Modifying
    @Query("UPDATE DiaChiKhachHang d SET d.laMacDinh = false WHERE d.khachHang.id = :khachHangId")
    void clearDefaultByKhachHangId(@Param("khachHangId") Long khachHangId);
}
