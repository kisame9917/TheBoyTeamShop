package com.vestshop.Service;

import com.vestshop.Entity.NhanVien;
import com.vestshop.dto.request.NhanVienRequest;
import com.vestshop.dto.response.NhanVienResponse;

import java.util.List;

public interface NhanVienService {
    List<NhanVienResponse> getAll();
    NhanVienResponse getById(Long id);
    NhanVienResponse create(NhanVienRequest request);
    NhanVienResponse update(Long id, NhanVienRequest request);
    void delete(Long id);
    NhanVienResponse updateTrangThai(Long id, Boolean trangThai);
    NhanVien findEntityByTaiKhoan(String taiKhoan);
}
