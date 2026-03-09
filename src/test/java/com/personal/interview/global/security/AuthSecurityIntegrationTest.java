package com.personal.interview.global.security;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.JsonNode;
import com.personal.interview.domain.auth.entity.UserRefreshToken;
import com.personal.interview.domain.auth.repository.UserRefreshTokenRepository;
import com.personal.interview.domain.user.entity.UserId;
import com.personal.interview.domain.auth.service.AuthService;
import com.personal.interview.domain.auth.service.EmailVerifySender;
import com.personal.interview.domain.user.entity.User;
import com.personal.interview.domain.user.repository.UserRepository;
import com.personal.interview.domain.user.UserFixture;
import com.personal.interview.global.config.properties.JwtProperties;

import jakarta.persistence.EntityManager;

/**
 * Security 필터 체인 통합 테스트.
 *
 * 실제 Spring Security Filter Chain을 통해 아래 흐름을 End-to-End로 검증합니다:
 * - 로그인 (CustomAuthenticationFilter → SuccessHandler / FailureHandler)
 * - JWT 인증 (JwtAuthenticationFilter → SecurityContext)
 * - 인증 실패 (JwtAuthenticationEntryPoint → 401)
 * - 토큰 재발급 (AuthController → AuthService RTR/해싱)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthSecurityIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private UserRefreshTokenRepository userRefreshTokenRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        @Autowired
        private JwtTokenProvider jwtTokenProvider;

        @Autowired
        private EntityManager em;

        @MockitoBean
        private EmailVerifySender emailVerifySender;

        private User testUser;

        @BeforeEach
        void setUp() {
                // 테스트 사용자 생성
                testUser = UserFixture.createDefaultUser(passwordEncoder);
                userRepository.save(testUser);
                em.flush();
                em.clear();
        }

        @Nested
        @DisplayName("로그인 (POST /api/auth/login)")
        class Login {

                @Test
                @DisplayName("올바른 이메일/비밀번호로 로그인하면 200 OK와 Access/Refresh Token을 반환한다")
                void login_Success() throws Exception {
                        String loginJson = """
                                        {"email": "%s", "password": "%s"}
                                        """.formatted(UserFixture.DEFAULT_EMAIL, UserFixture.DEFAULT_RAW_PASSWORD);

                        MvcResult result = mockMvc.perform(post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(loginJson))
                                        .andDo(print())
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.accessToken").isNotEmpty())
                                        .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                                        .andReturn();

                        UserRefreshToken stored = userRefreshTokenRepository
                                        .findByUserId(new UserId(testUser.getId().longValue()))
                                        .orElseThrow();

                        String responseBody = result.getResponse().getContentAsString();
                        JsonNode json = objectMapper.readTree(responseBody);
                        String rawRefreshToken = json.get("refreshToken").asText();

                        // DB 저장값은 SALT로 해싱된 값이어야 한다
                        assertThat(stored.getRefreshToken()).isEqualTo(
                                        AuthService.hashToken(rawRefreshToken, stored.getSalt()));
                }

                @Test
                @DisplayName("잘못된 비밀번호로 로그인하면 401과 INVALID_CREDENTIALS를 반환한다")
                void login_WrongPassword() throws Exception {
                        String loginJson = """
                                        {"email": "%s", "password": "wrongPassword"}
                                        """.formatted(UserFixture.DEFAULT_EMAIL);

                        mockMvc.perform(post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(loginJson))
                                        .andDo(print())
                                        .andExpect(status().isUnauthorized())
                                        .andExpect(jsonPath("$.code").value("AUTH_005"))
                                        .andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 올바르지 않습니다."));
                }

                @Test
                @DisplayName("존재하지 않는 이메일로 로그인하면 401과 동일한 Generic 에러 메시지를 반환한다")
                void login_UserNotFound_GenericError() throws Exception {
                        String loginJson = """
                                        {"email": "nonexistent@example.com", "password": "password"}
                                        """;

                        mockMvc.perform(post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(loginJson))
                                        .andDo(print())
                                        .andExpect(status().isUnauthorized())
                                        .andExpect(jsonPath("$.code").value("AUTH_005"));
                }
        }

        @Nested
        @DisplayName("JWT 인증 (보호된 리소스 접근)")
        class JwtAuthentication {

                @Test
                @DisplayName("유효한 Access Token으로 보호된 API에 접근하면 인증이 성공한다")
                void accessProtectedResource_WithValidToken() throws Exception {
                        String accessToken = jwtTokenProvider.createAccessToken(
                                        testUser.getId().longValue(), testUser.getRole().name());

                        // 보호된 리소스에 접근 — 인증은 통과하므로 401/403이 아닌 다른 상태코드가 반환되어야 함
                        mockMvc.perform(get("/api/protected/resource")
                                        .header("Authorization", "Bearer " + accessToken))
                                        .andDo(print())
                                        .andExpect(result -> {
                                                int status = result.getResponse().getStatus();
                                                assertThat(status).isNotEqualTo(401);
                                                assertThat(status).isNotEqualTo(403);
                                        });
                }

                @Test
                @DisplayName("토큰 없이 보호된 API에 접근하면 401 INVALID_TOKEN을 반환한다")
                void accessProtectedResource_NoToken() throws Exception {
                        mockMvc.perform(get("/api/protected/resource"))
                                        .andDo(print())
                                        .andExpect(status().isUnauthorized())
                                        .andExpect(jsonPath("$.code").value("AUTH_004"));
                }

                @Test
                @DisplayName("위변조된 토큰으로 접근하면 401 INVALID_TOKEN을 반환한다")
                void accessProtectedResource_TamperedToken() throws Exception {
                        mockMvc.perform(get("/api/protected/resource")
                                        .header("Authorization", "Bearer invalid.token.here"))
                                        .andDo(print())
                                        .andExpect(status().isUnauthorized())
                                        .andExpect(jsonPath("$.code").value("AUTH_004"));
                }

                @Test
                @DisplayName("만료된 토큰으로 접근하면 401 INVALID_TOKEN을 반환한다")
                void accessProtectedResource_ExpiredToken() throws Exception {
                        JwtProperties expiredProps = new JwtProperties(
                                        "myDefaultSecretKeyForDevelopmentPurposeOnly12345678901234567890",
                                        -1L, -1L, 10L);
                        JwtTokenProvider expiredProvider = new JwtTokenProvider(expiredProps);
                        String expiredToken = expiredProvider.createAccessToken(
                                        testUser.getId().longValue(), testUser.getRole().name());

                        mockMvc.perform(get("/api/protected/resource")
                                        .header("Authorization", "Bearer " + expiredToken))
                                        .andDo(print())
                                        .andExpect(status().isUnauthorized())
                                        .andExpect(jsonPath("$.code").value("AUTH_004"));
                }
        }

        @Nested
        @DisplayName("토큰 재발급 (POST /api/auth/refresh)")
        class RefreshToken {

                @Test
                @DisplayName("유효한 Refresh Token으로 재발급하면 새로운 토큰 쌍을 반환한다 (RTR)")
                void refresh_Success() throws Exception {
                        Long userId = testUser.getId().longValue();
                        String role = testUser.getRole().name();

                        // 기존 Refresh Token 발급 및 DB 저장 (SALT + 해싱)
                        String originalRefreshToken = jwtTokenProvider.createRefreshToken(userId, role);
                        String salt = AuthService.generateSalt();
                        String hashedToken = AuthService.hashToken(originalRefreshToken, salt);
                        UserRefreshToken stored = UserRefreshToken.create(new UserId(userId), hashedToken, salt,
                                        LocalDateTime.now().plusDays(7));

                        userRefreshTokenRepository.save(stored);
                        em.flush();
                        em.clear();

                        // 재발급 요청
                        String requestJson = """
                                        {"refreshToken": "%s"}
                                        """.formatted(originalRefreshToken);

                        MvcResult result = mockMvc.perform(post("/api/auth/refresh")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestJson))
                                        .andDo(print())
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.accessToken").isNotEmpty())
                                        .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                                        .andReturn();

                        // 응답의 새 토큰 추출
                        String responseBody = result.getResponse().getContentAsString();
                        JsonNode json = objectMapper.readTree(responseBody);
                        String newRefreshToken = json.get("refreshToken").asText();

                        // DB에 새 해싱된 토큰이 저장되었는지 확인 (새 salt로 해싱됨)
                        UserRefreshToken updated = userRefreshTokenRepository
                                        .findByUserId(new UserId(userId))
                                        .orElseThrow();
                        assertThat(updated.getRefreshToken()).isEqualTo(
                                        AuthService.hashToken(newRefreshToken, updated.getSalt()));
                }

                @Test
                @DisplayName("이미 교체된(재사용된) Refresh Token으로 재발급하면 401이고 전체 세션이 무효화된다")
                void refresh_ReusedToken_InvalidatesAllSessions() throws Exception {
                        Long userId = testUser.getId().longValue();
                        String role = testUser.getRole().name();

                        // 이미 교체된 상태 시뮬레이션:
                        // DB에는 새로운 해시가 저장되었는데, 클라이언트는 이전 토큰으로 요청
                        // 주의: 같은 초에 동일한 claims로 JWT를 생성하면 동일한 문자열이 되므로,
                        // old 토큰은 다른 만료시간을 가진 Provider로 생성하여 서로 다른 값을 보장한다.
                        JwtProperties altProps = new JwtProperties(
                                        "myDefaultSecretKeyForDevelopmentPurposeOnly12345678901234567890",
                                        3600000L, 86400000L, 10L); // 1일 만료 (기본값 7일과 다르므로 다른 JWT 생성)
                        JwtTokenProvider altProvider = new JwtTokenProvider(altProps);
                        String oldRefreshToken = altProvider.createRefreshToken(userId, role);
                        String newRefreshToken = jwtTokenProvider.createRefreshToken(userId, role);

                        // DB에는 새 토큰의 해시가 저장된 상태 (Grace Period 지난 후)
                        String salt = AuthService.generateSalt();
                        UserRefreshToken stored2 = UserRefreshToken.create(new UserId(userId),
                                        AuthService.hashToken(newRefreshToken, salt), salt,
                                        LocalDateTime.now().plusDays(7));
                        // Grace Period 밖이어야 재사용이 탈취로 감지되므로 rotatedAt을 과거로 설정
                        java.lang.reflect.Field createdAtField = UserRefreshToken.class.getDeclaredField("createdAt");
                        createdAtField.setAccessible(true);
                        createdAtField.set(stored2, LocalDateTime.now().minusMinutes(5));
                        userRefreshTokenRepository.save(stored2);
                        em.flush();
                        em.clear();

                        // 이전(old) 토큰으로 재발급 요청 → 탈취 간주
                        String requestJson = """
                                        {"refreshToken": "%s"}
                                        """.formatted(oldRefreshToken);

                        mockMvc.perform(post("/api/auth/refresh")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestJson))
                                        .andDo(print())
                                        .andExpect(status().isUnauthorized())
                                        .andExpect(jsonPath("$.code").value("AUTH_009"));
                        em.flush();
                        em.clear();

                        // 전체 세션 무효화 확인 (DB에서 삭제됨)
                        assertThat(userRefreshTokenRepository.findByUserId(new UserId(userId))).isEmpty();
                }

                @Test
                @DisplayName("만료된 Refresh Token으로 재발급하면 401이고 전체 세션이 무효화된다")
                void refresh_ExpiredToken_InvalidatesAllSessions() throws Exception {
                        Long userId = testUser.getId().longValue();
                        String role = testUser.getRole().name();

                        // 만료된 Refresh Token 생성
                        JwtProperties expiredProps = new JwtProperties(
                                        "myDefaultSecretKeyForDevelopmentPurposeOnly12345678901234567890",
                                        -1L, -1L, 10L);
                        JwtTokenProvider expiredProvider = new JwtTokenProvider(expiredProps);
                        String expiredRefreshToken = expiredProvider.createRefreshToken(userId, role);

                        // DB에 토큰 저장 (만료 감지 전 상태)
                        String salt = AuthService.generateSalt();
                        UserRefreshToken stored3 = UserRefreshToken.create(new UserId(userId),
                                        AuthService.hashToken(expiredRefreshToken, salt), salt,
                                        LocalDateTime.now().plusDays(7));
                        userRefreshTokenRepository.save(stored3);
                        em.flush();
                        em.clear();

                        String requestJson = """
                                        {"refreshToken": "%s"}
                                        """.formatted(expiredRefreshToken);

                        mockMvc.perform(post("/api/auth/refresh")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestJson))
                                        .andDo(print())
                                        .andExpect(status().isUnauthorized())
                                        .andExpect(jsonPath("$.code").value("AUTH_009"));

                        // 전체 세션 무효화 확인
                        assertThat(userRefreshTokenRepository.findByUserId(new UserId(userId))).isEmpty();
                }
        }

        @Nested
        @DisplayName("공개 엔드포인트 접근")
        class PublicEndpoints {

                @Test
                @DisplayName("회원가입 API는 인증 없이 접근 가능하다")
                void signUp_NoAuthRequired() throws Exception {
                        String signUpJson = """
                                        {
                                            "email": "newuser@example.com",
                                            "password": "password123",
                                            "nickname": "신규유저",
                                            "jobCategoryNames": ["BACKEND"]
                                        }
                                        """;

                        mockMvc.perform(post("/api/user/signup")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(signUpJson))
                                        .andDo(print())
                                        .andExpect(status().isOk());
                }
        }
}
