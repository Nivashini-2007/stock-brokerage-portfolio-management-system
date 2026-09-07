package com.stockbroker.backend.serviceimpl;

import com.stockbroker.backend.dto.LoginRequest;
import com.stockbroker.backend.dto.LoginResponse;
import com.stockbroker.backend.dto.ProfileResponse;
import com.stockbroker.backend.entity.BlacklistedToken;
import com.stockbroker.backend.entity.User;
import com.stockbroker.backend.exception.AccountLockedException;
import com.stockbroker.backend.repository.BlacklistedTokenRepository;
import com.stockbroker.backend.repository.UserRepository;
import com.stockbroker.backend.security.CustomUserPrincipal;
import com.stockbroker.backend.security.JwtService;
import com.stockbroker.backend.service.AuditLogService;
import com.stockbroker.backend.service.AuthenticationService;
import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * SRS Appendix D: 5 consecutive failed attempts locks the account for a
 * fixed 15 minutes (the progressive 15/30/1440-minute schedule is
 * simplified to a single fixed window - documented simplification).
 */
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_MINUTES = 15;

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final AuditLogService auditLogService;

    public AuthenticationServiceImpl(AuthenticationManager authenticationManager,
                                     JwtService jwtService,
                                     UserRepository userRepository,
                                     BlacklistedTokenRepository blacklistedTokenRepository,
                                     AuditLogService auditLogService) {

        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.blacklistedTokenRepository = blacklistedTokenRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new AccountLockedException(
                    "Account locked until " + user.getLockedUntil()
                            + " due to repeated failed login attempts");
        }

        try {

            Authentication authentication = authenticationManager.authenticate(

                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
            userRepository.save(user);

            CustomUserPrincipal principal =
                    (CustomUserPrincipal) authentication.getPrincipal();

            String token = jwtService.generateToken(principal);

            auditLogService.record("LOGIN_SUCCESS", "User", user.getId().toString(), null);

            LoginResponse response = new LoginResponse();

            response.setMessage("Login Successful");
            response.setToken(token);
            response.setEmail(principal.getUsername());
            response.setRole(principal.getRole());

            return response;

        } catch (BadCredentialsException ex) {

            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);

            if (attempts >= MAX_FAILED_ATTEMPTS) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCKOUT_MINUTES));
            }

            userRepository.save(user);

            auditLogService.record("LOGIN_FAILURE", "User", user.getId().toString(),
                    "failed attempt " + attempts);

            throw ex;
        }
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
    public String logout(String token) {

        if (token != null && !token.isBlank()) {

            String jti = jwtService.extractJti(token);

            if (jti != null) {

                Date expiration = jwtService.extractClaim(token, Claims::getExpiration);

                BlacklistedToken blacklisted = new BlacklistedToken();
                blacklisted.setJti(jti);
                blacklisted.setExpiresAt(
                        LocalDateTime.ofInstant(expiration.toInstant(), ZoneId.systemDefault()));

                blacklistedTokenRepository.save(blacklisted);
            }
        }

        auditLogService.record("LOGOUT", "User", null, null);

        return "Logged out successfully. The token has been invalidated.";
    }
}
