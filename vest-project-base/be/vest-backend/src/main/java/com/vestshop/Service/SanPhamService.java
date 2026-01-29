package com.vestshop.Service;
import com.vestshop.dto.request.SanPhamRequest;
import com.vestshop.dto.response.SanPhamResponse;
import org.springframework.data.domain.*;

import java.math.BigDecimal;

public interface SanPhamService {
    SanPhamResponse create(SanPhamRequest request);
    Page<SanPhamResponse> getAll(Pageable pageable);
    SanPhamResponse getById(Long id);
    SanPhamResponse update(Long id, SanPhamRequest request);
    void delete(Long id);
    BigDecimal getGiaMaxDb();
}
