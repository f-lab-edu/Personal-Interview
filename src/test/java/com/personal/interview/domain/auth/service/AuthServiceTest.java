package com.personal.interview.domain.auth.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.personal.interview.domain.auth.controller.dto.RefreshTokenRequest;
import com.personal.interview.domain.auth.controller.dto.TokenResponse;
import com.personal.interview.domain.auth.entity.UserRefreshToken;
import com.personal.interview.domain.auth.repository.UserRefreshTokenRepository;
import com.personal.interview.domain.user.entity.UserId;
import com.personal.interview.global.exception.DomainException;
import com.personal.interview.global.exception.ErrorCode;
import com.personal.interview.global.security.JwtTokenProvider;
import com.personal.interview.global.security.service.RefreshTokenService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserRefreshTokenRepository userRefreshTokenRepository;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Nested
    @DisplayName("Refresh Token 재발급")
    class Refresh {

        @Test
        @DisplayName("정상적인 Refresh Token으로 재발급 시, 새로운 Access/Refresh Token을 반환한다")
        void refresh_Success() {
            // given
            String validToken = "valid.refresh.token";
            UserId userId = new UserId(1L);
            String hashedToken = AuthService.hashToken(validToken);

            given(jwtTokenProvider.validateToken(validToken)).willReturn(true);
            given(jwtTokenProvider.getUserId(validToken)).willReturn(userId.longValue());

            UserRefreshToken storedToken = UserRefreshToken.create(
                    userId, hashedToken, LocalDateTime.now().plusDays(7));
            given(userRefreshTokenRepository.findByUserId(userId)).willReturn(Optional.of(storedToken));

            Authentication auth = new UsernamePasswordAuthenticationToken(
                    String.valueOf(userId), null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
            given(jwtTokenProvider.getAuthentication(validToken)).willReturn(auth);

            given(jwtTokenProvider.createAccessToken(eq(userId.longValue()), eq("ROLE_USER")))
                    .willReturn("new.access.token");
            given(jwtTokenProvider.createRefreshToken(eq(userId.longValue()), eq("ROLE_USER")))
                    .willReturn("new.refresh.token");

            // when
            TokenResponse response = authService.refresh(new RefreshTokenRequest(validToken));

            // then
            assertThat(response.accessToken()).isEqualTo("new.access.token");
            assertThat(response.refreshToken()).isEqualTo("new.refresh.token");

            // RefreshTokenService.rotateRefreshToken을 통해 새로운 토큰 생성이 위임되었는지 확인
            verify(refreshTokenService).rotateRefreshToken(userId, "new.refresh.token");
        }

        @Test
        @DisplayName("만료된 Refresh Token 요청 시, 해당 사용자의 모든 세션을 무효화하고 예외를 발생시킨다")
        void refresh_ExpiredToken_InvalidatesAllSessions() {
            // given
            String expiredToken = "expired.refresh.token";
            UserId userId = new UserId(1L);

            given(jwtTokenProvider.validateToken(expiredToken)).willReturn(false);
            given(jwtTokenProvider.getUserIdFromExpiredToken(expiredToken)).willReturn(userId.longValue());

            // when & then
            assertThatThrownBy(() -> authService.refresh(new RefreshTokenRequest(expiredToken)))
                    .isInstanceOf(DomainException.class)
                    .satisfies(ex -> {
                        DomainException domainEx = (DomainException) ex;
                        assertThat(domainEx.getErrorCode()).isEqualTo(ErrorCode.REFRESH_TOKEN_REUSE_DETECTED);
                    });

            // 전체 세션 무효화 확인
            then(userRefreshTokenRepository).should().deleteByUserId(userId);
        }

        @Test
        @DisplayName("이미 교체된(재사용된) Refresh Token 요청 시, 전체 세션을 무효화하고 예외를 발생시킨다")
        void refresh_ReusedToken_InvalidatesAllSessions() {
            // given
            String reusedToken = "reused.refresh.token";
            UserId userId = new UserId(1L);

            given(jwtTokenProvider.validateToken(reusedToken)).willReturn(true);
            given(jwtTokenProvider.getUserId(reusedToken)).willReturn(userId.longValue());

            // DB에는 다른 해시값이 저장되어 있음 (이미 RTR로 교체된 상태)
            String differentHash = AuthService.hashToken("different.token");
            UserRefreshToken storedToken = UserRefreshToken.create(
                    userId, differentHash, LocalDateTime.now().plusDays(7));
            given(userRefreshTokenRepository.findByUserId(userId)).willReturn(Optional.of(storedToken));

            // when & then
            assertThatThrownBy(() -> authService.refresh(new RefreshTokenRequest(reusedToken)))
                    .isInstanceOf(DomainException.class)
                    .satisfies(ex -> {
                        DomainException domainEx = (DomainException) ex;
                        assertThat(domainEx.getErrorCode()).isEqualTo(ErrorCode.REFRESH_TOKEN_REUSE_DETECTED);
                    });

            then(userRefreshTokenRepository).should().deleteByUserId(userId);
        }

        @Test
        @DisplayName("DB에 Refresh Token이 존재하지 않으면 예외를 발생시킨다")
        void refresh_NotFoundInDb() {
            // given
            String validToken = "valid.refresh.token";
            UserId userId = new UserId(1L);

            given(jwtTokenProvider.validateToken(validToken)).willReturn(true);
            given(jwtTokenProvider.getUserId(validToken)).willReturn(userId.longValue());
            given(userRefreshTokenRepository.findByUserId(userId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.refresh(new RefreshTokenRequest(validToken)))
                    .isInstanceOf(DomainException.class)
                    .satisfies(ex -> {
                        DomainException domainEx = (DomainException) ex;
                        assertThat(domainEx.getErrorCode()).isEqualTo(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
                    });
        }

        @Test
        @DisplayName("DB의 Refresh Token이 만료된 경우, 세션을 무효화하고 예외를 발생시킨다")
        void refresh_StoredTokenExpired_Deletes() {
            // given
            String validToken = "valid.refresh.token";
            UserId userId = new UserId(1L);
            String hashedToken = AuthService.hashToken(validToken);

            given(jwtTokenProvider.validateToken(validToken)).willReturn(true);
            given(jwtTokenProvider.getUserId(validToken)).willReturn(userId.longValue());

            // DB에 만료된 토큰이 저장된 상태
            UserRefreshToken expiredStoredToken = UserRefreshToken.create(
                    userId, hashedToken, LocalDateTime.now().minusDays(1));
            given(userRefreshTokenRepository.findByUserId(userId)).willReturn(Optional.of(expiredStoredToken));

            // when & then
            assertThatThrownBy(() -> authService.refresh(new RefreshTokenRequest(validToken)))
                    .isInstanceOf(DomainException.class)
                    .satisfies(ex -> {
                        DomainException domainEx = (DomainException) ex;
                        assertThat(domainEx.getErrorCode()).isEqualTo(ErrorCode.REFRESH_TOKEN_EXPIRED);
                    });

            then(userRefreshTokenRepository).should().deleteByUserId(userId);
        }
    }

    @Nested
    @DisplayName("SHA-256 해싱")
    class HashToken {

        @Test
        @DisplayName("동일한 토큰 입력 시 항상 동일한 해시값을 반환한다")
        void hashToken_Deterministic() {
            String token = "test.token.value";
            String hash1 = AuthService.hashToken(token);
            String hash2 = AuthService.hashToken(token);

            assertThat(hash1).isEqualTo(hash2);
        }

        @Test
        @DisplayName("다른 토큰 입력 시 다른 해시값을 반환한다")
        void hashToken_DifferentInputs() {
            String hash1 = AuthService.hashToken("token1");
            String hash2 = AuthService.hashToken("token2");

            assertThat(hash1).isNotEqualTo(hash2);
        }

        @Test
        @DisplayName("해시값은 64자의 16진수 문자열이다 (SHA-256)")
        void hashToken_Format() {
            String hash = AuthService.hashToken("test.token");

            assertThat(hash).hasSize(64);
            assertThat(hash).matches("[0-9a-f]+");
        }
    }
}
