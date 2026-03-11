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
                        .build()
        );
    }
}