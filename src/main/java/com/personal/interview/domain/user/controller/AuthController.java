package com.personal.interview.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.personal.interview.domain.user.entity.dto.SignUpRequest;
import com.personal.interview.domain.user.entity.dto.SignUpResponse;
import com.personal.interview.domain.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        SignUpResponse response = userService.signUp(request);

        return ResponseEntity.ok().body(response);
    }
}
