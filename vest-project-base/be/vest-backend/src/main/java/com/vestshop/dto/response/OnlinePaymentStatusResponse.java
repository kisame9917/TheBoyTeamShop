package com.vestshop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnlinePaymentStatusResponse {
    private Long orderId;
    private String paymentStatus;
    private Boolean paid;
    private String message;
    private String maGiaoDich;
}