package com.personal.interview.domain.user.entity;

import static com.personal.interview.domain.user.entity.UserId.*;
import static jakarta.persistence.GenerationType.*;
import static java.util.Objects.*;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.JavaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.personal.interview.domain.base.BaseTimeEntity;
import com.personal.interview.domain.user.entity.dto.SignUpRequest;
import com.personal.interview.domain.user.entity.vo.Email;
import com.personal.interview.domain.user.entity.vo.JobCategoryName;
import com.personal.interview.domain.user.entity.vo.UserRole;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @JavaType(UserIdJavaType.class)
    private UserId id;

    @Embedded
    private Email email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private UserRole role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JobCategory> jobCategories = new ArrayList<>();

    public static User signUp(SignUpRequest request, PasswordEncoder passwordEncoder) {
        var user = new User();

        user.email = new Email(requireNonNull(request.email()));
        user.password = passwordEncoder.encode(requireNonNull(request.password()));
        user.nickname = requireNonNull(request.nickname());

        user.role = UserRole.ROLE_DRAFT;

        for (JobCategoryName categoryName : request.jobCategoryNames()) {
            JobCategory jobCategory = JobCategory.create(categoryName, user);
            user.jobCategories.add(jobCategory);
        }

        return user;
    }

    public void modifyEmail(String newEmail) {
        this.email = new Email(requireNonNull(newEmail));
    }

    public void modifyRoleUser() {
        if (this.role.equals(UserRole.ROLE_USER)) {
            throw new IllegalStateException("이미 ROLE_USER 권한을 가지고 있습니다.");
        }

        this.role = UserRole.ROLE_USER;
    }
}
