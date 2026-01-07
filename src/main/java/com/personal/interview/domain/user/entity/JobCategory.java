package com.personal.interview.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "job_category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JobCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_category_name", nullable = false, length = 100)
    private String jobCategoryName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public static JobCategory create(String jobCategoryName, User user) {
        var jobCategory = new JobCategory();

        jobCategory.jobCategoryName = jobCategoryName;
        jobCategory.user = user;

        return jobCategory;
    }
}
