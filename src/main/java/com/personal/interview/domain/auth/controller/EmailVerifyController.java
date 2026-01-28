package com.personal.interview.domain.auth.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.personal.interview.domain.auth.controller.dto.EmailVerifyResponse;
import com.personal.interview.domain.auth.service.VerifyService;
import com.personal.interview.domain.user.entity.UserId;
import com.personal.interview.global.security.SecurityUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth/email")
@RequiredArgsConstructor
public class EmailVerifyController {

	private final VerifyService verifyService;

	@PostMapping("/send")
	public ResponseEntity<EmailVerifyResponse> sendVerificationEmail() {
		UserId userId = SecurityUtil.getCurrentUserId();

		return ResponseEntity
			.ok(EmailVerifyResponse.from(verifyService.sendVerifyEmail(userId)));
	}

	@GetMapping("/verify")
	public ResponseEntity<EmailVerifyResponse> verifyEmail(@RequestParam String token) {
		UUID uuidToken;
		try {
			uuidToken = UUID.fromString(token);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("유효하지 않은 토큰 형식입니다.");
		}

		return ResponseEntity
			.ok(EmailVerifyResponse.from(verifyService.verifyEmail(uuidToken)));
	}
}