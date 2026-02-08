package com.personal.interview.domain.auth.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.personal.interview.domain.auth.entity.EmailVerifySendEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmailVerifyEventListener {
	private final EmailVerifySender emailVerifySender;

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleEmailSendEvent(EmailVerifySendEvent event) {
		emailVerifySender.sendVerificationEmail(event.email(), event.token());
	}
}
