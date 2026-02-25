package com.personal.interview.domain.auth.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.lang.reflect.Field;
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
import org.springframework.orm.ObjectOptimisticLockingFailureException;
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

    private static final String TEST_SALT = "0123456789abcdef0123456789abcdef";

    // ── 헬퍼: createdAt을 리플렉션으로 조작 ──
    private void setCreatedAt(UserRefreshToken token, LocalDateTime createdAt) {
        try {
            Field field = UserRefreshToken.class.getDeclaredField("createdAt");
            field.setAccessible(true);
            field.set(token, createdAt);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Authentication mockAuth(UserId userId) {
        return new UsernamePasswordAuthenticationToken(
                String.valueOf(userId), null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Nested
    @DisplayName("Refresh Token 재발급 - 정상 흐름")
    class RefreshSuccess {

        @Test
        @DisplayName("정상 Refresh Token으로 재발급 시, 새 Access/Refresh Token을 반환하고 RTR을 수행한다")
        void refresh_Success() {
            // given
            String validToken = "valid.refresh.token";
            UserId userId = new UserId(1L);
            String hashedToken = AuthService.hashToken(validToken, TEST_SALT);

            given(jwtTokenProvider.validateToken(validToken)).willReturn(true);
            given(jwtTokenProvider.getUserId(validToken)).willReturn(userId.longValue());

            UserRefreshToken storedToken = UserRefreshToken.create(
                    userId, hashedToken, TEST_SALT, LocalDateTime.now().plusDays(7));
            given(userRefreshTokenRepository.findByUserId(userId)).willReturn(Optional.of(storedToken));
            given(jwtTokenProvider.getAuthentication(validToken)).willReturn(mockAuth(userId));
            given(jwtTokenProvider.createAccessToken(eq(userId.longValue()), eq("ROLE_USER")))
                    .willReturn("new.access.token");
            given(jwtTokenProvider.createRefreshToken(eq(userId.longValue()), eq("ROLE_USER")))
                    .willReturn("new.refresh.token");

            // when
            TokenResponse response = authService.refresh(new RefreshTokenRequest(validToken));

            // then
            assertThat(response.accessToken()).isEqualTo("new.access.token");
            assertThat(response.refreshToken()).isEqualTo("new.refresh.token");
            verify(refreshTokenService).rotateRefreshToken(userId, "new.refresh.token");
        }
    }

    @Nested
    @DisplayName("Refresh Token 재발급 - Grace Period (동시성 제어)")
    class GracePeriod {

        @Test
        @DisplayName("Grace Period 내 동시 요청: 토큰 해시 불일치이지만 rotatedAt이 유예 기간 내이면 회전 없이 제출 토큰을 반환한다")
        void refresh_GracePeriodHit_ReturnsSubmittedToken() {
            // given
            String oldToken = "old.refresh.token";
            UserId userId = new UserId(1L);

            given(jwtTokenProvider.validateToken(oldToken)).willReturn(true);
            given(jwtTokenProvider.getUserId(oldToken)).willReturn(userId.longValue());

            // DB에는 선행 스레드가 이미 회전한 다른 해시가 저장됨 (oldToken과 불일치)
            String differentHash = AuthService.hashToken("new.rotated.token", TEST_SALT);
            UserRefreshToken storedToken = UserRefreshToken.create(
                    userId, differentHash, TEST_SALT, LocalDateTime.now().plusDays(7));
            // createdAt이 방금 (grace period 내) → create 시 now()로 설정되므로 그대로 사용

            given(userRefreshTokenRepository.findByUserId(userId)).willReturn(Optional.of(storedToken));
            given(jwtTokenProvider.getRefreshGracePeriodSeconds()).willReturn(10L);
            given(jwtTokenProvider.getAuthentication(oldToken)).willReturn(mockAuth(userId));
            given(jwtTokenProvider.createAccessToken(eq(userId.longValue()), eq("ROLE_USER")))
                    .willReturn("grace.access.token");

            // when
            TokenResponse response = authService.refresh(new RefreshTokenRequest(oldToken));

            // then - 회전 없이 새 Access Token + 제출된 Refresh Token 반환
            assertThat(response.accessToken()).isEqualTo("grace.access.token");
            assertThat(response.refreshToken()).isEqualTo(oldToken);
            verify(refreshTokenService, never()).rotateRefreshToken(any(), anyString());
        }

        @Test
        @DisplayName("Grace Period 만료 후 이전 토큰 재사용: 탈취로 감지하여 전체 세션을 무효화한다")
        void refresh_GracePeriodExpired_DetectsReuse() {
            // given
            String reusedToken = "reused.refresh.token";
            UserId userId = new UserId(1L);

            given(jwtTokenProvider.validateToken(reusedToken)).willReturn(true);
            given(jwtTokenProvider.getUserId(reusedToken)).willReturn(userId.longValue());

            // DB에는 다른 해시가 저장 (이미 RTR로 교체된 상태)
            String differentHash = AuthService.hashToken("different.token", TEST_SALT);
            UserRefreshToken storedToken = UserRefreshToken.create(
                    userId, differentHash, TEST_SALT, LocalDateTime.now().plusDays(7));
            // createdAt을 5분 전으로 (grace period 밖)
            setCreatedAt(storedToken, LocalDateTime.now().minusMinutes(5));

            given(userRefreshTokenRepository.findByUserId(userId)).willReturn(Optional.of(storedToken));
            given(jwtTokenProvider.getRefreshGracePeriodSeconds()).willReturn(10L);

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
        @DisplayName("OptimisticLock 충돌 시, 새 Access Token과 제출된 Refresh Token을 반환한다")
        void refresh_OptimisticLockConflict_ReturnsSubmittedToken() {
            // given
            String validToken = "valid.refresh.token";
            UserId userId = new UserId(1L);
            String hashedToken = AuthService.hashToken(validToken, TEST_SALT);

            given(jwtTokenProvider.validateToken(validToken)).willReturn(true);
            given(jwtTokenProvider.getUserId(validToken)).willReturn(userId.longValue());

            UserRefreshToken storedToken = UserRefreshToken.create(
                    userId, hashedToken, TEST_SALT, LocalDateTime.now().plusDays(7));
            given(userRefreshTokenRepository.findByUserId(userId)).willReturn(Optional.of(storedToken));
            given(jwtTokenProvider.getAuthentication(validToken)).willReturn(mockAuth(userId));
            given(jwtTokenProvider.createAccessToken(eq(userId.longValue()), eq("ROLE_USER")))
                    .willReturn("new.access.token");
            given(jwtTokenProvider.createRefreshToken(eq(userId.longValue()), eq("ROLE_USER")))
                    .willReturn("new.refresh.token");

            willThrow(new ObjectOptimisticLockingFailureException(UserRefreshToken.class.getName(), "conflict"))
                    .given(refreshTokenService).rotateRefreshToken(any(UserId.class), anyString());

            // when
            TokenResponse response = authService.refresh(new RefreshTokenRequest(validToken));

            // then - 새 Access Token + 제출된 Refresh Token (grace period 내 재사용 가능)
            assertThat(response.accessToken()).isEqualTo("new.access.token");
            assertThat(response.refreshToken()).isEqualTo(validToken);
        }
    }

    @Nested
    @DisplayName("Refresh Token 재발급 - 예외 흐름")
    class RefreshException {

        @Test
        @DisplayName("JWT 유효성 검증 실패(만료/위변조) 시, 해당 사용자의 모든 세션을 무효화하고 예외를 발생시킨다")
        void refresh_InvalidJwt_InvalidatesAllSessions() {
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

            then(userRefreshTokenRepository).should().deleteByUserId(userId);
        }

        @Test
        @DisplayName("DB에 Refresh Token이 존재하지 않으면 REFRESH_TOKEN_NOT_FOUND 예외를 발생시킨다")
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
        @DisplayName("DB의 Refresh Token이 만료된 경우, 세션을 무효화하고 REFRESH_TOKEN_EXPIRED 예외를 발생시킨다")
        void refresh_StoredTokenExpired_Deletes() {
            // given
            String validToken = "valid.refresh.token";
            UserId userId = new UserId(1L);
            String hashedToken = AuthService.hashToken(validToken, TEST_SALT);

            given(jwtTokenProvider.validateToken(validToken)).willReturn(true);
            given(jwtTokenProvider.getUserId(validToken)).willReturn(userId.longValue());

            UserRefreshToken expiredStoredToken = UserRefreshToken.create(
                    userId, hashedToken, TEST_SALT, LocalDateTime.now().minusDays(1));
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

        @Test
        @DisplayName("완전히 손상된 토큰(userId 추출 불가)의 경우에도 예외를 정상적으로 발생시킨다")
        void refresh_CompletelyCorruptedToken() {
            // given
            String corruptedToken = "corrupted.token";

            given(jwtTokenProvider.validateToken(corruptedToken)).willReturn(false);
            given(jwtTokenProvider.getUserIdFromExpiredToken(corruptedToken))
                    .willThrow(new RuntimeException("파싱 불가"));

            // when & then
            assertThatThrownBy(() -> authService.refresh(new RefreshTokenRequest(corruptedToken)))
                    .isInstanceOf(DomainException.class)
                    .satisfies(ex -> {
                        DomainException domainEx = (DomainException) ex;
                        assertThat(domainEx.getErrorCode()).isEqualTo(ErrorCode.REFRESH_TOKEN_REUSE_DETECTED);
                    });

            // userId 추출 실패 시 deleteByUserId는 호출되지 않음
            then(userRefreshTokenRepository).should(never()).deleteByUserId(any());
        }
    }

    @Nested
    @DisplayName("SHA-256 + SALT 해싱")
    class HashToken {

        @Test
        @DisplayName("동일한 토큰과 SALT 입력 시 항상 동일한 해시값을 반환한다")
        void hashToken_Deterministic() {
            String token = "test.token.value";
            String salt = "testSalt123";
            String hash1 = AuthService.hashToken(token, salt);
            String hash2 = AuthService.hashToken(token, salt);

            assertThat(hash1).isEqualTo(hash2);
        }

        @Test
        @DisplayName("다른 SALT 입력 시 다른 해시값을 반환한다")
        void hashToken_DifferentSalts() {
            String token = "test.token.value";
            String hash1 = AuthService.hashToken(token, "salt1");
            String hash2 = AuthService.hashToken(token, "salt2");

            assertThat(hash1).isNotEqualTo(hash2);
        }

        @Test
        @DisplayName("해시값은 64자의 16진수 문자열이다 (SHA-256)")
        void hashToken_Format() {
            String hash = AuthService.hashToken("test.token", "testSalt");

            assertThat(hash).hasSize(64);
            assertThat(hash).matches("[0-9a-f]+");
        }

        @Test
        @DisplayName("generateSalt()는 32자의 16진수 문자열을 반환한다")
        void generateSalt_Format() {
            String salt = AuthService.generateSalt();

            assertThat(salt).hasSize(32);
            assertThat(salt).matches("[0-9a-f]+");
        }

        @Test
        @DisplayName("generateSalt()는 매번 다른 값을 반환한다")
        void generateSalt_Unique() {
            String salt1 = AuthService.generateSalt();
            String salt2 = AuthService.generateSalt();

            assertThat(salt1).isNotEqualTo(salt2);
        }
    }
}
