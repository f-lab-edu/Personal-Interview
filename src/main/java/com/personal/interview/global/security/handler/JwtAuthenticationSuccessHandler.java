package com.personal.interview.global.security.handler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import tools.jackson.databind.ObjectMapper;
import com.personal.interview.domain.auth.controller.dto.TokenResponse;
import com.personal.interview.domain.auth.entity.UserRefreshToken;
import com.personal.interview.domain.auth.repository.UserRefreshTokenRepository;
import com.personal.interview.domain.auth.service.AuthService;
import com.personal.interview.domain.user.entity.UserId;
import com.personal.interview.domain.user.entity.vo.UserRole;
import com.personal.interview.global.security.JwtTokenProvider;
import com.personal.interview.global.security.service.RefreshTokenService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * 로그인 인증 성공처리 handler
 */
@RequiredArgsConstructor
public class JwtAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;
    private final RefreshTokenService refreshTokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {
        final Long userId = Long.parseLong(authentication.getName());
        final String role = getRole(authentication);

        final String accessToken = jwtTokenProvider.createAccessToken(userId, role);
        final String refreshToken = jwtTokenProvider.createRefreshToken(userId, role);

        refreshTokenService.rotateRefreshToken(new UserId(userId), refreshToken);

        TokenResponse tokenResponse = new TokenResponse(accessToken, refreshToken);
        writeJsonResponse(response, tokenResponse);
    }

    private String getRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse(UserRole.ROLE_DRAFT.name());
    }

    private void writeJsonResponse(HttpServletResponse response, TokenResponse tokenResponse) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), tokenResponse);
    }
}
