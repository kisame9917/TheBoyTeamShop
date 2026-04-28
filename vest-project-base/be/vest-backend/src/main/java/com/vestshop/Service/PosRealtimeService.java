package com.vestshop.Service;

import com.vestshop.dto.response.HoaDonDetailResponse;

public interface PosRealtimeService {
    void pushUpsert(HoaDonDetailResponse data);
    void pushRemove(Long hoaDonId);
    void pushShowQr(HoaDonDetailResponse data, String qrCode, String message);
    void pushQrPaid(Long hoaDonId, String message);
}
