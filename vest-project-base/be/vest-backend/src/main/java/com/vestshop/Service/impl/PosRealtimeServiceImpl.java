package com.vestshop.Service.impl;

import com.vestshop.Service.PosRealtimeService;
import com.vestshop.dto.response.HoaDonDetailResponse;
import com.vestshop.dto.response.PosOrderRealtimeEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PosRealtimeServiceImpl implements PosRealtimeService {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void pushUpsert(HoaDonDetailResponse data) {
        messagingTemplate.convertAndSend(
                "/topic/pos-orders",
                PosOrderRealtimeEvent.builder()
                        .type("UPSERT")
                        .hoaDonId(data.getId())
                        .data(data)
                        .build()
        );
    }

    @Override
    public void pushRemove(Long hoaDonId) {
        messagingTemplate.convertAndSend(
                "/topic/pos-orders",
                PosOrderRealtimeEvent.builder()
                        .type("REMOVE")
                        .hoaDonId(hoaDonId)
                        .data(null)
                        .message("Đơn hàng đã hoàn tất")
                        .build()
        );
    }

    @Override
    public void pushShowQr(HoaDonDetailResponse data, String qrCode, String message) {
        messagingTemplate.convertAndSend(
                "/topic/pos-orders",
                PosOrderRealtimeEvent.builder()
                        .type("SHOW_QR")
                        .hoaDonId(data.getId())
                        .data(data)
                        .qrCode(qrCode)
                        .message(message == null || message.isBlank()
                                ? "Hiển thị QR thanh toán"
                                : message)
                        .build()
        );
    }

    @Override
    public void pushQrPaid(Long hoaDonId, String message) {
        messagingTemplate.convertAndSend(
                "/topic/pos-orders",
                PosOrderRealtimeEvent.builder()
                        .type("QR_PAID")
                        .hoaDonId(hoaDonId)
                        .data(null)
                        .message(message == null || message.isBlank()
                                ? "Thanh toán QR thành công"
                                : message)
                        .build()
        );
    }
}
