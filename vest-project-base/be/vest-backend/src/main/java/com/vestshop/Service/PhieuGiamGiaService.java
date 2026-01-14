package com.vestshop.Service;

import com.vestshop.Entity.PhieuGiamGia;

import java.util.List;
import java.util.Optional;

public interface PhieuGiamGiaService {
    List<PhieuGiamGia> getAll();
    Optional<PhieuGiamGia> findbyId(Long id);
    PhieuGiamGia create(PhieuGiamGia phieuGiamGia);
    PhieuGiamGia update(Long id, PhieuGiamGia phieuGiamGia) throws Exception;
}
