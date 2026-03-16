package com.vestshop.Service;

import com.vestshop.dto.request.ConfirmPaymentRequest;
import com.vestshop.dto.request.OnlineCheckoutRequest;
import com.vestshop.dto.response.ApiMessageResponse;
import com.vestshop.dto.response.OnlineCheckoutResponse;
import com.vestshop.dto.response.OnlineOrderLookupResponse;

public interface OnlineCheckoutService {

    OnlineCheckoutResponse checkout(OnlineCheckoutRequest request);

    ApiMessageResponse confirmQrPayment(Long orderId, ConfirmPaymentRequest request);


    OnlineOrderLookupResponse lookupOrder(String maHoaDon, String soDienThoai);
}