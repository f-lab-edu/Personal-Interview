package com.personal.interview.domain.auth.entity;

import static java.util.Objects.*;

import java.time.LocalDateTime;
import java.util.Objects;

import com.personal.interview.domain.user.entity.UserId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_refresh_tokens", indexes = {
        @Index(name = "idx_user_id", columnList = "userId")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private UserId userId;

    @Column(nullable = false, length = 500)
    private String refreshToken;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiryAt;

    public static UserRefreshToken create(UserId userId, String refreshHashToken, LocalDateTime expiryAt) {
        var userRefreshToken = new UserRefreshToken();

        userRefreshToken.refreshToken = requireNonNull(refreshHashToken);
        userRefreshToken.createdAt = LocalDateTime.now();
        userRefreshToken.userId = userId;
        userRefreshToken.expiryAt = requireNonNull(expiryAt);

        return userRefreshToken;
    }

    public void updateRefreshToken(String newRefreshToken, LocalDateTime newExpiryAt) {
        this.refreshToken = newRefreshToken;
        this.createdAt = LocalDateTime.now();
        this.expiryAt = newExpiryAt;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryAt);
    }
}
