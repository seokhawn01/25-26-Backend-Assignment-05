package com.skhu.oauthgoogleloginexample.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Food {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String foodName;

    @Column(nullable = false)
    private int calories;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    public Food(String foodName, int calories, User user) {
        this.foodName = foodName;
        this.calories = calories;
        this.user = user;
    }

    public void update(String foodName, int calories) {
        this.foodName = foodName;
        this.calories = calories;
    }

}
