package com.vestshop.Service;

import com.vestshop.dto.request.KhachHangRequest;
import com.vestshop.dto.response.KhachHangResponse;

import java.util.List;

public interface KhachHangService {
    List<KhachHangResponse> getAll();
    KhachHangResponse getById(Long id);
    KhachHangResponse create(KhachHangRequest request);
    KhachHangResponse update(Long id, KhachHangRequest request);
    void delete(Long id);
}
