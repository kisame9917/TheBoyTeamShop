package com.vestshop.Repository;

import com.vestshop.Entity.AnhChiTietSanPham;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnhChiTietSanPhamRepository extends JpaRepository<AnhChiTietSanPham, Long> {

    List<AnhChiTietSanPham> findAllBySanPhamChiTiet_IdAndTrangThaiTrue(Long spctId);

    List<AnhChiTietSanPham> findAllBySanPhamChiTiet_IdAndTrangThaiTrueOrderByThuTuHienThiAscIdAsc(Long spctId);

    Optional<AnhChiTietSanPham> findTop1BySanPhamChiTiet_IdAndTrangThaiTrueOrderByIdDesc(Long spctId);

    void deleteBySanPhamChiTiet_Id(Long spctId);

    List<AnhChiTietSanPham> findAllBySanPhamChiTiet_IdOrderByThuTuHienThiAscIdAsc(Long spctId);

    Optional<AnhChiTietSanPham> findTop1BySanPhamChiTiet_IdAndTrangThaiTrueOrderByThuTuHienThiAscIdAsc(Long spctId);
}
