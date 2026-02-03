package com.vestshop.Service;

import com.vestshop.dto.request.LoginRequest;
import com.vestshop.dto.response.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest req);
}
