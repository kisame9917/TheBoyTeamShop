package com.vestshop.Service;

import com.vestshop.dto.request.*;
import com.vestshop.dto.response.*;

import java.util.List;

public interface KhachHangService {
    List<KhachHangResponse> getAll();
    KhachHangResponse getById(Long id);

    KhachHangResponse create(KhachHangRequest request);
    KhachHangResponse update(Long id, KhachHangRequest request);

    KhachHangResponse updateTrangThai(Long id, Boolean trangThai);

    String getNextMaKhachHang(String prefix);
    // ✅ MỚI: Quản lý địa chỉ
    List<DiaChiKhachHangResponse> getDiaChiList(Long khachHangId);
    DiaChiKhachHangResponse addDiaChi(Long khachHangId, DiaChiKhachHangRequest request);
    DiaChiKhachHangResponse setDiaChiMacDinh(Long khachHangId, Long diaChiId);
}
