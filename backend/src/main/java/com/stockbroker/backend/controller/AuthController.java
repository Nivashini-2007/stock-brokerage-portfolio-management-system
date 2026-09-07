package com.stockbroker.backend.controller;

import com.stockbroker.backend.dto.LoginRequest;
import com.stockbroker.backend.dto.LoginResponse;
import com.stockbroker.backend.dto.ProfileResponse;
import com.stockbroker.backend.dto.RegisterRequest;
import com.stockbroker.backend.dto.RoleResponse;
import com.stockbroker.backend.dto.UserResponse;
import com.stockbroker.backend.exception.RateLimitExceededException;
import com.stockbroker.backend.repository.RoleRepository;
import com.stockbroker.backend.security.CustomUserPrincipal;
import com.stockbroker.backend.security.RateLimiter;
import com.stockbroker.backend.service.AuthenticationService;
import com.stockbroker.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationService authenticationService;
    private final RateLimiter rateLimiter;
    private final RoleRepository roleRepository;

    public AuthController(
            UserService userService,
            AuthenticationService authenticationService,
            RateLimiter rateLimiter,
            RoleRepository roleRepository) {

        this.userService = userService;
        this.authenticationService = authenticationService;
        this.rateLimiter = rateLimiter;
        this.roleRepository = roleRepository;
    }

    /**
     * Admin-only: lists assignable roles (id + name) so the admin UI can
     * populate register-staff without hardcoding role IDs.
     */
    @GetMapping(value = "/roles", produces = "application/json")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RoleResponse>> listRoles() {

        List<RoleResponse> roles = roleRepository.findAll().stream()
                .map(r -> new RoleResponse(r.getId(), r.getName()))
                .toList();

        return ResponseEntity.ok(roles);
    }

    /**
     * Register a new CLIENT account. Public endpoint - any roleId supplied
     * is ignored server-side (see UserServiceImpl.registerUser) so a
     * caller cannot self-escalate to a staff role.
     */
    @PostMapping(
            value = "/register",
            consumes = "application/json",
            produces = "application/json"
    )
    public ResponseEntity<UserResponse> registerUser(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {

        if (!rateLimiter.tryAcquire("register:" + clientIp(httpRequest), 3, 86400)) {
            throw new RateLimitExceededException(
                    "Too many registration attempts from this address today. Please try again tomorrow.");
        }

        UserResponse response =
                userService.registerUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Admin-only: provisions a staff account (Dealer/Research Analyst/
     * Compliance Officer/Risk Manager/Admin) with the role chosen by the
     * caller.
     */
    @PostMapping(
            value = "/register-staff",
            consumes = "application/json",
            produces = "application/json"
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> registerStaff(
            @Valid @RequestBody RegisterRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.registerStaff(request));
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
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        String key = "login:" + clientIp(httpRequest) + ":" + request.getEmail();

        if (!rateLimiter.tryAcquire(key, 5, 900)) {
            throw new RateLimitExceededException(
                    "Too many login attempts. Please try again in 15 minutes.");
        }

        LoginResponse response =
                authenticationService.login(request);

        return ResponseEntity.ok(response);
    }

    /**
     * Invalidates the caller's current JWT (adds its jti to the blacklist).
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String token = (authHeader != null && authHeader.startsWith("Bearer "))
                ? authHeader.substring(7) : authHeader;

        return ResponseEntity.ok(authenticationService.logout(token));
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

    private String clientIp(HttpServletRequest request) {

        String xff = request.getHeader("X-Forwarded-For");
        return xff != null ? xff.split(",")[0].trim() : request.getRemoteAddr();
    }
}
