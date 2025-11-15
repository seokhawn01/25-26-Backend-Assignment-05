package com.skhu.oauthgoogleloginexample.controller;

import com.skhu.oauthgoogleloginexample.domain.Food;
import com.skhu.oauthgoogleloginexample.dto.FoodResponseDto;
import com.skhu.oauthgoogleloginexample.dto.FoodSaveRequestDto;
import com.skhu.oauthgoogleloginexample.service.FoodService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/gdg/foods")
public class FoodController {

    private final FoodService foodService;

    @PostMapping
    public Food create(@RequestBody FoodSaveRequestDto dto,
                       Principal principal) {
        return foodService.save(dto.getName(), dto.getCalories(), principal);
    }

    @GetMapping
    public List<FoodResponseDto> myFoods(Principal principal) {
        return foodService.findMyFoods(principal);
    }


    @PatchMapping("/{foodId}")
    public FoodResponseDto partialUpdate(
            @PathVariable Long foodId,
            @RequestBody Map<String, Object> updates,
            Principal principal
    ) {
        String name = (String) updates.get("name");
        Integer calories = (Integer) updates.get("calories");

        return foodService.update(foodId, name, calories, principal);
    }




    @DeleteMapping("/{foodId}")
    public void delete(@PathVariable Long foodId, Principal principal) {
        foodService.delete(foodId, principal);
    }
}
