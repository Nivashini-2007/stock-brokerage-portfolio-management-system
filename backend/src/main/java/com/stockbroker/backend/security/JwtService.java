package com.stockbroker.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtService {

    private final String secretKey;
    private final long clientExpirationMs;
    private final long staffExpirationMs;
    private final long adminExpirationMs;

    public JwtService(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration.client-ms}") long clientExpirationMs,
            @Value("${jwt.expiration.staff-ms}") long staffExpirationMs,
            @Value("${jwt.expiration.admin-ms}") long adminExpirationMs) {

        this.secretKey = secretKey;
        this.clientExpirationMs = clientExpirationMs;
        this.staffExpirationMs = staffExpirationMs;
        this.adminExpirationMs = adminExpirationMs;
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractJti(String token) {
        return extractClaim(token, Claims::getId);
    }

    public <T> T extractClaim(String token,
                              Function<Claims, T> claimsResolver) {

        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {

        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Token validity per SRS Appendix D: 8h for regular clients, 12h for
     * staff (dealer/analyst/compliance/risk manager), 24h for admins.
     */
    private long expirationFor(UserDetails userDetails) {

        if (userDetails instanceof CustomUserPrincipal principal) {

            String role = principal.getRole();

            if ("ADMIN".equals(role)) {
                return adminExpirationMs;
            }

            if (!"CLIENT".equals(role)) {
                return staffExpirationMs;
            }
        }

        return clientExpirationMs;
    }

    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails) {

        return Jwts.builder()
                .setClaims(extraClaims)
                .setId(UUID.randomUUID().toString())
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis()
                                + expirationFor(userDetails)))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(
            String token,
            UserDetails userDetails) {

        final String username = extractUsername(token);

        return username.equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {

        return extractExpiration(token)
                .before(new Date());
    }

    private Date extractExpiration(String token) {

        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {

        byte[] keyBytes =
                Decoders.BASE64.decode(secretKey);

        return Keys.hmacShaKeyFor(keyBytes);
    }
}
