package com.cheftory.api.recipe.info;

import com.cheftory.api.recipe.info.dto.RecipeInfoFindResponse;
import com.cheftory.api.recipe.info.dto.VideoInfo;
import com.cheftory.api.recipe.info.entity.RecipeInfo;
import com.cheftory.api.recipe.info.entity.RecipeStatus;
import com.cheftory.api.recipe.info.repository.RecipeInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindRecipeInfoService {
    private final RecipeInfoRepository recipeInfoRepository;
    public void checkExist(UUID recipeId){
        getRecipeInfo(recipeId);
    }
    public String getVideoId(UUID recipeId){
        return getRecipeInfo(recipeId)
                .getVideoId();
    }
    private RecipeInfo getRecipeInfo(UUID recipeId){
        return recipeInfoRepository
                .findById(recipeId)
                .orElseThrow(()->new RecipeInfoNotFoundException("id에 해당하는 레시피가 존재하지 않습니다."));
    }
    public RecipeInfoFindResponse getInfoContent(
            UUID recipeInfoId
            , Boolean status
            , Boolean videoContent
    ){
        RecipeInfo recipeInfo = getRecipeInfo(recipeInfoId);

        RecipeStatus recipeStatus  = null;
        if(status){
            recipeStatus = recipeInfo.getStatus();
        }

        VideoInfo videoInfo = null;
        if(videoContent){
            videoInfo= VideoInfo.of(
                    recipeInfo.getUrl()
                    ,recipeInfo.getVideoSeconds()
                    ,recipeInfo.getThumbnailUrl());
        }

        return RecipeInfoFindResponse.of(recipeStatus,videoInfo);
    }
}
