package com.vestshop.Service;

import com.vestshop.dto.request.ConfirmPaymentRequest;
import com.vestshop.dto.request.OnlineCheckoutRequest;
import com.vestshop.dto.response.ApiMessageResponse;
import com.vestshop.dto.response.OnlineCheckoutResponse;

public interface OnlineCheckoutService {

    OnlineCheckoutResponse checkout(OnlineCheckoutRequest request);

    ApiMessageResponse confirmQrPayment(Long orderId, ConfirmPaymentRequest request);
}