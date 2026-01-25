package com.vestshop.Service;

import com.vestshop.Entity.PhieuGiamGia;
import com.vestshop.dto.request.PhieuGiamGiaCreateRequest;
import com.vestshop.dto.request.PhieuGiamGiaUpdateRequest;
import com.vestshop.dto.request.UpdateKhachHangNhanPhieuRequest;
import com.vestshop.dto.response.PhieuGiamGiaCaNhanResponse;
import com.vestshop.dto.response.PhieuGiamGiaDetailResponse;
import com.vestshop.dto.response.PhieuGiamGiaResponse;

import java.util.List;
import java.util.Optional;

public interface PhieuGiamGiaService {
    List<PhieuGiamGiaResponse> getAll();
    Optional<PhieuGiamGia> findbyId(Long id);
    PhieuGiamGiaDetailResponse detail(Long id);
    PhieuGiamGia create(PhieuGiamGiaCreateRequest dto) throws Exception;
    PhieuGiamGia update(Long id, PhieuGiamGiaUpdateRequest dto) throws Exception;
    void delete(Long id);
    void endpgg(Long id) throws Exception;
    List<PhieuGiamGiaCaNhanResponse> getKhachHangNhanPhieu(Long pggId);
    void updateKhachHangNhanPhieu(Long pggId, UpdateKhachHangNhanPhieuRequest req) throws Exception;
    void startpgg(Long id) throws Exception;

}
