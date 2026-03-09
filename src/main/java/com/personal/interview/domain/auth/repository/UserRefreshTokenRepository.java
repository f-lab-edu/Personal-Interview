package com.personal.interview.domain.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.personal.interview.domain.auth.entity.UserRefreshToken;
import com.personal.interview.domain.user.entity.UserId;

import jakarta.persistence.LockModeType;

@Repository
public interface UserRefreshTokenRepository extends JpaRepository<UserRefreshToken, Long> {

    Optional<UserRefreshToken> findByUserId(UserId userId);

    /**
     * 비관적 락을 사용하여 userId로 RefreshToken을 조회합니다.
     * RTR 동시성 이슈(TOCTOU)를 방지하기 위해 사용됩니다.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM UserRefreshToken t WHERE t.userId = :userId")
    Optional<UserRefreshToken> findByUserIdForUpdate(@Param("userId") UserId userId);

    void deleteByUserId(UserId userId);
}
