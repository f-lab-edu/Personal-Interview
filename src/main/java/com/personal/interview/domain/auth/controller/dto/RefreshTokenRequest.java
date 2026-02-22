package com.personal.interview.domain.auth.controller.dto;

import jakarta.validation.constraints.NotEmpty;

public record RefreshTokenRequest(
        @NotEmpty(message = "리프레시 토큰을 입력해주세요.") String refreshToken) {
}
