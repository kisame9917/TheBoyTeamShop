package com.vestshop.Service;

import com.vestshop.Entity.PhieuGiamGia;
import com.vestshop.dto.request.PhieuGiamGiaCreateRequest;
import com.vestshop.dto.request.PhieuGiamGiaUpdateRequest;
import com.vestshop.dto.response.PhieuGiamGiaDetailResponse;
import com.vestshop.dto.response.PhieuGiamGiaResponse;

import java.util.List;
import java.util.Optional;

public interface PhieuGiamGiaService {
    List<PhieuGiamGiaResponse> getAll();
    Optional<PhieuGiamGia> findbyId(Long id);
    PhieuGiamGiaDetailResponse detail(Long id);
    PhieuGiamGia create(PhieuGiamGiaCreateRequest dto);
    PhieuGiamGia update(Long id, PhieuGiamGiaUpdateRequest dto) throws Exception;
    void delete(Long id);
    void endpgg(Long id) throws Exception;

}
