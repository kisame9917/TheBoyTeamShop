package com.vestshop.Repository;

import com.vestshop.Entity.PhieuGiamGiaCaNhan;
import com.vestshop.dto.response.PhieuGiamGiaCaNhanResponse;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PhieuGiamGiaCaNhanRepository extends JpaRepository<PhieuGiamGiaCaNhan, Long> {
    @Query("""
        select new com.vestshop.dto.response.PhieuGiamGiaCaNhanResponse(
            cn.id,
            kh.id,
            kh.maKhachHang,
            kh.tenKhachHang,
            kh.soDienThoai,
            kh.email,
            cn.maPhieuGiamGiaCaNhan,
            cn.ngayNhan,
            cn.daSuDung
        )
        from PhieuGiamGiaCaNhan cn
        join cn.khachHang kh
        where cn.phieuGiamGia.id = :pggId
          and cn.trangThai = true
    """)
    List<PhieuGiamGiaCaNhanResponse> findDsKhachHangNhanPhieu(@Param("pggId") Long pggId);
    List<PhieuGiamGiaCaNhan> findByPhieuGiamGia_Id(Long pggId);
    @EntityGraph(attributePaths = {"khachHang"})
    List<PhieuGiamGiaCaNhan> findByPhieuGiamGia_IdAndTrangThaiTrue(Long pggId);
    Optional<PhieuGiamGiaCaNhan> findByPhieuGiamGia_IdAndKhachHang_Id(Long pggId, Long khId);
}
