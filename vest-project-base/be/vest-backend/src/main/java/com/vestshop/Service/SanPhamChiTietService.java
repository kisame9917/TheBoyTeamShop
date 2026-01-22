package com.vestshop.Service;

import com.vestshop.dto.request.SanPhamChiTietRequest;
import com.vestshop.dto.response.SanPhamChiTietResponse;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SanPhamChiTietService {
    Page<SanPhamChiTietResponse> getAll(Pageable pageable);
    List<SanPhamChiTietResponse> getByProductId(Long productId);
    SanPhamChiTietResponse create(SanPhamChiTietRequest request);
    SanPhamChiTietResponse update(Long id, SanPhamChiTietRequest request);
    void delete(Long id);
}
