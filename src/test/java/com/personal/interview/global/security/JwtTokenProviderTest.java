package com.personal.interview.global.security;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import com.personal.interview.global.config.properties.JwtProperties;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    private static final String SECRET_KEY = "myTestSecretKeyForJwtUnitTestPurposeOnly12345678901234567890";
    private static final long ACCESS_EXPIRATION_MS = 1800000L; // 30분
    private static final long REFRESH_EXPIRATION_MS = 604800000L; // 7일

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties(SECRET_KEY, ACCESS_EXPIRATION_MS, REFRESH_EXPIRATION_MS, 10L);
        jwtTokenProvider = new JwtTokenProvider(jwtProperties);
    }

    @Nested
    @DisplayName("토큰 생성")
    class CreateToken {

        @Test
        @DisplayName("Access Token을 생성하면 유효한 JWT 문자열을 반환한다")
        void createAccessToken() {
            String token = jwtTokenProvider.createAccessToken(1L, "ROLE_USER");

            assertThat(token).isNotBlank();
            assertThat(token.split("\\.")).hasSize(3); // header.payload.signature
        }

        @Test
        @DisplayName("Refresh Token을 생성하면 유효한 JWT 문자열을 반환한다")
        void createRefreshToken() {
            String token = jwtTokenProvider.createRefreshToken(1L, "ROLE_USER");

            assertThat(token).isNotBlank();
            assertThat(token.split("\\.")).hasSize(3);
        }
    }

    @Nested
    @DisplayName("토큰 검증")
    class ValidateToken {

        @Test
        @DisplayName("정상 토큰은 검증에 성공한다")
        void validateToken_Success() {
            String token = jwtTokenProvider.createAccessToken(1L, "ROLE_USER");

            assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        }

        @Test
        @DisplayName("위변조된 토큰은 검증에 실패한다")
        void validateToken_Tampered() {
            String token = jwtTokenProvider.createAccessToken(1L, "ROLE_USER");
            String tamperedToken = token + "tampered";

            assertThat(jwtTokenProvider.validateToken(tamperedToken)).isFalse();
        }

        @Test
        @DisplayName("만료된 토큰은 검증에 실패한다")
        void validateToken_Expired() {
            // 만료 시간이 -1ms인 토큰 생성용 Provider
            JwtProperties expiredProps = new JwtProperties(SECRET_KEY, -1L, -1L, 10L);
            JwtTokenProvider expiredProvider = new JwtTokenProvider(expiredProps);

            String expiredToken = expiredProvider.createAccessToken(1L, "ROLE_USER");

            assertThat(jwtTokenProvider.validateToken(expiredToken)).isFalse();
        }

        @Test
        @DisplayName("null 토큰은 검증에 실패한다")
        void validateToken_Null() {
            assertThat(jwtTokenProvider.validateToken(null)).isFalse();
        }

        @Test
        @DisplayName("빈 문자열 토큰은 검증에 실패한다")
        void validateToken_Empty() {
            assertThat(jwtTokenProvider.validateToken("")).isFalse();
        }

        @Test
        @DisplayName("다른 Secret Key로 생성된 토큰은 검증에 실패한다")
        void validateToken_DifferentKey() {
            JwtProperties otherProps = new JwtProperties(
                    "anotherSecretKeyThatIsDifferentFromOriginal12345678901234567890",
                    ACCESS_EXPIRATION_MS, REFRESH_EXPIRATION_MS, 10L);
            JwtTokenProvider otherProvider = new JwtTokenProvider(otherProps);

            String tokenFromOtherKey = otherProvider.createAccessToken(1L, "ROLE_USER");

            assertThat(jwtTokenProvider.validateToken(tokenFromOtherKey)).isFalse();
        }
    }

    @Nested
    @DisplayName("토큰에서 정보 추출")
    class ExtractInfo {

        @Test
        @DisplayName("토큰에서 userId를 추출한다")
        void getUserId() {
            Long expectedUserId = 42L;
            String token = jwtTokenProvider.createAccessToken(expectedUserId, "ROLE_USER");

            Long userId = jwtTokenProvider.getUserId(token);

            assertThat(userId).isEqualTo(expectedUserId);
        }

        @Test
        @DisplayName("토큰에서 Authentication 객체를 추출하면 userId와 role이 포함되어 있다")
        void getAuthentication() {
            Long expectedUserId = 42L;
            String expectedRole = "ROLE_ADMIN";
            String token = jwtTokenProvider.createAccessToken(expectedUserId, expectedRole);

            Authentication auth = jwtTokenProvider.getAuthentication(token);

            assertThat(auth.getName()).isEqualTo(String.valueOf(expectedUserId));
            assertThat(auth.getAuthorities()).hasSize(1);
            assertThat(auth.getAuthorities().iterator().next().getAuthority()).isEqualTo(expectedRole);
        }

        @Test
        @DisplayName("만료된 토큰에서도 userId를 추출할 수 있다")
        void getUserIdFromExpiredToken() {
            Long expectedUserId = 99L;

            JwtProperties expiredProps = new JwtProperties(SECRET_KEY, -1L, -1L, 10L);
            JwtTokenProvider expiredProvider = new JwtTokenProvider(expiredProps);
            String expiredToken = expiredProvider.createAccessToken(expectedUserId, "ROLE_USER");

            // 만료된 토큰이지만 userId 추출 가능
            Long userId = jwtTokenProvider.getUserIdFromExpiredToken(expiredToken);

            assertThat(userId).isEqualTo(expectedUserId);
        }
    }

    @Nested
    @DisplayName("만료 시간 설정")
    class ExpirationConfig {

        @Test
        @DisplayName("getRefreshExpirationMs는 설정된 값을 반환한다")
        void getRefreshExpirationMs() {
            assertThat(jwtTokenProvider.getRefreshExpirationMs()).isEqualTo(REFRESH_EXPIRATION_MS);
        }
    }
}
