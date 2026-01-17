package com.personal.interview.domain.user.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.personal.interview.domain.user.entity.User;

@Repository
public interface UserRepository extends org.springframework.data.repository.Repository<User, Long> {
    User save(User user);

    Optional<User> findById(Long id);
}
