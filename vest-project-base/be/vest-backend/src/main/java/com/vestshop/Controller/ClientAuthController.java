package com.vestshop.Controller;

import com.vestshop.Service.ClientAuthService;
import com.vestshop.dto.request.LoginRequest;
import com.vestshop.dto.response.ClientLoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/client/auth")
@RequiredArgsConstructor
public class ClientAuthController {

    private final ClientAuthService clientAuthService;

    @PostMapping("/login")
    public ResponseEntity<ClientLoginResponse> login(@RequestBody LoginRequest req) {
        return ResponseEntity.ok(clientAuthService.login(req));
    }
}