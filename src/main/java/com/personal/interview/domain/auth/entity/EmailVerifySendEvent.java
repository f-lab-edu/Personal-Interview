package com.personal.interview.domain.auth.entity;

import java.util.UUID;

import com.personal.interview.domain.user.entity.vo.Email;

public record EmailVerifySendEvent(Email email, UUID token) {
}
