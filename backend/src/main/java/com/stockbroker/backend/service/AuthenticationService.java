package com.stockbroker.backend.service;

import com.stockbroker.backend.dto.LoginRequest;
import com.stockbroker.backend.dto.LoginResponse;
import com.stockbroker.backend.dto.ProfileResponse;

public interface AuthenticationService {

    LoginResponse login(LoginRequest request);

    ProfileResponse getProfile();

    String logout();

}