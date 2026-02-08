package com.personal.interview.domain.auth.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.Repository;

import com.personal.interview.domain.auth.entity.EmailVerify;
import com.personal.interview.domain.auth.entity.EmailVerifyId;
import com.personal.interview.domain.user.entity.UserId;

import jakarta.persistence.LockModeType;

@org.springframework.stereotype.Repository
public interface EmailVerifyRepository extends Repository<EmailVerify, EmailVerifyId> {
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<EmailVerify> findTopWithLockByUserIdOrderByIdDesc(UserId userId);

	EmailVerify save(EmailVerify emailVerify);

	Optional<EmailVerify> findByUserIdAndVerificationToken(UserId userId, UUID token);

	Optional<EmailVerify> findTopByVerificationTokenOrderByIdDesc(UUID token);
}
