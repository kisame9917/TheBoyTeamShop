package com.vestshop.Repository;

import com.vestshop.Entity.DiaChiKhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DiaChiKhachHangRepository extends JpaRepository<DiaChiKhachHang, Long> {

    Optional<DiaChiKhachHang> findFirstByKhachHangIdAndLaMacDinhTrueOrderByIdDesc(Long khachHangId);

    @Modifying
    @Query("update DiaChiKhachHang d set d.laMacDinh = false where d.khachHang.id = :khachHangId")
    void clearDefaultByKhachHangId(@Param("khachHangId") Long khachHangId);
}
