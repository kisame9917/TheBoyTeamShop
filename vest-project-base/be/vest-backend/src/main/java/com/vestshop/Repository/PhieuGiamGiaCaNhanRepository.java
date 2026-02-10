package com.vestshop.Repository;

import com.vestshop.Entity.PhieuGiamGiaCaNhan;
import com.vestshop.dto.response.PhieuGiamGiaCaNhanProjection;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PhieuGiamGiaCaNhanRepository extends JpaRepository<PhieuGiamGiaCaNhan, Long> {

    // ✅ 1) Danh sách KH đã nhận phiếu (theo PGG) + stats
    @Query("""
        select
            kh.id as id,
            kh.maKhachHang as maKhachHang,
            kh.tenKhachHang as tenKhachHang,
            kh.soDienThoai as soDienThoai,
            kh.email as email,
            kh.ngaySinh as ngaySinh,

            (select count(distinct hd.id)
                                   from HoaDon hd
                                   where hd.khachHang.id = kh.id
                                     and hd.trangThai = true
                                     and hd.trangThaiDon = 4
                                     and coalesce(hd.ngayCapNhat, hd.ngayTao) >= :fromDate
                                     and coalesce(hd.ngayCapNhat, hd.ngayTao) <  :toDate
                                  ) as soDonThangHienTai,

            (select coalesce(sum(
                case when :includeShip = true
                     then (hd2.tongTienSauGiam + hd2.phiVanChuyen)
                     else hd2.tongTienSauGiam
                end
            ), 0)
             from HoaDon hd2
             where hd2.khachHang.id = kh.id
               and hd2.trangThai = true
               and hd2.trangThaiDon = 4
            ) as tongTienDaTieu

        from PhieuGiamGiaCaNhan cn
        join cn.khachHang kh
        where cn.phieuGiamGia.id = :pggId
          and cn.trangThai = true
        order by cn.id desc
    """)
    List<PhieuGiamGiaCaNhanProjection> findDsKhachHangNhanPhieuWithStats(
            @Param("pggId") Long pggId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("includeShip") boolean includeShip
    );

    // ✅ 2) Danh sách KH để CHỌN khi tạo PGG cá nhân (không theo pggId) + stats
    @Query("""
        select
            kh.id as id,
            kh.maKhachHang as maKhachHang,
            kh.tenKhachHang as tenKhachHang,
            kh.soDienThoai as soDienThoai,
            kh.email as email,
            kh.ngaySinh as ngaySinh,

            (select count(distinct hd.id)
                                    from HoaDon hd
                                    where hd.khachHang.id = kh.id
                                      and hd.trangThai = true
                                      and hd.trangThaiDon = 4
                                      and coalesce(hd.ngayCapNhat, hd.ngayTao) >= :fromDate
                                      and coalesce(hd.ngayCapNhat, hd.ngayTao) <  :toDate
                                   ) as soDonThangHienTai,

            (select coalesce(sum(
                case when :includeShip = true
                     then (hd2.tongTienSauGiam + hd2.phiVanChuyen)
                     else hd2.tongTienSauGiam
                end
            ), 0)
             from HoaDon hd2
             where hd2.khachHang.id = kh.id
               and hd2.trangThai = true
               and hd2.trangThaiDon = 4
            ) as tongTienDaTieu

        from KhachHang kh
        where kh.trangThai = true
        order by kh.id desc
    """)
    List<PhieuGiamGiaCaNhanProjection> findAllKhachHangWithStats(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("includeShip") boolean includeShip
    );

    // ---- các method khác bạn đang dùng ----
    List<PhieuGiamGiaCaNhan> findByPhieuGiamGia_Id(Long pggId);

    @EntityGraph(attributePaths = {"khachHang"})
    List<PhieuGiamGiaCaNhan> findByPhieuGiamGia_IdAndTrangThaiTrue(Long pggId);

    Optional<PhieuGiamGiaCaNhan> findByPhieuGiamGia_IdAndKhachHang_Id(Long pggId, Long khId);
}
