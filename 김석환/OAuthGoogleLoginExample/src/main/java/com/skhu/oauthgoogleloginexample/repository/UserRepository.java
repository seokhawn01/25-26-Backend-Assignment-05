package com.skhu.oauthgoogleloginexample.repository;

import com.skhu.oauthgoogleloginexample.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String userEmail);
}
