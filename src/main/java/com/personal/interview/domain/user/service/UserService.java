package com.personal.interview.domain.user.service;

import com.personal.interview.domain.user.entity.SignUpResponse;
import com.personal.interview.domain.user.entity.User;
import com.personal.interview.domain.user.entity.SignUpRequest;

import com.personal.interview.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
        User user = User.signUp(request, passwordEncoder);

        User savedUser = userRepository.save(user);

        return SignUpResponse.from(savedUser);
    }
}
