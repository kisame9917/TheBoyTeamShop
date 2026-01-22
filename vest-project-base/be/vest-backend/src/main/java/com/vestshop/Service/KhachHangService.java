package com.vestshop.Service;

import com.vestshop.dto.request.KhachHangRequest;
import com.vestshop.dto.response.KhachHangResponse;

import java.util.List;

public interface KhachHangService {
    List<KhachHangResponse> getAll();
    KhachHangResponse getById(Long id);

    KhachHangResponse create(KhachHangRequest request);
    KhachHangResponse update(Long id, KhachHangRequest request);

    KhachHangResponse updateTrangThai(Long id, Boolean trangThai);

    String getNextMaKhachHang(String prefix);
}
