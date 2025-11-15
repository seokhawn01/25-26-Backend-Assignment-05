package com.skhu.oauthgoogleloginexample.repository;

import com.skhu.oauthgoogleloginexample.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
}
