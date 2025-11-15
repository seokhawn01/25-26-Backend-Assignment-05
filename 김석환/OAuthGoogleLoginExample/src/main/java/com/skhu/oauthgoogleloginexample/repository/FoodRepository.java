package com.skhu.oauthgoogleloginexample.repository;

import com.skhu.oauthgoogleloginexample.domain.Food;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FoodRepository extends JpaRepository<Food, Long> {
    List<Food> findByUserId(Long userId);
}
