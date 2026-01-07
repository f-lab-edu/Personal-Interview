package com.personal.interview.domain.user.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class SignUpResponse {

    private Long userId;
    private String email;
    private String nickname;
    private UserRole role;
    private List<String> jobCategoryNames;
    private LocalDateTime createdAt;

    public static SignUpResponse from(User user) {
        return new SignUpResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole(),
                user.getJobCategories().stream()
                        .map(JobCategory::getJobCategoryName)
                        .collect(Collectors.toList()),
                user.getCreatedAt());
    }
}
