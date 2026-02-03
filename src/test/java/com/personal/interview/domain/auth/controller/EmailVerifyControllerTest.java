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
        mockVerify.verify(); // 인증 완료 상태로 변경
        
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
}
