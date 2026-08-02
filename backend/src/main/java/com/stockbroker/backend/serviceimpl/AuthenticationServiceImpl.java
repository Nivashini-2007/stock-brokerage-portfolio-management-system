package com.stockbroker.backend.serviceimpl;

import com.stockbroker.backend.dto.LoginRequest;
import com.stockbroker.backend.dto.LoginResponse;
import com.stockbroker.backend.dto.ProfileResponse;
import com.stockbroker.backend.security.CustomUserPrincipal;
import com.stockbroker.backend.security.JwtService;
import com.stockbroker.backend.service.AuthenticationService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthenticationServiceImpl(AuthenticationManager authenticationManager,
                                     JwtService jwtService) {

        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Override
    public LoginResponse login(LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(

                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        CustomUserPrincipal principal =
                (CustomUserPrincipal) authentication.getPrincipal();

        String token = jwtService.generateToken(principal);

        LoginResponse response = new LoginResponse();

        response.setMessage("Login Successful");
        response.setToken(token);
        response.setEmail(principal.getUsername());
        response.setRole(principal.getRole());

        return response;
    }

    @Override
    public ProfileResponse getProfile() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        CustomUserPrincipal principal =
                (CustomUserPrincipal) authentication.getPrincipal();

        ProfileResponse response = new ProfileResponse();

        response.setId(principal.getId());
        response.setFirstName(principal.getFirstName());
        response.setLastName(principal.getLastName());
        response.setEmail(principal.getUsername());
        response.setPhone(principal.getUser().getPhone());
        response.setRole(principal.getRole());

        return response;
    }
    @Override
    public String logout() {

        return "Logged out successfully. Please remove the JWT token from the client.";
    }
}