package com.vestshop.Controller;

import com.vestshop.Service.OnlineCheckoutService;
import com.vestshop.dto.request.ConfirmPaymentRequest;
import com.vestshop.dto.request.OnlineCheckoutRequest;
import com.vestshop.dto.response.ApiMessageResponse;
import com.vestshop.dto.response.OnlineCheckoutResponse;
import com.vestshop.dto.response.OnlineOrderLookupResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/online-checkout")
public class OnlineCheckoutController {

    private final OnlineCheckoutService onlineCheckoutService;

    public OnlineCheckoutController(OnlineCheckoutService onlineCheckoutService) {
        this.onlineCheckoutService = onlineCheckoutService;
    }

    @PostMapping
    public ResponseEntity<OnlineCheckoutResponse> checkout(@RequestBody OnlineCheckoutRequest request,
                                                           org.springframework.security.core.Authentication authentication) {
        return ResponseEntity.ok(onlineCheckoutService.checkout(request, authentication));
    }

    @PostMapping("/{orderId}/confirm-payment")
    public ResponseEntity<ApiMessageResponse> confirmPayment(@PathVariable Long orderId,
                                                             @RequestBody ConfirmPaymentRequest request) {
        return ResponseEntity.ok(onlineCheckoutService.confirmQrPayment(orderId, request));
    }

    @GetMapping("/lookup")
    public ResponseEntity<OnlineOrderLookupResponse> lookupOrder(
            @RequestParam String maHoaDon,
            @RequestParam String soDienThoai
    ) {
        return ResponseEntity.ok(onlineCheckoutService.lookupOrder(maHoaDon, soDienThoai));
    }
}