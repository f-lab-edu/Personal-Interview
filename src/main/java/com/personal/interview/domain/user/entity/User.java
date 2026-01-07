package com.personal.interview.domain.user.entity;

import static java.util.Objects.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JobCategory> jobCategories = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public static User signUp(SignUpRequest request, PasswordEncoder passwordEncoder) {
        var user = new User();

        user.email = requireNonNull(request.getEmail());
        user.password = passwordEncoder.encode(requireNonNull(request.getPassword()));
        user.nickname = requireNonNull(request.getNickname());

        user.role = UserRole.ROLE_DRAFT;

        for (String categoryName : request.getJobCategoryNames()) {
            JobCategory jobCategory = JobCategory.create(categoryName, user);
            user.jobCategories.add(jobCategory);
        }

        return user;
    }

    public void modifyEmail(String newEmail) {
        this.email = requireNonNull(newEmail);
    }

    public void modifyRoleUser() {
        if (this.role.equals(UserRole.ROLE_USER)) {
            throw new IllegalStateException("이미 ROLE_USER 권한을 가지고 있습니다.");
        }

        this.role = UserRole.ROLE_USER;
    }
}
