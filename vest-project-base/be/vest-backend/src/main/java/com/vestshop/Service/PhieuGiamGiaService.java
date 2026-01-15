package com.vestshop.Service;

import com.vestshop.Entity.PhieuGiamGia;
import com.vestshop.dto.request.PhieuGiamGiaCreateRequest;
import com.vestshop.dto.request.PhieuGiamGiaUpdateRequest;
import com.vestshop.dto.response.GetPhieuGiamGiaDto;

import java.util.List;
import java.util.Optional;

public interface PhieuGiamGiaService {
    List<GetPhieuGiamGiaDto> getAll();
    Optional<PhieuGiamGia> findbyId(Long id);
    PhieuGiamGia create(PhieuGiamGiaCreateRequest dto);
    PhieuGiamGia update(Long id, PhieuGiamGiaUpdateRequest dto) throws Exception;
    void delete(Long id);
}
