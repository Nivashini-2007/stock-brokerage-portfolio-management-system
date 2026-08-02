package com.stockbroker.backend.security;

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

    public JwtAuthenticationFilter(
            JwtService jwtService,
            CustomUserDetailsService userDetailsService) {

        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        try {

            System.out.println("\n========== JWT FILTER ==========");
            System.out.println("Request URI : " + request.getRequestURI());

            final String authHeader = request.getHeader("Authorization");

            System.out.println("Authorization Header : " + authHeader);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {

                System.out.println("No Bearer Token Found");
                filterChain.doFilter(request, response);
                return;
            }

            String jwt = authHeader.substring(7);

            System.out.println("JWT Token : " + jwt);

            String username = jwtService.extractUsername(jwt);

            System.out.println("Extracted Username : " + username);

            if (username != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(username);

                System.out.println("User Loaded : " + userDetails.getUsername());
                System.out.println("Authorities : " + userDetails.getAuthorities());

                boolean valid =
                        jwtService.isTokenValid(jwt, userDetails);

                System.out.println("Token Valid : " + valid);

                if (valid) {

                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities());

                    authenticationToken.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request));

                    SecurityContextHolder.getContext()
                            .setAuthentication(authenticationToken);

                    System.out.println("Authentication Stored Successfully");
                    System.out.println("Authentication : "
                            + SecurityContextHolder.getContext().getAuthentication());

                } else {

                    System.out.println("Invalid JWT Token");
                }

            } else {

                System.out.println("Username is null OR Authentication already exists");
            }

        } catch (Exception ex) {

            System.out.println("JWT FILTER ERROR");
            ex.printStackTrace();
        }

        filterChain.doFilter(request, response);
    }
}