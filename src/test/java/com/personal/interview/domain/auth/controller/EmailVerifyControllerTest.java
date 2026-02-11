package com.personal.interview.domain.auth.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.personal.interview.domain.auth.AuthPropertiesFixture;
import com.personal.interview.domain.auth.entity.EmailVerify;
import com.personal.interview.domain.auth.service.VerifyService;
import com.personal.interview.domain.user.entity.UserId;
import com.personal.interview.global.config.SecurityConfig;
import com.personal.interview.global.config.properties.AuthProperties;
import com.personal.interview.global.exception.DomainException;
import com.personal.interview.global.exception.BaseErrorCode;

import org.springframework.http.HttpStatus;

@WebMvcTest(EmailVerifyController.class)
@Import(SecurityConfig.class)
class EmailVerifyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VerifyService verifyService;

    private AuthProperties authProperties;

    @BeforeEach
    void setUp() {
        authProperties = AuthPropertiesFixture.createDefault();
    }

    @Test
    @DisplayName("인증 메일 발송 성공")
    @WithMockUser(username = "1")
    void sendVerificationEmail_Success() throws Exception {
        // given
        EmailVerify mockVerify = EmailVerify.create(new UserId(1L), authProperties);

        given(verifyService.sendVerifyEmail(any(UserId.class))).willReturn(mockVerify);

        // when & then
        mockMvc.perform(post("/api/auth/email/send")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("이메일 인증 성공")
    void verifyEmail_Success() throws Exception {
        // given
        UUID token = UUID.randomUUID();
        EmailVerify mockVerify = EmailVerify.create(new UserId(1L), authProperties);
        mockVerify.verify();

        given(verifyService.verifyEmail(token)).willReturn(mockVerify);

        // when & then
        mockMvc.perform(get("/api/auth/email/verify")
                .param("token", token.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isVerify").value(true));
    }

    @Test
    @DisplayName("이메일 인증 실패 - 잘못된 토큰 형식")
    void verifyEmail_InvalidTokenFormat() throws Exception {
        // given
        String invalidToken = "invalid-token";

        // when & then
        mockMvc.perform(get("/api/auth/email/verify")
                .param("token", invalidToken))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("이메일 인증 리다이렉트 - 성공 시 성공 페이지 렌더링")
    void verifyEmailWithRedirect_Success() throws Exception {
        // given
        UUID token = UUID.randomUUID();
        EmailVerify mockVerify = EmailVerify.create(new UserId(1L), authProperties);
        mockVerify.verify();

        given(verifyService.verifyEmail(token)).willReturn(mockVerify);

        // when & then
        mockMvc.perform(get("/api/auth/email/verify/redirect")
                .param("token", token.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("email/verify-success"));
    }

    @Test
    @DisplayName("이메일 인증 리다이렉트 - 도메인 예외 시 실패 페이지 렌더링")
    void verifyEmailWithRedirect_DomainException() throws Exception {
        // given
        UUID token = UUID.randomUUID();

        given(verifyService.verifyEmail(token))
                .willThrow(DomainException.create(new BaseErrorCode() {
                    @Override
                    public String getCode() {
                        return "EXPIRED_TOKEN";
                    }

                    @Override
                    public String getMessage() {
                        return "만료된 토큰입니다.";
                    }

                    @Override
                    public HttpStatus getStatus() {
                        return HttpStatus.BAD_REQUEST;
                    }
                }));

        // when & then
        mockMvc.perform(get("/api/auth/email/verify/redirect")
                .param("token", token.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("email/verify-failure"))
                .andExpect(model().attribute("errorCode", "EXPIRED_TOKEN"))
                .andExpect(model().attribute("errorMessage", "만료된 토큰입니다."));
    }

    @Test
    @DisplayName("이메일 인증 리다이렉트 - 잘못된 토큰 형식 시 실패 페이지 렌더링")
    void verifyEmailWithRedirect_InvalidTokenFormat() throws Exception {
        // given
        String invalidToken = "invalid-token";

        // when & then
        mockMvc.perform(get("/api/auth/email/verify/redirect")
                .param("token", invalidToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("email/verify-failure"))
                .andExpect(model().attribute("errorCode", "INVALID_TOKEN_FORMAT"))
                .andExpect(model().attribute("errorMessage", "유효하지 않은 토큰 형식입니다."));
    }
}
