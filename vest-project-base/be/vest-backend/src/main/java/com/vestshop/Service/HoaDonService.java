package com.vestshop.Service;

import com.vestshop.dto.request.HoaDonCreateRequest;
import com.vestshop.dto.request.HoaDonUpdateRequest;
import com.vestshop.dto.response.HoaDonResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HoaDonService {
    HoaDonResponse create(HoaDonCreateRequest req);
    HoaDonResponse getById(Long id);
    Page<HoaDonResponse> getPage(Boolean trangThai, Pageable pageable);
    HoaDonResponse update(Long id, HoaDonUpdateRequest req);
    void softDelete(Long id);
}
