package com.vestshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PosOrderRealtimeEvent {
    private String type;
    private Long hoaDonId;
    private HoaDonDetailResponse data;
    private String qrCode;
    private String message;
}
