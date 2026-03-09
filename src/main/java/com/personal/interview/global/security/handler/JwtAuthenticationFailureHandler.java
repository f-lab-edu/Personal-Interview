package com.personal.interview.global.security.handler;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import tools.jackson.databind.ObjectMapper;
import com.personal.interview.global.exception.ErrorCode;
import com.personal.interview.util.filter.FilterExceptionUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtAuthenticationFailureHandler implements AuthenticationFailureHandler {
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException {

        ErrorCode errorCode = ErrorCode.INVALID_CREDENTIALS;

        FilterExceptionUtil.sendErrorResponse(response, errorCode, objectMapper);
    }
}
