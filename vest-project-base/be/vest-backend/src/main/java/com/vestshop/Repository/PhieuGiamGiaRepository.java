package com.vestshop.Repository;

import com.vestshop.Entity.PhieuGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PhieuGiamGiaRepository extends JpaRepository<PhieuGiamGia, Long> {
    boolean existsByMaGiamGia(String maGiamGia);

    @Query("""
select p from PhieuGiamGia p
where p.trangThai = true
  and (p.loaiPhieu = false or p.loaiPhieu is null)
""")
    List<PhieuGiamGia> findPublicActive();
}
