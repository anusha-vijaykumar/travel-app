package com.springcloud.user_service.repository;

import com.springcloud.user_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {
    User getUserById(Long id);
    User getUserByEmail(String email);
}
