package com.personal.interview.domain.auth.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
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

	@PostMapping("/verify")
	public ResponseEntity<EmailVerifyResponse> verifyEmail(@RequestParam Long userId,@RequestParam String token) {
		return ResponseEntity
			.ok(EmailVerifyResponse.from(verifyService.verifyEmail(new UserId(userId), UUID.fromString(token))));
	}
}