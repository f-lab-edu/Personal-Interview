package com.personal.interview.global.security.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.personal.interview.domain.auth.entity.UserRefreshToken;
import com.personal.interview.domain.auth.repository.UserRefreshTokenRepository;
import com.personal.interview.domain.auth.service.AuthService;
import com.personal.interview.domain.user.entity.UserId;
import com.personal.interview.global.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
	private final UserRefreshTokenRepository userRefreshTokenRepository;
	private final JwtTokenProvider jwtTokenProvider;

	@Transactional
	public void rotateRefreshToken(UserId userId, String refreshToken) {
		String salt = AuthService.generateSalt();
		String hashedToken = AuthService.hashToken(refreshToken, salt);

		LocalDateTime expiryAt = LocalDateTime.now()
				.plusSeconds(jwtTokenProvider.getRefreshExpirationMs() / 1000);

		// 존재하면 회전(이전 토큰 보관), 없으면 새로 생성
		userRefreshTokenRepository.findByUserId(userId)
				.ifPresentOrElse(
						existing -> existing.rotateToken(hashedToken, salt, expiryAt),
						() -> userRefreshTokenRepository
								.save(UserRefreshToken.create(userId, hashedToken, salt, expiryAt)));
	}
}
