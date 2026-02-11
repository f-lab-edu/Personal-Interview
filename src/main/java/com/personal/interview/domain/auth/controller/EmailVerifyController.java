package com.personal.interview.domain.auth.controller;

import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.personal.interview.domain.auth.controller.dto.EmailVerifyResponse;
import com.personal.interview.domain.auth.service.VerifyService;
import com.personal.interview.domain.user.entity.UserId;
import com.personal.interview.global.config.properties.EmailProperties;
import com.personal.interview.global.exception.DomainException;
import com.personal.interview.global.security.SecurityUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth/email")
@RequiredArgsConstructor
public class EmailVerifyController {

	private final VerifyService verifyService;
	private final EmailProperties emailProperties;

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

	/**
	 * 이메일 인증 링크를 통한 인증 처리 (리다이렉트 방식)
	 * 
	 * @param token 인증 토큰
	 * @return 인증 성공 시 성공 페이지로, 실패 시 실패 페이지로 리다이렉트
	 */
	@GetMapping("/verify/redirect")
	public ResponseEntity<Void> verifyEmailWithRedirect(@RequestParam String token) {
		try {
			UUID uuidToken = UUID.fromString(token);
			verifyService.verifyEmail(uuidToken);

			// 인증 성공 시 성공 페이지로 리다이렉트
			String redirectUrl = emailProperties.baseUrl() + emailProperties.successRedirectPath();
			return ResponseEntity.status(HttpStatus.FOUND)
				.header(HttpHeaders.LOCATION, redirectUrl)
				.build();
				
		} catch (DomainException e) {
			// 비즈니스 예외 발생 시 실패 페이지로 리다이렉트 (에러 코드 포함)
			String redirectUrl = emailProperties.baseUrl() + emailProperties.failureRedirectPath()
				+ "?reason=" + e.getErrorCode().getCode();
			return ResponseEntity.status(HttpStatus.FOUND)
				.header(HttpHeaders.LOCATION, redirectUrl)
				.build();
				
		} catch (IllegalArgumentException e) {
			// 토큰 형식 오류 시 실패 페이지로 리다이렉트
			String redirectUrl = emailProperties.baseUrl() + emailProperties.failureRedirectPath()
				+ "?reason=INVALID_TOKEN_FORMAT";
			return ResponseEntity.status(HttpStatus.FOUND)
				.header(HttpHeaders.LOCATION, redirectUrl)
				.build();
		}
	}
}