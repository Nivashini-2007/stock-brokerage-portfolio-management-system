package com.stockbroker.backend.controller;

import com.stockbroker.backend.dto.LoginRequest;
import com.stockbroker.backend.dto.LoginResponse;
import com.stockbroker.backend.dto.ProfileResponse;
import com.stockbroker.backend.dto.RegisterRequest;
import com.stockbroker.backend.dto.UserResponse;
import com.stockbroker.backend.security.CustomUserPrincipal;
import com.stockbroker.backend.service.AuthenticationService;
import com.stockbroker.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationService authenticationService;

    public AuthController(
            UserService userService,
            AuthenticationService authenticationService) {

        this.userService = userService;
        this.authenticationService = authenticationService;
    }

    /**
     * Register a new user.
     *
     * Public endpoint.
     */
    @PostMapping(
            value = "/register",
            consumes = "application/json",
            produces = "application/json"
    )
    public ResponseEntity<UserResponse> registerUser(
            @Valid @RequestBody RegisterRequest request) {

        UserResponse response =
                userService.registerUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Authenticate user and generate JWT token.
     *
     * Public endpoint.
     */
    @PostMapping(
            value = "/login",
            consumes = "application/json",
            produces = "application/json"
    )
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response =
                authenticationService.login(request);

        return ResponseEntity.ok(response);
    }

    /**
     * Get the currently authenticated user's profile.
     *
     * The user is identified from the JWT token.
     * No client ID is accepted from the request.
     */
    @GetMapping(
            value = "/profile",
            produces = "application/json"
    )
    public ResponseEntity<ProfileResponse> getProfile(
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        ProfileResponse response = new ProfileResponse(
                principal.getId(),
                principal.getFirstName(),
                principal.getLastName(),
                principal.getUser().getEmail(),
                principal.getUser().getPhone(),
                principal.getRole(),
                principal.getUser().getEnabled()
        );

        return ResponseEntity.ok(response);
    }
}