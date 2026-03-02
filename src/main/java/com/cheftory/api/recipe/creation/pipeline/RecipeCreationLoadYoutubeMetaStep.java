package com.cheftory.api.recipe.creation.pipeline;

import com.cheftory.api.recipe.content.youtubemeta.RecipeYoutubeMetaService;
import com.cheftory.api.recipe.content.youtubemeta.entity.RecipeYoutubeMeta;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeVideoInfo;
import com.cheftory.api.recipe.creation.progress.RecipeProgressService;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressDetail;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressStep;
import com.cheftory.api.recipe.exception.RecipeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecipeCreationLoadYoutubeMetaStep implements RecipeCreationPipelineStep {

    private final RecipeYoutubeMetaService recipeYoutubeMetaService;
    private final RecipeProgressService recipeProgressService;

    /**
     * YouTube 메타 정보를 로드하고 실행 컨텍스트를 보강합니다.
     *
     * <p>`recipeId` 기준 메타가 이미 존재하면 재사용하고 외부 호출을 생략합니다.
     * 없으면 `videoId`로 메타를 조회한 뒤 `recipe_youtube_meta`를 생성하고 제목/비디오ID를 context에 반영합니다.</p>
     */
    @Override
    public RecipeCreationExecutionContext run(RecipeCreationExecutionContext context) throws RecipeException {
        recipeProgressService.start(
                context.getRecipeId(),
                RecipeProgressStep.LOAD_YOUTUBE_META,
                RecipeProgressDetail.LOAD_YOUTUBE_META,
                context.getJobId());
        try {
            if (recipeYoutubeMetaService.exists(context.getRecipeId())) {
                RecipeYoutubeMeta existing = recipeYoutubeMetaService.get(context.getRecipeId());
                recipeProgressService.success(
                        context.getRecipeId(),
                        RecipeProgressStep.LOAD_YOUTUBE_META,
                        RecipeProgressDetail.LOAD_YOUTUBE_META,
                        context.getJobId());
                return RecipeCreationExecutionContext.withYoutubeMeta(
                        context, existing.getVideoId(), existing.getTitle());
            }

            YoutubeVideoInfo videoInfo = recipeYoutubeMetaService.getVideoInfo(context.getVideoId());
            recipeYoutubeMetaService.create(videoInfo, context.getRecipeId());
            recipeProgressService.success(
                    context.getRecipeId(),
                    RecipeProgressStep.LOAD_YOUTUBE_META,
                    RecipeProgressDetail.LOAD_YOUTUBE_META,
                    context.getJobId());
            return RecipeCreationExecutionContext.withYoutubeMeta(
                    context, videoInfo.getVideoId(), videoInfo.getTitle());
        } catch (RecipeException ex) {
            recipeProgressService.failed(
                    context.getRecipeId(),
                    RecipeProgressStep.LOAD_YOUTUBE_META,
                    RecipeProgressDetail.LOAD_YOUTUBE_META,
                    context.getJobId());
            throw ex;
        }
    }
}
