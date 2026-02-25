package com.personal.interview.global.security;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.personal.interview.global.config.properties.JwtProperties;
import com.personal.interview.global.security.dto.TokenWithExpiry;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {
    private final SecretKey secretKey;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;
    private final long refreshGracePeriodSeconds;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secretKey().getBytes());
        this.accessExpirationMs = jwtProperties.accessExpirationMs();
        this.refreshExpirationMs = jwtProperties.refreshExpirationMs();
        this.refreshGracePeriodSeconds = jwtProperties.refreshGracePeriodSeconds();
    }

    public String createAccessToken(Long userId, String role) {
        return createToken(userId, role, accessExpirationMs);
    }

    public String createRefreshToken(Long userId, String role) {
        return createToken(userId, role, refreshExpirationMs);
    }

    /**
     * Refresh Token과 만료시간을 함께 반환합니다.
     * 토큰 생성 시점의 만료시간을 단일 진실 소스(Single Version of Truth)로 제공합니다.
     */
    public TokenWithExpiry createRefreshTokenWithExpiry(Long userId, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshExpirationMs);
        String token = buildToken(userId, role, now, expiry);
        LocalDateTime expiresAt = LocalDateTime.ofInstant(expiry.toInstant(), ZoneId.systemDefault());
        return new TokenWithExpiry(token, expiresAt);
    }

    public long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }

    public long getRefreshGracePeriodSeconds() {
        return refreshGracePeriodSeconds;
    }

    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        String userId = claims.getSubject();
        String role = claims.get("role", String.class);

        return new UsernamePasswordAuthenticationToken(
                userId,
                null,
                Collections.singletonList(new SimpleGrantedAuthority(role)));
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Long getUserId(String token) {
        Claims claims = parseClaims(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 만료된 토큰에서도 userId를 추출합니다.
     * RTR 탈취 감지 시, 만료된 Refresh Token에서 userId를 추출하여 해당 사용자의 모든 세션을 무효화하기 위해 사용됩니다.
     */
    public Long getUserIdFromExpiredToken(String token) {
        try {
            return getUserId(token);
        } catch (ExpiredJwtException e) {
            return Long.parseLong(e.getClaims().getSubject());
        }
    }

    private String createToken(Long userId, String role, long expirationMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        return buildToken(userId, role, now, expiry);
    }

    private String buildToken(Long userId, String role, Date issuedAt, Date expiry) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role)
                .issuedAt(issuedAt)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
