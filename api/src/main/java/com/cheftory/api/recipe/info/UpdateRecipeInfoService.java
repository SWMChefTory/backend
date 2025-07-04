package com.cheftory.api.recipe.info;

import com.cheftory.api.recipe.info.entity.RecipeStatus;
import com.cheftory.api.recipe.info.repository.RecipeInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UpdateRecipeInfoService {
    private final RecipeInfoRepository recipeInfoRepository;

    public void updateState(UUID recipeId, RecipeStatus recipeStatus) {
        Integer updatedCount = recipeInfoRepository.updateStatus(recipeId, recipeStatus);
        if(updatedCount == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "상태를 변화시킬 레시피가 존재하지 않습니다.");
        }
    }
}
