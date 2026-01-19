package com.personal.interview.domain.user.entity.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.personal.interview.domain.user.entity.JobCategory;
import com.personal.interview.domain.user.entity.User;
import com.personal.interview.domain.user.entity.vo.UserRole;

public record SignUpResponse(
    Long userId,
    String nickname,
    UserRole role,
    List<String> jobCategoryNames,
    LocalDateTime createdAt
) {
    public static SignUpResponse from(User user) {
        return new SignUpResponse(
                user.getId().longValue(),
                user.getNickname(),
                user.getRole(),
                user.getJobCategories().stream()
                        .map(JobCategory::getName)
                        .collect(Collectors.toList()),
                user.getCreatedAt());
    }
}
