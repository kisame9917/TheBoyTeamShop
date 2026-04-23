package com.vestshop.Service;

import com.vestshop.dto.request.ConfirmPaymentRequest;
import com.vestshop.dto.request.OnlineCheckoutRequest;
import com.vestshop.dto.response.ApiMessageResponse;
import com.vestshop.dto.response.OnlineCheckoutResponse;
import com.vestshop.dto.response.OnlineOrderLookupResponse;
import com.vestshop.dto.response.OnlinePaymentStatusResponse;

public interface OnlineCheckoutService {

    OnlineCheckoutResponse checkout(
            OnlineCheckoutRequest request,
            org.springframework.security.core.Authentication authentication
    );

    OnlineCheckoutResponse createVnpayPaymentUrl(Long orderId);

    ApiMessageResponse confirmQrPayment(Long orderId, ConfirmPaymentRequest request);

    OnlinePaymentStatusResponse getPaymentStatus(Long orderId);

    OnlineOrderLookupResponse lookupOrder(String maHoaDon, String soDienThoai);

}