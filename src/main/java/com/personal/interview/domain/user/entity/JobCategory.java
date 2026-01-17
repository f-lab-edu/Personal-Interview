package com.personal.interview.domain.user.entity;

import com.personal.interview.domain.base.BaseEntity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "job_category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JobCategory extends BaseEntity {
    @Column(name = "common_code", nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public static JobCategory create(String name, User user) {
        var jobCategory = new JobCategory();

        jobCategory.name = name;
        jobCategory.user = user;

        return jobCategory;
    }
}
