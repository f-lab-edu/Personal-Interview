package com.personal.interview.domain.user.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.personal.interview.domain.user.entity.User;
import com.personal.interview.domain.user.entity.UserId;
import com.personal.interview.domain.user.entity.vo.Email;

@Repository
public interface UserRepository extends org.springframework.data.repository.Repository<User, UserId> {
    User save(User user);
    
    void delete(User user);

    Optional<User> findById(UserId id);
    
    Optional<User> findByEmail(Email email);

    boolean existsByEmail(Email email);
}
