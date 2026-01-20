package com.vestshop.Repository;

import com.vestshop.Entity.AnhChiTietSanPham;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnhChiTietSanPhamRepository extends JpaRepository<AnhChiTietSanPham, Long> {

    List<AnhChiTietSanPham> findAllBySanPhamChiTiet_IdAndTrangThaiTrue(Long spctId);

    Optional<AnhChiTietSanPham> findTop1BySanPhamChiTiet_IdAndTrangThaiTrueOrderByIdDesc(Long spctId);
}
