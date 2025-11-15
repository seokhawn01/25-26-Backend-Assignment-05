package com.skhu.oauthgoogleloginexample.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FoodResponseDto {
    private Long id;
    private String name;
    private int calories;
}
