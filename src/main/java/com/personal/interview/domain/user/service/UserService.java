package com.personal.interview.domain.user.service;

import com.personal.interview.global.exception.DomainException;
import com.personal.interview.global.exception.ErrorCode;
import com.personal.interview.domain.user.controller.dto.SignUpResponse;
import com.personal.interview.domain.user.entity.User;
import com.personal.interview.domain.user.controller.dto.SignUpRequest;

import com.personal.interview.domain.user.entity.vo.Email;
import com.personal.interview.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        if(userRepository.existsByEmail(new Email(request.email()))) {
            throw DomainException.create(ErrorCode.DUPLICATE_EMAIL);
        }

        final String encryptedPassword = passwordEncoder.encode(request.password());

        User user = User.signUp(request, encryptedPassword);

        try {
            User savedUser = userRepository.save(user);
            return SignUpResponse.from(savedUser);
        } catch (DataIntegrityViolationException e) {
            throw DomainException.create(ErrorCode.DUPLICATE_EMAIL);
        }
    }
}
