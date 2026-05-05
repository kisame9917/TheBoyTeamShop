package com.vestshop.Repository;

import com.vestshop.Entity.PhienCa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface PhienCaRepository extends JpaRepository<PhienCa, Long> {

    Optional<PhienCa> findFirstByNhanVien_IdAndTrangThaiOrderByThoiGianMoDesc(Long nhanVienId, Integer trangThai);

    Optional<PhienCa> findFirstByNhanVien_IdAndTrangThaiOrderByThoiGianDongDesc(Long nhanVienId, Integer trangThai);

    Optional<PhienCa> findFirstByTrangThaiOrderByThoiGianDongDesc(Integer trangThai);

    @Query("""
        select p
        from PhienCa p
        join p.nhanVien nv
        left join p.caLamViec ca
        where (:keyword is null or :keyword = ''
              or lower(nv.tenNhanVien) like lower(concat('%', :keyword, '%'))
              or lower(nv.maNhanVien) like lower(concat('%', :keyword, '%'))
              or (ca is not null and lower(ca.tenCa) like lower(concat('%', :keyword, '%')))
              or (p.maPhien is not null and lower(p.maPhien) like lower(concat('%', :keyword, '%')))
        )
          and (:fromDate is null or p.ngayLamViec >= :fromDate)
          and (:toDate is null or p.ngayLamViec <= :toDate)
          and (:idCa is null or (ca is not null and ca.id = :idCa))
          and (:idNv is null or nv.id = :idNv)
          and (:trangThai is null or p.trangThai = :trangThai)
        order by p.thoiGianMo desc
    """)
    Page<PhienCa> searchAdmin(@Param("keyword") String keyword,
                              @Param("idCa") Long idCa,
                              @Param("idNv") Long idNv,
                              @Param("fromDate") LocalDate fromDate,
                              @Param("toDate") LocalDate toDate,
                              @Param("trangThai") Integer trangThai,
                              Pageable pageable);
}
