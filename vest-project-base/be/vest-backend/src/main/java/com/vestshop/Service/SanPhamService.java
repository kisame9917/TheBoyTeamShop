package com.vestshop.Service;

import com.vestshop.web.dto.SanPhamCreateRequest;
import com.vestshop.web.dto.SanPhamResponse;

import java.util.List;

public interface SanPhamService {
    List<SanPhamResponse> findAll();
    SanPhamResponse findById(Long id);
    SanPhamResponse create(SanPhamCreateRequest request);
}
