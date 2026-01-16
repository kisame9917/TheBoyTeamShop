package com.vestshop.Repository;

import com.vestshop.Entity.AnhChiTietSanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnhChiTietSanPhamRepository extends JpaRepository<AnhChiTietSanPham, Long> {
    List<AnhChiTietSanPham> findAllBySanPhamChiTiet_IdAndTrangThaiTrue(Long idSanPhamChiTiet);
}
