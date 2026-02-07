package com.cheftory.api.recipe.creation;

import com.cheftory.api.credit.exception.CreditErrorCode;
import com.cheftory.api.credit.exception.CreditException;
import com.cheftory.api.recipe.bookmark.RecipeBookmarkService;
import com.cheftory.api.recipe.content.info.RecipeInfoService;
import com.cheftory.api.recipe.content.info.entity.RecipeInfo;
import com.cheftory.api.recipe.content.info.exception.RecipeInfoErrorCode;
import com.cheftory.api.recipe.content.youtubemeta.RecipeYoutubeMetaService;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeVideoInfo;
import com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaErrorCode;
import com.cheftory.api.recipe.creation.credit.RecipeCreditPort;
import com.cheftory.api.recipe.creation.identify.RecipeIdentifyService;
import com.cheftory.api.recipe.creation.identify.exception.RecipeIdentifyErrorCode;
import com.cheftory.api.recipe.creation.progress.RecipeProgressService;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressDetail;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressStep;
import com.cheftory.api.recipe.dto.RecipeCreationTarget;
import com.cheftory.api.recipe.exception.RecipeErrorCode;
import com.cheftory.api.recipe.exception.RecipeException;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeCreationFacade {

    private final AsyncRecipeCreationService asyncRecipeCreationService;
    private final RecipeBookmarkService recipeBookmarkService;
    private final RecipeYoutubeMetaService recipeYoutubeMetaService;
    private final RecipeProgressService recipeProgressService;
    private final RecipeIdentifyService recipeIdentifyService;
    private final RecipeInfoService recipeInfoService;
    private final RecipeCreditPort creditPort;
    private final RecipeCreationTxService recipeCreationTxService;

    public UUID createBookmark(RecipeCreationTarget target) {
        try {
            UUID recipeId = recipeYoutubeMetaService.getByUrl(target.uri()).getRecipeId();
            RecipeInfo recipeInfo = recipeInfoService.getSuccess(recipeId);

            createBookmark(target, recipeInfo);
            return recipeInfo.getId();
        } catch (RecipeException e) {
            if (e.getError() == YoutubeMetaErrorCode.YOUTUBE_META_NOT_FOUND) {
                log.info("create new recipe. reason={}", e.getError());
                return createNewRecipe(target);
            }
            if (e.getError() == RecipeInfoErrorCode.RECIPE_FAILED) {
                log.info("create new recipe. reason={}", e.getError());
                return createNewRecipe(target);
            }
            if (e.getError() == YoutubeMetaErrorCode.YOUTUBE_META_BANNED) {
                log.warn("create failed. reason={}", e.getError(), e);
                throw new RecipeException(RecipeErrorCode.RECIPE_BANNED);
            }
            
            log.warn("create failed. reason={}", e.getError(), e);
            throw new RecipeException(RecipeErrorCode.RECIPE_CREATE_FAIL);
        }
    }

    private UUID createNewRecipe(RecipeCreationTarget target) {
        try {
            YoutubeVideoInfo videoInfo = recipeYoutubeMetaService.getVideoInfo(target.uri());
            RecipeInfo recipeInfo = recipeCreationTxService.createWithIdentifyWithVideoInfo(videoInfo);
            try {
                createBookmark(target, recipeInfo);
                asyncRecipeCreationService.create(
                    recipeInfo.getId(), recipeInfo.getCreditCost(), videoInfo.getVideoId(), target.uri());
                return recipeInfo.getId();
            } catch (Exception e) {
                recipeIdentifyService.delete(target.uri());
                recipeInfoService.failed(recipeInfo.getId());
                recipeProgressService.failed(recipeInfo.getId(), RecipeProgressStep.FINISHED, RecipeProgressDetail.FINISHED);
                throw e;
            }
        } catch (RecipeException e) {
            if (e.getError() == RecipeIdentifyErrorCode.RECIPE_IDENTIFY_PROGRESSING) {
                UUID recipeId = recipeYoutubeMetaService.getByUrl(target.uri()).getRecipeId();
                RecipeInfo recipe = recipeInfoService.getSuccess(recipeId);
                createBookmark(target, recipe);
                return recipe.getId();
            }
            throw e;
        }
    }

    private void createBookmark(RecipeCreationTarget target, RecipeInfo recipeInfo) {
        switch (target) {
            case RecipeCreationTarget.User user -> {
                UUID userId = user.userId();

                boolean created = recipeBookmarkService.create(userId, recipeInfo.getId());
                if (!created) return;

                try {
                    creditPort.spendRecipeCreate(userId, recipeInfo.getId(), recipeInfo.getCreditCost());
                } catch (CreditException e) {
                    recipeBookmarkService.delete(userId, recipeInfo.getId());

                    if (e.getError() == CreditErrorCode.CREDIT_INSUFFICIENT) {
                        throw new RecipeException(CreditErrorCode.CREDIT_INSUFFICIENT);
                    }
                    if (e.getError() == CreditErrorCode.CREDIT_CONCURRENCY_CONFLICT) {
                        throw new RecipeException(CreditErrorCode.CREDIT_CONCURRENCY_CONFLICT);
                    }
                    throw e;
                }
            }
            case RecipeCreationTarget.Crawler crawler -> {
            }
        }
    }
}
