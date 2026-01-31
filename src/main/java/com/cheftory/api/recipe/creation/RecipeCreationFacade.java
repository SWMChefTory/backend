package com.cheftory.api.recipe.creation;

import com.cheftory.api.recipe.content.info.RecipeInfoService;
import com.cheftory.api.recipe.content.info.entity.RecipeInfo;
import com.cheftory.api.recipe.content.info.exception.RecipeInfoErrorCode;
import com.cheftory.api.recipe.content.youtubemeta.RecipeYoutubeMetaService;
import com.cheftory.api.recipe.content.youtubemeta.entity.RecipeYoutubeMeta;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeVideoInfo;
import com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaErrorCode;
import com.cheftory.api.recipe.creation.credit.RecipeCreditPort;
import com.cheftory.api.recipe.creation.identify.RecipeIdentifyService;
import com.cheftory.api.recipe.creation.identify.exception.RecipeIdentifyErrorCode;
import com.cheftory.api.recipe.dto.RecipeCreationTarget;
import com.cheftory.api.recipe.exception.RecipeErrorCode;
import com.cheftory.api.recipe.exception.RecipeException;
import com.cheftory.api.recipe.history.RecipeHistoryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeCreationFacade {

    private final AsyncRecipeCreationService asyncRecipeCreationService;
    private final RecipeHistoryService recipeHistoryService;
    private final RecipeYoutubeMetaService recipeYoutubeMetaService;
    private final RecipeIdentifyService recipeIdentifyService;
    private final RecipeInfoService recipeInfoService;
    private final RecipeCreditPort creditPort;
    private final RecipeCreationTxService recipeCreationTxService;

    public UUID create(RecipeCreationTarget target) {
        try {
            List<UUID> recipeIds = recipeYoutubeMetaService.getByUrl(target.uri()).stream()
                    .map(RecipeYoutubeMeta::getRecipeId)
                    .toList();
            RecipeInfo recipeInfo = recipeInfoService.getNotFailed(recipeIds);

            create(target, recipeInfo);
            return recipeInfo.getId();
        } catch (RecipeException e) {
            if (e.getErrorMessage() == RecipeInfoErrorCode.RECIPE_INFO_NOT_FOUND) {
                return createNewRecipe(target);
            }
            if (e.getErrorMessage() == RecipeInfoErrorCode.RECIPE_FAILED) {
                return createNewRecipe(target);
            }
            if (e.getErrorMessage() == YoutubeMetaErrorCode.YOUTUBE_META_BANNED) {
                throw new RecipeException(RecipeErrorCode.RECIPE_BANNED);
            }
            throw new RecipeException(RecipeErrorCode.RECIPE_CREATE_FAIL);
        }
    }

    private UUID createNewRecipe(RecipeCreationTarget target) {
        try {
            RecipeInfo recipeInfo = recipeCreationTxService.createWithIdentify(target.uri());
            try {
                YoutubeVideoInfo videoInfo = recipeYoutubeMetaService.getVideoInfo(target.uri());
                recipeYoutubeMetaService.create(videoInfo, recipeInfo.getId());
                create(target, recipeInfo);
                asyncRecipeCreationService.create(
                        recipeInfo.getId(), recipeInfo.getCreditCost(), videoInfo.getVideoId(), target.uri());
                return recipeInfo.getId();
            } catch (Exception e) {
                recipeIdentifyService.delete(target.uri(), recipeInfo.getId());
                recipeInfoService.delete(recipeInfo.getId());
                throw e;
            }
        } catch (RecipeException e) {
            if (e.getErrorMessage() == RecipeIdentifyErrorCode.RECIPE_IDENTIFY_PROGRESSING) {
                UUID identifyRecipeId =
                        recipeIdentifyService.getRecipeId(target.uri()).orElse(null);
                if (identifyRecipeId != null) {
                    RecipeInfo identified = recipeInfoService.getNotFailed(List.of(identifyRecipeId));
                    create(target, identified);
                    return identified.getId();
                }
                List<UUID> recipeIds = recipeYoutubeMetaService.getByUrl(target.uri()).stream()
                        .map(RecipeYoutubeMeta::getRecipeId)
                        .toList();
                RecipeInfo identified = recipeInfoService.getNotFailed(recipeIds);
                create(target, identified);
                return identified.getId();
            }
            throw e;
        }
    }

    private void create(RecipeCreationTarget target, RecipeInfo recipeInfo) {
        switch (target) {
            case RecipeCreationTarget.User user -> {
                UUID userId = user.userId();
                boolean created = recipeHistoryService.create(userId, recipeInfo.getId());
                if (!created) return;

                creditPort.spendRecipeCreate(userId, recipeInfo.getId(), recipeInfo.getCreditCost());
            }
            case RecipeCreationTarget.Crawler crawler -> {}
        }
    }
}
