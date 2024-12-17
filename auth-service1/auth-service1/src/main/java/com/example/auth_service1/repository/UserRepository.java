package com.example.auth_service1.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.auth_service1.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}