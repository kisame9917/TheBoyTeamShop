package com.vestshop.Service;

import com.vestshop.dto.request.LoginRequest;
import com.vestshop.dto.response.ClientLoginResponse;
import com.vestshop.dto.response.LoginResponse;

public interface ClientAuthService {
    ClientLoginResponse login(LoginRequest req);
}
