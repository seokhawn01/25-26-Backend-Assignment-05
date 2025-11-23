package com.skhu.oauthgoogleloginexample.service;

import com.skhu.oauthgoogleloginexample.domain.Food;
import com.skhu.oauthgoogleloginexample.domain.User;
import com.skhu.oauthgoogleloginexample.dto.FoodResponseDto;
import com.skhu.oauthgoogleloginexample.exception.AccessDeniedException;
import com.skhu.oauthgoogleloginexample.exception.FoodNotFoundException;
import com.skhu.oauthgoogleloginexample.exception.UserNotFoundException;
import com.skhu.oauthgoogleloginexample.repository.FoodRepository;
import com.skhu.oauthgoogleloginexample.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FoodService {

    private final FoodRepository foodRepository;
    private final UserRepository userRepository;

    /**
     * CREATE
     * DB에 변경이 발생 → @Transactional
     */
    @Transactional
    public Food save(String foodName, int calories, Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("유저를 찾을 수 없습니다."));

        return foodRepository.save(Food.builder()
                .foodName(foodName)
                .calories(calories)
                .user(user)
                .build());
    }

    /**
     * READ
     * 읽기 전용 트랜잭션 → 성능 최적화
     */
    @Transactional(readOnly = true)
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

    /**
     * UPDATE
     * 엔티티 변경 감지 사용 → save() 호출 불필요
     */
    @Transactional
    public FoodResponseDto update(Long foodId, String name, Integer calories, Principal principal) {

        Long userId = Long.parseLong(principal.getName());

        Food food = foodRepository.findById(foodId)
                .orElseThrow(() -> new FoodNotFoundException("음식 없음"));

        // 본인 음식인지 체크
        if (!food.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("권한 없음");
        }

        // 엔티티 변경 → 트랜잭션 끝날 때 자동 반영 (dirty checking)
        food.update(name, calories);

        return FoodResponseDto.builder()
                .id(food.getId())
                .name(food.getFoodName())
                .calories(food.getCalories())
                .build();
    }

    /**
     * DELETE
     */
    @Transactional
    public void delete(Long foodId, Principal principal) {
        Food food = foodRepository.findById(foodId)
                .orElseThrow(() -> new FoodNotFoundException("존재하지 않는 음식입니다."));
    }

    /**
     * READ (내부 메서드)
     * 필요한 경우 readOnly로 지정해도 좋음
     */
    @Transactional(readOnly = true)
    public Food checkOwner(Long foodId, Principal principal) {

        Food food = foodRepository.findById(foodId)
                .orElseThrow(() -> new FoodNotFoundException("존재하지 않는 음식입니다."));

        Long userId = Long.parseLong(principal.getName());

        if (!food.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("본인의 음식만 수정/삭제할 수 있습니다.");
        }

        return food;
    }

}
