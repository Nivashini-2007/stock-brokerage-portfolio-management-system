package com.stockbroker.backend.service;

import com.stockbroker.backend.dto.RegisterRequest;
import com.stockbroker.backend.dto.UserResponse;

public interface UserService {

    UserResponse registerUser(RegisterRequest request);

}