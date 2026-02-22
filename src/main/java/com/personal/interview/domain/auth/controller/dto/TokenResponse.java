package com.personal.interview.domain.auth.controller.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken) {
}
