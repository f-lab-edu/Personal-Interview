package com.personal.interview.domain.auth.entity;

import static org.assertj.core.api.Assertions.*;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.personal.interview.domain.user.entity.UserId;

class UserRefreshTokenTest {

    private static final UserId TEST_USER_ID = new UserId(1L);
    private static final String HASH = "hashedToken123";
    private static final String SALT = "salt123";

    private void setCreatedAt(UserRefreshToken token, LocalDateTime createdAt) {
        try {
            Field field = UserRefreshToken.class.getDeclaredField("createdAt");
            field.setAccessible(true);
            field.set(token, createdAt);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("정상적으로 생성되면 모든 필드가 설정된다")
        void create_Success() {
            LocalDateTime expiryAt = LocalDateTime.now().plusDays(7);

            UserRefreshToken token = UserRefreshToken.create(TEST_USER_ID, HASH, SALT, expiryAt);

            assertThat(token.getUserId()).isEqualTo(TEST_USER_ID);
            assertThat(token.getRefreshToken()).isEqualTo(HASH);
            assertThat(token.getSalt()).isEqualTo(SALT);
            assertThat(token.getExpiryAt()).isEqualTo(expiryAt);
            assertThat(token.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("null 값이 들어오면 NullPointerException을 발생시킨다")
        void create_NullValues() {
            assertThatThrownBy(() -> UserRefreshToken.create(TEST_USER_ID, null, SALT, LocalDateTime.now()))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("rotateToken")
    class RotateToken {

        @Test
        @DisplayName("회전 시 토큰, salt, 만료시간, createdAt이 갱신된다")
        void rotateToken_UpdatesAllFields() {
            UserRefreshToken token = UserRefreshToken.create(
                    TEST_USER_ID, HASH, SALT, LocalDateTime.now().plusDays(7));

            String newHash = "newHashedToken456";
            String newSalt = "newSalt456";
            LocalDateTime newExpiry = LocalDateTime.now().plusDays(14);
            LocalDateTime beforeRotate = LocalDateTime.now();

            token.rotateToken(newHash, newSalt, newExpiry);

            assertThat(token.getRefreshToken()).isEqualTo(newHash);
            assertThat(token.getSalt()).isEqualTo(newSalt);
            assertThat(token.getExpiryAt()).isEqualTo(newExpiry);
            assertThat(token.getCreatedAt()).isAfterOrEqualTo(beforeRotate);
        }
    }

    @Nested
    @DisplayName("isWithinGracePeriod")
    class IsWithinGracePeriod {

        @Test
        @DisplayName("createdAt이 grace period 내이면 true를 반환한다")
        void withinGracePeriod_ReturnsTrue() {
            UserRefreshToken token = UserRefreshToken.create(
                    TEST_USER_ID, HASH, SALT, LocalDateTime.now().plusDays(7));

            assertThat(token.isWithinGracePeriod(10L)).isTrue();
        }

        @Test
        @DisplayName("createdAt이 grace period 밖이면 false를 반환한다")
        void outsideGracePeriod_ReturnsFalse() {
            UserRefreshToken token = UserRefreshToken.create(
                    TEST_USER_ID, HASH, SALT, LocalDateTime.now().plusDays(7));
            setCreatedAt(token, LocalDateTime.now().minusMinutes(5));

            assertThat(token.isWithinGracePeriod(10L)).isFalse();
        }
    }

    @Nested
    @DisplayName("isExpired")
    class IsExpired {

        @Test
        @DisplayName("만료 시간이 지나면 true를 반환한다")
        void expired_ReturnsTrue() {
            UserRefreshToken token = UserRefreshToken.create(
                    TEST_USER_ID, HASH, SALT, LocalDateTime.now().minusDays(1));

            assertThat(token.isExpired()).isTrue();
        }

        @Test
        @DisplayName("만료 시간 전이면 false를 반환한다")
        void notExpired_ReturnsFalse() {
            UserRefreshToken token = UserRefreshToken.create(
                    TEST_USER_ID, HASH, SALT, LocalDateTime.now().plusDays(7));

            assertThat(token.isExpired()).isFalse();
        }
    }
}
