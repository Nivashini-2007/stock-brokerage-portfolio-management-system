package com.stockbroker.backend.serviceimpl;

import com.stockbroker.backend.dto.LoginRequest;
import com.stockbroker.backend.dto.LoginResponse;
import com.stockbroker.backend.entity.User;
import com.stockbroker.backend.exception.ResourceNotFoundException;
import com.stockbroker.backend.repository.UserRepository;
import com.stockbroker.backend.service.AuthenticationService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationServiceImpl(UserRepository userRepository,
                                     PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResourceNotFoundException("Invalid email or password");
        }

        LoginResponse response = new LoginResponse();

        response.setMessage("Login Successful");
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().getName());

        return response;
    }
}