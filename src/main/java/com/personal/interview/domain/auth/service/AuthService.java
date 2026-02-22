package com.personal.interview.domain.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

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

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRefreshTokenRepository userRefreshTokenRepository;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public TokenResponse refresh(RefreshTokenRequest request) {
        String requestToken = request.refreshToken();

        var userId = validateTokenAndGetUserId(requestToken);

        validateStoredToken(userId, requestToken);

        return rotateAndCreateTokens(userId, requestToken);
    }

    private UserId validateTokenAndGetUserId(String requestToken) {
        if (!jwtTokenProvider.validateToken(requestToken)) {
            handleSuspiciousToken(requestToken);

            throw DomainException.create(ErrorCode.REFRESH_TOKEN_REUSE_DETECTED);
        }
        return new UserId(jwtTokenProvider.getUserId(requestToken));
    }

    private void validateStoredToken(UserId userId, String requestToken) {
        UserRefreshToken storedToken = userRefreshTokenRepository.findByUserId(userId)
                .orElseThrow(() -> DomainException.create(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        if (!storedToken.getRefreshToken().equals(hashToken(requestToken))) {
            userRefreshTokenRepository.deleteByUserId(userId);

            throw DomainException.create(ErrorCode.REFRESH_TOKEN_REUSE_DETECTED);
        }

        if (storedToken.isExpired()) {
            userRefreshTokenRepository.deleteByUserId(userId);
            throw DomainException.create(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }
    }

    private TokenResponse rotateAndCreateTokens(UserId userId, String requestToken) {
        String role = jwtTokenProvider.getAuthentication(requestToken)
                .getAuthorities().stream()
                .findFirst()
                .map(Object::toString)
                .orElse("ROLE_USER");

        String newAccessToken = jwtTokenProvider.createAccessToken(userId.longValue(), role);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(userId.longValue(), role);

        refreshTokenService.rotateRefreshToken(userId, newRefreshToken);

        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    /**
     * 만료/위변조 토큰에서 userId를 추출하여 해당 사용자의 모든 세션을 무효화합니다.
     */
    private void handleSuspiciousToken(String token) {
        try {
            Long userId = jwtTokenProvider.getUserIdFromExpiredToken(token);

            userRefreshTokenRepository.deleteByUserId(new UserId(userId));
        } catch (Exception ignored) {
            // userId 추출 불가능한 완전 손상 토큰은 무시
        }
    }

    /**
     * Refresh Token을 SHA-256으로 해싱합니다.
     */
    public static String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 알고리즘을 찾을 수 없습니다.", e);
        }
    }
}
