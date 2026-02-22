package com.personal.interview.global.security.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.personal.interview.domain.auth.entity.UserRefreshToken;
import com.personal.interview.domain.auth.repository.UserRefreshTokenRepository;
import com.personal.interview.domain.auth.service.AuthService;
import com.personal.interview.domain.user.entity.UserId;
import com.personal.interview.global.security.JwtTokenProvider;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private UserRefreshTokenRepository userRefreshTokenRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Test
    @DisplayName("DB에 존재하면 기존 토큰 엔티티를 업데이트한다")
    void rotateRefreshToken_UpdatesExisting() {
        // given
        UserId userId = new UserId(1L);
        String refreshToken = "refreshTokenString";
        long expirationMs = 1000000L;

        given(jwtTokenProvider.getRefreshExpirationMs()).willReturn(expirationMs);
        UserRefreshToken existingToken = mock(UserRefreshToken.class);
        given(userRefreshTokenRepository.findByUserId(userId)).willReturn(Optional.of(existingToken));

        // when
        refreshTokenService.rotateRefreshToken(userId, refreshToken);

        // then
        String expectedHashed = AuthService.hashToken(refreshToken);
        verify(existingToken).updateRefreshToken(eq(expectedHashed), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("DB에 존재하지 않으면 새로운 토큰 엔티티를 저장한다")
    void rotateRefreshToken_SavesNew() {
        // given
        UserId userId = new UserId(1L);
        String refreshToken = "newRefreshTokenString";
        long expirationMs = 1000000L;

        given(jwtTokenProvider.getRefreshExpirationMs()).willReturn(expirationMs);
        given(userRefreshTokenRepository.findByUserId(userId)).willReturn(Optional.empty());

        // when
        refreshTokenService.rotateRefreshToken(userId, refreshToken);

        // then
        ArgumentCaptor<UserRefreshToken> captor = ArgumentCaptor.forClass(UserRefreshToken.class);
        verify(userRefreshTokenRepository).save(captor.capture());

        UserRefreshToken savedToken = captor.getValue();
        String expectedHashed = AuthService.hashToken(refreshToken);

        assertThat(savedToken).isNotNull();
        assertThat(savedToken.getRefreshToken()).isEqualTo(expectedHashed);
    }
}
