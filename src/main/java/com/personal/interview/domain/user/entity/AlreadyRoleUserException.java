package com.personal.interview.domain.user.entity;

import org.springframework.http.HttpStatus;

import com.personal.interview.domain.base.DomainException;

public class AlreadyRoleUserException extends DomainException {
	public AlreadyRoleUserException() {
		super(HttpStatus.CONFLICT,"ALREADY_ROLE_USER" ,"이미 해당 권한을 가진 사용자입니다.");
	}
}
