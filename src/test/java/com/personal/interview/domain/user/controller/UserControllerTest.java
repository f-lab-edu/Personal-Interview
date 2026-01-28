package com.personal.interview.domain.user.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.personal.interview.domain.user.UserFixture;
import com.personal.interview.domain.user.controller.dto.SignUpRequest;
import com.personal.interview.domain.user.controller.dto.SignUpResponse;
import com.personal.interview.domain.user.service.UserService;
import com.personal.interview.global.config.SecurityConfig;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    void signUp_Success() throws Exception {
        // Given
        SignUpRequest request = UserFixture.createDefaultSignUpRequest();
        SignUpResponse response = UserFixture.createSignUpResponse(request);

        given(userService.signUp(any(SignUpRequest.class))).willReturn(response);

        // When & Then
        mockMvc.perform(post("/api/user/signup")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    void signUp_InvalidEnum() throws Exception {
        // Given
        String requestJson = """
                {
                    "email": "test@test.com",
                    "password": "password",
                    "nickname": "nickname",
                    "jobCategoryNames": ["INVALID_ENUM"]
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/user/signup")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
