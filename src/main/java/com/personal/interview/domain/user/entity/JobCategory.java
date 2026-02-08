package com.personal.interview.domain.user.entity;

import com.personal.interview.domain.user.entity.vo.JobCategoryName;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "common_code", nullable = false,columnDefinition = "VARCHAR(100)")
    private JobCategoryName name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public static JobCategory create(JobCategoryName name, User user) {
        var jobCategory = new JobCategory();

        jobCategory.name = name;
        jobCategory.user = user;

        return jobCategory;
    }
}
