package com.vestshop.Service;

import com.vestshop.dto.response.HoaDonDetailResponse;

public interface PosRealtimeService {
    void pushUpsert(HoaDonDetailResponse data);
    void pushRemove(Long hoaDonId);
}