package com.vestshop.Service;

import com.vestshop.dto.request.NhanVienRequest;
import com.vestshop.dto.response.NhanVienResponse;

import java.util.List;

public interface NhanVienService {
    List<NhanVienResponse> getAll();
    NhanVienResponse getById(Long id);
    NhanVienResponse create(NhanVienRequest request);
    NhanVienResponse update(Long id, NhanVienRequest request);
    void delete(Long id);
}
