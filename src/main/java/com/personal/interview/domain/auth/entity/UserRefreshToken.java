package com.personal.interview.domain.auth.entity;

import static java.util.Objects.*;

import java.time.LocalDateTime;

import com.personal.interview.domain.user.entity.UserId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_refresh_tokens", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_refresh_token_user_id", columnNames = "userId")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UserId userId;

    @Column(nullable = false, length = 500)
    private String refreshToken;

    @Column(nullable = false, length = 64)
    private String salt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiryAt;

    @Version
    private Long version;

    public static UserRefreshToken create(UserId userId, String refreshHashToken, String salt, LocalDateTime expiryAt) {
        var userRefreshToken = new UserRefreshToken();

        userRefreshToken.refreshToken = requireNonNull(refreshHashToken);
        userRefreshToken.salt = requireNonNull(salt);
        userRefreshToken.createdAt = LocalDateTime.now();
        userRefreshToken.userId = userId;
        userRefreshToken.expiryAt = requireNonNull(expiryAt);

        return userRefreshToken;
    }

    public void rotateToken(String newRefreshToken, String newSalt, LocalDateTime newExpiryAt) {
        this.refreshToken = requireNonNull(newRefreshToken);
        this.salt = requireNonNull(newSalt);
        this.expiryAt = requireNonNull(newExpiryAt);
        this.createdAt = LocalDateTime.now();
    }

    public boolean isWithinGracePeriod(long gracePeriodSeconds) {
        return LocalDateTime.now().isBefore(createdAt.plusSeconds(gracePeriodSeconds));
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryAt);
    }
}
