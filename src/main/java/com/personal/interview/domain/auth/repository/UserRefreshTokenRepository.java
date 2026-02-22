package com.personal.interview.domain.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.personal.interview.domain.auth.entity.UserRefreshToken;
import com.personal.interview.domain.user.entity.UserId;

@Repository
public interface UserRefreshTokenRepository extends JpaRepository<UserRefreshToken, Long> {

    Optional<UserRefreshToken> findByUserId(UserId userId);

    void deleteByUserId(UserId userId);
}
