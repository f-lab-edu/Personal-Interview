package com.personal.interview.domain.auth.controller;

import static com.personal.interview.domain.user.entity.vo.UserRole.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.personal.interview.domain.auth.controller.dto.RefreshTokenRequest;
import com.personal.interview.domain.auth.controller.dto.TokenResponse;
import com.personal.interview.domain.auth.service.AuthService;
import com.personal.interview.domain.user.entity.vo.UserRole;
import com.personal.interview.global.security.annotation.Authorize;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody @Valid RefreshTokenRequest request) {
        TokenResponse response = authService.refresh(request);

        return ResponseEntity.ok(response);
    }
}
