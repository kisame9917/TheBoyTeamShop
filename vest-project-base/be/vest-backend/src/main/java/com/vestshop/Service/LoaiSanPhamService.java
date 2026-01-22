package com.vestshop.Service;

import com.vestshop.Entity.LoaiSanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface LoaiSanPhamService {
    Page<LoaiSanPham> getAll(Pageable pageable);
    List<LoaiSanPham> getAllList();
    LoaiSanPham getById(Long id);
    LoaiSanPham create(LoaiSanPham request);
    LoaiSanPham update(Long id, LoaiSanPham request);
    void delete(Long id);
}
