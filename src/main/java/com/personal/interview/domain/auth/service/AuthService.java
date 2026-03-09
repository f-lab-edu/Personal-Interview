package com.personal.interview.domain.auth.service;

import static com.personal.interview.domain.user.entity.vo.UserRole.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.personal.interview.domain.auth.controller.dto.RefreshTokenRequest;
import com.personal.interview.domain.auth.controller.dto.TokenResponse;
import com.personal.interview.domain.auth.entity.UserRefreshToken;
import com.personal.interview.domain.auth.repository.UserRefreshTokenRepository;
import com.personal.interview.domain.user.entity.UserId;
import com.personal.interview.global.exception.DomainException;
import com.personal.interview.global.exception.ErrorCode;
import com.personal.interview.global.security.JwtTokenProvider;
import com.personal.interview.global.security.service.RefreshTokenService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRefreshTokenRepository userRefreshTokenRepository;
    private final RefreshTokenService refreshTokenService;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Transactional
    public TokenResponse refresh(RefreshTokenRequest request) {
        String requestToken = request.refreshToken();

        var userId = validateTokenAndGetUserId(requestToken);

        UserRefreshToken storedToken = userRefreshTokenRepository.findByUserId(userId)
                .orElseThrow(() -> DomainException.create(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        String requestTokenHash = hashToken(requestToken, storedToken.getSalt());
        boolean tokenMatches = storedToken.getRefreshToken().equals(requestTokenHash);

        // 토큰 해시 불일치 → grace period 내이면 동시 요청으로 판단
        if (!tokenMatches) {
            if (storedToken.isWithinGracePeriod(jwtTokenProvider.getRefreshGracePeriodSeconds())) {
                log.info("Grace Period 내 동시 요청 감지 (userId: {}). 회전 없이 새 Access Token만 발급합니다.", userId);

                String accessToken = jwtTokenProvider.createAccessToken(userId.longValue(), extractRole(requestToken));

                return new TokenResponse(accessToken, requestToken);
            }

            userRefreshTokenRepository.deleteByUserId(userId);
            throw DomainException.create(ErrorCode.REFRESH_TOKEN_REUSE_DETECTED);
        }

        if (storedToken.isExpired()) {
            userRefreshTokenRepository.deleteByUserId(userId);
            throw DomainException.create(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        return rotateAndCreateTokens(userId, requestToken);
    }

    private UserId validateTokenAndGetUserId(String requestToken) {
        if (!jwtTokenProvider.validateToken(requestToken)) {
            handleSuspiciousToken(requestToken);

            throw DomainException.create(ErrorCode.REFRESH_TOKEN_REUSE_DETECTED);
        }
        return new UserId(jwtTokenProvider.getUserId(requestToken));
    }

    private TokenResponse rotateAndCreateTokens(UserId userId, String requestToken) {
        String role = extractRole(requestToken);

        String newAccessToken = jwtTokenProvider.createAccessToken(userId.longValue(), role);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(userId.longValue(), role);

        try {
            refreshTokenService.rotateRefreshToken(userId, newRefreshToken);
        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("RTR 낙관적 락 충돌 (userId: {}). Grace Period 내 요청으로 처리합니다.", userId);

            return new TokenResponse(newAccessToken, requestToken);
        }

        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    private String extractRole(String token) {
        return jwtTokenProvider.getAuthentication(token)
                .getAuthorities().stream()
                .findFirst()
                .map(Object::toString)
                .orElse(ROLE_DRAFT.name());
    }

    private void handleSuspiciousToken(String token) {
        try {
            Long userId = jwtTokenProvider.getUserIdFromExpiredToken(token);

            userRefreshTokenRepository.deleteByUserId(new UserId(userId));
        } catch (Exception ignored) {
        }
    }

    public static String hashToken(String token, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest((salt + token).getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 알고리즘을 찾을 수 없습니다.", e);
        }
    }

    public static String generateSalt() {
        byte[] salt = new byte[16];

        SECURE_RANDOM.nextBytes(salt);

        return HexFormat.of().formatHex(salt);
    }
}
