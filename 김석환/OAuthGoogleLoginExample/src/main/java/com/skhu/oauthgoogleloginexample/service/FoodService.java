package com.skhu.oauthgoogleloginexample.service;

import com.skhu.oauthgoogleloginexample.domain.Food;
import com.skhu.oauthgoogleloginexample.domain.User;
import com.skhu.oauthgoogleloginexample.dto.FoodResponseDto;
import com.skhu.oauthgoogleloginexample.repository.FoodRepository;
import com.skhu.oauthgoogleloginexample.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FoodService {

    private final FoodRepository foodRepository;
    private final UserRepository userRepository;

    public Food save(String foodName, int calories, Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        return foodRepository.save(Food.builder()
                .foodName(foodName)
                .calories(calories)
                .user(user)
                .build());
    }

    public List<FoodResponseDto> findMyFoods(Principal principal) {
        Long userId = Long.parseLong(principal.getName());

        return foodRepository.findByUserId(userId)
                .stream()
                .map(food -> FoodResponseDto.builder()
                        .id(food.getId())
                        .name(food.getFoodName())
                        .calories(food.getCalories())
                        .build()
                ).toList();
    }


    public FoodResponseDto update(Long foodId, String name, Integer calories, Principal principal) {

        Long userId = Long.parseLong(principal.getName());

        Food food = foodRepository.findById(foodId)
                .orElseThrow(() -> new RuntimeException("음식 없음"));

        // 본인 음식인지 체크
        if (!food.getUser().getId().equals(userId)) {
            throw new RuntimeException("권한 없음");
        }
        food.update(name, calories);

        foodRepository.save(food);

        return FoodResponseDto.builder()
                .id(food.getId())
                .name(food.getFoodName())
                .calories(food.getCalories())
                .build();
    }



    public void delete(Long foodId, Principal principal) {
        Food food = checkOwner(foodId, principal);
        foodRepository.delete(food);
    }

    private Food checkOwner(Long foodId, Principal principal) {
        Food food = foodRepository.findById(foodId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 음식입니다."));

        Long userId = Long.parseLong(principal.getName());

        if (!food.getUser().getId().equals(userId)) {
            throw new RuntimeException("본인의 음식만 수정/삭제할 수 있습니다.");
        }
        return food;
    }
}

