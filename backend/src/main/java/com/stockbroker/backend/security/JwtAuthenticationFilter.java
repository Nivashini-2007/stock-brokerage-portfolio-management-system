package com.stockbroker.backend.security;

import com.stockbroker.backend.repository.BlacklistedTokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            CustomUserDetailsService userDetailsService,
            BlacklistedTokenRepository blacklistedTokenRepository) {

        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.blacklistedTokenRepository = blacklistedTokenRepository;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader =
                request.getHeader("Authorization");

        /*
         * If there is no Authorization header,
         * continue the filter chain.
         *
         * Spring Security will later decide whether
         * the requested endpoint requires authentication.
         */
        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        final String jwt =
                authHeader.substring(7);

        try {

            /*
             * Extract the username/email from JWT.
             */
            String username =
                    jwtService.extractUsername(jwt);

            String jti = jwtService.extractJti(jwt);

            /*
             * Reject tokens invalidated by logout/password-change/security
             * breach (SRS Appendix D "Token Blacklisting").
             */
            if (jti != null && blacklistedTokenRepository.existsByJti(jti)) {
                filterChain.doFilter(request, response);
                return;
            }

            /*
             * Only authenticate if:
             *
             * 1. Username exists
             * 2. SecurityContext does not already contain
             *    an authenticated user
             */
            if (username != null &&
                    SecurityContextHolder
                            .getContext()
                            .getAuthentication() == null) {

                UserDetails userDetails =
                        userDetailsService
                                .loadUserByUsername(username);

                /*
                 * Validate token signature,
                 * username and expiration.
                 */
                if (jwtService.isTokenValid(
                        jwt,
                        userDetails)) {

                    UsernamePasswordAuthenticationToken
                            authenticationToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authenticationToken.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    /*
                     * Store authenticated user
                     * inside Spring Security context.
                     */
                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(
                                    authenticationToken
                            );
                }
            }

        } catch (Exception exception) {

            /*
             * Invalid/expired/malformed JWT.
             *
             * Clear the security context and continue.
             * Protected endpoints will ultimately return 401.
             */
            SecurityContextHolder
                    .clearContext();

            System.out.println(
                    "JWT authentication failed: "
                            + exception.getMessage()
            );
        }

        filterChain.doFilter(request, response);
    }
}