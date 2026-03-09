package com.personal.interview.global.security.filter;

import java.io.IOException;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import com.personal.interview.domain.auth.controller.dto.LoginRequest;
import com.personal.interview.util.validator.SecurityValidatorUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

public class CustomAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private final ObjectMapper objectMapper;
    private final SecurityValidatorUtil securityValidatorUtil;

    public CustomAuthenticationFilter(AuthenticationManager authenticationManager, ObjectMapper objectMapper,
        SecurityValidatorUtil securityValidatorUtil) {
        super(PathPatternRequestMatcher.withDefaults().matcher(org.springframework.http.HttpMethod.POST,
                "/api/auth/login"), authenticationManager);
        this.objectMapper = objectMapper;
        this.securityValidatorUtil = securityValidatorUtil;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException {

        LoginRequest loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);

        // dto validation
        securityValidatorUtil.validate(loginRequest, response);

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(loginRequest.email(),
                loginRequest.password());

        return getAuthenticationManager().authenticate(authToken);
    }
}
