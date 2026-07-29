package com.stockbroker.backend.service;

import com.stockbroker.backend.dto.LoginRequest;
import com.stockbroker.backend.dto.LoginResponse;

public interface AuthenticationService {

    LoginResponse login(LoginRequest request);

}